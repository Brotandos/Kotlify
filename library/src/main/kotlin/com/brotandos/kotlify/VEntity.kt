package com.brotandos.kotlify

import android.content.Context
import androidx.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class VEntity<T> : Disposable {

    private val disposables = CompositeDisposable()

    override fun isDisposed(): Boolean = disposables.isDisposed

    @CallSuper
    override fun dispose() = disposables.dispose()

    protected fun Disposable.addToComposite() = disposables.add(this)

    abstract fun build(context: Context, kotlifyContext: KotlifyContext): T
}