package com.brotandos.kotlify

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog

class VBottomSheetDialog : ModalElement<BottomSheetDialog>() {

    override fun build(context: Context, kotlifyContext: KotlifyContext): BottomSheetDialog {
        val dialog = BottomSheetDialog(context)
        titleResId?.let(dialog::setTitle) ?: title?.let(dialog::setTitle)
        cancellable?.let(dialog::setCancelable)
        initSubscriptions(dialog)
        vContent?.let {
            val view = it.build(context, kotlifyContext)
            dialog.setContentView(view)
        }
        return dialog
    }
}