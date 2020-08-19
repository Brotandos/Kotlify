package com.brotandos.kotlify

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.brotandos.kotlify.common.viewModels
import com.brotandos.kotlify.sandbox.TestingViewModel

class TestingActivity : AppCompatActivity() {

    private val viewModel: TestingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.markup(this)
    }
}