package com.brotandos.kotlify.container

import android.widget.FrameLayout
import com.brotandos.kotlify.annotation.GenerateItself
import com.brotandos.kotlify.common.LayoutSize

@GenerateItself
abstract class VFrame<V : FrameLayout, LP : FrameLayout.LayoutParams>(
        size: LayoutSize
) : VContainer<V, LP>(size)
