package com.brotandos.kotlify.element

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.LayoutSize
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class VImagePager<V : ViewPager>(
    private var imageHolder: ImageHolder,
    size: LayoutSize
) : WidgetElement<V>(size) {

    var imageHolderRelay = BehaviorRelay.create<ImageHolder>()

    var pageChangeListener = BehaviorRelay.create<ViewPager.OnPageChangeListener>()

    private var images: List<ImageView>? = null
    private var adapter: PagerAdapter? = null

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val view = super.build(context, kotlifyContext)
        getAdapter().let {
            view.adapter = it
        }
        return view
    }

    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)

        imageHolderRelay
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                imageHolder = it
                getAdapter().let { adapter ->
                    view?.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }.untilLifecycleDestroy()

        pageChangeListener
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                view?.addOnPageChangeListener(it)
            }.untilLifecycleDestroy()
    }

    private fun getAdapter(): PagerAdapter = object : PagerAdapter() {
        override fun getCount(): Int {
            return imageHolder.list.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView = imageHolder.list.get(position)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            (container as ViewPager).addView(imageView, 0)
            return imageView
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object` as ImageView
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            `object`: Any
        ) {
            container.removeView(`object` as ImageView)
        }
    }

    class ImageHolder(images: List<ImageView>) {

        constructor(vararg images: ImageView) : this(images.toList())

        private val mutableList: MutableList<ImageView> = images.toMutableList()
        val list: MutableList<ImageView> = mutableList

        fun addItem(image: ImageView) {
            list.add(image)
        }

        fun addItem(index: Int, image: ImageView) {
            list.add(index, image)
        }

        fun removeItem(index: Int) {
            list.removeAt(index)
        }

        fun removeItems(startIndex: Int, itemsCount: Int) {
            list.subList(startIndex, startIndex + itemsCount).clear()
        }
    }
}