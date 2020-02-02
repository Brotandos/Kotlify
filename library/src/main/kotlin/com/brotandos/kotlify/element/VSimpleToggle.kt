package com.brotandos.kotlify.element

import android.widget.ToggleButton
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class VSimpleToggle<T : ToggleOption, V : ToggleButton>(
        size: LayoutSize
) : WidgetElement<V>(size) {

    var selectedOption: BehaviorRelay<T>? = null

    var isCheckedRelay: BehaviorRelay<Boolean>? = null

    var model: T? = null

    override fun initStyles(view: V?) {
        super.initStyles(view)
        view ?: return
        model?.name?.let {
            view.textOff = it
            view.textOn = it
        }
        view.setOnCheckedChangeListener { _, isChecked ->
            if (isCheckedRelay?.value == isChecked)
                return@setOnCheckedChangeListener view.setChecked(true)

            if (isChecked) {
                selectedOption?.accept(model)
            }
            isCheckedRelay?.accept(isChecked)
        }
    }

    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)
        selectedOption
                ?.distinctUntilChanged()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    val isChecked = it == model
                    isCheckedRelay?.accept(isChecked) ?: view?.setChecked(isChecked)
                }
                ?.untilLifecycleDestroy()
        isCheckedRelay
                ?.distinctUntilChanged()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    view?.isChecked = it
                    if (it) selectedOption?.accept(model)
                }
                ?.untilLifecycleDestroy()
    }
}