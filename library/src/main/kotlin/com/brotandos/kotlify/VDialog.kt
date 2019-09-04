package com.brotandos.kotlify

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class VDialog : VEntity<AlertDialog>() {

    // Need to keep strong reference to dialog for show dialog after hide
    // private var dialog: AlertDialog? = null

    var vShow: BehaviorRelay<Boolean>? = null

    var titleResId: Int? = null

    var title: String? = null

    var message: String? = null

    var cancellable: Boolean? = null

    private fun initSubscriptions(dialog: AlertDialog) {
        vShow
            ?.subscribe { if (it) dialog.show() else dialog.hide() }
            ?.addToComposite()
    }

    override fun build(context: Context, kotlifyContext: KotlifyContext): AlertDialog {
        val builder = AlertDialog.Builder(context)
        titleResId?.let(builder::setTitle) ?: title?.let(builder::setTitle)
        message?.let(builder::setMessage)
        cancellable?.let(builder::setCancelable)
        builder.setOnCancelListener { vShow?.accept(false) }
        val dialog = builder.create()
        initSubscriptions(dialog)
        return dialog
    }
}