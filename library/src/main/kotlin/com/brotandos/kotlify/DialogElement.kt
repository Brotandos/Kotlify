package com.brotandos.kotlify

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

abstract class DialogElement<D: Dialog> : UiEntity<D>() {

    var titleResId: Int? = null

    var title: String? = null

    var cancellable: Boolean? = null

    var vContent: MarkupElement<*>? = null

    @CallSuper
    protected open fun initSubscriptions(dialog: D) {
        vShow
            ?.subscribe { if (it) dialog.show() else dialog.hide() }
            ?.addToComposite()
    }

    inline fun <reified V : View> vCustom(init: MarkupElement<V>.() -> Unit): MarkupElement<V> {
        val vElement = object : MarkupElement<V>() {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        vElement.init()
        vContent = vElement
        return vElement
    }

    inline fun <reified V : ViewGroup> vContainer(init: VContainer<V>.() -> Unit): VContainer<V> {
        val vContainer = object : VContainer<V>() {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        vContainer.init()
        vContent = vContainer
        return vContainer
    }

    fun vToolbar(init: VToolbar.() -> Unit): Disposable {
        val vToolbar = VToolbar()
        vToolbar.init()
        vContent = vToolbar
        return vToolbar
    }

    fun <E> vRecycler(items: BehaviorRelay<List<E>>, init: VRecycler<E>.() -> Unit): Disposable {
        val vRecycler = VRecycler(items)
        vRecycler.init()
        vContent = vRecycler
        return vRecycler
    }

    override fun dispose() {
        super.dispose()
        vContent?.dispose()
    }

    override fun isDisposed(): Boolean = super.isDisposed() && vContent?.isDisposed ?: true
}