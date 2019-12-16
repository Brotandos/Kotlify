package com.brotandos.kotlify.container.modal

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.container.VCard
import com.brotandos.kotlify.container.VConstraint
import com.brotandos.kotlify.container.VContainer
import com.brotandos.kotlify.container.VToolbar
import com.brotandos.kotlify.container.WidgetContainer
import com.brotandos.kotlify.element.LayoutManager
import com.brotandos.kotlify.element.UiEntity
import com.brotandos.kotlify.element.VImage
import com.brotandos.kotlify.element.VLabel
import com.brotandos.kotlify.element.VRecycler
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

    override fun vToolbar(size: LayoutSize, init: VToolbar.() -> Unit): VToolbar {
        val vToolbar = VToolbar(size)
        vToolbar.init()
        vContent = vToolbar
        return vToolbar
    }

    override fun vLabel(size: LayoutSize, init: VLabel.() -> Unit): VLabel {
        val vLabel = VLabel(size)
        vLabel.init()
        vContent = vLabel
        return vLabel
    }

    override fun vLabel(
            size: LayoutSize,
            vararg styles: TextView.() -> Unit,
            init: VLabel.() -> Unit
    ): VLabel {
        val vLabel = VLabel(size)
        vLabel.styles = arrayOf(*styles)
        vLabel.init()
        vContent = vLabel
        return vLabel
    }

    override fun vLabel(
            size: LayoutSize,
            @StringRes textResId: Int,
            vararg styles: TextView.() -> Unit,
            init: VLabel.() -> Unit
    ): VLabel {
        val vLabel = VLabel(size)
        vLabel.textResId = textResId
        vLabel.styles = arrayOf(*styles)
        vLabel.init()
        vContent = vLabel
        return vLabel
    }

    override fun vLabel(
            size: LayoutSize,
            text: String,
            vararg styles: TextView.() -> Unit,
            init: VLabel.() -> Unit
    ): VLabel {
        val vLabel = VLabel(size)
        vLabel.text = text
        vLabel.styles = arrayOf(*styles)
        vLabel.init()
        vContent = vLabel
        return vLabel
    }

    override fun <E> vList(
            size: LayoutSize,
            items: BehaviorRelay<List<E>>,
            init: VRecycler<E>.() -> Unit
    ): VRecycler<E> {
        val vRecycler = VRecycler(
                itemsRelay = items,
                layoutManager = LayoutManager.Linear,
                size = size
        )
        vRecycler.init()
        vContent = vRecycler
        return vRecycler
    }

    override fun <E> vGrid(
            size: LayoutSize,
            items: BehaviorRelay<List<E>>,
            init: VRecycler<E>.() -> Unit
    ): VRecycler<E> = TODO("not implemented")

    override fun vLinear(
            size: LayoutSize,
            init: VContainer<LinearLayout>.() -> Unit
    ): VContainer<LinearLayout> {
        val vContainer = object : VContainer<LinearLayout>(size) {
            override fun createView(context: Context): LinearLayout = LinearLayout(context)
        }
        vContainer.init()
        vContent = vContainer
        return vContainer
    }

    override fun vVertical(
            size: LayoutSize,
            init: VContainer<LinearLayout>.() -> Unit
    ): VContainer<LinearLayout> {
        val vContainer = object : VContainer<LinearLayout>(size) {
            override fun createView(context: Context): LinearLayout =
                    LinearLayout(context)
                            .also { it.orientation = LinearLayout.VERTICAL }
        }
        vContainer.init()
        vContent = vContainer
        return vContainer
    }

    override fun vVertical(init: VContainer<LinearLayout>.() -> Unit): VContainer<LinearLayout> {
        val vContainer = object : VContainer<LinearLayout>(Air) {
            override fun createView(context: Context): LinearLayout =
                    LinearLayout(context)
                            .also { it.orientation = LinearLayout.VERTICAL }
        }
        vContainer.init()
        vContent = vContainer
        return vContainer
    }

    override fun vCard(size: LayoutSize, init: VCard.() -> Unit): VCard {
        val vCard = VCard(size)
        vCard.init()
        vContent = vCard
        return vCard
    }

    override fun vConstraint(size: LayoutSize, init: VConstraint.() -> Unit): VConstraint {
        val vConstraint = VConstraint(size)
        vConstraint.init()
        vContent = vConstraint
        return vConstraint
    }

    override fun vImage(size: LayoutSize, resId: Int, init: VImage.() -> Unit): VImage {
        val vImage = VImage(size)
        vImage.imageResId = BehaviorRelay.createDefault(resId)
        vImage.init()
        vContent = vImage
        return vImage
    }

    override fun vImage(size: LayoutSize, url: String, init: VImage.() -> Unit): VImage {
        val vImage = VImage(size)
        vImage.imageUrl = url
        vImage.init()
        vContent = vImage
        return vImage
    }

    override fun dispose() {
        super.dispose()
        vContent?.dispose()
    }

    override fun isDisposed(): Boolean = super.isDisposed() && vContent?.isDisposed ?: true
}