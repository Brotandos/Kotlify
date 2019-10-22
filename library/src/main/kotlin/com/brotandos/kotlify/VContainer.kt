package com.brotandos.kotlify

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

abstract class VContainer<V : ViewGroup>(
    size: LayoutSize
) : WidgetElement<V>(size), WidgetContainer{

    val children = mutableListOf<UiEntity<*>>()

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val view = super.build(context, kotlifyContext)
        children.forEach {
            val child = it.build(context, kotlifyContext)
            if (child is View) {
                view.addView(child)
            }
        }
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

    inline fun <reified V : View> vCustom(size: LayoutSize = Earth): WidgetElement<V> {
        val vElement = object : WidgetElement<V>(size) {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
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

    override fun <E> vRecycler(
        size: LayoutSize,
        items: BehaviorRelay<List<E>>,
        init: VRecycler<E>.() -> Unit
    ): Disposable {
        val vRecycler = VRecycler(size, items)
        vRecycler.init()
        children += vRecycler
        return vRecycler
    }

    override fun vVertical(size: LayoutSize, init: VContainer<LinearLayout>.() -> Unit): Disposable {
        val vContainer = object : VContainer<LinearLayout>(size) {
            override fun createView(context: Context): LinearLayout =
                LinearLayout(context)
                    .also { it.orientation = LinearLayout.VERTICAL }
        }
        vContainer.init()
        children += vContainer
        return vContainer
    }

    override fun vVertical(init: VContainer<LinearLayout>.() -> Unit): Disposable {
        val vContainer = object : VContainer<LinearLayout>(Earth) {
            override fun createView(context: Context): LinearLayout =
                LinearLayout(context)
                    .also { it.orientation = LinearLayout.VERTICAL }
        }
        vContainer.init()
        children += vContainer
        return vContainer
    }

    operator fun WidgetElement<*>.unaryPlus(): Disposable {
        children += this
        return this
    }

    override fun dispose() {
        super.dispose()
        children.forEach(Disposable::dispose)
    }
}