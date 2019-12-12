package com.brotandos.kotlify.container

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.CustomSize
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.common.MatchParent
import com.brotandos.kotlify.element.VLabel
import com.brotandos.kotlify.element.VRecycler
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

    val actionBarSize
        get() = android.R.attr.actionBarSize.dp.water

    fun vToolbar(
            size: LayoutSize = android.R.attr.actionBarSize.dp.water,
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

    fun <E> vRecycler(
        size: LayoutSize,
        items: BehaviorRelay<List<E>>,
        init: VRecycler<E>.() -> Unit
    ): VRecycler<E>

    fun vVertical(
        size: LayoutSize,
        init: VContainer<LinearLayout>.() -> Unit
    ): VContainer<LinearLayout>

    fun vVertical(init: VContainer<LinearLayout>.() -> Unit): VContainer<LinearLayout>

    fun vCard(size: LayoutSize, init: VContainer<CardView>.() -> Unit): VContainer<CardView>

    // VLabel styles
    val textCenter: TextView.() -> Unit
        get() = { gravity = Gravity.CENTER_HORIZONTAL }

    operator fun Typeface.unaryPlus(): TextView.() -> Unit =
            { typeface = this@unaryPlus }

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