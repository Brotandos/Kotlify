package com.brotandos.kotlify.container

import android.widget.RelativeLayout
import com.brotandos.kotlify.common.LayoutSize

abstract class VRelative<V : RelativeLayout, LP : RelativeLayout.LayoutParams>(
        size: LayoutSize
) : VContainer<V, LP>(size)