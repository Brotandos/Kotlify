package com.brotandos.kotlify.element

import android.widget.EditText
import com.brotandos.kotlify.common.LayoutSize

abstract class VEdit<V : EditText>(size: LayoutSize) : VLabel<V>(size)