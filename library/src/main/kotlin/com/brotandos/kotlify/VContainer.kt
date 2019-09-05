package com.brotandos.kotlify

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

abstract class VContainer<V : ViewGroup> : MarkupElement<V>() {

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

    inline fun <reified V : View> vCustom(init: MarkupElement<V>.() -> Unit): MarkupElement<V> {
        val vElement = object : MarkupElement<V>() {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        vElement.init()
        children += vElement
        return vElement
    }

    inline fun <reified V : ViewGroup> vContainer(init: VContainer<V>.() -> Unit): VContainer<V> {
        val vContainer = object : VContainer<V>() {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        vContainer.init()
        return vContainer
    }

    fun vToolbar(init: VToolbar.() -> Unit): Disposable {
        val vToolbar = VToolbar()
        vToolbar.init()
        children += vToolbar
        return vToolbar
    }

    fun <E> vRecycler(items: BehaviorRelay<List<E>>, init: VRecycler<E>.() -> Unit): Disposable {
        val vRecycler = VRecycler(items)
        vRecycler.init()
        children += vRecycler
        return vRecycler
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

    operator fun MarkupElement<*>.unaryPlus(): Disposable {
        children += this
        return this
    }

    override fun dispose() {
        super.dispose()
        children.forEach(Disposable::dispose)
    }
}

inline fun <reified V : ViewGroup> Activity.vContainer(init: VContainer<V>.() -> Unit): Disposable {
    val builder = object : VContainer<V>() {
        override fun createView(context: Context): V =
            KotlifyInternals.initiateView(context, V::class.java)
    }
    builder.init()
    val kotlifyContext = KotlifyContext()
    setContentView(builder.build(this, kotlifyContext))
    return builder
}