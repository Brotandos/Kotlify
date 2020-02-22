package com.brotandos.kotlify.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.brotandos.kotlify.common.LayoutSize
import com.google.android.material.button.MaterialButton
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class VButton<V : MaterialButton>(size: LayoutSize) : VLabel<V>(size) {

    private var progressDrawable: CircularProgressDrawable? = null

    private var textBeforeLoading = ""

    private var loadingString: SpannableString? = null

    var isChecked: BehaviorRelay<Boolean>? = null

    var isLoading: BehaviorRelay<Boolean>? = null

    override fun initStyles(view: V?) {
        super.initStyles(view)
        view ?: return
        view.addOnCheckedChangeListener { _, isChecked ->
            this.isChecked?.accept(isChecked)
        }
    }

    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)
        isChecked
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    if (view?.isChecked == it) return@subscribe
                    view?.isChecked = it
                }
                ?.untilLifecycleDestroy()

        isLoading?.let { isLoadingRelay ->
            view ?: return@let
            val progressDrawable = CircularProgressDrawable(view.context).apply {
                setStyle(CircularProgressDrawable.LARGE)
                setColorSchemeColors(Color.WHITE) // TODO customize color

                val size = (centerRadius + strokeWidth).toInt() * 2
                setBounds(0, 0, size, size)
            }
            val drawableSpan = DrawableSpan(progressDrawable, paddingStart = 20)
            val spannableString = SpannableString("Loading ").apply {
                setSpan(drawableSpan, length - 1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            progressDrawable.start()
            val callback = object : Drawable.Callback {
                override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) = Unit
                override fun unscheduleDrawable(who: Drawable, what: Runnable) = Unit
                override fun invalidateDrawable(who: Drawable) = view.invalidate()
            }
            progressDrawable.callback = callback
            loadingString = spannableString

            this.progressDrawable = progressDrawable

            isLoadingRelay.observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it) {
                            textBeforeLoading = view.text.toString()
                            view.isEnabled = false // TODO use isEnabledRelay
                            view.text = loadingString
                            this.progressDrawable?.start()
                        } else if (view.text == loadingString) {
                            this.progressDrawable?.stop()
                            view.isEnabled = true // TODO use isEnabledRelay
                            view.text = textBeforeLoading
                        }
                    }
                    .untilLifecycleDestroy()
        }
    }
}

class DrawableSpan(
        drawable: Drawable,
        var paddingStart: Int = 0,
        var paddingEnd: Int = 0,
        private val useTextAlpha: Boolean = false
) : ImageSpan(drawable) {

    override fun getSize(
            paint: Paint,
            text: CharSequence?,
            start: Int,
            end: Int,
            fontMetricsInt: Paint.FontMetricsInt?
    ): Int {
        val drawable = drawable
        val rect = drawable.bounds
        val size = rect.width() + paddingStart + paddingEnd
        fontMetricsInt ?: return size

        val fontMetrics = paint.fontMetricsInt
        val lineHeight = fontMetrics.bottom - fontMetrics.top
        val drHeight = lineHeight.coerceAtLeast(rect.bottom - rect.top)
        val centerY = fontMetrics.top + lineHeight / 2
        fontMetricsInt.apply {
            ascent = centerY - drHeight / 2
            descent = centerY + drHeight / 2
            top = ascent
            bottom = descent
        }
        return size
    }

    override fun draw(
            canvas: Canvas,
            text: CharSequence,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
    ) {
        val drawable = drawable
        canvas.save()
        val fontMetrics = paint.fontMetricsInt
        val lineHeight = fontMetrics.descent - fontMetrics.ascent
        val centerY = y + fontMetrics.descent - lineHeight / 2
        val transY = centerY - drawable.bounds.height() / 2
        val dx = if (paddingStart == 0) x else x + paddingStart
        canvas.translate(dx, transY.toFloat())
        if (useTextAlpha) {
            val colorAlpha = Color.alpha(paint.color)
            drawable.alpha = colorAlpha
        }
        drawable.draw(canvas)
        canvas.restore()
    }
}