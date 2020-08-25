package com.brotandos.kotlify.element

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.PublishRelay

abstract class VSimpleSpinner<T : VSimpleSpinner.Option, V : Spinner>(size: LayoutSize) : WidgetElement<V>(size) {

    private var options: List<T>? = null

    var selectedOption: PublishRelay<T>? = null

    var onNothingSelected: (() -> Unit)? = null

    override fun initStyles(view: V?) {
        super.initStyles(view)
        view ?: return
        val options = this.options ?: throw IllegalStateException("Options must be set")
        view.adapter = object : ArrayAdapter<String>(
            view.context,
            android.R.layout.simple_spinner_item,
            0,
            options.map(Option::getName)
        ) {
            init { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }

        view.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedOption?.accept(options[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                onNothingSelected?.invoke()
            }

        }
    }

    internal fun setOptions(options: List<T>) {
        this.options = options
    }

    interface Option {
        fun getName() = this::class.simpleName ?: throw IllegalStateException("Name shouldn't be empty")
    }
}