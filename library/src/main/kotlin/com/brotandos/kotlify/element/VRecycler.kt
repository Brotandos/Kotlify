package com.brotandos.kotlify.element

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.KotlifyContext
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.container.VContainer
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlin.reflect.KClass

/**
 * TODO list:
 * - Find way to use own ViewHolder for each viewType
 * - Add ability to add and remove data
 * - Add pagination
 * - Store items by node (each element has reference to previous and next elements)
 * */
class VRecycler(
        private val itemsRelay: BehaviorRelay<List<Item>>,
        private val layoutManager: LayoutManager,
        size: LayoutSize
) : WidgetElement<RecyclerView>(size) {

    private val itemsActions = BehaviorRelay.createDefault<RecyclerItemsActions<Item>>(
            RecyclerItemsActions.Empty
    )

    private val items: List<Item> get() = itemsRelay.value

    private var adapter: RecyclerView.Adapter<KotlifyViewHolder>? = null

    @PublishedApi
    internal val itemsMarkupMap = mutableMapOf<KClass<*>, (Item) -> WidgetElement<*>>()

    override fun createView(context: Context): RecyclerView = RecyclerView(context)

    override fun initSubscriptions(view: RecyclerView?) {
        super.initSubscriptions(view)
        itemsRelay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { adapter?.notifyDataSetChanged() }
                .untilLifecycleDestroy()

        itemsActions
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it) {
                        is RecyclerItemsActions.Empty -> {}
                        is RecyclerItemsActions.AddItem<Item> -> {
                            adapter?.notifyItemInserted(it.index)

                        }
                        is RecyclerItemsActions.AddItems<Item> -> {
                            adapter?.notifyItemRangeInserted(it.index, it.items.size)
                        }
                        is RecyclerItemsActions.RemoveItem<*> -> {
                            adapter?.notifyItemRemoved(it.index)
                        }
                        is RecyclerItemsActions.RemoveItems<*> -> {
                            adapter?.notifyItemRangeRemoved(it.startIndex, it.itemsCount)
                        }
                    }
                }
                .untilLifecycleDestroy()
    }

    override fun build(context: Context, kotlifyContext: KotlifyContext): RecyclerView {
        val view = super.build(context, kotlifyContext)
        getAdapter(kotlifyContext).let {
            view.adapter = it
            adapter = it
        }
        view.layoutManager = when (layoutManager){
            is LayoutManager.Linear -> LinearLayoutManager(context)
            is LayoutManager.Grid -> GridLayoutManager(context, layoutManager.spanCount)
            is LayoutManager.Staggered -> StaggeredGridLayoutManager(
                    layoutManager.spanCount,
                    if (layoutManager.isVertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
            )
        }
        return view
    }

    private fun getAdapter(
            kotlifyContext: KotlifyContext
    ) = object : RecyclerView.Adapter<KotlifyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): KotlifyViewHolder {
            val item = items[position]
            val vElement = itemsMarkupMap[item::class]
                    ?.invoke(item)
                    ?: throw RuntimeException("vItem is not set")
            // TODO use custom exception
            val path = pathInsideTree
                    ?: throw RuntimeException("WidgetContainer#buildWidget method must be called before to initialize pathInsideTree")
            val view = vElement.buildWidget(
                    parent.context,
                    kotlifyContext,
                    path + position
            )
            vElement.untilLifecycleDestroy()
            return KotlifyViewHolder(view, vElement)
        }

        override fun onBindViewHolder(holder: KotlifyViewHolder, position: Int) = Unit

        override fun getItemCount(): Int = items.size

        override fun getItemViewType(position: Int) = position
    }

    inline fun <reified E : Item> vItem(
            crossinline itemView: VContainer<*>.(E) -> WidgetElement<*>
    ) {
        val vContainer = object : VContainer<FrameLayout>(Air) {
            override fun createView(context: Context): FrameLayout = FrameLayout(context)
        }
        itemsMarkupMap[E::class] = {
            vContainer.itemView(it as E)
        }
    }

    class KotlifyViewHolder(
            itemView: View,
            private val widgetElement: WidgetElement<*>
    ) : RecyclerView.ViewHolder(itemView), Disposable {

        override fun dispose() = widgetElement.dispose()

        override fun isDisposed(): Boolean = widgetElement.isDisposed
    }

    interface Item
}

sealed class LayoutManager {
    object Linear : LayoutManager()
    data class Grid(val spanCount: Int) : LayoutManager()
    data class Staggered(val spanCount: Int, val isVertical: Boolean) : LayoutManager()
}

sealed class RecyclerItemsActions<T : VRecycler.Item> {
    object Empty : RecyclerItemsActions<VRecycler.Item>()
    class AddItem<T : VRecycler.Item>(val item: T, val index: Int = 0) : RecyclerItemsActions<T>()
    class AddItems<T : VRecycler.Item>(
            val items: List<T>,
            val index: Int = 0
    ) : RecyclerItemsActions<T>()
    class RemoveItem<T : VRecycler.Item>(val index: Int) : RecyclerItemsActions<T>()
    class RemoveItems<T : VRecycler.Item>(
            val itemsCount: Int,
            val startIndex: Int = 0
    )
}