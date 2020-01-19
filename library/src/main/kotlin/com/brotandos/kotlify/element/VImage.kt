package com.brotandos.kotlify.element

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.brotandos.kotlify.common.LayoutSize
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers

class VImage(size: LayoutSize) : WidgetElement<ImageView>(size) {

    private var glideInit: (RequestBuilder<Drawable>.() -> RequestBuilder<Drawable>)? = null

    var imageResId: BehaviorRelay<Int>? = null

    var imageUrlRelay: BehaviorRelay<String>? = null

    // TODO recycle after each and on dispose
    var imageBitmap: PublishRelay<Bitmap>? = null

    override fun initSubscriptions(view: ImageView?) {
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

    override fun createView(context: Context): ImageView = ImageView(context)

    fun initGlide(init: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable>) {
        glideInit = init
    }
}