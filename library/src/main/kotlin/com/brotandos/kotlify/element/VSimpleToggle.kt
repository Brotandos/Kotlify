package com.brotandos.kotlify.element

import android.content.Context
import android.widget.ToggleButton
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

class VSimpleToggle<T : ToggleOption>(
        size: LayoutSize
) : WidgetElement<ToggleButton>(size) {

    var selectedOption: BehaviorRelay<T>? = null

    var isCheckedRelay: BehaviorRelay<Boolean>? = null

    var model: T? = null

    override fun createView(context: Context): ToggleButton {
        val button = ToggleButton(context)
        model?.name?.let {
            button.textOff = it
            button.textOn = it
        }
        button.setOnCheckedChangeListener { _, isChecked ->
            if (isCheckedRelay?.value == isChecked) return@setOnCheckedChangeListener button.setChecked(true)

            if (isChecked) {
                selectedOption?.accept(model)
            }
            isCheckedRelay?.accept(isChecked)
        }
        return button
    }

    override fun initSubscriptions(view: ToggleButton?) {
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