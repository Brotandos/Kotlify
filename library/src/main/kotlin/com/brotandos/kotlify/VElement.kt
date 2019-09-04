package com.brotandos.kotlify

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class VElement<V : View> : VEntity<V>() {

    private var isDark: BehaviorRelay<Boolean>? = null

    private var backgroundColors: Pair<Int, Int>? = null

    open var vShow: BehaviorRelay<Boolean>? = null

    // open var isInvisible: BehaviorRelay<Boolean>? = null

    open var isEnabled: BehaviorRelay<Boolean>? = null

    open var navigatesTo: (() -> Fragment)? = null

    open var onClick: (() -> Unit)? = null

    open var viewInit: (V.() -> Unit)? = null

    var layoutInit: (V.() -> Unit)? = null

    val Int.dp get() = this

    abstract fun createView(context: Context): V

    fun isDark(isDarkRelay: BehaviorRelay<Boolean>, lightColor: Int, darkColor: Int) {
        isDark = isDarkRelay
        backgroundColors = lightColor to darkColor
    }

    @CallSuper
    protected open fun initSubscriptions(view: V?) {
        isDark
            ?.subscribe {
                val (light, dark) = backgroundColors ?: return@subscribe
                view?.setBackgroundColor(if (it) dark else light)
            }
            ?.addToComposite()

        vShow
            ?.subscribe { view?.visibility = if (it) View.VISIBLE else View.GONE }
            ?.addToComposite()

        isEnabled
            ?.subscribe { view?.isEnabled = it }
            ?.addToComposite()
    }

    inline fun <reified T : ViewGroup.LayoutParams> initLayout(
        width: Int,
        height: Int,
        crossinline init: T.() -> Unit
    ) {
        layoutInit = {
            val constructor = T::class.java.getConstructor(width::class.java, height::class.java)
            val instance = constructor.newInstance(width, height)
            instance.init()
            layoutParams = instance
        }
    }

    inline fun <reified T : ViewGroup.LayoutParams> initLayout(
        width: Int,
        height: Int
    ) {
        layoutInit = {
            val constructor = T::class.java.getConstructor(width::class.java, height::class.java)
            val instance = constructor.newInstance(width, height)
            layoutParams = instance
        }
    }

    fun initView(init: V.() -> Unit) {
        viewInit = init
    }

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val view = createView(context)
        viewInit?.invoke(view)
        layoutInit?.invoke(view)
        initSubscriptions(view)
        onClick?.let {
            view.setOnClickListener {
                it()
                navigatesTo?.let { getFragment ->
                    view.isEnabled = false
                    kotlifyContext.router?.navigateTo(getFragment())
                }
            }
        }
        return view
    }
}