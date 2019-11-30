package com.brotandos.kotlify.container.root

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.KotlifyLifecycleObserver
import com.brotandos.kotlify.container.VContainer

class VRoot<T : VContainer<*>>(private val vContainer: T) {

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
}

inline fun <reified T : VContainer<*>> Activity.vRoot(
        init: T.() -> Unit
): VRoot<T> {
    val vContainer = KotlifyInternals.initiateWidgetContainer(Air, T::class.java)
    val vRoot = VRoot(vContainer)
    vContainer.init()
    val view = vContainer.build(this, KotlifyContext())
    setContentView(view)
    return vRoot
}

inline fun <reified T : VContainer<*>> Activity.vRoot(
        lifecycleOwner: LifecycleOwner,
        vRootOwner: VRootOwner,
        init: T.() -> Unit
): VRoot<T> {
    val vContainer = KotlifyInternals.initiateWidgetContainer(Air, T::class.java)
    val vRoot = VRoot(vContainer)
    vContainer.init()
    val view = vContainer.build(this, KotlifyContext())
    setContentView(view)
    vRoot.disposeOnViewDestroyed(lifecycleOwner)
    vRootOwner.vRoot?.clearObservers(lifecycleOwner)
    vRootOwner.vRoot = vRoot
    return vRoot
}