package com.czhj.sdk.common;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowInsets;

import com.czhj.devicehelper.DeviceHelper;
import com.czhj.devicehelper.oaId.helpers.DevicesIDsHelper;
import com.czhj.sdk.common.Database.SQLiteMTAHelper;
import com.czhj.sdk.common.Database.SQLiteTrackHelper;
import com.czhj.sdk.common.ThreadPool.BackgroundThreadFactory;
import com.czhj.sdk.common.ThreadPool.RepeatingHandlerRunnable;
import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.common.models.Config;
import com.czhj.sdk.common.mta.BuriedPointManager;
import com.czhj.sdk.common.network.SigmobRequestUtil;
import com.czhj.sdk.common.track.TrackManager;
import com.czhj.sdk.common.utils.AESUtil;
import com.czhj.sdk.common.utils.AdvertisingId;
import com.czhj.sdk.common.utils.AppPackageUtil;
import com.czhj.sdk.common.utils.DeviceUtils;
import com.czhj.sdk.common.utils.IdentifierManager;
import com.czhj.sdk.common.utils.RomUtils;
import com.czhj.sdk.common.utils.SharedPreferencesUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.tan.mark.TanId;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class ClientMetadata implements IdentifierManager.AdvertisingIdChangeListener {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static String mUserId = "-1";
    private static String bootId;
    /**
     * 0=无法探测当前网络状态; 1=蜂窝数据接入，未知网络类型; 2=2G; 3=3G; 4=4G; 5=5G; 100=Wi-Fi网络接入; 101=以太网接入
     */

    private static volatile ClientMetadata sInstance;
    // Network type constant defined after API 9:
    private Location mLocation;
    private IdentifierManager mIdentifierManager;
    private static String uid;
    private int mInsetBottom;
    private boolean mIsRetryAble = true;
    private String mImei;
    private String mImeiIndex0;
    private String mImeiIndex1;
    private String updateId;
    private Context mContext;
    private boolean enableLocation;

    private String mOaid;
    private String mOAID_SDK;

    private String mUDID;

    private String mOAID_API;
    private String targetSdkVersion;
    private long install_time;
    private int mDeviceHeight;
    private int mDeviceWidth;
    private Display mDefaultDisplay;
    private RepeatingHandlerRunnable repeatingHandlerRunnable;
    private boolean oaidSDKCallbacked;

    public static String getUserId() {
        return TextUtils.isEmpty(mUserId) ? "-1" : mUserId;
    }

    public static void setUserId(String userId) {
        if (!TextUtils.isEmpty(userId)) {
            mUserId = userId;
        }
    }

    public static String getDeviceOsVersion() {
        return DeviceUtils.getDeviceOsVersion();
    }

    public static String getDeviceManufacturer() {
        return DeviceUtils.getDeviceManufacturer();
    }

    public static String getDeviceModel() {
        return DeviceUtils.getDeviceModel();
    }

    public static String getCell_ip() {
        return DeviceUtils.getCell_ip();
    }

    public static Long getBootSystemTime() {
        return DeviceUtils.getBootSystemTime();
    }

    public static Integer getDeviceOSLevel() {
        return DeviceUtils.getDeviceOSLevel();

    }

    public static String getMacAddress() {
        try {
            return DeviceHelper.getMacAddress();
        } catch (Throwable t) {

        }
        return "";
    }

    public static String getCPUInfo() {
        try {
            return DeviceUtils.getCPUInfo();
        } catch (Throwable t) {

        }
        return null;
    }


    public static String getCPUModel() {
        return Build.BOARD;
    }

    public static String getVAID() {
        try {
            return DeviceHelper.getVAID();
        } catch (Throwable t) {

        }
        return null;
    }


    public static PackageInfo getPackageInfoWithUri(Context context, String path) {
        try {
            return context.getPackageManager().getPackageArchiveInfo(path, 0);

        } catch (Throwable e) {
            //TODO: handle exception
        }
        return null;
    }

    /**
     * Can be used by background threads and other objects without a context to attempt to get
     * ClientMetadata. If the object has never been referenced from a thread with a context,
     * this will return null.
     */

    public static ClientMetadata getInstance() {
        if (sInstance == null) {
            synchronized (ClientMetadata.class) {
                if (sInstance == null) sInstance = new ClientMetadata();
            }
        }
        return sInstance;
    }

    public static boolean isRoot() {
        try {
            return DeviceUtils.isRoot();
        } catch (Throwable t) {

        }
        return false;
    }

    public static boolean isEmulator() {
        try {
            return DeviceUtils.isEmulator();
        } catch (Throwable t) {

        }
        return false;
    }

    public String getUDID() {
        if (mUDID != null) {
            return mUDID;
        }

        mUDID = UUID.randomUUID().toString();
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sig_UDID", mUDID);
        editor.apply();
        return mUDID;
    }


    public static boolean isPermissionGranted(final Context context, final String permission) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context != null) {
                return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static void setOAIDCertPem(String certPem) {
        try {
            DevicesIDsHelper.setOAIDCertPem(certPem);
        } catch (Throwable th) {
            SigmobLog.e("not support OAID Module");
        }
    }


    public static void setOaidCertFileName(String fileName) {
        try {

            DevicesIDsHelper.setPemCustomFileName(fileName);
        } catch (Throwable th) {
            SigmobLog.e("not support OAID Module");
        }
    }


    public static Map<String, String> getQueryParamMap(final Uri uri) {

        final Map<String, String> params = new HashMap<>();
        for (final String queryParam : uri.getQueryParameterNames()) {
            params.put(queryParam, TextUtils.join(",", uri.getQueryParameters(queryParam)));
        }

        return params;
    }

    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public void setEnableLocation(boolean enableLocation) {
        this.enableLocation = enableLocation;
    }

    public int getScreenOrientation(Context context) {
        Display display = DeviceUtils.getDisplay(context);

        if (display == null) return Surface.ROTATION_0;

        return display.getRotation();
    }


    private String getOAIDSDK() {

        if (!TextUtils.isEmpty(mOAID_SDK)) {
            return mOAID_SDK;
        }

        try {
            DeviceHelper.getOAID(mContext, new DevicesIDsHelper.AppIdsUpdater() {
                @Override
                public void OnIdsAvalid(String oaid) {

                    if (!TextUtils.isEmpty(oaid)) {

                        mOAID_SDK = oaid;

                        if (!oaid.equalsIgnoreCase(mOaid)) {
                            SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(mContext);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("oaid_aes_gcm", AESUtil.EncryptString(oaid, Constants.AESKEY));
                            editor.apply();
                        }

                    }
                    oaidSDKCallbacked = true;

                }
            });
        } catch (Throwable th) {

        }
        return mOAID_SDK;

    }

    public String getOAID_API() {
        try {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P && !Config.sharedInstance().getOaidApiDisable()) {

                if (!TextUtils.isEmpty(mOAID_API)) {
                    return mOAID_API;
                }
                DeviceHelper.getOAID_API(mContext, new DevicesIDsHelper.AppIdsUpdater() {
                    @Override
                    public void OnIdsAvalid(String oaid) {
                        if (!TextUtils.isEmpty(oaid)) {

                            if (oaid.equalsIgnoreCase(mOAID_API)) return;

                            mOAID_API = oaid;
                            if (!oaid.equalsIgnoreCase(mOaid)) {
                                SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(mContext);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("oaid_aes_gcm", AESUtil.EncryptString(oaid, Constants.AESKEY));
                                editor.apply();
                            }

                        }
                    }
                });
            }

        } catch (Throwable t) {

        }
        return null;
    }

    public String getAndroidId() {
        try {

            return DeviceUtils.getAndroidId(mContext);
        } catch (Throwable th) {

        }
        return null;
    }

    public String getApkSha1() {
        try {

            return DeviceUtils.getApkSha1OrMd5(mContext, "SHA1");
        } catch (Throwable th) {

        }
        return null;
    }

    public String getApkMd5() {
        try {

            return DeviceUtils.getApkSha1OrMd5(mContext, "MD5");
        } catch (Throwable th) {

        }
        return null;
    }

    public String getAdvertisingId() {
        try {
            if (Constants.GOOGLE_PLAY) {
                return mIdentifierManager.getAdvertisingInfo().mAdvertisingId;
            }
        } catch (Throwable t) {

        }
        return null;
    }

    public boolean getLimitAdTrackingEnabled() {
        try {
            if (Constants.GOOGLE_PLAY) {
                return mIdentifierManager.getAdvertisingInfo().mDoNotTrack;
            }
        } catch (Throwable t) {

        }
        return false;
    }

    public String getDeviceId() {
        try {

            return getDeviceId(-1);
        } catch (Throwable t) {

            SigmobLog.e("getDeviceId:" + t.getMessage());
        }
        return null;
    }

    public synchronized String getDeviceId(int index) {
        try {

            if (TextUtils.isEmpty(mImei) && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

                if (!DeviceUtils.isCanUsePhoneState(mContext) || !DeviceUtils.isCanRetryIMEI())
                    return null;
                mImei = DeviceHelper.getIMEI(mContext);
                mImeiIndex0 = DeviceHelper.getIMEI(mContext, 0);
                mImeiIndex1 = DeviceHelper.getIMEI(mContext, 1);
            }

            if (index == -1) {
                return mImei;
            } else if (index == 0) {
                return mImeiIndex0;
            } else {
                return mImeiIndex1;
            }
        } catch (Throwable t) {

            SigmobLog.e("getDeviceId:" + t.getMessage());
        }
        return null;
    }

    public String getBootId() {

        try {
            if (Config.sharedInstance().isDisableBootMark()) return "";

            if (TextUtils.isEmpty(bootId)) {
                byte[] bootFromJNI = TanId.getBootFromJNI();

                if (bootFromJNI != null && bootFromJNI.length > 0) {
                    bootId = new String(bootFromJNI);
                }

                SigmobLog.i("origin bootId:" + bootId);

                if (!TextUtils.isEmpty(bootId)) {
                    bootId = bootId.replaceAll("\\s*|\t|\r|\n", "");
                    if (bootId.length() > 36) {
                        bootId = bootId.substring(0, 36);
                    }
                }

                SigmobLog.i("bootId:" + bootId);
            }
        } catch (Throwable t) {
            SigmobLog.i("getBootId:" + t.getMessage());
        }

        return bootId;
    }

    public String getUpdateId() {

        try {
            if (Config.sharedInstance().isDisableBootMark()) return "";

            if (TextUtils.isEmpty(updateId)) {
                updateId = TanId.getUpdateFromJNI();
                SigmobLog.i("updateId:" + updateId);
            }
        } catch (Throwable t) {
            SigmobLog.i("getUpdateId:" + t.getMessage());
        }
        return updateId;
    }

    public String getDeviceSerial() {
        try {
            return DeviceUtils.getDeviceSerial();
        } catch (Throwable t) {

        }
        return null;
    }

    public String getIMSI() {
        try {
            return DeviceHelper.getIMSI(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public static String getDeviceBrand() {
        return DeviceUtils.getDeviceBrand();
    }

    public boolean isTablet() {
        try {
            return DeviceUtils.isTablet(mContext);
        } catch (Throwable t) {

        }
        return false;
    }

    public Float getBatteryLevel() {
        try {
            return DeviceUtils.getBatteryLevel(mContext);
        } catch (Throwable t) {

        }
        return 0.0f;
    }

    public Integer getBatteryState() {
        try {
            return DeviceUtils.getBatteryState(mContext);
        } catch (Throwable t) {

        }
        return 0;
    }

    public Boolean getBatterySaveEnable() {

        try {
            return DeviceUtils.getBatterySaveEnable(mContext);
        } catch (Throwable t) {

        }
        return false;
    }

    public int getDensityDpi() {
        try {
            return (int) DeviceUtils.getDensityDpi(mContext);
        } catch (Throwable t) {

        }
        return 0;
    }

    public Integer getDeviceScreenHeightDip() {
        try {
            return (int) DeviceUtils.getDeviceScreenHeightDip(mContext);
        } catch (Throwable t) {

        }
        return 0;
    }

    public Integer getDeviceScreenWidthDip() {
        try {
            return (int) DeviceUtils.getDeviceScreenWidthDip(mContext);
        } catch (Throwable t) {

        }
        return 0;
    }

    public String getSDCardPath() {
        try {
            return DeviceUtils.getSDCardPath(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public Integer getDeviceScreenRealHeightDip() {
        try {
            return DeviceUtils.getDeviceScreenRealHeightDip(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public Integer getDeviceScreenRealWidthDip() {
        try {
            return DeviceUtils.getDeviceScreenRealWidthDip(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public String getTargetSdkVersion() {
        try {
            if (!TextUtils.isEmpty(targetSdkVersion)) return targetSdkVersion;
            targetSdkVersion = String.valueOf(mContext.getApplicationInfo().targetSdkVersion);
            return targetSdkVersion;
        } catch (Throwable t) {

        }
        return null;
    }

    public String getAppPackageName() {
        try {
            return AppPackageUtil.getAppPackageName(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public Integer getOrientationInt() {
        try {
            return DeviceUtils.getOrientationInt(mContext);
        } catch (Throwable t) {

        }
        return 0;
    }

    public String getAppVersion() {
        try {
            return AppPackageUtil.getAppVersionFromContext(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public int getActiveNetworkType() {
        try {
            return DeviceUtils.getActiveNetworkType().getId();
        } catch (Throwable t) {

        }
        return DeviceUtils.NetworkType.UNKNOWN.getId();
    }

    public String getNetworkOperatorForUrl() {
        try {
            return DeviceUtils.getNetworkOperatorForUrl(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public String getNetworkOperatorName() {
        try {
            return DeviceUtils.getNetworkOperatorName(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public String getWifimac() {
        try {
            return DeviceHelper.getWifimac(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public String getWifiName() {
        try {
            return DeviceHelper.getWifiName(mContext);
        } catch (Throwable t) {

        }
        return null;
    }


    public String getBlueToothName() {
        try {
            return DeviceUtils.getBlueToothName(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public Locale getDeviceLocale() {
        try {
            return DeviceUtils.getDeviceLocale(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public String getOAID() {


        int disable_up_oAid = Config.sharedInstance().getDisable_up_OAid();
        boolean oaidApiDisable = Config.sharedInstance().getOaidApiDisable();

        if ((disable_up_oAid < 0 || disable_up_oAid > 1) && oaidApiDisable) {
            return null;
        }

        String oaid = getOAID_SDK();

        if (TextUtils.isEmpty(oaid) && oaidSDKCallbacked ) {
            oaid = getOAID_API();
        }

        if (!TextUtils.isEmpty(oaid) && !oaid.equalsIgnoreCase(mOaid)) {
            mOaid = oaid;
            return oaid;
        }

        return mOaid;

    }

    public String getOAID_SDK() {
        try {

            int disable_up_oAid = Config.sharedInstance().getDisable_up_OAid();

            switch (disable_up_oAid) {
                case 0:
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        return getOAIDSDK();
                    }
                    break;
                case 1:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//不禁止上传并且大于23
                        return getOAIDSDK();
                    }
                    break;
//                case 2://禁止上传
//                    return null;
            }

        } catch (Throwable t) {

        }
        return null;
    }

    public DisplayMetrics getRealMetrics() {

        try {
            return DeviceUtils.getRealMetrics(mContext);
        } catch (Throwable t) {


        }
        return DeviceUtils.getDisplayMetrics(mContext);
    }

    @Override
    public void onIdChanged(AdvertisingId oldId, AdvertisingId newId) {
    }

    public static String getUid() {
        return uid;
    }

    // Cached client metadata used for generating URLs and events.

    public void setUid(String uid) {

        try {
            if (!TextUtils.isEmpty(uid) && (TextUtils.isEmpty(this.uid) || !uid.equalsIgnoreCase(this.uid))) {
                this.uid = uid;
                SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("uid_aes_gcm", AESUtil.EncryptString(uid, Constants.AESKEY));
                editor.apply();
            }
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

    }

    public void setWindInsets(WindowInsets insets) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if (insets != null && insets.isRound()) {
                mInsetBottom = insets.getSystemWindowInsetBottom();
            }
        }
    }

    public int getInsetBottom() {
        return mInsetBottom;
    }

    public boolean isRetryAble() {
        return mIsRetryAble;
    }

    public void setRetryAble(boolean retryAble) {
        mIsRetryAble = retryAble;
    }

    public String getStringResources(String name, String defaultString) {
        if (mContext != null) {
            Resources res = mContext.getResources();
            if (res != null) {
                int resId = res.getIdentifier(name, "string", mContext.getPackageName());

                if (resId != 0) {
                    return res.getString(resId);
                } else {
                    return defaultString;
                }
            }

        }
        return defaultString;
    }

    public int getStyleResources(String name) {
        int resId = 0;
        if (mContext != null) {
            Resources res = mContext.getResources();
            if (res != null) {
                resId = res.getIdentifier(name, "style", mContext.getPackageName());
            }
        }
        return resId;
    }

    public String getStringResources(String name, String defaultString, Object... formatArgs) {
        if (mContext != null) {
            Resources res = mContext.getResources();
            if (res != null) {
                int resId = res.getIdentifier(name, "string", mContext.getPackageName());
                if (resId != 0) {
                    SigmobLog.d("getStringResources resid" + resId);
                    return res.getString(resId, formatArgs);
                } else {
                    return String.format(defaultString, formatArgs);
                }
            }

        }
        return defaultString;
    }

    /**
     * Returns the singleton ClientMetadata object, using the context to obtain data if necessary.
     */
    public synchronized void initialize(Context context) {
        // Use a local variable so we can reduce accesses of the volatile field.
        if (mContext == null) {
            mContext = context.getApplicationContext();

            install_time = SharedPreferencesUtil.getSharedPreferences(mContext).getLong("install_time", 0);


            if (install_time == 0) {
                PackageInfo packageInfo = AppPackageUtil.getPackageInfo(context);

                if (packageInfo == null) {
                    install_time = System.currentTimeMillis() / 1000;
                } else {
                    install_time = packageInfo.firstInstallTime / 1000;
                }

                SharedPreferences.Editor edit = SharedPreferencesUtil.getSharedPreferences(mContext).edit();
                edit.putLong("install_time", install_time);
                edit.apply();
            }

            mUDID = SharedPreferencesUtil.getSharedPreferences(mContext).getString("sig_UDID", null);

            String uid_aes = SharedPreferencesUtil.getSharedPreferences(mContext).getString("uid_aes_gcm", null);

            if (uid_aes != null) {
                uid = AESUtil.DecryptString(uid_aes, Constants.AESKEY);
            } else {
                uid = SharedPreferencesUtil.getSharedPreferences(mContext).getString("uid", null);

                if (uid != null) {
                    SharedPreferences.Editor edit = SharedPreferencesUtil.getSharedPreferences(mContext).edit();
                    edit.remove("uid");
                    edit.putString("uid_aes_gcm", AESUtil.EncryptString(uid, Constants.AESKEY));
                    edit.apply();
                }
            }

            String oaid_aes = SharedPreferencesUtil.getSharedPreferences(mContext).getString("oaid_aes_gcm", null);
            if (oaid_aes != null) {
                mOaid = AESUtil.DecryptString(oaid_aes, Constants.AESKEY);
            } else {
                mOaid = SharedPreferencesUtil.getSharedPreferences(mContext).getString("oaid", null);
                if (mOaid != null) {
                    SharedPreferences.Editor edit = SharedPreferencesUtil.getSharedPreferences(mContext).edit();
                    edit.remove("oaid");
                    edit.putString("oaid_aes_gcm", AESUtil.EncryptString(mOaid, Constants.AESKEY));
                    edit.apply();
                }
            }

//            try {
//                getOAID();
//            } catch (Throwable th) {
//
//            }
            mIdentifierManager = new IdentifierManager(mContext, this);
            SQLiteMTAHelper.initialize(mContext);
            SQLiteTrackHelper.initialize(mContext);
            BuriedPointManager.getInstance().start();
            DeviceUtils.registerNetworkChange(mContext);

            TrackManager.getInstance().startRetryTracking();
            ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    RomUtils.getRomInfo();
                }
            });
        }
    }

    public Context getContext() {
        return mContext;
    }

    public String getDeviceName() {
        try {
            return DeviceUtils.getDeviceName(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public Long getSystemTotalMemorySize() {
        try {
            return DeviceUtils.getSysteTotalMemorySize(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public DisplayMetrics getDisplayMetrics() {
        try {
            return DeviceUtils.getDisplayMetrics(mContext);
        } catch (Throwable t) {

        }
        return null;
    }

    public boolean isNetworkConnected(String configUrl) {
        try {
            URL url = new URL(configUrl);
            return SigmobRequestUtil.isConnection(url.getHost());

        } catch (Throwable throwable) {
        }
        return false;
    }

    public String getRotation() {
        try {
            return DeviceUtils.getRotation(mContext);
        } catch (Throwable throwable) {
        }
        return null;
    }


    DownloadManager getDownloadManager() {
        DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

        return manager;
    }


    private boolean isNoOptions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = null;
        intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);


        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public LocationManager getLocationManager() {
        if (!Constants.GOOGLE_PLAY) {
            try {

                if (!DeviceUtils.isCanUseLocation(mContext)) return null;

                return (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            } catch (Throwable throwable) {
                SigmobLog.e(throwable.getMessage());
            }
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {


            if (!enableLocation) {
                return null;
            }

            if (mLocation != null) {
                return mLocation;
            }

            LocationManager locationManager = getLocationManager();
            if (locationManager != null && DeviceUtils.isCanRetryLocation()) {

                SigmobLog.d("private :use_location ");
                Location l = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                if (l != null) {
                    // Found best last known location: %s", l);
                    mLocation = l;
                }
            }

        } catch (Exception e) {
            //TODO: handle exception
        }

        return mLocation;
    }

    public void setLocation(Location location) {

        mLocation = location;
    }

//    public RefWatcher getRefWatcher(Context context) {
//
//        if (!Constants.IS_TEST) return null;
//
//        try {
//            Field field = context.getClass().getDeclaredField("refWatcher");
//            field.setAccessible(true);
//            RefWatcher refWatcher = (RefWatcher) field.get(context);
//
//            return refWatcher;
//        } catch (NoSuchFieldException e) {
//             SigmobLog.e(e.getMessage());
//        } catch (IllegalAccessException e) {
//             SigmobLog.e(e.getMessage());
//        } catch (Throwable e) {
//             SigmobLog.e(e.getMessage());
//        }
//        return null;
//    }

    public String getPermission(Context context) {
        String permissionReq = "";
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo pack = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] permissionStrings = pack.requestedPermissions;
            for (int i = 0; i < permissionStrings.length; i++) {
                boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission(permissionStrings[i], packageName));
                if (permission) {
                    if (i == permissionStrings.length - 1) {
                        permissionReq = permissionReq + permissionStrings[i];
                    } else {
                        permissionReq = permissionReq + permissionStrings[i] + ",";
                    }
                }
            }
            SigmobLog.d("permissionReq:" + permissionReq);
            if (!TextUtils.isEmpty(permissionReq)) {
                return Base64.encodeToString(permissionReq.getBytes(), Base64.NO_WRAP);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return permissionReq;
    }

    /**
     * SD卡判断
     *
     * @return
     */
    public boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

//
//    @SuppressLint("MissingPermission")
//    ClientMetadata(Context context) {
//        mContext = context.getApplicationContext();
//        uid = SharedPreferencesUtil.getSharedPreferences(mContext).getString("uid", null);
//        mIdentifierManager = new IdentifierManager(mContext, this);
//    }

    /**
     * public String getAdvertisingId(){
     * return  mIdentifierManager.getAdvertisingInfo().mAdvertisingId;
     * }
     * /**
     *
     * @return the name of the application the SDK is included in.
     */

    public long getInstallTime() {
        return install_time;
    }

    public String getAppName() {
        return AppPackageUtil.getAppName(mContext);
    }


    public enum ForceOrientation {
        FORCE_PORTRAIT("portrait"), FORCE_LANDSCAPE("landscape"), DEVICE_ORIENTATION("device"), UNDEFINED("");


        private final String mKey;

        ForceOrientation(final String key) {
            mKey = key;
        }

    }
}