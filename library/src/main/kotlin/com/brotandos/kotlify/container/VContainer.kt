package com.brotandos.kotlify.container

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.brotandos.kotlify.common.Earth
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.container.modal.VBottomSheetDialog
import com.brotandos.kotlify.container.modal.VDialog
import com.brotandos.kotlify.element.UiEntity
import com.brotandos.kotlify.element.VLabel
import com.brotandos.kotlify.element.VRecycler
import com.brotandos.kotlify.element.WidgetElement
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

abstract class VContainer<V : ViewGroup>(
    size: LayoutSize
) : WidgetElement<V>(size), WidgetContainer {

    protected val children = mutableListOf<UiEntity<*>>()
    fun add(uiEntity: UiEntity<*>) = children.add(uiEntity)

    // TODO find way to inject it
    private var getDisplayMetrics: (() -> DisplayMetrics)? = null

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        getDisplayMetrics = { context.resources.displayMetrics }
        val view = super.build(context, kotlifyContext)
        children.forEachIndexed { index, uiEntity ->
            if (uiEntity !is WidgetElement) {
                uiEntity.build(context, kotlifyContext)
                return@forEachIndexed
            }
            // TODO use custom exception
            val path = pathInsideTree ?: throw RuntimeException("WidgetContainer#buildWidget method must be called before to initialize pathInsideTree")
            val child = uiEntity.buildWidget(
                    context,
                    kotlifyContext,
                    path + index
            ) as? View ?: throw RuntimeException("Generic type of widget must extend View")
            // TODO use custom exception
            view.addView(child)
        }
        onBuildFinished(view)
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
        add(vElement)
        return vElement
    }

    inline fun <reified V : View> vCustom(size: LayoutSize = Earth): WidgetElement<V> {
        val vElement = object : WidgetElement<V>(size) {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        add(vElement)
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

    override fun vToolbar(
            size: LayoutSize,
            init: VToolbar.() -> Unit
    ): VToolbar {
        val vToolbar = VToolbar(size)
        vToolbar.init()
        children += vToolbar
        return vToolbar
    }

    override fun vLabel(size: LayoutSize, init: VLabel.() -> Unit): VLabel {
        val vLabel = VLabel(size)
        vLabel.init()
        children += vLabel
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
        children += vLabel
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
        children += vLabel
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
        children += vLabel
        return vLabel
    }

    override fun <E> vRecycler(
        size: LayoutSize,
        items: BehaviorRelay<List<E>>,
        init: VRecycler<E>.() -> Unit
    ): VRecycler<E> {
        val vRecycler = VRecycler(size = size, itemsRelay = items)
        vRecycler.init()
        children += vRecycler
        return vRecycler
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
        children += vContainer
        return vContainer
    }

    override fun vVertical(init: VContainer<LinearLayout>.() -> Unit): VContainer<LinearLayout> {
        val vContainer = object : VContainer<LinearLayout>(Earth) {
            override fun createView(context: Context): LinearLayout =
                LinearLayout(context)
                    .also { it.orientation = LinearLayout.VERTICAL }
        }
        vContainer.init()
        children += vContainer
        return vContainer
    }

    @Throws(RuntimeException::class)
    fun vDialog(init: VDialog.() -> Unit): Disposable {
        val vDialog = VDialog()
        vDialog.init()
        add(vDialog)
        return vDialog
    }

    fun vBottomSheetDialog(init: VBottomSheetDialog.() -> Unit): Disposable {
        val vBottomSheetDialog = VBottomSheetDialog()
        vBottomSheetDialog.init()
        add(vBottomSheetDialog)
        return vBottomSheetDialog
    }

    operator fun WidgetElement<*>.unaryPlus(): Disposable {
        children += this
        return this
    }

    open fun onBuildFinished(view: V) = Unit

    override fun dispose() {
        super.dispose()
        children.forEach(Disposable::dispose)
    }
}