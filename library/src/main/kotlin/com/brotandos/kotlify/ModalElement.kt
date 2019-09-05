package com.brotandos.kotlify

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

abstract class ModalElement<D: Dialog> : UiEntity<D>(), WidgetContainer {

    var titleResId: Int? = null

    var title: String? = null

    var cancellable: Boolean? = null

    var vContent: WidgetElement<*>? = null

    @CallSuper
    protected open fun initSubscriptions(dialog: D) {
        vShow
            ?.subscribe { if (it) dialog.show() else dialog.hide() }
            ?.addToComposite()
    }

    inline fun <reified V : View> vCustom(size: LayoutSize, init: WidgetElement<V>.() -> Unit): WidgetElement<V> {
        val vElement = object : WidgetElement<V>(size) {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        vElement.init()
        vContent = vElement
        return vElement
    }

    inline fun <reified V : ViewGroup> vContainer(init: VContainer<V>.() -> Unit): VContainer<V> {
        val vContainer = object : VContainer<V>(Air) {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        vContainer.init()
        vContent = vContainer
        return vContainer
    }

    override fun vToolbar(size: LayoutSize, init: VToolbar.() -> Unit): Disposable {
        val vToolbar = VToolbar(size)
        vToolbar.init()
        vContent = vToolbar
        return vToolbar
    }

    override fun <E> vRecycler(size: LayoutSize, items: BehaviorRelay<List<E>>, init: VRecycler<E>.() -> Unit): Disposable {
        val vRecycler = VRecycler(size, items)
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