package com.brotandos.kotlify.common

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.brotandos.kotlify.element.UiEntity

class KotlifyLifecycleObserver(
    private val uiEntity: UiEntity<*>
) : LifecycleObserver {

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        uiEntity.dispose()
    }
}