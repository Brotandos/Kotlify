package com.brotandos.kotlify.container

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.ViewPager
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.CustomLength
import com.brotandos.kotlify.common.CustomSize
import com.brotandos.kotlify.common.LayoutSize
import com.brotandos.kotlify.common.MatchParent
import com.brotandos.kotlify.element.*
import com.brotandos.kotlify.element.VSimpleSpinner
import com.brotandos.kotlify.element.list.LayoutManager
import com.brotandos.kotlify.element.list.VRecycler
import com.google.android.material.button.MaterialButton
import com.jakewharton.rxrelay2.BehaviorRelay

interface WidgetContainer {

    val Int.dp: CustomLength
        get() = CustomLength(this)

    val CustomLength.water: LayoutSize
        get() = CustomSize(MatchParent, this)

    val CustomLength.fire: LayoutSize
        get() = CustomSize(this, MatchParent)

    val CustomLength.square: LayoutSize
        get() = CustomSize(this, this)

    val Activity.density: Float
        get() = resources.displayMetrics.density

    val Context.actionBarSize: LayoutSize
        get() {
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
                    .takeIf { it }
                    ?: return 50.dp.water
            val height = TypedValue.complexToFloat(typedValue.data).toInt()
            return height.dp.water
        }

    fun accept(widget: WidgetElement<*>)

    fun vToolbar(
            size: LayoutSize,
            init: (VToolbar<Toolbar, Toolbar.LayoutParams>.() -> Unit)? = null
    ): VToolbar<Toolbar, Toolbar.LayoutParams> =
            object : VToolbar<Toolbar, Toolbar.LayoutParams>(size) {
                override fun createView(context: Context): Toolbar = Toolbar(context)
                override fun getChildLayoutParams(width: Int, height: Int): Toolbar.LayoutParams =
                        Toolbar.LayoutParams(width, height)
            }
                    .also(::accept)
                    .apply { init?.invoke(this) }

    fun vLabel(
            size: LayoutSize,
            init: (VLabel<TextView>.() -> Unit)? = null
    ): VLabel<TextView> = object : VLabel<TextView>(size) {
        override fun createView(context: Context): TextView = TextView(context)
    }.also(::accept).apply { init?.invoke(this) }

    fun vLabel(
            size: LayoutSize,
            vararg styles: TextView.() -> Unit,
            init: (VLabel<TextView>.() -> Unit)? = null
    ): VLabel<TextView> {
        val vLabel = object : VLabel<TextView>(size) {
            override fun createView(context: Context): TextView = TextView(context)
        }
        accept(vLabel)
        vLabel.styles = arrayOf(*styles)
        init?.invoke(vLabel)
        return vLabel
    }

    fun vLabel(
            size: LayoutSize,
            @StringRes textResId: Int,
            vararg styles: TextView.() -> Unit,
            init: (VLabel<TextView>.() -> Unit)? = null
    ): VLabel<TextView> {
        val vLabel = object : VLabel<TextView>(size) {
            override fun createView(context: Context): TextView = TextView(context)
        }.also(::accept)
        vLabel.textResId = textResId
        vLabel.styles = arrayOf(*styles)
        init?.invoke(vLabel)
        return vLabel
    }

    fun vLabel(
            size: LayoutSize,
            text: String,
            vararg styles: TextView.() -> Unit,
            init: (VLabel<TextView>.() -> Unit)? = null
    ): VLabel<TextView> {
        val vLabel = object : VLabel<TextView>(size) {
            override fun createView(context: Context): TextView = TextView(context)
        }.also(::accept)
        vLabel.text = text
        vLabel.styles = arrayOf(*styles)
        init?.invoke(vLabel)
        return vLabel
    }

    fun vEdit(
            size: LayoutSize,
            vararg styles: EditText.() -> Unit,
            init: (VEdit<EditText>.() -> Unit)? = null
    ): VEdit<EditText> {
        val vEdit = object : VEdit<EditText>(size) {
            override fun createView(context: Context): EditText = EditText(context)
        }.also(::accept)
        vEdit.styles = arrayOf(*styles)
        init?.invoke(vEdit)
        return vEdit
    }

    fun vButton(
            size: LayoutSize,
            init: (VButton<MaterialButton>.() -> Unit)? = null
    ): VButton<MaterialButton> = object : VButton<MaterialButton>(size) {
        override fun createView(context: Context): MaterialButton = MaterialButton(context)
    }.also(::accept).apply { init?.invoke(this) }

    fun vList(
        size: LayoutSize,
        mediator: VRecycler.Mediator,
        init: (VRecycler.() -> Unit)? = null
    ): VRecycler = VRecycler(mediator = mediator, layoutManager = LayoutManager.Linear, size = size)
        .also(::accept)
        .apply { init?.invoke(this) }

    fun vListHorizontal(
        size: LayoutSize,
        mediator: VRecycler.Mediator,
        init: (VRecycler.() -> Unit)? = null
    ): VRecycler = VRecycler(mediator = mediator, layoutManager = LayoutManager.Horizontal, size = size)
        .also(::accept)
        .apply { init?.invoke(this) }

    fun vGrid(
            size: LayoutSize,
            items: BehaviorRelay<List<VRecycler.Item>>,
            init: (VRecycler.() -> Unit)? = null
    ): VRecycler = TODO("not implemented")

    fun vLinear(
            size: LayoutSize,
            init: (VContainer<LinearLayout, LinearLayout.LayoutParams>.() -> Unit)? = null
    ): VLinear<LinearLayout, LinearLayout.LayoutParams> =
            object : VLinear<LinearLayout, LinearLayout.LayoutParams>(size) {
                override fun createView(context: Context): LinearLayout = LinearLayout(context)

                override fun getChildLayoutParams(width: Int, height: Int): LinearLayout.LayoutParams =
                        LinearLayout.LayoutParams(width, height)
            }.also(::accept).apply { init?.invoke(this) }

    fun vVertical(
            size: LayoutSize,
            init: (VContainer<LinearLayout, LinearLayout.LayoutParams>.() -> Unit)? = null
    ) = VVertical(size).also(::accept).apply { init?.invoke(this) }

    fun vCard(
            size: LayoutSize,
            init: (VCard<CardView, FrameLayout.LayoutParams>.() -> Unit)? = null
    ): VCard<CardView, FrameLayout.LayoutParams> =
            object : VCard<CardView, FrameLayout.LayoutParams>(size) {
                override fun createView(context: Context): CardView = CardView(context)

                override fun getChildLayoutParams(width: Int, height: Int): FrameLayout.LayoutParams =
                        FrameLayout.LayoutParams(width, height)
            }
                    .also(::accept)
                    .apply { init?.invoke(this) }

    fun vConstraint(
            size: LayoutSize,
            init: (VConstraintItself.() -> Unit)? = null
    ): VConstraintItself {
        val vConstraint = VConstraintItself(Air)
        accept(vConstraint)
        init?.invoke(vConstraint)
        return vConstraint
    }

    fun vImage(
        size: LayoutSize,
        resId: Int,
        init: (VImage<ImageView>.() -> Unit)? = null
    ): VImage<ImageView> {
        val vImage = object : VImage<ImageView>(size) {
            override fun createView(context: Context): ImageView = ImageView(context)
        }.also(::accept)
        vImage.imageResId = BehaviorRelay.createDefault(resId)
        init?.invoke(vImage)
        return vImage
    }

    fun vImage(
        size: LayoutSize,
        init: (VImage<ImageView>.() -> Unit)? = null
    ): VImage<ImageView> =
            object : VImage<ImageView>(size) {
                override fun createView(context: Context): ImageView = ImageView(context)
            }.also(::accept).apply { init?.invoke(this) }

    fun vCheckBox(
        size: LayoutSize,
        init: (VCheckBox<CheckBox>.() -> Unit)? = null): VCheckBox<CheckBox> =
        object  : VCheckBox<CheckBox>(size) {
            override fun createView(context: Context): CheckBox = CheckBox(context)
        }.also(::accept).apply { init?.invoke(this) }

    fun vImagePager(
        size: LayoutSize,
        imageHolder: VImagePager.ImageHolder,
        init: (VImagePager<ViewPager>.() -> Unit)? = null) : VImagePager<ViewPager> =
        object : VImagePager<ViewPager>(imageHolder, size) {
            override fun createView(context: Context): ViewPager {
                return ViewPager(context)
            }
        }.also(::accept).apply { init?.invoke(this) }

    fun vImagePager(
        size: LayoutSize,
        init: (VImagePager<ViewPager>.() -> Unit)? = null) : VImagePager<ViewPager> =
        object : VImagePager<ViewPager>(ImageHolder() ,size) {
            override fun createView(context: Context): ViewPager {
                return ViewPager(context)
            }
        }.also(::accept).apply { init?.invoke(this) }

    fun <T : ToggleOption> vToggleGroup(
            size: LayoutSize,
            selectedOption: BehaviorRelay<T>,
            init: (VToggleGroup<T>.() -> Unit)? = null
    ): VToggleGroup<T> {
        val vToggleGroup = VToggleGroup<T>(size).also(::accept)
        vToggleGroup.selectedOption = selectedOption
        init?.invoke(vToggleGroup)
        return vToggleGroup
    }

    fun <T : ToggleOption> T.vSimpleToggle(
            size: LayoutSize,
            selectedOption: BehaviorRelay<T>,
            init: (VSimpleToggle<T, ToggleButton>.() -> Unit)? = null
    ): VSimpleToggle<T, ToggleButton> {
        val vSimpleToggle = object : VSimpleToggle<T, ToggleButton>(size) {
            override fun createView(context: Context): ToggleButton = ToggleButton(context)
        }.also(::accept)
        vSimpleToggle.model = this
        vSimpleToggle.selectedOption = selectedOption
        init?.invoke(vSimpleToggle)
        return vSimpleToggle
    }

    // VLabel styles
    val textCenter: TextView.() -> Unit
        get() = { gravity = Gravity.CENTER_HORIZONTAL }

    operator fun Typeface.unaryPlus(): TextView.() -> Unit =
            { typeface = this@unaryPlus }

    val Typeface.bold: TextView.() -> Unit
        get() = { setTypeface(this@bold, Typeface.BOLD) }

    val Typeface.italic: TextView.() -> Unit
        get() = { setTypeface(this@italic, Typeface.ITALIC) }

    val Typeface.boldItalic: TextView.() -> Unit
        get() = { setTypeface(this@boldItalic, Typeface.BOLD_ITALIC) }

    val bold: TextView.() -> Unit
        get() = { setTypeface(typeface, Typeface.BOLD) }

    val italic: TextView.() -> Unit
        get() = { setTypeface(typeface, Typeface.ITALIC) }

    val boldItalic: TextView.() -> Unit
        get() = { setTypeface(typeface, Typeface.BOLD_ITALIC) }

    val blackText: TextView.() -> Unit
        get() = { setTextColor(Color.BLACK) }

    val Float.sp: TextView.() -> Unit
        get() = { textSize = this@sp }

    val Int.textColor: TextView.() -> Unit
        get() = { setTextColor(this@textColor) }

    val selectable: TextView.() -> Unit
        get() = { setTextIsSelectable(true) }

    val unselectable: TextView.() -> Unit
        get() = { setTextIsSelectable(false) }

    val singleLine: TextView.() -> Unit
        get() = { isSingleLine = true }
}

inline fun <reified T : VSimpleSpinner.Option> WidgetContainer.vSimpleSpinner(
    size: LayoutSize,
    init: VSimpleSpinner<T, Spinner>.() -> Unit
): VSimpleSpinner<T, Spinner> {
    val options = T::class.nestedClasses.map {
        val instance = it.objectInstance ?: throw IllegalStateException("Nested classes must be object instance")
        return@map instance as? T ?: throw IllegalStateException("${instance::class.qualifiedName} must implement ${VSimpleSpinner.Option::class.qualifiedName}")
    }
    return object : VSimpleSpinner<T, Spinner>(size) {
        override fun createView(context: Context): Spinner = Spinner(context)
    }.also {
        accept(it)
        it.init()
        it.setOptions(options)
    }
}