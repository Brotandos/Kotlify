package com.brotandos.kotlify.element

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.ColorInt
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

class VLabel(size: LayoutSize) : WidgetElement<TextView>(size) {

    var textResId: Int? = null

    var text: String? = null

    var textResourceRelay: BehaviorRelay<Int>? = null

    var textRelay: BehaviorRelay<String>? = null

    var typeface: Typeface? = null

    var textSize: Float? = null

    var isTextSelectable: Boolean? = null

    var gravity: Int? = null

    var styles: Array<TextView.() -> Unit>? = null

    @ColorInt
    var textColor: Int? = null

    override fun createView(context: Context): TextView {
        val textView = TextView(context)
        textResId?.let(textView::setText) ?: text?.let(textView::setText)
        typeface?.let(textView::setTypeface)
        textSize?.let(textView::setTextSize)
        gravity?.let(textView::setGravity)
        textColor?.let(textView::setTextColor)
        isTextSelectable?.let(textView::setTextIsSelectable)
        styles?.forEach { style -> textView.style() }
        return textView
    }

    override fun initSubscriptions(view: TextView?) {
        super.initSubscriptions(view)
        textResourceRelay
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { view?.setText(it) }
                ?.untilLifecycleDestroy()
                ?: textRelay
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe { view?.text = it }
                        ?.untilLifecycleDestroy()
    }
}