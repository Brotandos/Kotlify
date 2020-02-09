package com.brotandos.kotlify.element

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.KotlifyInternals.NO_GETTER
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.exception.ContextAnonymousException
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

const val ID_NOT_SET = -1

private const val ID_KEY_SEPARATOR = "-"

/**
 * TODO List:
 * - Classify properties: (e.g: vRootOwnerName and packageName to identifiers)
 * - Encapsulate relay properties using [KotlifyInternals.noGetter]
 * - Find proper way to define width and height
 * */
abstract class WidgetElement<V : View>(val size: LayoutSize) : UiEntity<V>() {

    protected var tag: Any? = null

    /**
     * Must be initialized inside [WidgetElement.buildWidget]
     * TODO implement @InitializesInside(Method) annotation
     * */
    protected lateinit var vRootOwnerName: String

    /**
     * Must be initialized inside [WidgetElement.buildWidget]
     * TODO implement @InitializesInside(Method) annotation
     * */
    protected lateinit var packageName: String

    protected var pathInsideTree: List<Int>? = null

    private var isDarkRelay: BehaviorRelay<Boolean>? = null

    private var backgroundColors: Pair<Int, Int>? = null

    @PublishedApi
    internal var activityToNavigateOnClick: Class<*>? = null

    var id = ID_NOT_SET

    var minWidth: CustomLength? = null

    var minHeight: CustomLength? = null

    var actualWidth: Int? = null
        internal set

    var actualHeight: Int? = null
        internal set

    // TODO implement
    // open var isInvisible: BehaviorRelay<Boolean>? = null

    // TODO implement
    // open var navigatesTo: (() -> Fragment)? = null

    private var isEnabledRelay: BehaviorRelay<Boolean>? = null
    var isEnabled: BehaviorRelay<Boolean>
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) {
            isEnabledRelay = value
        }

    var clickRelay: PublishRelay<Unit>? = null
        set(value) {
            if (field != null) throw IllegalStateException("You can assign field of clickRelay only once")
            field = value
        }

    fun throttleClick(
            timeout: Int = ThrottleClickProperties.DEFAULT_TIMEOUT.toInt(),
            onClick: () -> Unit
    ): Disposable =
            getClickRelayInstance()
                    .throttleFirst(timeout.toLong(), ThrottleClickProperties.timeUnit)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { onClick() }
                    .also { disposables.add(it) }

    fun onClick(f: () -> Unit): Disposable =
            getClickRelayInstance()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { f() }
                    .also { disposables.add(it) }

    private var viewInit: (V.() -> Unit)? = null
    fun initView(init: V.() -> Unit) {
        viewInit = init
    }

    internal var layoutInit: (V.() -> Unit)? = null

    abstract fun createView(context: Context): V

    fun isDark(isDarkRelay: BehaviorRelay<Boolean>, lightColor: Int, darkColor: Int) {
        this.isDarkRelay = isDarkRelay
        backgroundColors = lightColor to darkColor
    }

    @CallSuper
    protected open fun initSubscriptions(view: V?) {
        isDarkRelay
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    val (light, dark) = backgroundColors ?: return@subscribe
                    view?.setBackgroundColor(if (it) dark else light)
                }
                ?.untilLifecycleDestroy()

        isAppearedRelay
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { view?.visibility = if (it) View.VISIBLE else View.GONE }
                ?.untilLifecycleDestroy()

        isEnabledRelay
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { view?.isEnabled = it }
                ?.untilLifecycleDestroy()
    }

    @CallSuper
    protected open fun initStyles(view: V?) = Unit

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val density = context.density
        val view = createView(context).also(::initStyles)
        if (id != ID_NOT_SET) {
            view.id = id
        }
        minWidth?.let { view.minimumWidth = it.getValue(density) }
        minHeight?.let { view.minimumHeight = it.getValue(density) }
        val (width, height) = size.getValuePair(density)
        view.layoutParams = ViewGroup.LayoutParams(width, height)
        viewInit?.invoke(view)
        layoutInit?.invoke(view)
        initSubscriptions(view)
        // FIXME itemView inside VRecycler doesn't emit WidgetElement#onClick
        if (!view.hasOnClickListeners()) {
            clickRelay?.let { relay ->
                view.setOnClickListener {
                    relay.accept(Unit)
                }
            }
        }
        activityToNavigateOnClick?.let {
            throttleClick {
                val intent = Intent(context, it)
                context.startActivity(intent)
            }
        }
        return view
    }

    /**
     * Must be initialized before [WidgetElement.build]
     * */
    @CallSuper
    @Throws(ContextAnonymousException::class)
    fun buildWidget(
            context: Context,
            kotlifyContext: KotlifyContext,
            pathInTree: List<Int>
    ): V {
        vRootOwnerName = context::class.simpleName ?: throw ContextAnonymousException()
        packageName = context.packageName
        this.pathInsideTree = pathInTree
        return build(context, kotlifyContext)
    }

    fun getIdKey(): String = buildString {
        append(packageName)
        append(ID_KEY_SEPARATOR)
        append(vRootOwnerName)
        pathInsideTree?.forEach {
            append(ID_KEY_SEPARATOR)
            append(it)
        }
    }

    inline fun <reified T : Activity> to(vararg parcelables: Pair<String, Parcelable>) {

        activityToNavigateOnClick = T::class.java
    }

    protected val Context.density get() = resources.displayMetrics.density.toInt()

    private fun getClickRelayInstance() =
            clickRelay ?: PublishRelay.create<Unit>().also { clickRelay = it }

    object ThrottleClickProperties {
        const val DEFAULT_TIMEOUT = 400L
        val timeUnit = TimeUnit.MILLISECONDS
    }

    class NavigationTarget<T : Activity>(
            val parcelables: Map<String, Parcelable>
    )
}