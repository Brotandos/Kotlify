package com.brotandos.kotlify.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class KotlifyContext(
    val router: Router? = null
)

class Router(val containerId: Int) {

    var fragmentManager: FragmentManager? = null

    fun navigateTo(fragment: Fragment) {
        fragmentManager?.commit { replace(containerId, fragment) }
    }
}