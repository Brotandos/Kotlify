package com.brotandos.kotlify.container

import android.content.Context
import android.widget.FrameLayout
import com.brotandos.kotlify.common.LayoutSize

class VFrame(size: LayoutSize) : VContainer<FrameLayout>(size) {

    override fun createView(context: Context): FrameLayout = FrameLayout(context)
}