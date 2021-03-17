package com.brotandos.kotlify.element

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.content.edit
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.KotlifyInternals.NO_GETTER
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.exception.ContextAnonymousException
import com.brotandos.kotlify.exception.WidgetNotBuiltException
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

const val ID_NOT_SET = View.NO_ID

private const val ID_KEY_SEPARATOR = "-"

private const val CLASS_NAME = "WidgetElement"

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
    protected var packageName: String? = null

    protected var pathInsideTree: List<Int>? = null

    private var isDarkRelay: BehaviorRelay<Boolean>? = null

    private var backgroundColors: Pair<Int, Int>? = null

    @PublishedApi
    internal var activityToNavigateOnClick: Class<*>? = null

    var id = ID_NOT_SET

    var widthRange: Pair<CustomLength?, CustomLength?>? = null

    var heightRange: Pair<CustomLength?, CustomLength?>? = null

    var minWidth: CustomLength
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) {
            val currentMaxWidth = widthRange?.second
            widthRange = value to currentMaxWidth
        }

    var maxWidth: CustomLength
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) {
            val currentMinWidth = widthRange?.first
            widthRange = currentMinWidth to value
        }

    var minHeight: CustomLength
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) {
            val currentMaxHeight = heightRange?.second
            widthRange = value to currentMaxHeight
        }

    var maxHeight: CustomLength
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) {
            val currentMinHeight = heightRange?.first
            widthRange = currentMinHeight to value
        }

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

    private var backgroundRelay: BehaviorRelay<Drawable>? = null
    var background: BehaviorRelay<Drawable>
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) {
            backgroundRelay = value
        }

    private var backgroundResourceRelay: BehaviorRelay<Int>? = null
    var backgroundResource: BehaviorRelay<Int>
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) {
            backgroundResourceRelay = value
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

        backgroundRelay
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { view?.background = it }
                ?.untilLifecycleDestroy()

        backgroundResourceRelay
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { view?.setBackgroundResource(it) }
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

        val (width, height) = size.getValuePair(density)
        view.layoutParams = ViewGroup.LayoutParams(width, height)
        layoutInit?.invoke(view)
        viewInit?.invoke(view)
        initSubscriptions(view)

        // Fails on adding AutoCompleteTextView
        // if (view.hasOnClickListeners()) throw IllegalStateException("There shouldn't be onClickListener set up straightly, use onClick or throttleClick functions instead")

        clickRelay?.let { relay ->
            view.setOnClickListener {
                relay.accept(Unit)
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
     * Must be initialized before [WidgetElement.build] if inside VConstraint
     * */
    @CallSuper
    @Throws(ContextAnonymousException::class)
    open fun buildWidget(
            context: Context,
            kotlifyContext: KotlifyContext,
            pathInTree: List<Int>
    ): V {
        vRootOwnerName = context::class.simpleName ?: throw ContextAnonymousException()
        packageName = context.packageName
        this.pathInsideTree = pathInTree
        return build(context, kotlifyContext)
    }

    // FIXME doesn't work for recyclerView
    @CallSuper
    internal open fun onAttachedToParent(builtView: View) {
        val density = builtView.context.density
        widthRange?.let { (minWidth, maxWidth) ->
            minWidth?.run {
                val minimumWidth = getValue(density)
                if (minimumWidth <= builtView.width) return@run
                val layoutParams = builtView.layoutParams
                layoutParams.width = minimumWidth
                builtView.layoutParams = layoutParams
                return@let
            }
            maxWidth?.run {
                val maximumWidth = getValue(density)
                if (maximumWidth >= builtView.width) return@run
                val layoutParams = builtView.layoutParams
                layoutParams.width = maximumWidth
                builtView.layoutParams = layoutParams
                return@let
            }
        }
        heightRange?.let { (minHeight, maxHeight) ->
            minHeight?.run {
                val minimumHeight = getValue(density)
                if (minimumHeight <= builtView.height) return@run
                val layoutParams = builtView.layoutParams
                layoutParams.width = minimumHeight
                builtView.layoutParams = layoutParams
                return@let
            }
            maxHeight?.run {
                val maximumHeight = getValue(density)
                if (maximumHeight >= builtView.height) return@run
                val layoutParams = builtView.layoutParams
                layoutParams.width = maximumHeight
                builtView.layoutParams = layoutParams
                return@let
            }
        }
        actualWidth = builtView.width
        actualHeight = builtView.height
    }

    /**
     * Should be called after [buildWidget] due to init [packageName]
     * */
    fun identify(sharedPreferences: SharedPreferences): Int {
        packageName ?: throw WidgetNotBuiltException("$CLASS_NAME#identify")
        val key = getIdKey()
        id = sharedPreferences
                .getInt(key, ID_NOT_SET)
                .takeIf { it != ID_NOT_SET }
                ?: generateId(key, sharedPreferences)
        return id
    }

    private fun getIdKey(): String = buildString {
        packageName ?: throw WidgetNotBuiltException("$CLASS_NAME#getIdKey")
        append(packageName)
        append(ID_KEY_SEPARATOR)
        append(vRootOwnerName)
        pathInsideTree?.forEach {
            append(ID_KEY_SEPARATOR)
            append(it)
        }
    }

    fun identify(
            sharedPreferences: SharedPreferences,
            packageName: String,
            vRootOwnerName: String,
            pathInTree: List<Int>
    ): Int {
        val key = getIdKey(packageName, vRootOwnerName, pathInTree)
        id = sharedPreferences
                .getInt(key, ID_NOT_SET)
                .takeIf { it != ID_NOT_SET }
                ?: generateId(key, sharedPreferences)
        return id
    }

    private fun getIdKey(
            packageName: String,
            vRootOwnerName: String,
            pathInTree: List<Int>
    ): String = buildString {
        append(packageName)
        append(ID_KEY_SEPARATOR)
        append(vRootOwnerName)
        pathInTree.forEach {
            append(ID_KEY_SEPARATOR)
            append(it)
        }
    }

    private fun generateId(key: String, sharedPreferences: SharedPreferences): Int {
        val id = View.generateViewId()
        sharedPreferences.edit { putInt(key, id) }
        return id
    }

    inline fun <reified T : Activity> to(vararg parcelables: Pair<String, Parcelable>) {
        activityToNavigateOnClick = T::class.java
    }

    protected val Context.density get() = resources.displayMetrics.density

    private fun getClickRelayInstance() =
            clickRelay ?: PublishRelay.create<Unit>().also { clickRelay = it }

    operator fun CustomLength?.rangeTo(upperBound: CustomLength?) = this to upperBound

    object ThrottleClickProperties {
        const val DEFAULT_TIMEOUT = 400L
        val timeUnit = TimeUnit.MILLISECONDS
    }

    class NavigationTarget<T : Activity>(
            val parcelables: Map<String, Parcelable>
    )
}