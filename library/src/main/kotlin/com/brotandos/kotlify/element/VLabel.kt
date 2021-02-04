package com.brotandos.kotlify.element

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.annotation.ColorInt
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer

abstract class VLabel<V : TextView>(size: LayoutSize) : WidgetElement<V>(size) {

    private var listener: Listener? = null

    var textResId: Int? = null

    var text: String? = null

    var textResourceRelay: BehaviorRelay<Int>? = null

    var textRelay: BehaviorRelay<String>? = null

    var typeface: Typeface? = null

    var textSize: Float? = null

    var isTextSelectable: Boolean? = null

    var gravity: Int? = null

    var styles: Array<V.() -> Unit>? = null

    var textColors: ColorStateList? = null

    @ColorInt
    var textColor: Int? = null

    override fun initStyles(view: V?) {
        super.initStyles(view)
        view ?: return
        textResId?.let(view::setText) ?: text?.let(view::setText)
        typeface?.let(view::setTypeface)
        textSize?.let(view::setTextSize)
        gravity?.let(view::setGravity)
        textColors?.let(view::setTextColor) ?: textColor?.let(view::setTextColor)
        isTextSelectable?.let(view::setTextIsSelectable)
        styles?.forEach { style -> view.style() }
    }

    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)
        textResourceRelay
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { view?.setText(it) }
                ?.untilLifecycleDestroy()
        textRelay?.let { relay ->
            view?.let { listener = Listener(it, relay).also(it::addTextChangedListener) }
            relay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { text ->
                    listener?.let { view?.removeTextChangedListener(it) }
                    view?.text = text
                    listener?.let { view?.addTextChangedListener(it) }
                }
                .untilLifecycleDestroy()
        }
    }

    private class Listener(private val view: TextView, private val observer: Consumer<in String>) :
        MainThreadDisposable(), TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable) {
            if (!isDisposed) {
                view.removeTextChangedListener(this)
                observer.accept(s.toString())
                view.addTextChangedListener(this)
            }
        }

        override fun onDispose() {
            view.removeTextChangedListener(this)
        }
    }
}