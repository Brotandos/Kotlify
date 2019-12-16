package com.brotandos.kotlify.container

import android.content.Context
import androidx.cardview.widget.CardView
import com.brotandos.kotlify.common.LayoutSize

class VCard(size: LayoutSize) : VContainer<CardView>(size) {

    var radius: Float? = null

    override fun createView(context: Context): CardView {
        val view = CardView(context)
        radius?.let(view::setRadius)
        return view
    }
}