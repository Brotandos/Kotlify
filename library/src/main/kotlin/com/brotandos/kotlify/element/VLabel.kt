package com.brotandos.kotlify.element

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class VLabel<V : TextView>(size: LayoutSize) : WidgetElement<V>(size) {

    var textResId: Int? = null

    var text: String? = null

    var inputType: BehaviorRelay<Int>? = null

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
        textRelay
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { view?.text = it }
            ?.untilLifecycleDestroy()
        inputType
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { view?.inputType = it }
            ?.untilLifecycleDestroy()
    }
}

@CheckResult
fun TextView.textChanges(): InitialValueObservable<CharSequence> {
    return TextViewTextChangesObservable(this)
}

private class TextViewTextChangesObservable(
    private val view: TextView
) : InitialValueObservable<CharSequence>() {

    override fun subscribeListener(observer: Observer<in CharSequence>) {
        val listener = Listener(view, observer)
        observer.onSubscribe(listener)
        view.addTextChangedListener(listener)
    }

    override val initialValue get() = view.text

    private class Listener(
        private val view: TextView,
        private val observer: Observer<in CharSequence>
    ) : MainThreadDisposable(), TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (!isDisposed) {
                observer.onNext(s)
            }
        }

        override fun afterTextChanged(s: Editable) {
        }

        override fun onDispose() {
            view.removeTextChangedListener(this)
        }
    }
}

abstract class InitialValueObservable<T> : Observable<T>() {
    protected abstract val initialValue: T

    override fun subscribeActual(observer: Observer<in T>) {
        subscribeListener(observer)
        observer.onNext(initialValue)
    }

    protected abstract fun subscribeListener(observer: Observer<in T>)

    fun skipInitialValue(): Observable<T> = Skipped()

    private inner class Skipped : Observable<T>() {
        override fun subscribeActual(observer: Observer<in T>) {
            subscribeListener(observer)
        }
    }
}