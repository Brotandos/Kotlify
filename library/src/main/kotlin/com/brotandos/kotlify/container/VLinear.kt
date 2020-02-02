package com.brotandos.kotlify.container

import android.content.Context
import android.widget.LinearLayout
import com.brotandos.kotlify.common.LayoutSize

class VLinear(
        size: LayoutSize
) : VContainer<LinearLayout, LinearLayout.LayoutParams>(size) {

    override fun createView(context: Context): LinearLayout = LinearLayout(context)

    override fun getChildLayoutParams(width: Int, height: Int): LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(width, height)
}