package com.brotandos.kotlify.common

import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

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