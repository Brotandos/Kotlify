package com.brotandos.kotlify.element

import android.content.Context
import com.brotandos.kotlify.common.LayoutSize
import com.google.android.material.button.MaterialButton
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

class VButton(size: LayoutSize) : WidgetElement<MaterialButton>(size) {

    var isChecked: BehaviorRelay<Boolean>? = null

    override fun createView(context: Context): MaterialButton {
        val view = MaterialButton(context)
        view.addOnCheckedChangeListener { _, isChecked ->
            this.isChecked?.accept(isChecked)
        }
        return view
    }

    override fun initSubscriptions(view: MaterialButton?) {
        super.initSubscriptions(view)
        isChecked
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    if (view?.isChecked == it) return@subscribe
                    view?.isChecked = it
                }
                ?.untilLifecycleDestroy()
    }
}