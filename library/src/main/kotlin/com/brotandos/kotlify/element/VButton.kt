package com.brotandos.kotlify.element

import com.brotandos.kotlify.common.LayoutSize
import com.google.android.material.button.MaterialButton
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class VButton<V : MaterialButton>(size: LayoutSize) : WidgetElement<V>(size) {

    var isChecked: BehaviorRelay<Boolean>? = null

    override fun initStyles(view: V?) {
        super.initStyles(view)
        view ?: return
        view.addOnCheckedChangeListener { _, isChecked ->
            this.isChecked?.accept(isChecked)
        }
    }

    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)
        isChecked
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    if (view?.isChecked == it) return@subscribe
                    view?.isChecked = it
                }
                ?.untilLifecycleDestroy()
    }
}