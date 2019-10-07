package com.brotandos.kotlify

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

abstract class VContainer<V : ViewGroup>(size: LayoutSize) : WidgetElement<V>(size), WidgetContainer {

    private var lifecycleObserver: KotlifyLifecycleObserver? = null

    val children = mutableListOf<UiEntity<*>>()

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val view = super.build(context, kotlifyContext)
        children.forEach {
            val child = it.build(context, kotlifyContext)
            if (child is View) {
                view.addView(child)
            }
        }
        lifecycleObserver = KotlifyLifecycleObserver(this)
        return view
    }

    inline fun <reified V : View> vCustom(
        size: LayoutSize = Earth,
        init: WidgetElement<V>.() -> Unit
    ): WidgetElement<V> {
        val vElement = object : WidgetElement<V>(size) {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        vElement.init()
        children += vElement
        return vElement
    }

    inline fun <reified V : ViewGroup> vContainer(
        size: LayoutSize,
        init: VContainer<V>.() -> Unit
    ): VContainer<V> {
        val vContainer = object : VContainer<V>(size) {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        vContainer.init()
        return vContainer
    }

    override fun vToolbar(size: LayoutSize, init: VToolbar.() -> Unit): Disposable {
        val vToolbar = VToolbar(size)
        vToolbar.init()
        children += vToolbar
        return vToolbar
    }

    override fun <E> vRecycler(size: LayoutSize, items: BehaviorRelay<List<E>>, init: VRecycler<E>.() -> Unit): Disposable {
        val vRecycler = VRecycler(size, items)
        vRecycler.init()
        children += vRecycler
        return vRecycler
    }

    operator fun WidgetElement<*>.unaryPlus(): Disposable {
        children += this
        return this
    }

    override fun dispose() {
        super.dispose()
        children.forEach(Disposable::dispose)
    }

    fun vDialog(init: VDialog.() -> Unit): Disposable {
        val vDialog = VDialog()
        vDialog.init()
        children += vDialog
        return vDialog
    }

    fun vBottomSheetDialog(init: VBottomSheetDialog.() -> Unit): Disposable {
        val vBottomSheetDialog = VBottomSheetDialog()
        vBottomSheetDialog.init()
        children += vBottomSheetDialog
        return vBottomSheetDialog
    }

    fun disposeOnViewDestroyed(lifecycleOwner: LifecycleOwner) {
        KotlifyLifecycleObserver(this).let {
            lifecycleObserver = it
            lifecycleOwner.lifecycle.addObserver(it)
        }
    }

    fun clearObservers(lifecycleOwner: LifecycleOwner) {
        lifecycleObserver?.let(lifecycleOwner.lifecycle::removeObserver)
    }
}

inline fun <reified V : ViewGroup> Activity.vContainer(init: VContainer<V>.() -> Unit): Disposable {
    val builder = object : VContainer<V>(Air) {
        override fun createView(context: Context): V =
            KotlifyInternals.initiateView(context, V::class.java)
    }
    builder.init()
    val kotlifyContext = KotlifyContext()
    setContentView(builder.build(this, kotlifyContext))
    return builder
}

inline fun <reified V : ViewGroup> Activity.vContainer(
    lifecycleOwner: LifecycleOwner,
    vContainerOwner: VContainerOwner,
    init: VContainer<V>.() -> Unit
): VContainer<*> {
    val vContainer = object : VContainer<V>(Air) {
        override fun createView(context: Context): V =
            KotlifyInternals.initiateView(context, V::class.java)
    }
    vContainer.init()
    val kotlifyContext = KotlifyContext()
    setContentView(vContainer.build(this, kotlifyContext))
    vContainer.disposeOnViewDestroyed(lifecycleOwner)
    vContainerOwner.vContainer?.clearObservers(lifecycleOwner)
    vContainerOwner.vContainer = vContainer
    return vContainer
}