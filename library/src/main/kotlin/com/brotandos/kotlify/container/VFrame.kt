package com.brotandos.kotlify.container

import android.content.Context
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.container.root.VRoot
import com.brotandos.kotlify.container.root.VRootOwner

typealias VFrameActual = VFrame<FrameLayout, FrameLayout.LayoutParams>

abstract class VFrame<V : FrameLayout, LP : FrameLayout.LayoutParams>(
        size: LayoutSize
) : VContainer<V, LP>(size)

inline fun VRootOwner.vFrameRoot(
        activity: ComponentActivity,
        init: VFrameActual.() -> Unit
): VRoot<VFrameActual> {
    val vContainer = object : VFrameActual(Air) {
        override fun createView(context: Context): FrameLayout = FrameLayout(context)

        override fun getChildLayoutParams(width: Int, height: Int): FrameLayout.LayoutParams =
                FrameLayout.LayoutParams(width, height)
    }
    val vNewRoot = VRoot<VFrameActual>(vContainer)
    vContainer.init()
    val view = vContainer.buildWidget(activity, KotlifyContext(), KotlifyInternals.rootPath)
    activity.setContentView(view)
    vNewRoot.disposeOnViewDestroyed(activity)
    vRoot?.clearObservers(activity)
    vRoot = vNewRoot
    return vNewRoot
}