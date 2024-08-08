package com.gt.sdk;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.common.exceptions.CrashHandler;
import com.czhj.sdk.common.json.JSONSerializer;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.admanager.GtConfigManager;
import com.gt.sdk.admanager.GtLifecycleManager;
import com.gt.sdk.admanager.PrivacyDataManager;
import com.gt.sdk.api.GtCustomController;
import com.gt.sdk.base.common.AdStackManager;
import com.gt.sdk.base.models.point.GtPointEntityAd;
import com.gt.sdk.base.models.point.PointCategory;
import com.gt.sdk.base.models.point.GtPointEntityCrash;
import com.gt.sdk.base.models.point.PointType;
import com.gt.sdk.admanager.DeviceContextManager;
import com.gt.sdk.utils.GtSharedPreUtil;
import com.gt.sdk.utils.GtFileUtil;


import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class GtAdSdk {

    private Context mContext;
    private GtSdkConfig mSdkConfig;
    private Handler mHandler;
    private static volatile GtAdSdk gInstance;
    private boolean mIsAdult = true;
    private int mUserAge = 0;
    private boolean mIsPersonalizedAdvertisingOn = true;
    private WindMillUserAgeStatus mAge_restricted = WindMillUserAgeStatus.WindAgeRestrictedStatusUnknown;
    private WindMillConsentStatus mConsentStatus = WindMillConsentStatus.UNKNOWN;
    private WeakReference<Activity> activityWeakReference;

    public static final AtomicBoolean sHasInit = new AtomicBoolean(false);

    public GtAdSdk() {
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

    public GtSdkConfig getSdkConfig() {
        return mSdkConfig;
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

    public synchronized boolean init(Context context, GtSdkConfig config) {
        try {
            if (context == null || config == null) {
                throw new RuntimeException("init context or config is null");
            }

            mContext = context;
            mSdkConfig = config;

            if (!TextUtils.isEmpty(mSdkConfig.getUserId())) {
                ClientMetadata.setUserId(mSdkConfig.getUserId());
            }

            if (config.getGtCustomController() != null) {
                GtCustomController controller = config.getGtCustomController();
                DeviceContextManager.sharedInstance().init(controller);
            } else {
                DeviceContextManager.sharedInstance().init(null);
            }

            GtFileUtil.initSDKCacheFolder(mContext, GtConstants.SDK_FOLDER);

            ClientMetadata.getInstance().initialize(mContext);

            initNetworking(mContext);

            PrivacyDataManager.getInstance().initialize(mContext);

            PrivacyDataManager.getInstance().setAdult(mIsAdult, false);
            PrivacyDataManager.getInstance().setPersonalizedAdvertisingOn(mIsPersonalizedAdvertisingOn, false);
            PrivacyDataManager.getInstance().setUserAge(getUserAge(), false);
            PrivacyDataManager.getInstance().setAge_restricted(getAgeRestrictedStatus().getValue(), false);
            PrivacyDataManager.getInstance().setGDPRConsentStatus(getUserGDPRConsentStatus().getValue(), false);
            PrivacyDataManager.getInstance().setExtGDPRRegion(false);

            GtLifecycleManager.initialize((Application) context.getApplicationContext());

            startServices();

            sHasInit.set(true);
            if (mSdkConfig.getGtInitCallback() != null) {
                mSdkConfig.getGtInitCallback().onSuccess();
            }
        } catch (Throwable throwable) {
            if (mSdkConfig.getGtInitCallback() != null) {
                mSdkConfig.getGtInitCallback().onFail(AdError.ERROR_AD_INIT_FAIL.getErrorCode(), throwable.getMessage());
            }
        }

        return sHasInit.get();
    }

    private void startServices() {
        //初始化埋点
        trackInitEvent();

        PrivacyDataManager.getInstance().setAdult(mIsAdult, true);
        PrivacyDataManager.getInstance().setPersonalizedAdvertisingOn(mIsPersonalizedAdvertisingOn, true);
        PrivacyDataManager.getInstance().setUserAge(getUserAge(), true);
        PrivacyDataManager.getInstance().setAge_restricted(getAgeRestrictedStatus().getValue(), true);

        GtConfigManager.sharedInstance().startUpdate();

        if (GtConfigManager.sharedInstance().isEnable_report_crash()) {
            CrashHandler.getInstance().add(new CrashHandler.CrashHandlerListener() {
                @Override
                public void reportCrash(String crash) {
                    if (!TextUtils.isEmpty(crash) && crash.contains("com.gt.sdk")) {
                        GtPointEntityCrash entityCrash = GtPointEntityCrash.AdCrash(crash);
                        entityCrash.sendServe();
                    }
                }
            });
        }

        ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                clearAdCache();
            }
        });
    }

    private void clearAdCache() {
        AdStackManager.clearNativeAdCache();
        AdStackManager.clearVideoAdCache();
        AdStackManager.clearSplashAd();
        AdStackManager.clearDownloadAPK();
    }

    private void trackInitEvent() {
        GtPointEntityAd entityInit = new GtPointEntityAd();
        entityInit.setAc_type(PointType.GT_COMMON);
        entityInit.setCategory(PointCategory.INIT);
        HashMap<String, String> options = new HashMap<>();
        try {
            if (getCustomData() != null && !getCustomData().isEmpty()) {
                String custom_info = JSONSerializer.Serialize(getCustomData());
                if (!TextUtils.isEmpty(custom_info)) {
                    options.put("custom_data", custom_info);
                    entityInit.setOptions(options);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        options.put("is_minor", mIsAdult ? GtConstants.FAIL : GtConstants.SUCCESS);
        options.put("is_unpersonalized", mIsPersonalizedAdvertisingOn ? GtConstants.FAIL : GtConstants.SUCCESS);

        entityInit.setOptions(options);
        entityInit.commit();
    }

    /**
     * 初始化网络监听
     */
    private static void initNetworking(Context context) {
        Networking.AddSigmobServerURL(GtConfigManager.getConfigUrl());
        Networking.AddSigmobServerURL(GtConfigManager.sharedInstance().getLogUrl());
        Networking.AddSigmobServerURL(GtConfigManager.sharedInstance().getAdUrl());

        Networking.initializeMill(context);
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    public Context getContext() {
        return mContext.getApplicationContext();
    }

    public Activity getActivity() {
        if (activityWeakReference != null) {
            return activityWeakReference.get();
        }
        return null;
    }

    public void setActivity(Activity mActivity) {
        this.activityWeakReference = new WeakReference<>(mActivity);
    }

    public String getAppId() {
        if (mSdkConfig != null) {
            return mSdkConfig.getAppId();
        }
        return "";
    }

    /**
     * 设置年龄
     *
     * @param age
     */
    public void setUserAge(int age) {
        mUserAge = age;
        if (sHasInit.get()) {
            PrivacyDataManager.getInstance().setUserAge(age, true);
        }
    }

    public int getUserAge() {
        try {
            if (mUserAge == 0 && mContext != null) {
                mUserAge = GtSharedPreUtil.getSharedPreferences(mContext).getInt(Constants.USER_AGE, 0);
            }
        } catch (Throwable th) {

        }
        return mUserAge;
    }

    /**
     * 设置coppa,年龄限制用户
     *
     * @param ageRestrictedUserStatus
     */
    public void setIsAgeRestrictedUser(WindMillUserAgeStatus ageRestrictedUserStatus) {
        mAge_restricted = ageRestrictedUserStatus;
        if (sHasInit.get()) {
            PrivacyDataManager.getInstance().setAge_restricted(mAge_restricted.getValue(), true);
        }
    }

    public WindMillUserAgeStatus getAgeRestrictedStatus() {
        try {
            if (mAge_restricted == WindMillUserAgeStatus.WindAgeRestrictedStatusUnknown && mContext != null) {

                int status = GtSharedPreUtil.getSharedPreferences(mContext).getInt(Constants.AGE_RESTRICTED_STATUS, WindMillUserAgeStatus.WindAgeRestrictedStatusUnknown.getValue());

                if (status == WindMillUserAgeStatus.WindAgeRestrictedStatusNO.getValue()) {
                    mAge_restricted = WindMillUserAgeStatus.WindAgeRestrictedStatusNO;
                } else if (status == WindMillUserAgeStatus.WindAgeRestrictedStatusYES.getValue()) {
                    mAge_restricted = WindMillUserAgeStatus.WindAgeRestrictedStatusYES;
                } else {
                    mAge_restricted = WindMillUserAgeStatus.WindAgeRestrictedStatusUnknown;
                }
            }
        } catch (Throwable ignored) {

        }
        return mAge_restricted;
    }


    public boolean isAdult() {
        return mIsAdult;
    }

    /**
     * 设置成年
     *
     * @param isAdult
     */
    public void setAdult(boolean isAdult) {
        mIsAdult = isAdult;
        if (sHasInit.get()) {
            PrivacyDataManager.getInstance().setAdult(mIsAdult, true);
        }
    }

    public boolean isPersonalizedAdvertisingOn() {
        return mIsPersonalizedAdvertisingOn;
    }

    /**
     * 设置个性化广告开关
     *
     * @param isPersonalizedAdvertisingOn
     */
    public void setPersonalizedAdvertisingOn(boolean isPersonalizedAdvertisingOn) {
        mIsPersonalizedAdvertisingOn = isPersonalizedAdvertisingOn;
        if (sHasInit.get()) {
            PrivacyDataManager.getInstance().setPersonalizedAdvertisingOn(mIsPersonalizedAdvertisingOn, true);
        }
    }

    /**
     * user id -1 is invalid value, default value is -1
     *
     * @return
     */
    public static String getUserId() {
        return ClientMetadata.getUserId();
    }

    /**
     * 设置用户id
     *
     * @param userId
     */
    public static void setUserId(String userId) {
        ClientMetadata.setUserId(userId);
    }

    /**
     * 获取版本好
     *
     * @return
     */
    public static String getVersion() {
        return GtConstants.SDK_VERSION;
    }

    private void clearImageCache() {
        ImageManager.with(mContext).clearCache();
    }

    /**
     * 设置gdpr状态
     *
     * @param status
     */
    public void setUserGDPRConsentStatus(WindMillConsentStatus status) {
        mConsentStatus = status;
        if (sHasInit.get()) {
            PrivacyDataManager.getInstance().setGDPRConsentStatus(status.getValue(), true);
        }
    }

    public WindMillConsentStatus getUserGDPRConsentStatus() {
        if (mConsentStatus == WindMillConsentStatus.UNKNOWN && mContext != null) {
            try {
                int status = GtSharedPreUtil.getSharedPreferences(mContext).getInt(Constants.GDPR_CONSENT_STATUS, WindMillConsentStatus.UNKNOWN.getValue());
                if (status == WindMillConsentStatus.ACCEPT.getValue()) {
                    mConsentStatus = WindMillConsentStatus.ACCEPT;
                } else if (status == WindMillConsentStatus.DENIED.getValue()) {
                    mConsentStatus = WindMillConsentStatus.DENIED;
                } else {
                    mConsentStatus = WindMillConsentStatus.UNKNOWN;
                }
            } catch (Throwable th) {

            }
        }
        return mConsentStatus;
    }

    public boolean isInit() {
        return sHasInit.get();
    }

    public Map<String, String> getCustomData() {
        if (mSdkConfig != null) {
            return mSdkConfig.getCustomData();
        }
        return null;
    }
}
