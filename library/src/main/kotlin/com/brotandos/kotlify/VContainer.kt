package com.brotandos.kotlify

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup

abstract class VContainer<V : ViewGroup> : VElement<V>() {

    val children = mutableListOf<VElement<*>>()

    override fun build(context: Context): V {
        val view = super.build(context)
        children.forEach {
            val childView = it.build(context)
            view.addView(childView)
        }
        return view
    }

    inline fun <reified V : View> vCustom(init: VElement<V>.() -> Unit) {
        val vElement = object : VElement<V>() {
            override fun createView(context: Context): V = KotlifyInternals.initiateView(context, V::class.java)
        }
        vElement.init()
        children += vElement
    }

    fun vToolbar(init: VToolbar.() -> Unit) {
        val vToolbar = VToolbar()
        vToolbar.init()
        children += vToolbar
    }
}

inline fun <reified V : ViewGroup> Activity.vContainer(init: VContainer<V>.() -> Unit) {
    val builder = object : VContainer<V>() {
        override fun createView(context: Context): V = KotlifyInternals.initiateView(context, V::class.java)

    }
    builder.init()
    setContentView(builder.build(this))
}