package com.brotandos.kotlify.container

import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import com.brotandos.kotlify.common.LayoutSize

abstract class VCard<V : CardView, LP : FrameLayout.LayoutParams>(
        size: LayoutSize
) : VFrame<V, LP>(size) {

    var radius: Float? = null

    override fun initStyles(view: V?) {
        super.initStyles(view)
        view ?: return
        radius?.let(view::setRadius)
    }
}