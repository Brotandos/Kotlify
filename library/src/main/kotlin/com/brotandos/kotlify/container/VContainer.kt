package com.brotandos.kotlify.container

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat.getSystemService
import com.brotandos.kotlify.common.Earth
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.container.modal.VBottomSheetDialog
import com.brotandos.kotlify.container.modal.VDialog
import com.brotandos.kotlify.element.UiEntity
import com.brotandos.kotlify.element.WidgetElement
import io.reactivex.disposables.Disposable
import kotlin.reflect.full.isSubclassOf

abstract class
VContainer<V : ViewGroup, LP : ViewGroup.LayoutParams>(
    size: LayoutSize
) : WidgetElement<V>(size), WidgetContainer {

    protected val children = mutableListOf<UiEntity<*>>()

    // TODO find way to inject it
    private var getDisplayMetrics: (() -> DisplayMetrics)? = null

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        getDisplayMetrics = { context.resources.displayMetrics }
        val view = super.build(context, kotlifyContext)
        buildChildren(view, kotlifyContext)
        onBuildFinished(view)
        return view
    }

    private fun buildChildren(
            viewGroup: ViewGroup,
            kotlifyContext: KotlifyContext
    ) {
        val context = viewGroup.context
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
            viewGroup.addView(child)
            // uiEntity.onAttachedToParent(child)
        }
    }

    @Throws(IllegalStateException::class)
    fun <T : WidgetElement<*>> T.lparams(init: LP.() -> Unit): T {
        val parent = this@VContainer
        val element = this.takeIf { it !== parent }
                ?: throw IllegalStateException("You cannot create layout params from itself")

        if (parent.children.none { it === element })
            throw IllegalStateException("$parent isn't parent of $element. Check lparams function attaching for validity")

        element.layoutInit = {
            val density = context.density
            val (widgetWidth, widgetHeight) = size.getValuePair(density)
            val instance = getChildLayoutParams(widgetWidth, widgetHeight)
            instance.init()
            layoutParams = instance
        }
        return element
    }

    abstract fun getChildLayoutParams(width: Int, height: Int): LP

    inline fun <reified V : View> vCustom(
        size: LayoutSize = Earth,
        init: WidgetElement<V>.() -> Unit
    ): WidgetElement<V> {
        val vElement = object : WidgetElement<V>(size) {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        inlineChildren += vElement
        vElement.init()
        return vElement
    }

    inline fun <reified V : View> vCustom(size: LayoutSize = Earth): WidgetElement<V> {
        val vElement = object : WidgetElement<V>(size) {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)
        }
        inlineChildren += vElement
        return vElement
    }

    inline fun <reified V : ViewGroup, reified LP : ViewGroup.LayoutParams> vContainer(
            size: LayoutSize,
            init: VContainer<V, LP>.() -> Unit
    ): VContainer<V, LP> {
        val vContainer = object : VContainer<V, LP>(size) {
            override fun createView(context: Context): V =
                KotlifyInternals.initiateView(context, V::class.java)

            override fun getChildLayoutParams(width: Int, height: Int): LP {
                val constructor = LP::class.java.getConstructor(width::class.java, height::class.java)
                return constructor.newInstance(width, height)
            }
        }
        inlineChildren += vContainer
        vContainer.init()
        return vContainer
    }

    inline fun <reified V : ViewGroup, reified LP : ViewGroup.LayoutParams> vContainer(
            size: LayoutSize,
            @LayoutRes layoutId: Int,
            init: VContainer<V, LP>.() -> Unit
    ): VContainer<V, LP> {
        val vContainer = object : VContainer<V, LP>(size) {
            override fun createView(context: Context): V {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                return inflater.inflate(layoutId, null, false) as? V
                        ?: throw IllegalStateException("Cannot cast view to type of V")
            }

            override fun getChildLayoutParams(width: Int, height: Int): LP {
                val constructor = LP::class.java.getConstructor(width::class.java, height::class.java)
                return constructor.newInstance(width, height)
            }
        }
        inlineChildren += vContainer
        vContainer.init()
        return vContainer
    }

    override fun accept(widget: WidgetElement<*>) {
        children += widget
    }

    @Throws(RuntimeException::class)
    fun vDialog(init: VDialog.() -> Unit): Disposable {
        val vDialog = VDialog()
        vDialog.init()
        children.add(vDialog)
        return vDialog
    }

    fun vBottomSheetDialog(init: VBottomSheetDialog.() -> Unit): Disposable {
        val vBottomSheetDialog = VBottomSheetDialog()
        vBottomSheetDialog.init()
        children.add(vBottomSheetDialog)
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

    @Suppress("unused")
    /**
     * Used inside [vContainer] function
     * */
    @PublishedApi
    internal val inlineChildren: MutableList<UiEntity<*>>
        get() = children
}