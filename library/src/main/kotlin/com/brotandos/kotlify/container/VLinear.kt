package com.brotandos.kotlify.container

import android.widget.LinearLayout
import com.brotandos.kotlify.annotation.GenerateItself
import com.brotandos.kotlify.common.LayoutSize

@GenerateItself
abstract class VLinear<V : LinearLayout, LP : LinearLayout.LayoutParams>(
        size: LayoutSize
) : VContainer<V, LP>(size)