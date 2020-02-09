package com.brotandos.kotlify.element

import android.content.Context
import android.widget.ProgressBar
import com.brotandos.kotlify.common.LayoutSize

class VProgress(
        size: LayoutSize,
        private val isHorizontal: Boolean = false
) : WidgetElement<ProgressBar>(size) {

    override fun createView(context: Context): ProgressBar = if (isHorizontal) {
        ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
    } else {
        ProgressBar(context)
    }
}