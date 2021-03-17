package com.brotandos.kotlify.element

import android.widget.CheckBox
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class VCheckBox<V : CheckBox>(size: LayoutSize) : WidgetElement<V>(size) {

    var checkedRelay: BehaviorRelay<Boolean>? = null

    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)
        checkedRelay
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { view?.isChecked = it }
            ?.untilLifecycleDestroy()
    }
}