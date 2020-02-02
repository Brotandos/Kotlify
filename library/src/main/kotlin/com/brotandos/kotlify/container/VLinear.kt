package com.brotandos.kotlify.container

import android.widget.LinearLayout
import com.brotandos.kotlify.common.LayoutSize

typealias VLinearActual = VLinear<LinearLayout, LinearLayout.LayoutParams>

abstract class VLinear<V : LinearLayout, LP : LinearLayout.LayoutParams>(
        size: LayoutSize
) : VContainer<V, LP>(size)