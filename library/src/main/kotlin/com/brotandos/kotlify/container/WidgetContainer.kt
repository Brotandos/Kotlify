package com.brotandos.kotlify.container

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.CustomSize
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.common.MatchParent
import com.brotandos.kotlify.element.ToggleOption
import com.brotandos.kotlify.element.VButton
import com.brotandos.kotlify.element.VEdit
import com.brotandos.kotlify.element.VImage
import com.brotandos.kotlify.element.VLabel
import com.brotandos.kotlify.element.VSimpleToggle
import com.brotandos.kotlify.element.VToggleGroup
import com.brotandos.kotlify.element.WidgetElement
import com.brotandos.kotlify.element.list.LayoutManager
import com.brotandos.kotlify.element.list.VRecycler
import com.google.android.material.button.MaterialButton
import com.jakewharton.rxrelay2.BehaviorRelay

interface WidgetContainer {

    val Int.dp: CustomLength
        get() = CustomLength(this)

    val CustomLength.water: LayoutSize
        get() = CustomSize(MatchParent, this)

    val CustomLength.fire: LayoutSize
        get() = CustomSize(this, MatchParent)

    val CustomLength.square: LayoutSize
        get() = CustomSize(this, this)

    val Activity.density: Float
        get() = resources.displayMetrics.density

    val Context.actionBarSize: LayoutSize
        get() {
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
                    .takeIf { it }
                    ?: return 50.dp.water
            val height = TypedValue.complexToFloat(typedValue.data).toInt()
            return height.dp.water
        }

    fun accept(widget: WidgetElement<*>)

    fun vToolbar(
            size: LayoutSize,
            init: VToolbar<Toolbar, Toolbar.LayoutParams>.() -> Unit
    ): VToolbar<Toolbar, Toolbar.LayoutParams> =
            object : VToolbar<Toolbar, Toolbar.LayoutParams>(size) {
                override fun createView(context: Context): Toolbar = Toolbar(context)
                override fun getChildLayoutParams(width: Int, height: Int): Toolbar.LayoutParams =
                        Toolbar.LayoutParams(width, height)
            }
                    .also(::accept)
                    .apply(init)

    fun vLabel(
            size: LayoutSize,
            init: VLabel<TextView>.() -> Unit
    ): VLabel<TextView> = object : VLabel<TextView>(size) {
        override fun createView(context: Context): TextView = TextView(context)
    }.also(::accept).apply(init)

    fun vLabel(
            size: LayoutSize,
            vararg styles: TextView.() -> Unit,
            init: VLabel<TextView>.() -> Unit
    ): VLabel<TextView> {
        val vLabel = object : VLabel<TextView>(size) {
            override fun createView(context: Context): TextView = TextView(context)
        }
        accept(vLabel)
        vLabel.styles = arrayOf(*styles)
        vLabel.init()
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
        }.also(::accept)
        vLabel.textResId = textResId
        vLabel.styles = arrayOf(*styles)
        vLabel.init()
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
        }.also(::accept)
        vLabel.text = text
        vLabel.styles = arrayOf(*styles)
        vLabel.init()
        return vLabel
    }

    fun vEdit(
            size: LayoutSize,
            vararg styles: EditText.() -> Unit,
            init: VEdit<EditText>.() -> Unit
    ): VEdit<EditText> {
        val vEdit = object : VEdit<EditText>(size) {
            override fun createView(context: Context): EditText = EditText(context)
        }.also(::accept)
        vEdit.styles = arrayOf(*styles)
        vEdit.init()
        return vEdit
    }

    fun vButton(
            size: LayoutSize,
            init: VButton<MaterialButton>.() -> Unit
    ): VButton<MaterialButton> = object : VButton<MaterialButton>(size) {
        override fun createView(context: Context): MaterialButton = MaterialButton(context)
    }.also(::accept).apply(init)

    fun vList(
            size: LayoutSize,
            mediator: VRecycler.Mediator,
            init: VRecycler.() -> Unit
    ): VRecycler = VRecycler(mediator = mediator, layoutManager = LayoutManager.Linear, size = size)
            .also(::accept)
            .apply(init)

    fun vGrid(
            size: LayoutSize,
            items: BehaviorRelay<List<VRecycler.Item>>,
            init: VRecycler.() -> Unit
    ): VRecycler = TODO("not implemented")

    fun vLinear(
            size: LayoutSize,
            init: VContainer<LinearLayout, LinearLayout.LayoutParams>.() -> Unit
    ): VLinear<LinearLayout, LinearLayout.LayoutParams> =
            object : VLinear<LinearLayout, LinearLayout.LayoutParams>(size) {
                override fun createView(context: Context): LinearLayout = LinearLayout(context)

                override fun getChildLayoutParams(width: Int, height: Int): LinearLayout.LayoutParams =
                        LinearLayout.LayoutParams(width, height)
            }.also(::accept).apply(init)

    fun vVertical(
            size: LayoutSize,
            init: VContainer<LinearLayout, LinearLayout.LayoutParams>.() -> Unit
    ) = VVertical(size).also(::accept).apply(init)

    fun vCard(
            size: LayoutSize,
            init: VCard<CardView, FrameLayout.LayoutParams>.() -> Unit
    ): VCard<CardView, FrameLayout.LayoutParams> =
            object : VCard<CardView, FrameLayout.LayoutParams>(size) {
                override fun createView(context: Context): CardView = CardView(context)

                override fun getChildLayoutParams(width: Int, height: Int): FrameLayout.LayoutParams =
                        FrameLayout.LayoutParams(width, height)
            }
                    .also(::accept)
                    .apply(init)

    fun vConstraint(
            size: LayoutSize,
            init: VConstraintItself.() -> Unit
    ): VConstraintItself {
        val vConstraint = VConstraintItself(Air)
        accept(vConstraint)
        vConstraint.init()
        return vConstraint
    }

    fun vImage(size: LayoutSize, resId: Int, init: VImage<ImageView>.() -> Unit): VImage<ImageView> {
        val vImage = object : VImage<ImageView>(size) {
            override fun createView(context: Context): ImageView = ImageView(context)
        }.also(::accept)
        vImage.imageResId = BehaviorRelay.createDefault(resId)
        vImage.init()
        return vImage
    }

    fun vImage(size: LayoutSize, init: VImage<ImageView>.() -> Unit): VImage<ImageView> =
            object : VImage<ImageView>(size) {
                override fun createView(context: Context): ImageView = ImageView(context)
            }.also(::accept).apply(init)

    fun <T : ToggleOption> vToggleGroup(
            size: LayoutSize,
            selectedOption: BehaviorRelay<T>,
            init: VToggleGroup<T>.() -> Unit
    ): VToggleGroup<T> {
        val vToggleGroup = VToggleGroup<T>(size).also(::accept)
        vToggleGroup.selectedOption = selectedOption
        vToggleGroup.init()
        return vToggleGroup
    }

    fun <T : ToggleOption> T.vSimpleToggle(
            size: LayoutSize,
            selectedOption: BehaviorRelay<T>,
            init: VSimpleToggle<T, ToggleButton>.() -> Unit
    ): VSimpleToggle<T, ToggleButton> {
        val vSimpleToggle = object : VSimpleToggle<T, ToggleButton>(size) {
            override fun createView(context: Context): ToggleButton = ToggleButton(context)
        }.also(::accept)
        vSimpleToggle.model = this
        vSimpleToggle.selectedOption = selectedOption
        vSimpleToggle.init()
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
        get() = { setTypeface(this@boldItalic, Typeface.BOLD_ITALIC) }

    val bold: TextView.() -> Unit
        get() = { setTypeface(typeface, Typeface.BOLD) }

    val italic: TextView.() -> Unit
        get() = { setTypeface(typeface, Typeface.ITALIC) }

    val boldItalic: TextView.() -> Unit
        get() = { setTypeface(typeface, Typeface.BOLD_ITALIC) }

    val blackText: TextView.() -> Unit
        get() = { setTextColor(Color.BLACK) }

    val Float.sp: TextView.() -> Unit
        get() = { textSize = this@sp }

    val Int.textColor: TextView.() -> Unit
        get() = { setTextColor(this@textColor) }

    val selectable: TextView.() -> Unit
        get() = { setTextIsSelectable(true) }

    val unselectable: TextView.() -> Unit
        get() = { setTextIsSelectable(false) }

    val singleLine: TextView.() -> Unit
        get() = { isSingleLine = true }
}