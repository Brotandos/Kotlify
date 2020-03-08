package com.brotandos.kotlify.common

import android.content.res.ColorStateList
import androidx.annotation.ColorInt

fun colorStates(
        @ColorInt defaultColor: Int,
        vararg state2ColorArray: Pair<Int, Int>
): ColorStateList {
    val states = state2ColorArray.map { intArrayOf(it.first) } + intArrayOf()
    val colors = state2ColorArray.map { it.second } + defaultColor
    return ColorStateList(states.toTypedArray(), colors.toIntArray())
}