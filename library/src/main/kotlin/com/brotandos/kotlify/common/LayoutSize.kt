package com.brotandos.kotlify.common

import android.view.ViewGroup

sealed class LayoutSize(val width: LayoutLength, val height: LayoutLength) {
    open fun getValuePair(density: Float): Pair<Int, Int> = width.coefficient to height.coefficient
}

object Earth : LayoutSize(
    WrapContent,
    WrapContent
)

object Water : LayoutSize(
    MatchParent,
    WrapContent
)

object Fire : LayoutSize(
    WrapContent,
    MatchParent
)

object Air : LayoutSize(
    MatchParent,
    MatchParent
)

class CustomSize(width: LayoutLength, height: LayoutLength) : LayoutSize(width, height) {
    override fun getValuePair(density: Float): Pair<Int, Int> =
            width.getValue(density) to height.getValue(density)
}


sealed class LayoutLength(val coefficient: Int) {

    open fun getValue(density: Float): Int = coefficient
}

object WrapContent : LayoutLength(ViewGroup.LayoutParams.WRAP_CONTENT)

object MatchParent : LayoutLength(ViewGroup.LayoutParams.MATCH_PARENT)

class CustomLength(coefficient: Int) : LayoutLength(coefficient) {
    override fun getValue(density: Float): Int =
            if (coefficient == 0) 0 else (coefficient * density).toInt()

    infix fun x(height: LayoutLength) =
        CustomSize(this, height)
}