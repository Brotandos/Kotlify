package com.brotandos.kotlify.container

import android.content.Context
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.container.root.VRoot
import com.brotandos.kotlify.container.root.VRootOwner

class VVertical(size: LayoutSize) : VContainer<LinearLayout, LinearLayout.LayoutParams>(size) {

    override fun createView(context: Context): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    override fun getChildLayoutParams(width: Int, height: Int): LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(width, height)
}

inline fun VRootOwner.vVerticalRoot(
        activity: ComponentActivity,
        init: VVertical.() -> Unit
): VRoot<VVertical> {
    val vContainer = VVertical(Air)
    val vNewRoot = VRoot(vContainer)
    vContainer.init()
    val view = vContainer.buildWidget(activity, KotlifyContext(), KotlifyInternals.rootPath)
    activity.setContentView(view)
    vNewRoot.disposeOnViewDestroyed(activity)
    this.vRoot?.clearObservers(activity)
    this.vRoot = vNewRoot
    return vNewRoot
}