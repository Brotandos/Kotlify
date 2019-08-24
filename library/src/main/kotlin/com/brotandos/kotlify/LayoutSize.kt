package com.brotandos.kotlify

sealed class LayoutSize
object Submissive : LayoutSize()
object Row : LayoutSize()
object Column : LayoutSize()
object Dominant : LayoutSize()
data class Custom(val width: Int, val height: Int) : LayoutSize()