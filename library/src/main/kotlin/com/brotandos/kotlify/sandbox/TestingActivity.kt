package com.brotandos.kotlify.sandbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.brotandos.kotlify.common.viewModels
import com.brotandos.kotlify.container.root.VRoot
import com.brotandos.kotlify.container.root.VRootOwner

class TestingActivity : AppCompatActivity(), VRootOwner {

    override var vRoot: VRoot<*>? = null
    private val viewModel: TestingViewModel by viewModels()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, TestingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.markup(this)
    }
}