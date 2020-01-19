package com.brotandos.kotlify.permission

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

private const val ARG_PERMISSIONS = "permissions"
private const val SAVE_RATIONALE = "save-rationale"
private const val REQUEST_CODE = 42

@TargetApi(Build.VERSION_CODES.M)
class ShadowActivity : Activity() {

    companion object {
        fun start(context: Context, permissions: Array<String>) {
            val intent = Intent(context, ShadowActivity::class.java)
                    .putExtra(ARG_PERMISSIONS, permissions)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private lateinit var shouldShowRequestPermissionRationale: BooleanArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) return handleIntent(intent)
        shouldShowRequestPermissionRationale = savedInstanceState.getBooleanArray(SAVE_RATIONALE)
                ?: return
    }

    private fun handleIntent(intent: Intent) {
        val permissions = intent.getStringArrayExtra(ARG_PERMISSIONS) ?: return
        shouldShowRequestPermissionRationale = rationales(permissions)
        requestPermissions(permissions, REQUEST_CODE)
    }

    private fun rationales(permissions: Array<out String>): BooleanArray =
            permissions
                .map { shouldShowRequestPermissionRationale(it) }
                .toBooleanArray()

    override fun onNewIntent(intent: Intent) = handleIntent(intent)

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode != REQUEST_CODE) return
        val rationales = rationales(permissions)
        RxPermission.getInstance(this).onRequestPermissionsResult(
                grantResults,
                shouldShowRequestPermissionRationale,
                rationales,
                *permissions
        )
        finish()
    }

    override fun finish() {
        super.finish()
        // Reset the animation to avoid flickering.
        overridePendingTransition(0, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBooleanArray(SAVE_RATIONALE, shouldShowRequestPermissionRationale)
        super.onSaveInstanceState(outState)
    }
}