package com.brotandos.kotlify.permission

sealed class Permission(val name: String) {
    class Granted(name: String) : Permission(name)
    class Denied(name: String) : Permission(name)
    class NotShowAgain(name: String) : Permission(name)
    class RevokeByPolicy(name: String) : Permission(name)
}
