package com.brotandos.kotlify.container

import android.content.Context
import android.widget.LinearLayout
import com.brotandos.kotlify.common.LayoutSize

class VLinear(
        size: LayoutSize
) : VContainer<LinearLayout>(size) {

    override fun createView(context: Context): LinearLayout = LinearLayout(context)
}