package com.brotandos.kotlify

import android.content.Context
import androidx.annotation.CallSuper
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class UiEntity<T> : Disposable {

    private val disposables = CompositeDisposable()

    open var vShow: BehaviorRelay<Boolean>? = null

    override fun isDisposed(): Boolean = disposables.isDisposed

    @CallSuper
    override fun dispose() = disposables.dispose()

    protected fun Disposable.addToComposite() = disposables.add(this)

    abstract fun build(context: Context, kotlifyContext: KotlifyContext): T
}