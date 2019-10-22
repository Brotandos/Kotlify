package com.brotandos.kotlify

import android.widget.LinearLayout
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

interface WidgetContainer {

    val Int.dp: CustomLength get() = CustomLength(this)

    val CustomLength.water: LayoutSize get() = CustomSize(MatchParent, this)

    val CustomLength.fire: LayoutSize get() = CustomSize(this, MatchParent)

    fun vToolbar(size: LayoutSize, init: VToolbar.() -> Unit): Disposable

    fun <E> vRecycler(
        size: LayoutSize,
        items: BehaviorRelay<List<E>>,
        init: VRecycler<E>.() -> Unit
    ): Disposable

    fun vVertical(
        size: LayoutSize,
        init: VContainer<LinearLayout>.() -> Unit
    ): Disposable

    fun vVertical(init: VContainer<LinearLayout>.() -> Unit): Disposable
}