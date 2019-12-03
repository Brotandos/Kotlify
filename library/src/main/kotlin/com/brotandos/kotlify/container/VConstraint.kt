package com.brotandos.kotlify.container

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.edit
import androidx.core.view.children
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.element.ID_NOT_SET
import com.brotandos.kotlify.element.WidgetElement

private val START_SIDE_POLE = ConstraintSidePole.POSITIVE
private val TOP_SIDE_POLE = ConstraintSidePole.POSITIVE
private val END_SIDE_POLE = ConstraintSidePole.NEGATIVE
private val BOTTOM_SIDE_POLE = ConstraintSidePole.NEGATIVE
private val CONSTRAINT_TARGET_PARENT = null

typealias ConstraintMap = MutableMap<
        WidgetElement<*>,
        MutableMap<ConstraintSide, ConstraintTarget>
>

class VConstraint(size: LayoutSize) : VContainer<ConstraintLayout>(size) {

    private val constraintMap: ConstraintMap = mutableMapOf()

    private val constraintLayoutInits = mutableListOf<(ConstraintSet) -> Unit>()

    val parentStart = HorizontalTarget(START_SIDE_POLE, CONSTRAINT_TARGET_PARENT)
    val parentTop = VerticalTarget(TOP_SIDE_POLE, CONSTRAINT_TARGET_PARENT)
    val parentEnd = HorizontalTarget(END_SIDE_POLE, CONSTRAINT_TARGET_PARENT)
    val parentBottom = VerticalTarget(BOTTOM_SIDE_POLE, CONSTRAINT_TARGET_PARENT)

    val WidgetElement<*>.start get() = HorizontalTarget(START_SIDE_POLE, this)
    val WidgetElement<*>.top get() = VerticalTarget(TOP_SIDE_POLE, this)
    val WidgetElement<*>.end get() = HorizontalTarget(END_SIDE_POLE, this)
    val WidgetElement<*>.bottom get() = VerticalTarget(BOTTOM_SIDE_POLE, this)

    override fun createView(context: Context) = ConstraintLayout(context)

    override fun onBuildFinished(constraintLayout: ConstraintLayout) {
        super.onBuildFinished(constraintLayout)
        val sharedPreferences = constraintLayout.context.getSharedPreferences(
                KotlifyInternals.IDS_CACHE_FILE_NAME,
                Context.MODE_PRIVATE
        )
        identifyWidgets(sharedPreferences, constraintLayout.children.toList())
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintMap.forEach { (sourceWidget, constraints) ->
            constraints.forEach { (side, target) ->
                target.margin?.let { it ->
                    val margin = it.getValue(
                            constraintLayout.context.resources.displayMetrics.density.toInt()
                    )
                    constraintSet.connect(
                            sourceWidget.id,
                            side.value,
                            target.widgetElement?.id ?: ConstraintSet.PARENT_ID,
                            target.side.value,
                            margin
                    )
                } ?: constraintSet.connect(
                        sourceWidget.id,
                        side.value,
                        target.widgetElement?.id ?: ConstraintSet.PARENT_ID,
                        target.side.value
                )
            }
        }
        constraintLayoutInits.forEach { it.invoke(constraintSet) }
        constraintSet.applyTo(constraintLayout)
    }

    private fun identifyWidgets(
            sharedPreferences: SharedPreferences,
            viewChildren: List<View>
    ) {
        children
                .filterIsInstance<WidgetElement<*>>()
                .filter { it.id == ID_NOT_SET }
                .forEachIndexed { index, element ->
                    val key = element.getIdKey()
                    element.id = sharedPreferences
                            .getInt(element.getIdKey(), ID_NOT_SET)
                            .takeIf { it != ID_NOT_SET }
                            ?: generateId(key, sharedPreferences)
                    viewChildren[index].id = element.id
                }
    }

    private fun generateId(key: String, sharedPreferences: SharedPreferences): Int {
        val id = View.generateViewId()
        sharedPreferences.edit {
            putInt(key, id)
        }
        return id
    }

    fun WidgetElement<*>.startTo(target: HorizontalTarget) =
            registerConstraint(this, ConstraintSide.Start, target)

    fun WidgetElement<*>.topTo(target: VerticalTarget) =
            registerConstraint(this, ConstraintSide.Top, target)

    fun WidgetElement<*>.endTo(target: HorizontalTarget) =
            registerConstraint(this, ConstraintSide.End, target)

    fun WidgetElement<*>.bottomTo(target: VerticalTarget) =
            registerConstraint(this, ConstraintSide.Bottom, target)

    private fun registerConstraint(
            sourceWidget: WidgetElement<*>,
            sourceSide: ConstraintSide,
            target: ConstraintTarget
    ) {
        constraintMap[sourceWidget]?.let {
            it[sourceSide] = target
            return
        }
        constraintMap[sourceWidget] = mutableMapOf(sourceSide to target)
    }

    var WidgetElement<*>.horizontalBias: Float
        get() = KotlifyInternals.noGetter()
        set(value) {
            constraintLayoutInits += { it.setHorizontalBias(this.id, value) }
        }

    var WidgetElement<*>.verticalBias: Float
        get() = KotlifyInternals.noGetter()
        set(value) {
            constraintLayoutInits += { it.setVerticalBias(id, value) }
        }

    operator fun HorizontalTarget.plus(customLength: CustomLength) = HorizontalTarget(
            pole,
            widgetElement,
            customLength
    )

    operator fun HorizontalTarget.minus(customLength: CustomLength) = HorizontalTarget(
            pole,
            widgetElement,
            CustomLength(-customLength.coefficient)
    )

    operator fun VerticalTarget.plus(customLength: CustomLength) = VerticalTarget(
            pole,
            widgetElement,
            customLength
    )

    operator fun VerticalTarget.minus(customLength: CustomLength) = VerticalTarget(
            pole,
            widgetElement,
            CustomLength(-customLength.coefficient)
    )
}

enum class ConstraintSidePole {
    POSITIVE,
    NEGATIVE
}

sealed class ConstraintSide(val value: Int) {
    object Start : ConstraintSide(ConstraintSet.START)
    object Top : ConstraintSide(ConstraintSet.TOP)
    object End : ConstraintSide(ConstraintSet.END)
    object Bottom : ConstraintSide(ConstraintSet.BOTTOM)
}

open class ConstraintTarget(
        val side: ConstraintSide,
        val widgetElement: WidgetElement<*>? = CONSTRAINT_TARGET_PARENT,
        val margin: CustomLength? = null
)

class HorizontalTarget(
        val pole: ConstraintSidePole,
        widgetElement: WidgetElement<*>? = CONSTRAINT_TARGET_PARENT,
        margin: CustomLength? = null
) : ConstraintTarget(
        if (pole == START_SIDE_POLE) ConstraintSide.Start else ConstraintSide.End,
        widgetElement,
        margin
)

class VerticalTarget(
        val pole: ConstraintSidePole,
        widgetElement: WidgetElement<*>? = CONSTRAINT_TARGET_PARENT,
        margin: CustomLength? = null
) : ConstraintTarget(
        if (pole == TOP_SIDE_POLE) ConstraintSide.Top else ConstraintSide.Bottom,
        widgetElement,
        margin
)