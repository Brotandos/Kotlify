package com.brotandos.kotlify.element

import android.content.Context
import android.widget.LinearLayout
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.exception.PathInTreeIgnoredException
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * TODO list:
 * - multiselect
 * */
class VToggleGroup<T : ToggleOption>(
        size: LayoutSize
) : WidgetElement<MaterialButtonToggleGroup>(size) {

    private val children = linkedMapOf<T, VButton<MaterialButton>>()

    private lateinit var options: Map<Int, T>

    var selectedOption: BehaviorRelay<T>? = null

    var isVertical: Boolean = false

    var commonChildrenInit: ((VButton<*>) -> Unit)? = null

    override fun createView(context: Context): MaterialButtonToggleGroup {
        val toggleGroup = MaterialButtonToggleGroup(context)

        // TODO make customizable
        toggleGroup.isSingleSelection = true
        toggleGroup.isSelectionRequired = true
        var index = 0
        children.forEach { (option, vToggle) ->
            // TODO add multiselect
            commonChildrenInit?.invoke(vToggle)
            val sharedPreferences = context.getSharedPreferences(
                    KotlifyInternals.IDS_CACHE_FILE_NAME,
                    Context.MODE_PRIVATE
            )
            val path = (pathInsideTree ?: throw PathInTreeIgnoredException()) + index
            vToggle.identify(sharedPreferences, context.packageName, context.javaClass.simpleName, path)
            val view: MaterialButton = vToggle.buildWidget(context, KotlifyContext(), path)
            view.text = option.name
            toggleGroup.addView(view)
            index++
        }
        options = children.map { (option, vToggle) -> vToggle.id to option }.toMap()
        toggleGroup.orientation = if (isVertical) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
        // TODO make for multiselect
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val option = options[checkedId]
                    ?: throw IllegalStateException("There's no option with id: [$checkedId]")
            selectedOption?.accept(option)
        }
        return toggleGroup
    }

    override fun initSubscriptions(view: MaterialButtonToggleGroup?) {
        super.initSubscriptions(view)
        selectedOption
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { selectedOption ->
                    val checkedToggle = children[selectedOption]
                            ?: throw IllegalStateException("There's no vToggle binded to option [${selectedOption.name}]")
                    if (view?.checkedButtonId == checkedToggle.id) return@subscribe
                    view?.check(checkedToggle.id)
                }
                ?.untilLifecycleDestroy()
    }

    fun T.vToggle(
            size: LayoutSize,
            init: (VButton<MaterialButton>.() -> Unit)? = null
    ): VButton<MaterialButton> {
        val vToggle = object : VButton<MaterialButton>(size) {
            override fun createView(context: Context): MaterialButton = MaterialButton(context)
        }.apply { init?.invoke(this) }
        disposables.add(vToggle)
        children += this to vToggle
        return vToggle
    }
}

open class ToggleOption(val name: String)