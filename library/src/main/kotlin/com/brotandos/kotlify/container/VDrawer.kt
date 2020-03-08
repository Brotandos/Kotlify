package com.brotandos.kotlify.container

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.drawerlayout.widget.DrawerLayout
import com.brotandos.kotlify.annotation.WidgetContainer
import com.brotandos.kotlify.common.CustomTypefaceSpan
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.KotlifyInternals
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.common.getTint
import com.brotandos.kotlify.element.WidgetElement
import com.brotandos.kotlify.exception.PathInTreeIgnoredException
import com.google.android.material.navigation.NavigationView
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

@WidgetContainer
abstract class VDrawer<V : DrawerLayout, LP : DrawerLayout.LayoutParams>(
        size: LayoutSize
) : VContainer<V, LP>(size) {

    private val menuItems = mutableListOf<VMenu>()

    // TODO make gravity customizable
    private val drawerSide = Gravity.START

    private var navViewInit: (NavigationView.() -> Unit)? = null
    fun initNavView(init: NavigationView.() -> Unit) {
        navViewInit = init
    }

    var typeface: Typeface? = null

    @ColorInt
    var menuTint: Int? = null

    var vHeader: WidgetElement<*>? = null

    var isOpen: BehaviorRelay<Boolean>? = null

    /**
     * TODO make [T] extend WidgetElement
     * */
    inline fun <reified T : VContainer<*, *>> vHeader(size: LayoutSize, init: T.() -> Unit): T =
            KotlifyInternals.initiateWidgetContainer(size, T::class.java)
                    .also(init)
                    .also { vHeader = it }

    @SuppressLint("WrongConstant")
    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)
        isOpen
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    if (it) {
                        view?.openDrawer(drawerSide)
                    } else {
                        view?.closeDrawer(drawerSide)
                    }
                }
                ?.untilLifecycleDestroy()
    }

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val drawerLayout = super.build(context, kotlifyContext)
        val navigationView = NavigationView(context)

        menuTint?.let {
            val menuTintColorList = getTint(it)
            navigationView.itemTextColor = menuTintColorList
            navigationView.itemIconTintList = menuTintColorList
        }

        navViewInit?.let { navigationView.it() }

        navigationView.layoutParams = DrawerLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ).apply { gravity = drawerSide }
        drawerLayout.addView(navigationView)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) = Unit

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit

            override fun onDrawerClosed(drawerView: View) { isOpen?.accept(false) }

            override fun onDrawerOpened(drawerView: View) { isOpen?.accept(true) }
        })

        vHeader?.let {
            val path = (pathInsideTree ?: throw PathInTreeIgnoredException()) +
                    KotlifyInternals.FIRST_CHILD_ADDRESS
            navigationView.addHeaderView(
                    it.buildWidget(context, kotlifyContext, path)
            )
        }

        val menu = navigationView.menu
        menu.clear()
        menuItems.forEach { it.inflate(menu) }

        return drawerLayout
    }

    override fun dispose() {
        super.dispose()
        vHeader?.dispose()
    }

    fun vMenuItem(
            title: String,
            iconResId: Int? = null,
            onClick: (() -> Unit)? = null
    ) = menuItems.add(VMenu(iconResId, onClick).apply { this.title = title })

    fun vMenuItem(
            titleResId: Int,
            iconResId: Int? = null,
            onClick: (() -> Unit)? = null
    ) = menuItems.add(VMenu(iconResId, onClick).apply { this.titleResId = titleResId })

    /**
     * class [VMenu] is private because menu requires title for creating
     * FIXME use either
     *
     * TODO:
     * - provide context to get string from resources in order to use text from xml with typeface set
     * */
    private inner class VMenu(
            private val iconResId: Int? = null,
            private val onClick: (() -> Unit)? = null
    ) {
        var titleResId: Int? = null

        var title: String? = null

        fun inflate(menu: Menu) {
            val menuItem = titleResId?.let(menu::add)
                    ?: typeface?.let {
                        val spannable = SpannableString(title)
                        spannable.setSpan(CustomTypefaceSpan("", it), 0, spannable.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        menu.add(spannable)
                    } ?: menu.add(title)
            iconResId?.let(menuItem::setIcon)
            onClick?.let { onClicked -> menuItem.setOnMenuItemClickListener { onClicked(); true } }
        }
    }
}