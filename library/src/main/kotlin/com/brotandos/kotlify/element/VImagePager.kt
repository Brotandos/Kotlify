package com.brotandos.kotlify.element

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.LayoutSize

abstract class VImagePager<V : ViewPager>(
    private var imageHolder: ImageHolder,
    size: LayoutSize
) : WidgetElement<V>(size) {

    lateinit var images: List<ImageView>

    private var adapter: PagerAdapter? = null
    private var onPageChangeListener: ViewPager.OnPageChangeListener? = null

    override fun build(context: Context, kotlifyContext: KotlifyContext): V {
        val view = super.build(context, kotlifyContext)
        getAdapter().let {
            view.adapter = it
        }
        return view
    }

    private fun getListener(): ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                positionOffsetPixels: Int
            ) = Unit

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) = Unit

        }

    private fun getAdapter(): PagerAdapter = object : PagerAdapter() {
        override fun getCount(): Int {
            return imageHolder.list.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView = imageHolder.list[position]
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
        val list: List<ImageView> = mutableList

        fun addItem(image: ImageView) {
            mutableList.add(image)
        }

        fun addItem(imageUrl: String) {
            TODO()
        }

        fun removeItem(index: Int) {
            mutableList.removeAt(index)
        }

        fun removeItems(startIndex: Int, itemsCount: Int) {
            mutableList.subList(startIndex, startIndex + itemsCount).clear()
        }
    }
}