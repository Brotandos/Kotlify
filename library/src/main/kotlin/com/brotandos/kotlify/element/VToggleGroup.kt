package com.brotandos.kotlify.element

import android.content.Context
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.LayoutSize
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * TODO list:
 * */
class VToggleGroup<T : ToggleOption>(
        size: LayoutSize
) : WidgetElement<MaterialButtonToggleGroup>(size) {

    val children = mutableListOf<Pair<T, VButton<MaterialButton>>>()

    var selectedOption: BehaviorRelay<T>? = null

    var options: BehaviorRelay<List<T>>? = null

    override fun createView(context: Context): MaterialButtonToggleGroup {
        val toggleGroup = MaterialButtonToggleGroup(context)
        toggleGroup.isSingleSelection = true
        children.forEachIndexed { index, (option, vToggle) ->
            // TODO add multiselect
            lateinit var view: MaterialButton
            if (vToggle.isChecked == null) {
                vToggle.isChecked = BehaviorRelay.createDefault(false)
            }
            vToggle.isChecked?.subscribe {
                selectedOption?.accept(option)
            }
            view = vToggle.build(context, KotlifyContext())
            view.text = option.name
            toggleGroup.addView(view, index)
        }
        return toggleGroup
    }

    override fun initSubscriptions(view: MaterialButtonToggleGroup?) {
        super.initSubscriptions(view)
        selectedOption
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { selectedOption ->
                    children.forEach { (option, vToggle) ->
                        vToggle.isChecked?.accept(option == selectedOption)
                    }
                }
                ?.untilLifecycleDestroy()
    }

    fun T.vToggle(
            size: LayoutSize,
            init: VButton<MaterialButton>.() -> Unit
    ): VButton<MaterialButton> {
        val vToggle = object : VButton<MaterialButton>(size) {
            override fun createView(context: Context): MaterialButton = MaterialButton(context)
        }.also(init)
        disposables.add(vToggle)
        children += this to vToggle
        return vToggle
    }

}

open class ToggleOption(val name: String)