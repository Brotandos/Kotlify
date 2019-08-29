package com.brotandos.kotlify

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.setPadding
import com.jakewharton.rxrelay2.BehaviorRelay

class VToolbar : VContainer<Toolbar>() {

    val title = BehaviorRelay.createDefault("")

    val titleResId = BehaviorRelay.createDefault(0)

    val menuItems = mutableListOf<VMenu>()

    /**
     * Need for [titleResId]
     * */
    var resources: (() -> Resources)? = null

    override fun createView(context: Context): Toolbar = Toolbar(context)

    override fun initSubscriptions(view: Toolbar) {
        super.initSubscriptions(view)
        title.subscribe(view::setTitle)
    }

    override fun build(context: Context): Toolbar {
        val view = super.build(context)
        resources = { context.resources }
        val menu = view.menu
        menu.clear()
        menuItems.forEach { it.inflate(menu, context) }
        return view
    }

    fun vAction(
        title: String,
        iconResId: Int? = null,
        isLoading: BehaviorRelay<Boolean>? = null,
        init: VMenu.() -> Unit
    ) {
        val vMenu = VMenu(title)
        vMenu.iconResId = iconResId
        vMenu.isLoading = isLoading
        vMenu.init()
        menuItems += vMenu
    }

    class VMenu(val title: String) {

        var isLoading: BehaviorRelay<Boolean>? = null

        var iconResId: Int? = null

        var onClick: (() -> Unit)? = null

        var vBadge: VBadge? = null

        fun inflate(menu: Menu, context: Context) {
            val menuItem = menu.add(title)

            val imageView = ImageView(context).apply {
                iconResId?.let(::setImageResource)
                val typedValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
                setBackgroundResource(typedValue.resourceId)
                setPadding(2 * resources.displayMetrics.density.toInt())

                layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
                onClick?.let { onClick -> setOnClickListener { onClick() }}
            }

            val progressBar = ProgressBar(context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
            }

            isLoading?.subscribe {
                imageView.visibility = if (it) View.GONE else View.VISIBLE
                progressBar.visibility = if (it) View.VISIBLE else View.INVISIBLE
            }

            menuItem.actionView = RelativeLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply { gravity = Gravity.CENTER }
                addView(imageView)
                addView(progressBar)
            }

            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
    }

    class VBadge()
}