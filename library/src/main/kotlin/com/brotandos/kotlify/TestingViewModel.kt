package com.brotandos.kotlify

import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxrelay2.BehaviorRelay

class TestingViewModel : ViewModel() {

    private val isDark = BehaviorRelay.createDefault(false)
    private val isTextVisible = BehaviorRelay.createDefault(true)
    private val isLoading = BehaviorRelay.createDefault(false)
    private val isLoading2 = BehaviorRelay.createDefault(false)
    private val isDialog = BehaviorRelay.createDefault(false)
    private val isBottomDialog = BehaviorRelay.createDefault(false)
    private val listRelay = BehaviorRelay.createDefault(listOf(1, 2, 3, 4, 5))
    private val isBottomSheetButtonVisible = BehaviorRelay.createDefault(false)

    lateinit var vRoot: VContainer<*>

    fun markup(activity: AppCompatActivity) {
        vRoot = activity.vRoot<LinearLayout>(activity, activity as VRootOwner) {
            isDark(isDark, 0xFFEEEEEE.toInt(), 0xFF222222.toInt())
            initView {
                orientation = LinearLayout.VERTICAL
            }

            vToolbar(50.dp.water) {
                initView { setBackgroundColor(0xFFAAAADF.toInt()) }
                vAction("Check", R.drawable.ic_send, isLoading) {
                    onClick = {
                        isDialog.accept(true)
                        isLoading2.accept(false)
                        isBottomSheetButtonVisible.accept(!isBottomSheetButtonVisible.value)
                    }
                }
                vAction("Test") {
                    iconResId = R.drawable.ic_camera
                    this.isLoading = isLoading2
                    onClick = { isLoading2.accept(true) }
                }
            }

            vCustom<Button>(200.dp x 200.dp) {
                initLayout<LinearLayout.LayoutParams> { gravity = Gravity.START }
                initView {
                    text = "Show bottom sheet"
                    setOnClickListener {
                        isBottomDialog.accept(true)
                    }
                }
                vShow = isBottomSheetButtonVisible
            }

            vCustom<TextView> {
                vShow = isTextVisible

                initLayout<LinearLayout.LayoutParams> {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                initView {
                    text = "Hello, World!"
                    setTextColor(0xFFFF0000.toInt())
                }
            }

            vRecycler(Water, listRelay) {
                initView { layoutManager = LinearLayoutManager(context) }
                vItem {
                    vCustom<TextView> {
                        initView {
                            setTextColor(Color.RED)
                            text = it.toString()
                        }
                    }
                }
            }

            vDialog {
                vShow = isDialog
                title = "Hello, World!"

                vContainer<LinearLayout> {
                    initView { orientation = LinearLayout.VERTICAL }
                    vCustom<Button> { }
                    vCustom<Button> { }
                }
            }

            vBottomSheetDialog {
                vShow = isBottomDialog
                title = "Hello, World!"

                vContainer<LinearLayout> {
                    initView { orientation = LinearLayout.VERTICAL }
                    vCustom<Button> {
                        onClick = { isBottomDialog.accept(false) }
                    }
                    vCustom<Button> {}
                }
            }
        }
    }
}