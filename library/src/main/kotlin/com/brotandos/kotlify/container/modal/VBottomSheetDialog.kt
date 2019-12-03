package com.brotandos.kotlify.container.modal

import android.content.Context
import com.brotandos.kotlify.common.KotlifyContext
import com.google.android.material.bottomsheet.BottomSheetDialog

class VBottomSheetDialog : ModalElement<BottomSheetDialog>() {

    override fun build(context: Context, kotlifyContext: KotlifyContext): BottomSheetDialog {
        val dialog = BottomSheetDialog(context)
        titleResId?.let(dialog::setTitle) ?: title?.let(dialog::setTitle)
        cancellable?.let(dialog::setCancelable)
        initSubscriptions(dialog)
        vContent?.buildWidget(
                context,
                kotlifyContext,
                listOf() // TODO implement path for dialogs
        )?.let(dialog::setContentView)
        return dialog
    }
}