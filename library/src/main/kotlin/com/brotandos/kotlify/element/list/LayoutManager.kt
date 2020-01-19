package com.brotandos.kotlify.element.list

sealed class LayoutManager {
    object Linear : LayoutManager()
    data class Grid(val spanCount: Int) : LayoutManager()
    data class Staggered(val spanCount: Int, val isVertical: Boolean) : LayoutManager()
}