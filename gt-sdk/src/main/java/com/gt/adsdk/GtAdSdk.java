package com.gt.adsdk;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.logger.SigmobLog;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class GtAdSdk {

    private String mAppId = "";
    private Context mContext;
    private SdkConfig mSdkConfig;
    private Handler mHandler;
    private static volatile GtAdSdk gInstance;

    public static final AtomicBoolean sHasInit = new AtomicBoolean(false);

    public GtAdSdk() {
        this.mAppId = "";
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public static GtAdSdk sharedAds() {
        if (gInstance == null) {
            synchronized (GtAdSdk.class) {
                if (gInstance == null) {
                    gInstance = new GtAdSdk();
                    SigmobLog.setSdkHandlerLevel(Level.INFO);
                }
            }
        }
        return gInstance;
    }

    /**
     * 请求权限
     **/
    public static void requestPermission(Activity activity) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                boolean read_phone_state = ClientMetadata.isPermissionGranted(activity, READ_PHONE_STATE);
                boolean write_external_storage = ClientMetadata.isPermissionGranted(activity, WRITE_EXTERNAL_STORAGE);
                boolean access_fine_location = ClientMetadata.isPermissionGranted(activity, ACCESS_FINE_LOCATION);

                if (!read_phone_state || !write_external_storage || !access_fine_location) {
                    activity.requestPermissions(new String[]{READ_PHONE_STATE, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION}, PERMISSION_GRANTED);
                }
            }
        }
    }

    public synchronized boolean init(Context context, SdkConfig config) {
        try {
            if (context == null || config == null) {
                throw new RuntimeException("init context or config is null");
            }

            mContext = context;
            mSdkConfig = config;

            sHasInit.set(true);
            if (mSdkConfig.gtInitCallback != null) {
                mSdkConfig.gtInitCallback.onSuccess();
            }
        } catch (Throwable throwable) {
            if (mSdkConfig.gtInitCallback != null) {
                mSdkConfig.gtInitCallback.onFail(0, throwable.getMessage());
            }
        }

        return sHasInit.get();
    }

    public static void setPersonalRecommend(boolean recommend) {

    }
}
