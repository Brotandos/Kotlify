package com.brotandos.kotlify.permission

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.annotations.CheckReturnValue

interface RxPermissionContract {
    /**
     * Requests a single permission.
     */
    @CheckReturnValue
    fun request(permission: String): Single<Permission>

    /**
     * Requests multiple permissions.
     */
    @CheckReturnValue
    fun requestEach(vararg permissions: String): Observable<Permission>

    /**
     * Returns true when the given permission is granted.
     */
    @CheckReturnValue
    fun isGranted(permission: String): Boolean

    /**
     * Returns true when the given permission is revoked by a policy.
     */
    @CheckReturnValue
    fun isRevokedByPolicy(permission: String): Boolean
}