package com.brotandos.kotlify.element

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.brotandos.kotlify.common.LayoutSize
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers

class VImage(size: LayoutSize) : WidgetElement<ImageView>(size) {

    private var glideInit: (RequestBuilder<Drawable>.() -> RequestBuilder<Drawable>)? = null

    var imageResId: BehaviorRelay<Int>? = null

    var imageUrl: String? = null

    override fun initSubscriptions(view: ImageView?) {
        super.initSubscriptions(view)
        imageResId
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { view?.setImageResource(it) }
                ?.untilLifecycleDestroy()
    }

    override fun createView(context: Context): ImageView {
        val imageView = ImageView(context)
        imageUrl?.let { url ->
            Glide.with(context)
                    .load(url)
                    .also { glideInit?.invoke(it) }
                    .into(imageView)
        }
        return imageView
    }

    fun initGlide(init: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable>) {
        glideInit = init
    }
}