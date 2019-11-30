package com.brotandos.kotlify.container

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.element.WidgetElement

class VConstraint(size: LayoutSize) : VContainer<ConstraintLayout>(size) {

    private val constraints = mutableMapOf<Side, ConstraintTarget>()

    private val constraintLayoutInits = mutableListOf<(ConstraintSet) -> Unit>()

    val parentStart = ConstraintTarget(ConstraintSet.PARENT_ID, ConstraintSet.START)
    val parentTop = ConstraintTarget(ConstraintSet.PARENT_ID, ConstraintSet.TOP)
    val parentEnd = ConstraintTarget(ConstraintSet.PARENT_ID, ConstraintSet.END)
    val parentBottom = ConstraintTarget(ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
    val Int.start get() = ConstraintTarget(this, ConstraintSet.START)
    val Int.top get() = ConstraintTarget(this, ConstraintSet.TOP)
    val Int.end get() = ConstraintTarget(this, ConstraintSet.END)
    val Int.bottom get() = ConstraintTarget(this, ConstraintSet.BOTTOM)

    override fun createView(context: Context) = ConstraintLayout(context)

    override fun onBuildFinished(view: ConstraintLayout) {
        super.onBuildFinished(view)
        val constraintSet = ConstraintSet()
        constraintSet.clone(view)
        constraints.forEach { (side, target) ->
            val marginValue = target.margin?.getValue(
                    view.context.resources.displayMetrics.density.toInt()
            ) ?: return@forEach constraintSet.connect(side.id, side.value, target.id, target.value)
            constraintSet.connect(side.id, side.value, target.id, target.value, marginValue)
        }
        constraintLayoutInits.forEach { it.invoke(constraintSet) }
        constraintSet.applyTo(view)
    }

    fun WidgetElement<*>.startTo(target: ConstraintTarget) {
        constraints[Side(id, ConstraintSet.START)] = target
    }

    fun WidgetElement<*>.topTo(target: ConstraintTarget) {
        constraints[Side(id, ConstraintSet.TOP)] = target
    }

    fun WidgetElement<*>.endTo(target: ConstraintTarget) {
        constraints[Side(id, ConstraintSet.END)] = target
    }

    fun WidgetElement<*>.bottomTo(target: ConstraintTarget) {
        constraints[Side(id, ConstraintSet.BOTTOM)] = target
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

    operator fun ConstraintTarget.plus(margin: CustomLength): ConstraintTarget {
        this.margin = margin
        return this
    }

    operator fun ConstraintTarget.minus(margin: CustomLength): ConstraintTarget {
        this.margin = margin
        return this
    }
}

class ConstraintTarget(
        id: Int,
        value: Int,
        var margin: CustomLength? = null
) : Side(id, value)

open class Side(
        val id: Int,
        val value: Int
)