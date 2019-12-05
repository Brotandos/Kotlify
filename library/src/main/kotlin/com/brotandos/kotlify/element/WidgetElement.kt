package com.brotandos.kotlify.element

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.KotlifyInternals.NO_GETTER
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.exception.ContextAnonymousException
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

const val ID_NOT_SET = -1

private const val ID_KEY_SEPARATOR = "-"

abstract class WidgetElement<V : View>(val size: LayoutSize) : UiEntity<V>() {

    var id = ID_NOT_SET

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

    private var activityToNavigateOnClick: Class<Activity>? = null

    // TODO implement
    // open var isInvisible: BehaviorRelay<Boolean>? = null

    // TODO implement
    // open var navigatesTo: (() -> Fragment)? = null

    private var isEnabledRelay: BehaviorRelay<Boolean>? = null
    var isEnabled: BehaviorRelay<Boolean>
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) { isEnabledRelay = value }

    var clickRelay: PublishRelay<Unit> = PublishRelay.create()

    fun throttleClick(
            timeout: Int = ThrottleClickProperties.TIMEOUT.toInt(),
            onClick: () -> Unit
    ): Disposable {
        val clickDisposable = clickRelay
                .throttleFirst(timeout.toLong(), ThrottleClickProperties.timeUnit)
                .subscribe { onClick() }
        disposables.add(clickDisposable)
        return clickDisposable
    }

    fun onClick(f: () -> Unit): Disposable {
        val clickDisposable = clickRelay.subscribe { f() }
        disposables.add(clickDisposable)
        return clickDisposable
    }

    private var viewInit: (V.() -> Unit)? = null
    fun initView(init: V.() -> Unit) { viewInit = init }

    @PublishedApi
    internal var layoutInit: (V.() -> Unit)? = null
    inline fun <reified T : ViewGroup.LayoutParams> initLayout(crossinline init: T.() -> Unit) {
        layoutInit = {
            val constructor = T::class.java.getConstructor(width::class.java, height::class.java)
            val density = context.resources.displayMetrics.density.toInt()
            val (widgetWidth, widgetHeight) = size.getValuePair(density)
            val instance = constructor.newInstance(widgetWidth, widgetHeight)
            instance.init()
            layoutParams = instance
        }
    }

    abstract fun createView(context: Context): V

    fun isDark(isDarkRelay: BehaviorRelay<Boolean>, lightColor: Int, darkColor: Int) {
        this.isDarkRelay = isDarkRelay
        backgroundColors = lightColor to darkColor
    }

    @CallSuper
    protected open fun initSubscriptions(view: V?) {
        view ?: return
        isDarkRelay
            ?.subscribe {
                val (light, dark) = backgroundColors ?: return@subscribe
                view.setBackgroundColor(if (it) dark else light)
            }
            ?.untilLifecycleDestroy()

        isAppearedRelay
            ?.subscribe { view.visibility = if (it) View.VISIBLE else View.GONE }
            ?.untilLifecycleDestroy()

        isEnabledRelay
            ?.subscribe { view.isEnabled = it }
            ?.untilLifecycleDestroy()
    }

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val view = createView(context)
        if (id != ID_NOT_SET) {
            view.id = id
        }
        val (width, height) = size.getValuePair(context.density)
        view.layoutParams = ViewGroup.LayoutParams(width, height)
        viewInit?.invoke(view)
        layoutInit?.invoke(view)
        initSubscriptions(view)
        view.setOnClickListener {
            clickRelay.accept(Unit)
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

    fun to(clazz: Class<Activity>) {
        activityToNavigateOnClick = clazz
    }

    object ThrottleClickProperties {
        const val TIMEOUT = 400L
        val timeUnit = TimeUnit.MILLISECONDS
    }
}