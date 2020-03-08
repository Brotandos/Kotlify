package com.brotandos.kotlify.common

object Constants {

    val DRAWABLE_ALL_STATES = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed)
    )
}