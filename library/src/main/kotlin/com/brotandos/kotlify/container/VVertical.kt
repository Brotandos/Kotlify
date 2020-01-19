package com.brotandos.kotlify.container

import android.content.Context
import android.widget.LinearLayout
import com.brotandos.kotlify.common.LayoutSize

class VVertical(size: LayoutSize) : VContainer<LinearLayout>(size) {

    override fun createView(context: Context): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }
}