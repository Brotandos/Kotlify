package com.brotandos.kotlify.container

import android.content.Context
import android.widget.FrameLayout
import com.brotandos.kotlify.common.LayoutSize

class VFrame(size: LayoutSize) : VContainer<FrameLayout, FrameLayout.LayoutParams>(size) {

    override fun createView(context: Context): FrameLayout = FrameLayout(context)

    override fun getChildLayoutParams(width: Int, height: Int): FrameLayout.LayoutParams =
            FrameLayout.LayoutParams(width, height)
}