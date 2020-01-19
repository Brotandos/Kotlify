package com.brotandos.kotlify.permission;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

/**
 * Real implementation of RxPermissionContract that will show the usual Android dialog when requesting for permissions.
 */
public final class RxPermission implements RxPermissionContract {
    static final Object TRIGGER = new Object();
    static RxPermission instance;

    /**
     * @param context any context
     * @return a Singleton instance of this class
     */
    public static RxPermission getInstance(final Context context) {
        synchronized (RxPermission.class) {
            if (instance == null) {
                instance = new RxPermission((Application) context.getApplicationContext());
            }
        }

        return instance;
    }

    private final Application application;

    // Contains all the current permission requests. Once granted or denied, they are removed from it.
    private final Map<String, PublishSubject<Permission>> currentPermissionRequests = new HashMap<>();

    RxPermission(final Application application) {
        this.application = application;
    }

    /**
     * Requests permissions immediately, <b>must be invoked during initialization phase of your application</b>.
     */
    @Override
    @NonNull
    @CheckReturnValue
    public Observable<Permission> requestEach(@NonNull final String... permissions) {
        return Observable.just(TRIGGER)
                .compose(ensureEach(permissions));
    }

    /**
     * Requests the permission immediately, <b>must be invoked during initialization phase of your application</b>.
     */
    @Override
    @NonNull
    public Single<Permission> request(@NonNull final String permission) {
        return requestEach(permission).firstOrError();
    }

    /**
     * Map emitted items from the source observable into {@link Permission} objects for each
     * permission in parameters.
     * <p>
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    @NonNull
    @CheckReturnValue
    private <T> ObservableTransformer<T, Permission> ensureEach(@NonNull final String... permissions) {
        Utils.INSTANCE.checkPermissions(permissions);

        return new ObservableTransformer<T, Permission>() {
            @Override
            @NonNull
            @CheckReturnValue
            public ObservableSource<Permission> apply(final Observable<T> o) {
                return request(o, permissions);
            }
        };
    }

    @NonNull
    @CheckReturnValue
    @SuppressWarnings("checkstyle:overloadmethodsdeclarationorder")
    Observable<Permission> request(final Observable<?> trigger, @NonNull final String... permissions) {
        return Observable.merge(trigger, pending(permissions))
                .flatMap(new Function<Object, Observable<Permission>>() {
                    @Override
                    @NonNull
                    @CheckReturnValue
                    public Observable<Permission> apply(final Object o) {
                        return requestOnM(permissions);
                    }
                });
    }

    @NonNull
    @CheckReturnValue
    private Observable<?> pending(@NonNull final String... permissions) {
        for (final String p : permissions) {
            if (!currentPermissionRequests.containsKey(p)) {
                return Observable.empty();
            }
        }

        return Observable.just(TRIGGER);
    }

    @NonNull
    @CheckReturnValue
    @TargetApi(M)
    Observable<Permission> requestOnM(@NonNull final String... permissions) {
        final List<Observable<? extends Permission>> list = new ArrayList<>(permissions.length);
        final List<String> unrequestedPermissions = new ArrayList<>();

        // In case of multiple permissions, we create an observable for each of them.
        // At the end, the observables are combined to have a unique response.

        for (final String permission : permissions) {
            if (isGranted(permission)) {
                list.add(Observable.just(new Permission.Granted(permission)));
            } else if (isRevokedByPolicy(permission)) {
                list.add(Observable.just(new Permission.RevokeByPolicy(permission)));
            } else {
                PublishSubject<Permission> subject = currentPermissionRequests.get(permission);

                // Create a new subject if not exists
                if (subject == null) {
                    unrequestedPermissions.add(permission);
                    subject = PublishSubject.create();
                    currentPermissionRequests.put(permission, subject);
                }

                list.add(subject);
            }
        }

        if (!unrequestedPermissions.isEmpty()) {
            final String[] permissionsToRequest = unrequestedPermissions.toArray(new String[0]);
            startShadowActivity(permissionsToRequest);
        }

        return Observable.concat(Observable.fromIterable(list));
    }

    /**
     * Returns true if the permission is already granted.
     * <p>
     * Always true if SDK &lt; 23.
     */
    @Override
    @CheckReturnValue
    public boolean isGranted(@NonNull final String permission) {
        return !isMarshmallow() || isGrantedOnM(permission);
    }

    /**
     * Returns true if the permission has been revoked by a policy.
     * <p>
     * Always false if SDK &lt; 23.
     */
    @Override
    @CheckReturnValue
    public boolean isRevokedByPolicy(@NonNull final String permission) {
        return isMarshmallow() && isRevokedOnM(permission);
    }

    @TargetApi(M)
    private boolean isGrantedOnM(final String permission) {
        return application.checkSelfPermission(permission) == PERMISSION_GRANTED;
    }

    @TargetApi(M)
    private boolean isRevokedOnM(final String permission) {
        return application.getPackageManager().isPermissionRevokedByPolicy(permission, application.getPackageName());
    }

    void startShadowActivity(final String[] permissions) {
        ShadowActivity.Companion.start(application, permissions);
    }

    void onRequestPermissionsResult(@NonNull final int[] grantResults, @NonNull final boolean[] rationale, @NonNull final boolean[] rationaleAfter, @NonNull final String... permissions) {
        final int size = permissions.length;

        for (int i = 0; i < size; i++) {
            final PublishSubject<Permission> subject = currentPermissionRequests.get(permissions[i]);

            if (subject == null) {
                throw new IllegalStateException("RxPermission.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
            }

            currentPermissionRequests.remove(permissions[i]);

            final boolean granted = grantResults[i] == PERMISSION_GRANTED;

            if (granted) {
                subject.onNext(new Permission.Granted(permissions[i]));
            } else if (!rationale[i] && !rationaleAfter[i]) {
                subject.onNext(new Permission.NotShowAgain(permissions[i]));
            } else {
                subject.onNext(new Permission.Denied(permissions[i]));
            }

            subject.onComplete();
        }
    }

    boolean isMarshmallow() {
        return SDK_INT >= M;
    }
}
