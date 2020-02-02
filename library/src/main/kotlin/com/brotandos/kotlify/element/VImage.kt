package com.brotandos.kotlify.element

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.brotandos.kotlify.common.LayoutSize
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class VImage<V : ImageView>(size: LayoutSize) : WidgetElement<V>(size) {

    private var glideInit: (RequestBuilder<Drawable>.() -> RequestBuilder<Drawable>)? = null

    var imageResId: BehaviorRelay<Int>? = null

    var imageUrlRelay: BehaviorRelay<String>? = null

    // TODO recycle after each and on dispose
    var imageBitmap: PublishRelay<Bitmap>? = null

    override fun initSubscriptions(view: V?) {
        super.initSubscriptions(view)
        imageResId
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { view?.setImageResource(it) }
                ?.untilLifecycleDestroy()

        imageBitmap
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    view?.clearColorFilter()
                    view?.setImageBitmap(it)
                }
                ?.untilLifecycleDestroy()

        imageUrlRelay
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { url ->
                    view?.let { imageView ->
                        Glide.with(imageView.context)
                                .load(url)
                                .also { glideInit?.invoke(it) }
                                .into(imageView)
                    }
                }
                ?.untilLifecycleDestroy()
    }

    fun initGlide(init: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable>) {
        glideInit = init
    }
}