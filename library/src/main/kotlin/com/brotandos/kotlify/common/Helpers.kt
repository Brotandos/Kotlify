package com.brotandos.kotlify.common

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

const val NO_FLAGS = -1

open class KotlifyException(message: String = "") : RuntimeException(message)

object NoGetterException : KotlifyException("There's no getter for this property")

@MainThread
inline fun <reified VM : ViewModel> FragmentActivity.viewModels(): Lazy<VM> =
    object : Lazy<VM> {
        private var cached: VM? = null

        override val value: VM
            get() = cached ?: ViewModelProviders
                    .of(this@viewModels)
                    .get(VM::class.java)
                    .also { cached = it }

        override fun isInitialized(): Boolean = cached != null
    }

inline fun <reified T : Activity> Activity.startActivity(flags: Int = NO_FLAGS) {
    val intent = Intent(this, T::class.java)
    if (flags != NO_FLAGS) intent.addFlags(flags)
    startActivity(intent)
}

inline fun <reified T : Activity> Activity.startActivity(
        vararg parcelables: Pair<String, Parcelable>,
        flags: Int = NO_FLAGS
) {
    val intent = Intent(this, T::class.java)
    if (flags != NO_FLAGS) intent.addFlags(flags)
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

inline fun <reified T : Activity> T.restart() =
        startActivity<T>(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

fun FragmentManager.commit(block: FragmentTransaction.() -> FragmentTransaction): Int =
        beginTransaction().block().commit()

fun FragmentManager.commitNow(block: FragmentTransaction.() -> FragmentTransaction) =
        beginTransaction().block().commitNow()

fun FragmentManager.commitAllowingStateLoss(
        block: FragmentTransaction.() -> FragmentTransaction
): Int = beginTransaction().block().commitAllowingStateLoss()

fun FragmentManager.commitNowAllowingStateLoss(
        block: FragmentTransaction.() -> FragmentTransaction
) = beginTransaction().block().commitNowAllowingStateLoss()