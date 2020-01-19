package com.brotandos.kotlify.element

import android.content.Context
import androidx.annotation.CallSuper
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.KotlifyInternals.NO_GETTER
import com.brotandos.kotlify.common.NoGetterException
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.lang.RuntimeException

abstract class UiEntity<T> : Disposable {

    protected val disposables = CompositeDisposable()

    protected var isAppearedRelay: BehaviorRelay<Boolean>? = null
    var isAppeared: BehaviorRelay<Boolean>
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
        get() = KotlifyInternals.noGetter()
        set(value) { isAppearedRelay = value }

    abstract fun build(
            context: Context,
            kotlifyContext: KotlifyContext
    ): T

    fun addToComposite(disposable: Disposable) {
        disposables.add(disposable)
    }

    override fun isDisposed(): Boolean = disposables.isDisposed

    @CallSuper
    override fun dispose() = disposables.dispose()

    protected fun Disposable.untilLifecycleDestroy() = disposables.add(this)
}