package com.brotandos.kotlify.element

import android.content.Context
import android.graphics.Typeface
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.PublishRelay

class VEdit(size: LayoutSize) : WidgetElement<EditText>(size) {

    var textRelay = PublishRelay.create<CharSequence>()

    var textResId: Int? = null

    var text: String? = null

    var typeface: Typeface? = null

    var textSize: Float? = null

    var gravity: Int? = null

    var styles: Array<TextView.() -> Unit>? = null

    @ColorInt
    var textColor: Int? = null

    override fun createView(context: Context): EditText {
        val editText = EditText(context)
        textResId?.let(editText::setText) ?: text?.let(editText::setText)
        typeface?.let(editText::setTypeface)
        textSize?.let(editText::setTextSize)
        gravity?.let(editText::setGravity)
        textColor?.let(editText::setTextColor)
        styles?.forEach { style -> editText.style() }
        return editText
    }
}