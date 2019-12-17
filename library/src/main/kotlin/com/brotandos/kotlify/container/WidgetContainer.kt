package com.brotandos.kotlify.container

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.CustomSize
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.common.MatchParent
import com.brotandos.kotlify.element.ToggleOption
import com.brotandos.kotlify.element.VImage
import com.brotandos.kotlify.element.VLabel
import com.brotandos.kotlify.element.VRecycler
import com.brotandos.kotlify.element.VToggleGroup
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

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

    fun vToolbar(
            size: LayoutSize = 50.dp.water,
            init: VToolbar.() -> Unit
    ): VToolbar

    fun vLabel(size: LayoutSize, init: VLabel.() -> Unit): VLabel

    fun vLabel(
            size: LayoutSize,
            @StringRes textResId: Int,
            vararg styles: TextView.() -> Unit,
            init: VLabel.() -> Unit
    ): VLabel

    fun vLabel(
            size: LayoutSize,
            text: String,
            vararg styles: TextView.() -> Unit,
            init: VLabel.() -> Unit
    ): VLabel

    fun vLabel(
            size: LayoutSize,
            vararg styles: TextView.() -> Unit,
            init: VLabel.() -> Unit
    ): VLabel

    fun vList(
        size: LayoutSize,
        items: BehaviorRelay<List<VRecycler.Item>>,
        init: VRecycler.() -> Unit
    ): VRecycler

    fun vGrid(
            size: LayoutSize,
            items: BehaviorRelay<List<VRecycler.Item>>,
            init: VRecycler.() -> Unit
    ): VRecycler

    fun vLinear(
            size: LayoutSize,
            init: VContainer<LinearLayout>.() -> Unit
    ): VContainer<LinearLayout>

    fun vVertical(
        size: LayoutSize,
        init: VContainer<LinearLayout>.() -> Unit
    ): VContainer<LinearLayout>

    fun vVertical(init: VContainer<LinearLayout>.() -> Unit): VContainer<LinearLayout>

    fun vCard(size: LayoutSize, init: VCard.() -> Unit): VCard

    fun vConstraint(size: LayoutSize, init: VConstraint.() -> Unit): VConstraint

    fun vImage(size: LayoutSize, resId: Int, init: VImage.() -> Unit): VImage

    fun vImage(size: LayoutSize, url: String, init: VImage.() -> Unit): VImage

    fun <T : ToggleOption> vToggleGroup(
            size: LayoutSize,
            selectedOption: BehaviorRelay<T>,
            init: VToggleGroup<T>.() -> Unit
    ): VToggleGroup<T>

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