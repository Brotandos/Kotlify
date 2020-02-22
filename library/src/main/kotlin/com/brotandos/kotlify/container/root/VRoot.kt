package com.brotandos.kotlify.container.root

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.KotlifyLifecycleObserver
import com.brotandos.kotlify.container.VContainer
import io.reactivex.disposables.Disposable

class VRoot<T : VContainer<*, *>>(
        private val vContainer: T
) {

    private var lifecycleObserver: KotlifyLifecycleObserver? = null

    init {
        lifecycleObserver = KotlifyLifecycleObserver(vContainer)
    }

    fun disposeOnViewDestroyed(lifecycleOwner: LifecycleOwner) {
        KotlifyLifecycleObserver(vContainer).let {
            lifecycleObserver = it
            lifecycleOwner.lifecycle.addObserver(it)
        }
    }

    fun clearObservers(lifecycleOwner: LifecycleOwner) {
        lifecycleObserver?.let(lifecycleOwner.lifecycle::removeObserver)
    }

    fun addToComposite(disposable: Disposable) = vContainer.addToComposite(disposable)
}

inline fun <reified T : VContainer<*, *>> VRootOwner.vRoot(
        activity: ComponentActivity,
        init: T.() -> Unit
): VRoot<T> {
    val vContainer = KotlifyInternals.initiateWidgetContainer(Air, T::class.java)
    val vNewRoot = VRoot(vContainer)
    vContainer.init()
    val view = vContainer.buildWidget(activity, KotlifyContext(), KotlifyInternals.rootPath)
    activity.setContentView(view)
    vNewRoot.disposeOnViewDestroyed(activity)
    this.vRoot?.clearObservers(activity)
    this.vRoot = vNewRoot
    return vNewRoot
}
