package com.brotandos.kotlify.container.root

import io.reactivex.disposables.Disposable

interface VRootOwner {

    var vRoot: VRoot<*>?

    fun Disposable.untilLifecycleDestroy() {
        vRoot?.addToComposite(this)
    }
}