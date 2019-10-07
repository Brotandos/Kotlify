package com.brotandos.kotlify

import android.content.Context
import androidx.appcompat.app.AlertDialog

class VDialog : ModalElement<AlertDialog>() {

    var message: String? = null

    override fun build(context: Context, kotlifyContext: KotlifyContext): AlertDialog {
        val builder = AlertDialog.Builder(context)
        titleResId?.let(builder::setTitle) ?: title?.let(builder::setTitle)
        message?.let(builder::setMessage)
        cancellable?.let(builder::setCancelable)
        vContent?.let {
            val view = it.build(context, kotlifyContext)
            builder.setView(view)
        }
        val dialog = builder.create()
        initSubscriptions(dialog)
        return dialog
    }
}