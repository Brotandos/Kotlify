package com.brotandos.kotlify.container

import android.content.Context
import android.widget.LinearLayout
import com.brotandos.kotlify.common.LayoutSize

class VVertical(size: LayoutSize) : VLinear<LinearLayout, LinearLayout.LayoutParams>(size) {

    override fun createView(context: Context): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    override fun getChildLayoutParams(width: Int, height: Int): LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(width, height)
}