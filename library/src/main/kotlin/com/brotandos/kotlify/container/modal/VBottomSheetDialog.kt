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
        vContent?.build(context, kotlifyContext)?.let(dialog::setContentView)
        return dialog
    }
}