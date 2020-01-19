package com.brotandos.kotlify.permission

object Utils {
    fun checkPermissions(permissions: Array<String>?) {
        permissions
                ?.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("permissions are null or empty")
    }
}