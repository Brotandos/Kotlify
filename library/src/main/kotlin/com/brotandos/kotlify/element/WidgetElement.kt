package com.brotandos.kotlify.element

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.KotlifyInternals.NO_GETTER
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.common.NoGetterException
import com.jakewharton.rxrelay2.BehaviorRelay
import java.lang.RuntimeException

const val ID_NOT_SET = -1

abstract class WidgetElement<V : View>(val size: LayoutSize) : UiEntity<V>() {

    var id = ID_NOT_SET

    private var isDarkRelay: BehaviorRelay<Boolean>? = null

    private var backgroundColors: Pair<Int, Int>? = null

    // TODO implement
    // open var isInvisible: BehaviorRelay<Boolean>? = null

    // TODO implement
    // open var navigatesTo: (() -> Fragment)? = null

    private var isEnabledRelay: BehaviorRelay<Boolean>? = null
    var isEnabled: BehaviorRelay<Boolean>
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) { isEnabledRelay = value }

    private var onClick: (() -> Unit)? = null
    fun onClick(f: () -> Unit) { onClick = f }

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
        val density = context.resources.displayMetrics.density.toInt()
        val (width, height) = size.getValuePair(density)
        view.layoutParams = ViewGroup.LayoutParams(width, height)
        viewInit?.invoke(view)
        layoutInit?.invoke(view)
        initSubscriptions(view)
        onClick?.let { onClick ->
            view.setOnClickListener {
                onClick.invoke()
            }
        }
        return view
    }
}