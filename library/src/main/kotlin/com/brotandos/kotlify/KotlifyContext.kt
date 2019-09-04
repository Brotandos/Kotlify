package com.brotandos.kotlify

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class KotlifyContext(
    val router: Router? = null
)

class Router(val containerId: Int) {

    var fragmentManager: FragmentManager? = null

    fun navigateTo(fragment: Fragment) {
        fragmentManager?.beginTransaction()?.apply {
            replace(containerId, fragment)
            commit()
        }
    }
}