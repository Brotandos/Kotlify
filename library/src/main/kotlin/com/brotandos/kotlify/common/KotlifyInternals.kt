package com.brotandos.kotlify.common

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.View
import androidx.core.content.edit
import com.brotandos.kotlify.container.VContainer
import com.brotandos.kotlify.element.WidgetElement
import com.jakewharton.rxrelay2.BehaviorRelay

object KotlifyInternals {

    const val NO_GETTER: String = "Property does not have a getter"

    const val IDS_CACHE_FILE_NAME = "KotlifyIdsCache"

    val rootPath = listOf(0)

    fun <T> noGetter(): T = throw NoGetterException

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

    @JvmStatic
    @Throws(RuntimeException::class)
    fun <T : VContainer<*, *>> initiateWidgetContainer(
            size: LayoutSize,
            clazz: Class<T>
    ): T = try {
        clazz.getConstructor(LayoutSize::class.java).newInstance(size)
    } catch (e: NoSuchMethodException) {
        throw RuntimeException("Can't initiate WidgetContainer of class ${clazz.name}: can't find proper constructor")
    }

    @JvmStatic
    @Throws(RuntimeException::class)
    fun <T : WidgetElement<*>> initiateWidget(clazz: Class<T>, size: LayoutSize = Earth): T = try {
        clazz.getConstructor(LayoutSize::class.java).newInstance(size)
    } catch (e: NoSuchMethodException) {
        throw RuntimeException("Can't initiate WidgetContainer of class ${clazz.name}: can't find proper constructor")
    }

    /**
     * Should be registered in [Application.onCreate]
     * */
    fun registerApp(app: Application) {
        AsyncTask.execute {
            val sharedPreferences = app.getSharedPreferences(
                    IDS_CACHE_FILE_NAME,
                    Context.MODE_PRIVATE
            )
            sharedPreferences.edit { clear() }
        }
    }
}

infix fun <T> BehaviorRelay<T>.accept(newValue: T) = accept(newValue)
fun <T>BehaviorRelay<T>.reAccept() = accept(value)
fun BehaviorRelay<Boolean>.toggleValue() = accept(!value)

operator fun BehaviorRelay<Int>.plusAssign(number: Int) = accept(value + number)
operator fun BehaviorRelay<Int>.minusAssign(number: Int) = accept(value - number)
operator fun BehaviorRelay<Int>.timesAssign(number: Int) = accept(value * number)
operator fun BehaviorRelay<Int>.divAssign(number: Int) = accept(value / number)