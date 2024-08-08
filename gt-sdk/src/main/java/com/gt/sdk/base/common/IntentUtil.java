package com.gt.sdk.base.common;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.czhj.sdk.common.exceptions.IntentNotResolvableException;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.logger.SigmobLog;

import java.util.List;

public class IntentUtil {
    private IntentUtil() {
    }


    public static void registerReceiver(final Context context, final BroadcastReceiver receiver, IntentFilter intentFilter) {
        registerReceiver(context, receiver, intentFilter, true);
    }

    @SuppressLint("WrongConstant")
    public static void registerReceiver(final Context context, final BroadcastReceiver receiver, IntentFilter intentFilter, boolean isExported) {
        if (Build.VERSION.SDK_INT >= 34 && context.getApplicationInfo().targetSdkVersion >= 34) {
            context.registerReceiver(receiver, intentFilter, isExported ? 2 : 4);
        } else {
            context.registerReceiver(receiver, intentFilter);
        }
    }

    private static void startActivity(final Context context, final Intent intent) throws IntentNotResolvableException {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(intent);


        try {

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            context.startActivity(intent);
        } catch (Throwable e) {
            throw new IntentNotResolvableException(e);
        }
    }


    /**
     * Adding FLAG_ACTIVITY_NEW_TASK with startActivityForResult will always result in a
     * RESULT_CANCELED, so don't use it for Activity contexts.
     */

    public static Intent getStartActivityIntent(final Context context, final Class clazz, final Bundle extras) {
        final Intent intent = new Intent(context, clazz);

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);

        if (extras != null) {
            intent.putExtras(extras);
        }

        return intent;
    }

    public static boolean deviceCanHandlePackageName(final Context context, final String packageName) {

        try {

            final PackageManager packageManager = context.getPackageManager();
            final Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            return intent != null;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean deviceCanHandleIntent(final Context context, final Intent intent) {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//            return true;
//        }
        try {
            final PackageManager packageManager = context.getPackageManager();
            final List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            return !activities.isEmpty();
        } catch (NullPointerException e) {
            return false;
        }
    }


    public static boolean launchApplicationForPackageName(final Context context, final String packageName) {
        try {
            Intent shortcutIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

            if (shortcutIntent != null) {
                startActivity(context, shortcutIntent);
                return true;
            }

        } catch (Throwable throwable) {
            SigmobLog.e(throwable.getMessage());

        }
        return false;
    }


    private static void launchIntentForUserClick(final Context context, final Intent intent, final String errorMessage) throws IntentNotResolvableException {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(intent);

        try {
            startActivity(context, intent);

        } catch (Throwable e) {
            throw new IntentNotResolvableException(errorMessage + "\n" + e.getMessage());
        }
    }

    public static void launchApplicationUrl(final Context context, final Uri uri) throws IntentNotResolvableException {
        launchApplicationUrl(context, uri, null);
    }

    public static void launchApplicationUrl(final Context context, final Uri uri, final String packageName) throws IntentNotResolvableException {
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (!TextUtils.isEmpty(packageName)) {
            intent.setPackage(packageName);
        }
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(uri);

        if (deviceCanHandleIntent(context, intent)) {
            final String errorMessage = "Unable to open intent: " + intent;
            launchIntentForUserClick(context, intent, errorMessage);
        } else {
            // Deeplink+ needs this exception to know primaryUrl failed and then attempt fallbackUrl
            // See UrlAction.FOLLOW_DEEP_LINK_WITH_FALLBACK
            throw new IntentNotResolvableException("Could not handle application specific " + "action: " + uri + "\n\tYou may be running in the emulator or another " + "device which does not have the required application.");
        }
    }

    public static void launchApplicationIntent(final Context context, final Intent intent) throws IntentNotResolvableException {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(intent);

        if (deviceCanHandleIntent(context, intent)) {
            final String errorMessage = "Unable to open intent: " + intent;
            launchIntentForUserClick(context, intent, errorMessage);
        } else {
            throw new IntentNotResolvableException("Could not handle application specific " + "action: " + intent.toString() + "\n\tYou may be running in the emulator or another " + "device which does not have the required application.");
        }
    }


    private static Uri getPlayStoreUri(final Intent intent) {
        Preconditions.NoThrow.checkNotNull(intent);

        return Uri.parse("market://details?id=" + intent.getPackage());
    }

    public static void launchActionViewIntent(final Context context, final Uri uri, final String errorMessage) throws IntentNotResolvableException {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(uri);

        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        launchIntentForUserClick(context, intent, errorMessage);
    }

}
