package com.brotandos.kotlify.element

import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.ColorInt
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class VLabel<V : TextView>(size: LayoutSize) : WidgetElement<V>(size) {

    var textResId: Int? = null

    var text: String? = null

    var textResourceRelay: BehaviorRelay<Int>? = null

    var textRelay: BehaviorRelay<String>? = null

    var typeface: Typeface? = null

    var textSize: Float? = null

    var isTextSelectable: Boolean? = null

    var gravity: Int? = null

    var styles: Array<V.() -> Unit>? = null

    @ColorInt
    var textColor: Int? = null

    override fun initStyles(view: V?) {
        super.initStyles(view)
        view ?: return
        textResId?.let(view::setText) ?: text?.let(view::setText)
        typeface?.let(view::setTypeface)
        textSize?.let(view::setTextSize)
        gravity?.let(view::setGravity)
        textColor?.let(view::setTextColor)
        isTextSelectable?.let(view::setTextIsSelectable)
        styles?.forEach { style -> view.style() }
    }

    override fun initSubscriptions(view: V?) {
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