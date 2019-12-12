package com.brotandos.kotlify.container.modal

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.brotandos.kotlify.common.KotlifyContext
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

class VDialog : ModalElement<AlertDialog>() {

    var message: String? = null

    var messageRelay: BehaviorRelay<CharSequence>? = null

    override fun build(context: Context, kotlifyContext: KotlifyContext): AlertDialog {
        val dialog = AlertDialog.Builder(context)
            .create()
        titleRelay
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { dialog.setTitle(it) }
            ?.untilLifecycleDestroy()
            ?: titleResId?.let(dialog::setTitle)
            ?: title?.let(dialog::setTitle)
        messageRelay
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(dialog::setMessage)
            ?.untilLifecycleDestroy()
            ?: message?.let(dialog::setMessage)
        cancellable?.let(dialog::setCancelable)
        vContent?.let {
            val view = it.buildWidget(
                    context,
                    kotlifyContext,
                    listOf() // TODO implement path for modal
            )
            dialog.setView(view)
        }
        initSubscriptions(dialog)
        return dialog
    }
}