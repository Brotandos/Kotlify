package com.brotandos.kotlify.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace

class KotlifyContext(
    val router: Router? = null
)

class Router(val containerId: Int) {

    var fragmentManager: FragmentManager? = null

    inline fun <reified T : Fragment> navigateTo(args: Bundle? = null) {
        fragmentManager?.commit {
            replace<T>(containerId, args = args)
        }
    }
}