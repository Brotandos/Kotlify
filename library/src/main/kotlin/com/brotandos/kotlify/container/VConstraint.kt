package com.brotandos.kotlify.container

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.edit
import androidx.core.view.children
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.CustomSize
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.common.WrapContent
import com.brotandos.kotlify.container.root.VRoot
import com.brotandos.kotlify.container.root.VRootOwner
import com.brotandos.kotlify.element.ID_NOT_SET
import com.brotandos.kotlify.element.WidgetElement

private val START_SIDE_POLE = ConstraintSidePole.POSITIVE
private val TOP_SIDE_POLE = ConstraintSidePole.POSITIVE
private val END_SIDE_POLE = ConstraintSidePole.NEGATIVE
private val BOTTOM_SIDE_POLE = ConstraintSidePole.NEGATIVE
private val CONSTRAINT_TARGET_PARENT = null

typealias ConstraintMap = MutableMap<
        WidgetElement<*>,
        MutableMap<ConstraintSide, () -> ConstraintTarget>
>

typealias VConstraintActual = VConstraint<ConstraintLayout>

abstract class VConstraint<V : ConstraintLayout>(
        size: LayoutSize
) : VContainer<V, ConstraintLayout.LayoutParams>(size) {

    private val constraints: ConstraintMap = mutableMapOf()

    private val constraintLayoutInits = mutableListOf<(ConstraintSet) -> Unit>()

    val parentStart = HorizontalTarget(START_SIDE_POLE, CONSTRAINT_TARGET_PARENT)
    val parentTop = VerticalTarget(TOP_SIDE_POLE, CONSTRAINT_TARGET_PARENT)
    val parentEnd = HorizontalTarget(END_SIDE_POLE, CONSTRAINT_TARGET_PARENT)
    val parentBottom = VerticalTarget(BOTTOM_SIDE_POLE, CONSTRAINT_TARGET_PARENT)

    val WidgetElement<*>.start get() = HorizontalTarget(START_SIDE_POLE, this)
    val WidgetElement<*>.top get() = VerticalTarget(TOP_SIDE_POLE, this)
    val WidgetElement<*>.end get() = HorizontalTarget(END_SIDE_POLE, this)
    val WidgetElement<*>.bottom get() = VerticalTarget(BOTTOM_SIDE_POLE, this)

    val restSpaceWater = CustomSize(0.dp, WrapContent)

    override fun onBuildFinished(constraintLayout: V) {
        super.onBuildFinished(constraintLayout)
        val sharedPreferences = constraintLayout.context.getSharedPreferences(
                KotlifyInternals.IDS_CACHE_FILE_NAME,
                Context.MODE_PRIVATE
        )
        identifyWidgets(sharedPreferences, constraintLayout.children.toList())
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        for ((sourceWidget, targets) in constraints) {
            targets.forEach { (side, targetGetter) ->
                val target = targetGetter.invoke()
                val margin = target.margin ?: return@forEach constraintSet.connect(
                        sourceWidget.id,
                        side.value,
                        target.widgetElement?.id ?: ConstraintSet.PARENT_ID,
                        target.side.value
                )
                constraintSet.connect(
                        sourceWidget.id,
                        side.value,
                        target.widgetElement?.id ?: ConstraintSet.PARENT_ID,
                        target.side.value,
                        margin.getValue(constraintLayout.context.density)
                )
            }
        }
        constraintLayoutInits.forEach { it.invoke(constraintSet) }
        constraintSet.applyTo(constraintLayout)
    }

    override fun getChildLayoutParams(width: Int, height: Int): ConstraintLayout.LayoutParams =
            ConstraintLayout.LayoutParams(width, height)

    private fun identifyWidgets(
            sharedPreferences: SharedPreferences,
            childViews: List<View>
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
                    childViews[index].id = element.id
                }
    }

    private fun generateId(key: String, sharedPreferences: SharedPreferences): Int {
        val id = View.generateViewId()
        sharedPreferences.edit {
            putInt(key, id)
        }
        return id
    }

    fun WidgetElement<*>.startTo(targetGetter: () -> HorizontalTarget) =
            addConstraint(this, ConstraintSide.Start, targetGetter)

    fun WidgetElement<*>.topTo(targetGetter: () -> VerticalTarget) =
            addConstraint(this, ConstraintSide.Top, targetGetter)

    fun WidgetElement<*>.endTo(targetGetter: () -> HorizontalTarget) =
            addConstraint(this, ConstraintSide.End, targetGetter)

    fun WidgetElement<*>.bottomTo(targetGetter: () -> VerticalTarget) =
            addConstraint(this, ConstraintSide.Bottom, targetGetter)

    private fun addConstraint(
            sourceWidget: WidgetElement<*>,
            sourceSide: ConstraintSide,
            targetGetter: () -> ConstraintTarget
    ) {
        constraints[sourceWidget]?.let {
            it[sourceSide] = targetGetter
            return
        }
        constraints[sourceWidget] = mutableMapOf(sourceSide to targetGetter)
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

    fun WidgetElement<*>.packChainsHorizontally() {
        constraintLayoutInits += { it.setHorizontalChainStyle(id, ConstraintSet.CHAIN_PACKED) }
    }

    fun WidgetElement<*>.packChainsVertically() {
        constraintLayoutInits += { it.setVerticalChainStyle(id, ConstraintSet.CHAIN_PACKED) }
    }

    var defaultWidth: Int
        get() = KotlifyInternals.noGetter()
        set(value) {
            constraintLayoutInits += { it.constrainDefaultWidth(id, value) }
        }

    var defaultHeight: Int
        get() = KotlifyInternals.noGetter()
        set(value) {
            constraintLayoutInits += { it.constrainDefaultHeight(id, value) }
        }

    operator fun HorizontalTarget.plus(customLength: CustomLength) = HorizontalTarget(
            pole,
            targetWidget = widgetElement,
            margin = customLength
    )

    operator fun HorizontalTarget.minus(customLength: CustomLength) = HorizontalTarget(
            pole,
            targetWidget = widgetElement,
            margin = CustomLength(-customLength.coefficient)
    )

    operator fun VerticalTarget.plus(customLength: CustomLength) = VerticalTarget(
            pole,
            targetWidget = widgetElement,
            margin = customLength
    )

    operator fun VerticalTarget.minus(customLength: CustomLength) = VerticalTarget(
            pole,
            targetWidget = widgetElement,
            margin = CustomLength(-customLength.coefficient)
    )
}

/**
 * If [ConstraintSidePole.POSITIVE], then side is [ConstraintSide.Start] or [ConstraintSide.Top]
 * If [ConstraintSidePole.NEGATIVE], then side is [ConstraintSide.End] or [ConstraintSide.Bottom]
 * */
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
        targetWidget: WidgetElement<*>? = CONSTRAINT_TARGET_PARENT,
        margin: CustomLength? = null
) : ConstraintTarget(
        if (pole == START_SIDE_POLE) ConstraintSide.Start else ConstraintSide.End,
        targetWidget,
        margin
)

class VerticalTarget(
        val pole: ConstraintSidePole,
        targetWidget: WidgetElement<*>? = CONSTRAINT_TARGET_PARENT,
        margin: CustomLength? = null
) : ConstraintTarget(
        if (pole == TOP_SIDE_POLE) ConstraintSide.Top else ConstraintSide.Bottom,
        targetWidget,
        margin
)

inline fun VRootOwner.vConstraintRoot(
        activity: ComponentActivity,
        init: VConstraintActual.() -> Unit
): VRoot<VConstraintActual> {
    val vContainer = object : VConstraintActual(Air) {
        override fun createView(context: Context): ConstraintLayout = ConstraintLayout(context)
    }
    val vNewRoot = VRoot<VConstraintActual>(vContainer)
    vContainer.init()
    val view = vContainer.buildWidget(activity, KotlifyContext(), KotlifyInternals.rootPath)
    activity.setContentView(view)
    vNewRoot.disposeOnViewDestroyed(activity)
    this.vRoot?.clearObservers(activity)
    this.vRoot = vNewRoot
    return vNewRoot
}