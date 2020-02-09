package com.brotandos.kotlify.element

import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class VEdit<V : EditText>(size: LayoutSize) : VLabel<V>(size) {

    var textChanged: PublishRelay<String>? = null

    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)
        textChanged?.let { textChanged ->
            view?.doAfterTextChanged { textChanged.accept(it.toString()) }
        }
    }
}