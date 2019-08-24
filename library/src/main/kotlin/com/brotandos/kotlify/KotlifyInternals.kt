package com.brotandos.kotlify

import android.content.Context
import android.util.AttributeSet
import android.view.View

object KotlifyInternals {

    /**
     * Thanks to anko library
     * */
    @JvmStatic
    fun <T : View> initiateView(context: Context, viewClass: Class<T>): T {
        fun getConstructor1() = viewClass.getConstructor(Context::class.java)
        fun getConstructor2() = viewClass.getConstructor(Context::class.java, AttributeSet::class.java)

        return try {
            getConstructor1().newInstance(context)
        } catch (e: NoSuchMethodException) {
            try {
                getConstructor2().newInstance(context, null)
            } catch (e: NoSuchMethodException) {
                // TODO custom exception
                throw RuntimeException("Can't initiate View of class ${viewClass.name}: can't find proper constructor")
            }
        }

    }
}