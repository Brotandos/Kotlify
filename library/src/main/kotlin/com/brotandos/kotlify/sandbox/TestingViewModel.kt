package com.brotandos.kotlify.sandbox

import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.brotandos.kotlify.R
import com.brotandos.kotlify.common.Air
import com.brotandos.kotlify.common.Water
import com.brotandos.kotlify.common.toggleValue
import com.brotandos.kotlify.container.VContainer
import com.brotandos.kotlify.container.VLinear
import com.brotandos.kotlify.container.root.VRoot
import com.brotandos.kotlify.container.root.VRootOwner
import com.brotandos.kotlify.container.root.vRoot
import com.brotandos.kotlify.element.list.VRecycler
import com.jakewharton.rxrelay2.BehaviorRelay

class TestingViewModel : ViewModel() {

    private val isDark = BehaviorRelay.createDefault(false)
    private val isTextVisible = BehaviorRelay.createDefault(true)
    private val isLoading = BehaviorRelay.createDefault(false)
    private val isLoading2 = BehaviorRelay.createDefault(false)
    private val isDialog = BehaviorRelay.createDefault(false)
    private val isBottomDialog = BehaviorRelay.createDefault(false)
    private val listRelay = BehaviorRelay.createDefault(
            listOf(
                    Number(1),
                    Text("a"),
                    Text("b"),
                    Number(2)
            )
    )
    private val isBottomSheetButtonVisible = BehaviorRelay.createDefault(false)

    private lateinit var vRoot: VRoot<*>

    fun markup(activity: AppCompatActivity) {
        vRoot = activity.vRoot<VLinear>(activity, activity as VRootOwner) {
            isDark(isDark, 0xFFEEEEEE.toInt(), 0xFF222222.toInt())
            initView {
                orientation = LinearLayout.VERTICAL
            }

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
                initView {
                    text = "Show bottom sheet"
                    setOnClickListener {
                        isBottomDialog.accept(true)
                        isDark.toggleValue()
                    }
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

            vList(Water, listRelay) {
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

            vDialog {
                isAppeared = isDialog
                title = "Hello, World!"

                vVertical(Air) {
                    initView { orientation = LinearLayout.VERTICAL }
                    vCustom<Button>()
                    vCustom<Button>()
                }
            }

            vBottomSheetDialog {
                isAppeared = isBottomDialog
                title = "Hello, World!"

                vVertical(Air) {
                    initView { orientation = LinearLayout.VERTICAL }
                    vCustom<Button> {
                        clickRelay.subscribe { isBottomDialog.accept(false) }
                    }
                    vCustom<Button>()
                }
            }
        }
    }

    class Number(val value: Int) : VRecycler.Item
    class Text(val value: String) : VRecycler.Item
}