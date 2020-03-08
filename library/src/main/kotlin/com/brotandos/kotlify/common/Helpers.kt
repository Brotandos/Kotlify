package com.brotandos.kotlify.common

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

const val NO_INTENT_FLAGS = -1

const val EMPTY = ""

open class KotlifyException(message: String = EMPTY) : RuntimeException(message)

object NoGetterException : KotlifyException("There's no getter for this property")

@MainThread
inline fun <reified VM : ViewModel> FragmentActivity.viewModels(): Lazy<VM> =
        object : Lazy<VM> {
            private var cached: VM? = null

            override val value: VM
                get() = cached ?: ViewModelProvider(this@viewModels)
                        .get(VM::class.java)
                        .also { cached = it }

            override fun isInitialized(): Boolean = cached != null
        }

fun getTint(@ColorInt color: Int) = ColorStateList(
        Constants.DRAWABLE_ALL_STATES,
        intArrayOf(color, color, color, color)
)

inline fun <reified T : Activity> Activity.startActivity(flags: Int = NO_INTENT_FLAGS) {
    val intent = Intent(this, T::class.java)
    if (flags != NO_INTENT_FLAGS) intent.addFlags(flags)
    startActivity(intent)
}

inline fun <reified T : Activity> Activity.startActivity(
        vararg parcelables: Pair<String, Parcelable>,
        flags: Int = NO_INTENT_FLAGS
) {
    val intent = Intent(this, T::class.java)
    if (flags != NO_INTENT_FLAGS) intent.addFlags(flags)
    parcelables.forEach {
        intent.putExtra(it.first, it.second)
    }
    startActivity(intent)
}

fun Activity.browse(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

inline fun <reified T : Activity> T.restart() {
    finish()
    startActivity<T>()
}

inline fun <reified T : Activity> T.clearAndRestart() {
    finish()
    startActivity<T>(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
}