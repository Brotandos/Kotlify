package com.brotandos.kotlify.container

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.setPadding
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

abstract class VToolbar<V : Toolbar, LP : Toolbar.LayoutParams>(
        size: LayoutSize
) : VContainer<V, LP>(size) {

    private val menuItems = mutableListOf<VMenu>()

    /**
     * Need for [titleResId]
     * */
    private var resources: (() -> Resources)? = null

    private var navigationPair: Pair<Int, () -> Unit>? = null

    var title: BehaviorRelay<String>? = null

    var titleResId: BehaviorRelay<Int>? = null

    var elevation: CustomLength? = null

    var startContentInset: CustomLength? = null

    var endContentInset: CustomLength? = null

    var contentInsets: Pair<CustomLength?, CustomLength?>
        get() = startContentInset to endContentInset
        set(value) {
            startContentInset = value.first
            endContentInset = value.second
        }

    var typeface: Typeface? = null

    @ColorRes
    var backgroundRes: Int? = null

    var titleTextColor: Int? = null

    @ColorInt
    var iconColor: Int? = null

    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)
        titleResId
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { view?.setTitle(it) }
                ?.untilLifecycleDestroy()
                ?: title
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe { view?.title = it }
                        ?.untilLifecycleDestroy()
    }

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val toolbar = super.build(context, kotlifyContext)
        backgroundRes?.let(toolbar::setBackgroundResource)
        resources = { context.resources }
        navigationPair?.let { (iconResId, onClick) ->
            toolbar.setNavigationIcon(iconResId)
            toolbar.setNavigationOnClickListener { onClick() }
        }
        elevation?.let {
            toolbar.elevation = it.getValue(context.density).toFloat()
        }
        toolbar.contentInsetStartWithNavigation = 0
        titleTextColor?.let(toolbar::setTitleTextColor)
        val menu = toolbar.menu
        menu.clear()
        menuItems.forEach { it.inflate(menu, context) }
        typeface?.let { typeface ->
            toolbar.children
                    .filterIsInstance<TextView>()
                    .find { it.text == toolbar.title }
                    ?.setTypeface(typeface)
        }
        return toolbar
    }

    fun setTitle(titleRes: Int) {
        titleResId?.accept(titleRes) ?: let {
            titleResId = BehaviorRelay.createDefault(titleRes)
        }
    }

    fun vAction(
            title: String,
            iconResId: Int? = null,
            isLoading: BehaviorRelay<Boolean>? = null,
            init: (VMenu.() -> Unit)? = null
    ) {
        val vMenu =
                VMenu(title) { untilLifecycleDestroy() }
        vMenu.iconResId = iconResId
        vMenu.isLoading = isLoading
        init?.invoke(vMenu)
        menuItems += vMenu
    }

    fun vNavigation(iconResId: Int, onNavigationClick: () -> Unit) {
        navigationPair = iconResId to onNavigationClick
    }

    inner class VMenu(private val title: String, private val addToComposite: Disposable.() -> Unit) {

        var isLoading: BehaviorRelay<Boolean>? = null

        var iconResId: Int? = null

        private var onClick: (() -> Unit)? = null
        fun onClick(f: () -> Unit) {
            onClick = f
        }

        fun inflate(menu: Menu, context: Context) {
            val menuItem = menu.add(title)

            val imageView: ImageView? = AppCompatImageView(context).apply {
                iconResId?.let(::setImageResource)
                iconColor?.let(::setColorFilter)
                val typedValue = TypedValue()
                context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackgroundBorderless,
                        typedValue,
                        true
                )
                setBackgroundResource(typedValue.resourceId)
                setPadding(2 * resources.displayMetrics.density.toInt())

                layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
                onClick?.let { onClick -> setOnClickListener { onClick() } }
            }

            val progressBar: ProgressBar? = ProgressBar(context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
            }

            isLoading
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe {
                        imageView?.visibility = if (it) View.GONE else View.VISIBLE
                        progressBar?.visibility = if (it) View.VISIBLE else View.INVISIBLE
                    }
                    ?.addToComposite()

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

    /**
     * TODO VBadge
     * */
}