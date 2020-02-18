package com.brotandos.kotlify.container

import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.brotandos.kotlify.annotation.WidgetContainer
import com.brotandos.kotlify.common.LayoutSize

@WidgetContainer
abstract class VFrame<V : FrameLayout, LP : FrameLayout.LayoutParams>(
        size: LayoutSize
) : VContainer<V, LP>(size)
