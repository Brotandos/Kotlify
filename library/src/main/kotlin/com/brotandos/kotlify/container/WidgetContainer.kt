package com.brotandos.kotlify.container

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.CustomSize
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.common.MatchParent
import com.brotandos.kotlify.element.ToggleOption
import com.brotandos.kotlify.element.VImage
import com.brotandos.kotlify.element.VLabel
import com.brotandos.kotlify.element.list.VRecycler
import com.brotandos.kotlify.element.VSimpleToggle
import com.brotandos.kotlify.element.VToggleGroup
import com.brotandos.kotlify.element.WidgetElement
import com.brotandos.kotlify.element.list.LayoutManager
import com.jakewharton.rxrelay2.BehaviorRelay

interface WidgetContainer {

    val Int.dp: CustomLength
        get() = CustomLength(
            this
        )

    val CustomLength.water: LayoutSize
        get() = CustomSize(
            MatchParent,
            this
        )

    val CustomLength.fire: LayoutSize
        get() = CustomSize(
            this,
            MatchParent
        )

    // FIXME real height smaller than from xml
    val Context.actionBarSize: LayoutSize
        get() {
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
                    .takeIf { it }
                    ?: return 70.dp.water
            val height = TypedValue.complexToFloat(typedValue.data).toInt()
            return height.dp.water
        }

    fun accept(widget: WidgetElement<*>)

    fun vToolbar(
            size: LayoutSize,
            init: VToolbar<Toolbar, Toolbar.LayoutParams>.() -> Unit
    ): VToolbar<Toolbar, Toolbar.LayoutParams> {
        val vToolbar = object : VToolbar<Toolbar, Toolbar.LayoutParams>(size) {

            override fun createView(context: Context): Toolbar = Toolbar(context)

            override fun getChildLayoutParams(width: Int, height: Int): Toolbar.LayoutParams =
                    Toolbar.LayoutParams(width, height)
        }
        vToolbar.init()
        accept(vToolbar)
        return vToolbar
    }

    fun vLabel(size: LayoutSize, init: VLabel<TextView>.() -> Unit): VLabel<TextView> {
        val vLabel = object : VLabel<TextView>(size) {
            override fun createView(context: Context): TextView = TextView(context)
        }
        vLabel.init()
        accept(vLabel)
        return vLabel
    }

    fun vLabel(
            size: LayoutSize,
            vararg styles: TextView.() -> Unit,
            init: VLabel<TextView>.() -> Unit
    ): VLabel<TextView> {
        val vLabel = object : VLabel<TextView>(size) {
            override fun createView(context: Context): TextView = TextView(context)
        }
        vLabel.styles = arrayOf(*styles)
        vLabel.init()
        accept(vLabel)
        return vLabel
    }

    fun vLabel(
            size: LayoutSize,
            @StringRes textResId: Int,
            vararg styles: TextView.() -> Unit,
            init: VLabel<TextView>.() -> Unit
    ): VLabel<TextView> {
        val vLabel = object : VLabel<TextView>(size) {
            override fun createView(context: Context): TextView = TextView(context)
        }
        vLabel.textResId = textResId
        vLabel.styles = arrayOf(*styles)
        vLabel.init()
        accept(vLabel)
        return vLabel
    }

    fun vLabel(
            size: LayoutSize,
            text: String,
            vararg styles: TextView.() -> Unit,
            init: VLabel<TextView>.() -> Unit
    ): VLabel<TextView> {
        val vLabel = object : VLabel<TextView>(size) {
            override fun createView(context: Context): TextView = TextView(context)
        }
        vLabel.text = text
        vLabel.styles = arrayOf(*styles)
        vLabel.init()
        accept(vLabel)
        return vLabel
    }

    fun vList(
            size: LayoutSize,
            items: BehaviorRelay<List<VRecycler.Item>>,
            init: VRecycler.() -> Unit
    ): VRecycler {
        val vRecycler = VRecycler(
                itemsRelay = items,
                layoutManager = LayoutManager.Linear,
                size = size
        )
        vRecycler.init()
        accept(vRecycler)
        return vRecycler
    }

    fun vGrid(
            size: LayoutSize,
            items: BehaviorRelay<List<VRecycler.Item>>,
            init: VRecycler.() -> Unit
    ): VRecycler = TODO("not implemented")

    fun vLinear(
            size: LayoutSize,
            init: VContainer<LinearLayout, LinearLayout.LayoutParams>.() -> Unit
    ): VLinear<LinearLayout, LinearLayout.LayoutParams> {
        val vContainer = object : VLinear<LinearLayout, LinearLayout.LayoutParams>(size) {
            override fun createView(context: Context): LinearLayout = LinearLayout(context)

            override fun getChildLayoutParams(width: Int, height: Int): LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(width, height)
        }
        vContainer.init()
        accept(vContainer)
        return vContainer
    }

    fun vVertical(
            size: LayoutSize,
            init: VContainer<LinearLayout, LinearLayout.LayoutParams>.() -> Unit
    ): VVertical {
        val vContainer = VVertical(size)
        vContainer.init()
        accept(vContainer)
        return vContainer
    }

    fun vCard(
            size: LayoutSize,
            init: VCard<CardView, FrameLayout.LayoutParams>.() -> Unit
    ): VCard<CardView, FrameLayout.LayoutParams> {
        val vCard = object : VCard<CardView, FrameLayout.LayoutParams>(size) {
            override fun createView(context: Context): CardView =
                    CardView(context)

            override fun getChildLayoutParams(width: Int, height: Int): FrameLayout.LayoutParams =
                    FrameLayout.LayoutParams(width, height)
        }
        vCard.init()
        accept(vCard)
        return vCard
    }

    fun vRelative(
            size: LayoutSize,
            init: VRelative<RelativeLayout, RelativeLayout.LayoutParams>.() -> Unit
    ) {
        val vRelative = object : VRelative<RelativeLayout, RelativeLayout.LayoutParams>(size) {

            override fun createView(context: Context): RelativeLayout = RelativeLayout(context)

            override fun getChildLayoutParams(width: Int, height: Int): RelativeLayout.LayoutParams =
                    RelativeLayout.LayoutParams(width, height)
        }
    }

    fun vConstraint(
            size: LayoutSize,
            init: VConstraint<ConstraintLayout>.() -> Unit
    ): VConstraint<ConstraintLayout> {
        val vConstraint = object : VConstraint<ConstraintLayout>(size) {
            override fun createView(context: Context): ConstraintLayout = ConstraintLayout(context)
        }
        vConstraint.init()
        accept(vConstraint)
        return vConstraint
    }

    fun vImage(size: LayoutSize, resId: Int, init: VImage<ImageView>.() -> Unit): VImage<ImageView> {
        val vImage = object : VImage<ImageView>(size) {
            override fun createView(context: Context): ImageView = ImageView(context)
        }
        vImage.imageResId = BehaviorRelay.createDefault(resId)
        vImage.init()
        accept(vImage)
        return vImage
    }

    fun vImage(size: LayoutSize, init: VImage<ImageView>.() -> Unit): VImage<ImageView> {
        val vImage = object : VImage<ImageView>(size) {
            override fun createView(context: Context): ImageView = ImageView(context)
        }
        vImage.init()
        accept(vImage)
        return vImage
    }

    fun <T : ToggleOption> vToggleGroup(
            size: LayoutSize,
            selectedOption: BehaviorRelay<T>,
            init: VToggleGroup<T>.() -> Unit
    ): VToggleGroup<T> {
        val vToggleGroup = VToggleGroup<T>(size)
        vToggleGroup.selectedOption = selectedOption
        vToggleGroup.init()
        accept(vToggleGroup)
        return vToggleGroup
    }

    fun <T : ToggleOption> T.vSimpleToggle(
            size: LayoutSize,
            selectedOption: BehaviorRelay<T>,
            init: VSimpleToggle<T, ToggleButton>.() -> Unit
    ): VSimpleToggle<T, ToggleButton> {
        val vSimpleToggle = object : VSimpleToggle<T, ToggleButton>(size) {
            override fun createView(context: Context): ToggleButton = ToggleButton(context)
        }
        vSimpleToggle.model = this
        vSimpleToggle.selectedOption = selectedOption
        vSimpleToggle.init()
        accept(vSimpleToggle)
        return vSimpleToggle
    }

    // VLabel styles
    val textCenter: TextView.() -> Unit
        get() = { gravity = Gravity.CENTER_HORIZONTAL }

    operator fun Typeface.unaryPlus(): TextView.() -> Unit =
            { typeface = this@unaryPlus }

    val Typeface.bold: TextView.() -> Unit
        get() = { setTypeface(this@bold, Typeface.BOLD) }

    val Typeface.italic: TextView.() -> Unit
        get() = { setTypeface(this@italic, Typeface.ITALIC) }

    val Typeface.boldItalic: TextView.() -> Unit
        get() =  { setTypeface(this@boldItalic, Typeface.BOLD_ITALIC) }

    val bold: TextView.() -> Unit
        get() = { setTypeface(typeface, Typeface.BOLD) }

    val italic: TextView.() -> Unit
        get() = { setTypeface(typeface, Typeface.ITALIC) }

    val boldItalic: TextView.() -> Unit
        get() =  { setTypeface(typeface, Typeface.BOLD_ITALIC) }

    val blackText: TextView.() -> Unit
        get() =  { setTextColor(Color.BLACK) }

    val Float.sp: TextView.() -> Unit
        get() = { textSize = this@sp }

    val Int.textColor: TextView.() -> Unit
        get() = { setTextColor(this@textColor) }

    val selectable: TextView.() -> Unit
        get() = { setTextIsSelectable(true) }

    val unselectable: TextView.() -> Unit
        get() = { setTextIsSelectable(false) }
}