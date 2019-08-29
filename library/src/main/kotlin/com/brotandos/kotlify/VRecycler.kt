package com.brotandos.kotlify

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxrelay2.BehaviorRelay

class VRecycler<E>(val items: BehaviorRelay<List<E>>) : VElement<RecyclerView>() {

    var vItem: ((E) -> VElement<*>)? = null

    private val adapter = object : RecyclerView.Adapter<KotlifyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KotlifyViewHolder {
            val vElement = vItem?.invoke(items.value[viewType])
                ?: throw RuntimeException("vItem is not set")
            val view = vElement.build(parent.context)
            return KotlifyViewHolder(view)
        }

        override fun onBindViewHolder(holder: KotlifyViewHolder, position: Int) = Unit

        override fun onViewRecycled(holder: KotlifyViewHolder) {
            super.onViewRecycled(holder)
            holder.dispose()
        }

        override fun getItemCount(): Int = items.value.size

        override fun getItemViewType(position: Int) = position
    }

    override fun createView(context: Context): RecyclerView = RecyclerView(context).apply {
        this.adapter = this@VRecycler.adapter
    }

    override fun initSubscriptions(view: RecyclerView) {
        super.initSubscriptions(view)
        items.subscribe {
            adapter.notifyDataSetChanged()
        }
    }

    fun vItem(itemView: VContainer<*>.(E) -> VElement<*>) {
        val vContainer = object : VContainer<FrameLayout>() {
            override fun createView(context: Context): FrameLayout = FrameLayout(context)
        }
        vItem = { vContainer.itemView(it) }
    }

    class KotlifyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // TODO clear subscriptions
        fun dispose() = Unit
    }
}