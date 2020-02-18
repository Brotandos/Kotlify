package com.brotandos.kotlify.container.modal

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.container.VContainer
import com.brotandos.kotlify.container.WidgetContainer
import com.brotandos.kotlify.element.UiEntity
import com.brotandos.kotlify.element.WidgetElement
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class ModalElement<D : Dialog> : UiEntity<D>(),
        WidgetContainer {

    var titleResId: Int? = null

    var title: String? = null

    var titleRelay: BehaviorRelay<String>? = null

    var cancellable: Boolean? = null

    var vContent: WidgetElement<*>? = null

    var onShow: (() -> Unit)? = null

    var onCancel: (() -> Unit)? = null

    @CallSuper
    protected open fun initSubscriptions(dialog: D?) {
        dialog ?: return
        isAppearedRelay
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { if (it) dialog.show() else dialog.hide() }
                ?.untilLifecycleDestroy()

        onShow?.let { onShow ->
            dialog.setOnShowListener(DialogInterface.OnShowListener { onShow() })
        }

        dialog.setOnCancelListener(DialogInterface.OnCancelListener {
            isAppearedRelay?.accept(false)
            onCancel?.invoke()
        })
    }

    inline fun <reified V : View> vCustom(
            size: LayoutSize,
            init: WidgetElement<V>.() -> Unit
    ): WidgetElement<V> {
        val vElement = object : WidgetElement<V>(size) {
            override fun createView(context: Context): V =
                    KotlifyInternals.initiateView(context, V::class.java)
        }
        vContent = vElement
        vElement.init()
        return vElement
    }

    inline fun <reified V : ViewGroup, reified LP : ViewGroup.LayoutParams> vContainer(
            init: VContainer<V, LP>.() -> Unit
    ): VContainer<V, LP> {
        val vContainer = object : VContainer<V, LP>(Air) {
            override fun createView(context: Context): V =
                    KotlifyInternals.initiateView(context, V::class.java)

            override fun getChildLayoutParams(width: Int, height: Int): LP {
                val constructor = LP::class.java.getConstructor(width::class.java, height::class.java)
                return constructor.newInstance(width, height)
            }
        }
        vContent = vContainer
        vContainer.init()
        return vContainer
    }

    override fun accept(widget: WidgetElement<*>) {
        vContent = widget
    }

    override fun dispose() {
        super.dispose()
        vContent?.dispose()
    }

    override fun isDisposed(): Boolean = super.isDisposed() && vContent?.isDisposed ?: true
}