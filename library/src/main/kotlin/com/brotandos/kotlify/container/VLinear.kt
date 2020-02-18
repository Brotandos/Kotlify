package com.brotandos.kotlify.container

import android.widget.LinearLayout
import com.brotandos.kotlify.annotation.WidgetContainer
import com.brotandos.kotlify.common.LayoutSize

@WidgetContainer
abstract class VLinear<V : LinearLayout, LP : LinearLayout.LayoutParams>(
        size: LayoutSize
) : VContainer<V, LP>(size)