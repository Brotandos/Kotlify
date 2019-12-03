package com.brotandos.kotlify.container.modal

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.brotandos.kotlify.common.KotlifyContext

class VDialog : ModalElement<AlertDialog>() {

    var message: String? = null

    override fun build(context: Context, kotlifyContext: KotlifyContext): AlertDialog {
        val builder = AlertDialog.Builder(context)
        titleResId?.let(builder::setTitle) ?: title?.let(builder::setTitle)
        message?.let(builder::setMessage)
        cancellable?.let(builder::setCancelable)
        vContent?.let {
            val view = it.buildWidget(
                    context,
                    kotlifyContext,
                    listOf() // TODO implement path for modal
            )
            builder.setView(view)
        }
        val dialog = builder.create()
        initSubscriptions(dialog)
        return dialog
    }
}