package com.brotandos.kotlify.sandbox

import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.brotandos.kotlify.R
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.Earth
import com.brotandos.kotlify.common.Water
import com.brotandos.kotlify.common.toggleValue
import com.brotandos.kotlify.container.VVertical
import com.brotandos.kotlify.container.root.VRoot
import com.brotandos.kotlify.container.root.VRootOwner
import com.brotandos.kotlify.container.root.vRoot
import com.brotandos.kotlify.container.vSimpleSpinner
import com.brotandos.kotlify.element.VSimpleSpinner
import com.brotandos.kotlify.element.list.VRecycler
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers

class TestingViewModel : ViewModel(), VRootOwner {

    private val isDark = BehaviorRelay.createDefault(false)
    private val isTextVisible = BehaviorRelay.createDefault(true)
    private val isLoading = BehaviorRelay.createDefault(false)
    private val isLoading2 = BehaviorRelay.createDefault(false)
    private val isDialog = BehaviorRelay.createDefault(false)
    private val isBottomDialog = BehaviorRelay.createDefault(false)
    private val selection = PublishRelay.create<Selection>()
    private val listMediator = VRecycler.Mediator().also {
        it.addItems(listOf(
                Number(1),
                Text("a"),
                Text("b"),
                Number(2)
        ))
    }
    private val isBottomSheetButtonVisible = BehaviorRelay.createDefault(false)

    override var vRoot: VRoot<*>? = null

    fun markup(activity: AppCompatActivity) {
        vRoot = vRoot<VVertical>(activity) {
            isDark(isDark, 0xFFEEEEEE.toInt(), 0xFF222222.toInt())

            vToolbar(50.dp.water) {
                initView { setBackgroundColor(0xFFAAAADF.toInt()) }
                vAction("Check", R.drawable.ic_send, isLoading) {
                    onClick {
                        isDialog.accept(true)
                        isLoading2.accept(false)
                        isBottomSheetButtonVisible.toggleValue()
                    }
                }
                vAction("Test") {
                    iconResId = R.drawable.ic_camera
                    this.isLoading = isLoading2
                    onClick { isLoading2.accept(true) }
                }
            }

            vCustom<Button>(200.dp x 200.dp) {
                id = R.id.bottom_sheet_dialog_trigger
                lparams { gravity = Gravity.START }
                initView { text = "Show bottom sheet" }
                onClick {
                    isBottomDialog.accept(true)
                    isDark.toggleValue()
                }
            }

            vCustom<TextView> {
                id = R.id.hello_world_text_view
                isAppeared = isTextVisible
                lparams { gravity = Gravity.CENTER_HORIZONTAL }
                initView {
                    text = "Hello, World!"
                    setTextColor(0xFFFF0000.toInt())
                }
            }

            vList(Water, listMediator) {
                initView { layoutManager = LinearLayoutManager(context) }
                vItem<Number> {
                    vCustom<TextView> {
                        initView {
                            setTextColor(Color.RED)
                            text = it.value.toString()
                        }
                    }
                }
                vItem<Text> {
                    vCustom<TextView> {
                        initView {
                            setTextColor(Color.BLUE)
                            text = it.value
                        }
                    }
                }
            }

            vSimpleSpinner<Selection>(Earth) {
                selectedOption = selection
            }

            vDialog {
                isAppeared = isDialog
                title = "Hello, World!"

                vVertical(Air) {
                    vCustom<Button>()
                    vCustom<Button>()
                }
            }

            vBottomSheetDialog {
                isAppeared = isBottomDialog
                title = "Hello, World!"

                vVertical(Air) {
                    vCustom<Button> {
                        onClick { isBottomDialog.accept(false) }
                    }
                    vCustom<Button>()
                }
            }
        }
        selection
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { Toast.makeText(activity, it.getName(), Toast.LENGTH_SHORT).show() }
            .untilLifecycleDestroy()
    }

    class Number(val value: Int) : VRecycler.Item
    class Text(val value: String) : VRecycler.Item

    sealed class Selection : VSimpleSpinner.Option {
        object Option1 : Selection()
        object Option2 : Selection()
        object Option3 : Selection()
    }
}