package com.brotandos.kotlify.element.list

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.brotandos.kotlify.common.*
import com.brotandos.kotlify.container.VContainer
import com.brotandos.kotlify.container.VFrameItself
import com.brotandos.kotlify.element.WidgetElement
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlin.reflect.KClass

/**
 * TODO list:
 * - Find way to use own ViewHolder for each viewType
 * - Add ability to add and remove data
 * - Add pagination
 * - Store items by node (each element has reference to previous and next elements)
 * - Fix broken height of each item
 * */
class VRecycler(
        private val mediator: Mediator,
        private val layoutManager: LayoutManager,
        size: LayoutSize
) : WidgetElement<RecyclerView>(size) {

    private var adapter: RecyclerView.Adapter<KotlifyViewHolder>? = null

    @PublishedApi
    internal val itemsMarkupMap = mutableMapOf<KClass<*>, (Item) -> WidgetElement<*>>()

    override fun createView(context: Context): RecyclerView = RecyclerView(context)

    override fun initSubscriptions(view: RecyclerView?) {
        super.initSubscriptions(view)
        mediator.actionObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it) {
                        is RecyclerItemsAction.Clear -> clearItems(it.size)
                        is RecyclerItemsAction.AddItem<*> -> addItem(it)
                        is RecyclerItemsAction.AddItems<*> -> addItems(it)
                        is RecyclerItemsAction.RemoveItem<*> -> removeItem(it.index)
                        is RecyclerItemsAction.RemoveItems<*> -> removeItems(it)
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
        view.layoutManager = when (layoutManager) {
            is LayoutManager.Horizontal -> {
                val linearLayoutManager = LinearLayoutManager(context)
                linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                linearLayoutManager
            }
            is LayoutManager.Linear -> LinearLayoutManager(context)
            is LayoutManager.Grid -> GridLayoutManager(context, layoutManager.spanCount)
            is LayoutManager.Staggered -> StaggeredGridLayoutManager(
                    layoutManager.spanCount,
                    if (layoutManager.isVertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
            )
        }
        return view
    }

    private fun clearItems(size: Int) {
        adapter?.notifyItemRangeRemoved(0, size)
    }

    private fun addItem(action: RecyclerItemsAction.AddItem<*>) {
        when (val condition = action.insertCondition) {
            is RecyclerItemsAction.InsertCondition.AddToEnd ->
                adapter?.notifyItemInserted(mediator.list.size)
            is RecyclerItemsAction.InsertCondition.AddInside ->
                adapter?.notifyItemInserted(condition.insertPosition)
        }
    }

    private fun addItems(action: RecyclerItemsAction.AddItems<*>) {
        when (val condition = action.insertCondition) {
            is RecyclerItemsAction.InsertCondition.AddToEnd ->
                adapter?.notifyItemRangeInserted(mediator.list.size, action.newItems.size)
            is RecyclerItemsAction.InsertCondition.AddInside ->
                adapter?.notifyItemRangeInserted(condition.insertPosition, action.newItems.size)
        }
    }

    private fun removeItem(index: Int) {
        adapter?.notifyItemRemoved(index)
    }

    private fun removeItems(action: RecyclerItemsAction.RemoveItems<*>) {
        try {
            adapter?.notifyItemRangeRemoved(action.startIndex, action.itemsCount)
        } catch (indexOutOfBoundException: IndexOutOfBoundsException) {
            adapter?.notifyDataSetChanged()
        }
    }

    private fun getAdapter(
            kotlifyContext: KotlifyContext
    ) = object : RecyclerView.Adapter<KotlifyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): KotlifyViewHolder {
            val item = mediator.list[position]
            val vElement = itemsMarkupMap[item::class]
                    ?.invoke(item)
                    ?: throw RuntimeException("vItem is not set for ${item::class.qualifiedName}")
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

        override fun getItemCount(): Int = mediator.list.size

        override fun getItemViewType(position: Int) = position
    }

    inline fun <reified E : Item> vItem(
            crossinline itemView: VContainer<*, *>.(E) -> WidgetElement<*>
    ) {
        itemsMarkupMap[E::class] = {
            VFrameItself(Earth).apply {
                itemView(it as E)
            }
        }
    }

    inline fun <reified E : Item, reified V : VContainer<*, *>>vItem(
            size: LayoutSize,
            crossinline itemView: V.(E) -> Unit
    ) {
        itemsMarkupMap[E::class] = {
            KotlifyInternals.initiateWidgetContainer(size, V::class.java).apply { itemView(it as E) }
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

    class Mediator(items: List<Item> = listOf()) {

        constructor(vararg items: Item) : this(items.toMutableList())

        // TODO check for thread safety
        private val mutableList: MutableList<Item> = items.toMutableList()
        val list: List<Item> = mutableList

        private val actionRelay = PublishRelay.create<RecyclerItemsAction<Item>>()
        val actionObservable: Observable<RecyclerItemsAction<Item>> = actionRelay.hide()

        fun clearItems() {
            if (list.isEmpty()) return
            val size = list.size
            mutableList.clear()
            actionRelay accept RecyclerItemsAction.Clear(size)
        }

        fun addItem(item: Item) {
            mutableList.add(item)
            actionRelay accept RecyclerItemsAction.AddItem(item)
        }

        fun addItem(item: Item, index: Int) {
            mutableList.add(item)
            actionRelay accept RecyclerItemsAction.AddItem(
                    item,
                    RecyclerItemsAction.InsertCondition.AddInside(index)
            )
        }

        fun addItems(items: List<Item>) {
            mutableList.addAll(items)
            actionRelay accept RecyclerItemsAction.AddItems(items)
        }

        fun removeItem(index: Int) {
            mutableList.removeAt(index)
            actionRelay accept RecyclerItemsAction.RemoveItem(index)
        }

        fun removeItems(startIndex: Int, itemsCount: Int) {
            mutableList.subList(startIndex, startIndex + itemsCount).clear()
            actionRelay accept RecyclerItemsAction.RemoveItems(startIndex = startIndex, itemsCount = itemsCount)
        }
    }
}

sealed class RecyclerItemsAction<T : VRecycler.Item> {

    class Clear(val size: Int) : RecyclerItemsAction<VRecycler.Item>()

    class AddItem<T : VRecycler.Item>(
            val item: T,
            val insertCondition: InsertCondition = InsertCondition.AddToEnd
    ) : RecyclerItemsAction<T>()

    class AddItems<T : VRecycler.Item>(
            val newItems: List<T>,
            val insertCondition: InsertCondition = InsertCondition.AddToEnd
    ) : RecyclerItemsAction<T>()

    class RemoveItem<T : VRecycler.Item>(val index: Int) : RecyclerItemsAction<T>()

    class RemoveItems<T : VRecycler.Item>(
            val itemsCount: Int,
            val startIndex: Int = 0
    ) : RecyclerItemsAction<T>()

    sealed class InsertCondition {
        object AddToEnd : InsertCondition()
        class AddInside(val insertPosition: Int = 0) : InsertCondition()
    }
}