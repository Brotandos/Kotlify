package com.brotandos.kotlify

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import io.reactivex.disposables.Disposable

class VRoot<V : ViewGroup>(
    size: LayoutSize,
    private val createViewGroup: () -> V // FIXME
) : VContainer<V>(size), WidgetContainer {

    private var lifecycleObserver: KotlifyLifecycleObserver? = null

    // TODO find proper way to instantiate view
    override fun createView(context: Context): V = createViewGroup()

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val view = super.build(context, kotlifyContext)
        lifecycleObserver = KotlifyLifecycleObserver(this)
        return view
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

inline fun <reified V : ViewGroup> Activity.vRoot(
    init: VRoot<V>.() -> Unit
): Disposable {
    val createViewGroup = { KotlifyInternals.initiateView(this, V::class.java) }
    val builder = VRoot(Air, createViewGroup)
    builder.init()
    val view = builder.build(this, KotlifyContext())
    setContentView(view)
    return builder
}

inline fun <reified V : ViewGroup> Activity.vRoot(
    lifecycleOwner: LifecycleOwner,
    vRootOwner: VRootOwner,
    init: VRoot<V>.() -> Unit
): VRoot<*> {
    val createViewGroup = { KotlifyInternals.initiateView(this, V::class.java) }
    val vContainer = VRoot(Air, createViewGroup)
    vContainer.init()
    val view = vContainer.build(this, KotlifyContext())
    setContentView(view)
    vContainer.disposeOnViewDestroyed(lifecycleOwner)
    vRootOwner.vRoot?.clearObservers(lifecycleOwner)
    vRootOwner.vRoot = vContainer
    return vContainer
}