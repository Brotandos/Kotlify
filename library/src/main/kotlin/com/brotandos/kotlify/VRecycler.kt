package com.brotandos.kotlify

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.Disposable

class VRecycler<E>(
    size: LayoutSize,
    val itemsRelay: BehaviorRelay<List<E>>
) : WidgetElement<RecyclerView>(size) {

    private val items: List<E> get() = itemsRelay.value

    var vItem: ((E) -> WidgetElement<*>)? = null

    private var adapter: RecyclerView.Adapter<KotlifyViewHolder>? = null

    override fun createView(context: Context): RecyclerView = RecyclerView(context)

    override fun initSubscriptions(view: RecyclerView?) {
        super.initSubscriptions(view)
        itemsRelay
            .subscribe { adapter?.notifyDataSetChanged() }
            .addToComposite()
    }

    override fun build(context: Context, kotlifyContext: KotlifyContext): RecyclerView {
        val view = super.build(context, kotlifyContext)
        val adapter = getAdapter(kotlifyContext)
        view.adapter = adapter
        this.adapter = adapter
        return view
    }

    fun vItem(itemView: VContainer<*>.(E) -> WidgetElement<*>) {
        val vContainer = object : VContainer<FrameLayout>(Air) {
            override fun createView(context: Context): FrameLayout = FrameLayout(context)
        }
        vItem = { vContainer.itemView(it) }
    }

    private fun getAdapter(kotlifyContext: KotlifyContext) = object : RecyclerView.Adapter<KotlifyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): KotlifyViewHolder {
            val vElement = vItem?.invoke(items[position])
                ?: throw RuntimeException("vItem is not set")
            val view = vElement.build(parent.context, kotlifyContext)
            return KotlifyViewHolder(view, vElement)
        }

        override fun onBindViewHolder(holder: KotlifyViewHolder, position: Int) = Unit

        override fun onViewRecycled(holder: KotlifyViewHolder) {
            super.onViewRecycled(holder)
            holder.dispose()
        }

        override fun getItemCount(): Int = itemsRelay.value.size

        override fun getItemViewType(position: Int) = position
    }

    class KotlifyViewHolder(
        itemView: View,
        private val widgetElement: WidgetElement<*>
    ) : RecyclerView.ViewHolder(itemView), Disposable {

        override fun dispose() = widgetElement.dispose()

        override fun isDisposed(): Boolean = widgetElement.isDisposed
    }
}