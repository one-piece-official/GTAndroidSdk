package com.sigmob.sdk.base;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.models.Config;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.network.SigmobRequestQueue;
import com.czhj.sdk.common.track.TrackManager;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.VolleyError;
import com.czhj.wire.Wire;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.models.config.SigmobAndroid;
import com.sigmob.sdk.base.models.config.SigmobCommon;
import com.sigmob.sdk.base.models.config.SigmobCommonEndpointsConfig;
import com.sigmob.sdk.base.models.config.SigmobDialogSetting;
import com.sigmob.sdk.base.models.config.SigmobNativeConfig;
import com.sigmob.sdk.base.models.config.SigmobRvConfig;
import com.sigmob.sdk.base.models.config.SigmobSdkConfig;
import com.sigmob.sdk.base.models.config.SigmobSdkConfigResponse;
import com.sigmob.sdk.base.models.config.SigmobSplashConfig;
import com.sigmob.sdk.base.network.SDKConfigRequest;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.sdk.base.utils.WindPrefsUtils;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

public class WindSDKConfig {
    private static final String SDK_CONFIG_VER = "sdkConfigVer";
    private static WindSDKConfig gInstance;
    private static boolean mIsGDPRRegion = false;
    private final Handler mHandler;
    private final Runnable mRefreshRunnable;
    private final String ver = WindConstants.SDK_VERSION;
    private boolean mCurrentAutoRefreshStatus;
    private long mRefreshTimeMillis = 0;
    private SigmobSdkConfig mDefaultSigmobSDKConfig = null;
    private SigmobSdkConfig mSigmobSdkConfig = null;
    private OnSDKUpdateListener mOnSDKUpdateListener;

    private static String backupConfigUrl = null;

    private static boolean isRetryed = false;

    private WindSDKConfig() {
        mHandler = new Handler(Looper.getMainLooper());

        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                scheduleRefreshSDKConfigRunnable();
            }
        };
        mCurrentAutoRefreshStatus = true;
        initDefaultSDKConfig();
    }

    public static synchronized WindSDKConfig getInstance() {
        if (gInstance == null) {
            gInstance = new WindSDKConfig();
        }
        return gInstance;
    }

    public static String sigmobServerQueryString() {

        String sdkVersion = "sdkVersion=" + WindConstants.SDK_VERSION;

        return "appId=" + WindAds.sharedAds().getAppId() + "&" + sdkVersion;

    }

    public static String getConfigUrl() {

        if (!TextUtils.isEmpty(backupConfigUrl)){
            if (backupConfigUrl.indexOf('?') == -1)
                return backupConfigUrl + "?" + sigmobServerQueryString();
            else {
                return backupConfigUrl + "&" + sigmobServerQueryString();
            }
        }

        WindAdOptions options = WindAds.sharedAds().getOptions();
        if (options != null && options.getExtData() != null) {
            String s = options.getExtData().get(WindConstants.SIGDEMO_CONF_URL);
            if (s != null && !TextUtils.isEmpty(s)) {
                if (s.indexOf('?') == -1)
                    return s + "?" + sigmobServerQueryString();
                else {
                    return s + "&" + sigmobServerQueryString();
                }
            }
        }
        String configUrl = new StringBuilder(Constants.HTTPS).append("://").append(WindConstants.CONFIG_URL).toString();

        if (!TextUtils.isEmpty(configUrl)) {

            if (configUrl.indexOf('?') == -1)
                return configUrl + "?" + sigmobServerQueryString();
            else {
                return configUrl + "&" + sigmobServerQueryString();
            }
        }
        return  new StringBuilder(Constants.HTTPS).append("://").append(WindConstants.CONFIG_URL).append("?").append(sigmobServerQueryString()).toString();
    }

    public static String getGDPRRegionURL() {
        return new StringBuilder(Constants.HTTPS).append("://").append(WindConstants.EXT_CONFIG_URL).append("?").append(sigmobServerQueryString()).toString();
    }

    private void initDefaultSDKConfig() {

        if (mDefaultSigmobSDKConfig == null) {

            SigmobSdkConfig.Builder sdkConfigBuilder = new SigmobSdkConfig.Builder();

            SigmobCommon.Builder comBuilder = new SigmobCommon.Builder();

            SigmobAndroid.Builder androidBuilder = new SigmobAndroid.Builder();

            //setup sdk_endPoints builder
            SigmobCommonEndpointsConfig.Builder endPointsBuilder = new SigmobCommonEndpointsConfig.Builder();
            endPointsBuilder.native_ad(getAdsUrl());

            comBuilder.endpoints(endPointsBuilder.build());

            //setup rv builder
            SigmobRvConfig.Builder rvBuilder = new SigmobRvConfig.Builder();
            rvBuilder.cacheTop(4);

            comBuilder.rv_config(rvBuilder.build());

            //setup rv builder
            comBuilder.configRefresh(1000);
            comBuilder.disable_up_location(true);
            comBuilder.is_gdpr_region(false);
            comBuilder.enable_debug_level(false);

            androidBuilder.disable_boot_mark(true);
            androidBuilder.disableUpAppInfo(true);
            androidBuilder.oaid_api_is_disable(true);
            androidBuilder.enable_permission(false);
            androidBuilder.enable_report_crash(false);

            // build
            sdkConfigBuilder.common_config(comBuilder.build());

            sdkConfigBuilder.android_config(androidBuilder.build());

            mDefaultSigmobSDKConfig = sdkConfigBuilder.build();
        }
        SigmobLog.d("Default config: " + mDefaultSigmobSDKConfig.toString());

    }

    public void loadFromFile() {

//        handleUpdateConfig(mDefaultSigmobSDKConfig);
        SharedPreferences sp = WindPrefsUtils.getPrefs();

        String sdkConfigVer = sp.getString(SDK_CONFIG_VER, null);

        ObjectInputStream objectInputStream = null;

        File file = new File(SigmobFileUtil.getCachePath() + "/config");
        if (ver.equals(sdkConfigVer) && file.exists()) {
            try {
                objectInputStream = new ObjectInputStream(new FileInputStream(file));

                SigmobSdkConfig sigmobSdkConfig = SigmobSdkConfig.ADAPTER.decode(objectInputStream);
                handleUpdateConfig(sigmobSdkConfig, false);

            } catch (Throwable e) {

                handleUpdateConfig(mDefaultSigmobSDKConfig, false);
                SigmobLog.e(e.getMessage());
            } finally {
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (Throwable e) {
                        SigmobLog.e(e.getMessage());
                    }
                }
            }
        } else {
            handleUpdateConfig(mDefaultSigmobSDKConfig, false);
        }

    }

    private void saveToFile(SigmobSdkConfig sigmobSdkConfig) {
        if (sigmobSdkConfig != null) {

            ObjectOutputStream out = null;

            File file = new File(SigmobFileUtil.getCachePath() + "/config");
            if (file.exists()) {
                file.delete();
            }
            file.getParentFile().mkdirs();

            try {
                out = new ObjectOutputStream(new FileOutputStream(file));
                sigmobSdkConfig.encode(out);
                SharedPreferences sp = WindPrefsUtils.getPrefs();
                sp.edit().putString(SDK_CONFIG_VER, ver).apply();

            } catch (IOException e) {
                SigmobLog.e(e.getMessage());
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        SigmobLog.e(e.getMessage());
                    }
                }
            }

        }

    }

    private void scheduleRefreshSDKConfigRunnable() {

        if (SDKContext.getApplicationContext() == null) return;

        if (!ClientMetadata.getInstance().isNetworkConnected(getConfigUrl()) || !PrivacyManager.getInstance().canCollectPersonalInformation()) {
            SigmobLog.d("Can't load an ad because there is no network connectivity.");
            scheduleRefreshTimerIfEnabled();
            return;
        }

        loadConfig();

    }

    public void startUpdate() {

        cancelRefreshTimer();
        mHandler.post(mRefreshRunnable);

    }

    private void handleUpdateConfig(SigmobSdkConfig config, boolean isOnline) {

        if (config != null && config.sigmobCommon_config != null) {
            mSigmobSdkConfig = config;
//            mIsGDPRRegion = mSigmobSdkConfig.sigmobCommon_config.is_gdpr_region;//unused by wjz;
            mRefreshTimeMillis = Wire.get(mSigmobSdkConfig.sigmobCommon_config.configRefresh, 1800) * 1000;

            SigmobLog.d("config: " + config.toString());
            if (mOnSDKUpdateListener != null) {
                mOnSDKUpdateListener.onUpdate(isOnline);
            }

            Config.sharedInstance().setEnable_okhttp3(isEnableOkHttp3());
            Config.sharedInstance().setNetworkTimeout(getNetworkTimeout());
            Config.sharedInstance().update(mIsGDPRRegion, isDisableBootMark(), getOaidApiDisable(), getDisable_up_OAid(), getLogUrl(), getSendLogInterval(), getMaxSendLogRecords(), getlogEnc());
            TrackManager.getInstance().setRetryInterval(getAdTrackerRetryInterval());
            TrackManager.getInstance().setRetryExpiredTime(getADTrackerExpiredTime());
        }
    }

    private boolean getlogEnc() {
        SigmobCommon sigmobCommonConfig = getCommonConfig();
        if (sigmobCommonConfig != null) {
            return Wire.get(sigmobCommonConfig.log_enc, false);
        }
        return false;
    }

    public boolean isFeedback_debug() {
        SigmobCommon sigmobCommonConfig = getCommonConfig();
        if (sigmobCommonConfig != null) {
            return Wire.get(sigmobCommonConfig.feedback_debug, false);
        }
        return false;
    }

    public boolean isLockPlay() {
        SigmobAndroid sdkConfig = getAndroidConfig();
        if (sdkConfig != null) {
            return Wire.get(sdkConfig.lock_play, false);
        }
        return false;
    }
    public boolean isScreenKeep() {
        SigmobAndroid sdkConfig = getAndroidConfig();
        if (sdkConfig != null) {
            return Wire.get(sdkConfig.screen_keep, false);
        }
        return false;
    }

    private void loadConfig() {

        SigmobRequestQueue queue = Networking.getSigRequestQueue();

        SDKConfigRequest request = new SDKConfigRequest(getConfigUrl(), new SDKConfigRequest.Listener() {
            @Override
            public void onSuccess(SigmobSdkConfigResponse sdkConfigResponse) {

                isRetryed = false;
                SigmobLog.d(sdkConfigResponse.toString());
                final SigmobSdkConfig config = sdkConfigResponse.config;
                if (config != null) {
                    WindAds.sharedAds().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            handleUpdateConfig(config, true);
                        }
                    });

                    saveToFile(config);
                } else {
                    SigmobLog.e(sdkConfigResponse.error_message);
                }
                scheduleRefreshTimerIfEnabled();

            }

            @Override
            public void onErrorResponse(VolleyError error) {


                backupConfigUrl = getBackupConfigUrl();

                if (isRetryed){
                    scheduleRefreshTimerIfEnabled();
                }else{
                    isRetryed = true;
                    mHandler.post(mRefreshRunnable);
                }

                SigmobLog.e(error.toString());
            }
        });

        if (queue == null) {
            SigmobLog.e("queue is null");
            scheduleRefreshTimerIfEnabled();
            return;
        }

        queue.add(request);

        SigmobLog.i("update sdk config");
    }

    private String getBackupConfigUrl(){
        SigmobCommon commonConfig = getCommonConfig();
        if (commonConfig != null && commonConfig.endpoints != null){
            String default_backup_url = new StringBuilder(Constants.HTTPS).append("://").append(WindConstants.DEFAULT_BACKUP_CONFIG_URL).toString();

            return Wire.get(commonConfig.endpoints.config, default_backup_url);
        }
        return "";
    }

    private int getNetworkTimeout() {
        SigmobCommon sdkConfig = getCommonConfig();
        if (sdkConfig != null) {
            return Wire.get(sdkConfig.network_timeout, 0);
        }
        return 0;
    }
    private void cancelRefreshTimer() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    private void scheduleRefreshTimerIfEnabled() {
        cancelRefreshTimer();
        if (mCurrentAutoRefreshStatus) {
            long MIN_REFRESH_TIME_MILLIS = 30 * 1000;
            mHandler.postDelayed(mRefreshRunnable, Math.max(MIN_REFRESH_TIME_MILLIS, mRefreshTimeMillis));
        }
    }


    public static boolean isGDPRRegion() {

        return mIsGDPRRegion;
    }


    public SigmobSdkConfig getSdkConfig() {
        return mSigmobSdkConfig == null ? mDefaultSigmobSDKConfig : mSigmobSdkConfig;
    }

    public SigmobCommon getCommonConfig() {
        if (getSdkConfig() == null) {
            return null;
        }
        return getSdkConfig().sigmobCommon_config;
    }

    public SigmobAndroid getAndroidConfig() {
        if (getSdkConfig() == null) {
            return null;
        }
        return getSdkConfig().sigmobAndroid_config;
    }


////////////////////////////////////////SigmobCommon///////////////////////////////////////

    public String getLogUrl() {

        String url = getCommonConfig().endpoints.log;

        if (TextUtils.isEmpty(url)) {
            url = new StringBuilder(Constants.HTTPS).append("://").append(WindConstants.LOG_URL).toString();
        }

        if (url.indexOf('?') == -1)
            return url + "?" + sigmobServerQueryString();
        else {
            return url + "&" + sigmobServerQueryString();
        }
    }

    public String getFeedbackUrl() {
        String default_feedback_url = new StringBuilder(Constants.HTTPS).append("://").append(WindConstants.FEEDBACK_URL).toString();
        String feedback = Wire.get(getCommonConfig().endpoints.feedback, default_feedback_url);
        if (!TextUtils.isEmpty(feedback)) {
            if (feedback.indexOf('?') == -1) {
                return feedback + "?" + sigmobServerQueryString();
            } else {
                return feedback + "&" + sigmobServerQueryString();
            }
        }
        return feedback;

    }

    public String getAdsUrl() {

        String url = "";
        SigmobCommon commonConfig = getCommonConfig();
        if (commonConfig != null){
            SigmobCommonEndpointsConfig endpoints = commonConfig.endpoints;
            if (endpoints != null){
                url = endpoints.ads;
            }
        }

        if (TextUtils.isEmpty(url)) {
            url = new StringBuilder(Constants.HTTPS).append("://").append(WindConstants.ADS_URL).toString();
        }

        if (url.indexOf('?') == -1) {
            return url + "?" + sigmobServerQueryString();
        } else {
            return url + "&" + sigmobServerQueryString();
        }
    }

    public String getHbAdsUrl() {

        String url = getCommonConfig().endpoints.hb_ads;

        if (TextUtils.isEmpty(url)) {
            url = new StringBuilder(Constants.HTTPS).append("://").append(WindConstants.HB_ADS_URL).toString();
        }

        if (url.indexOf('?') == -1) {
            return url + "?" + sigmobServerQueryString();
        } else {
            return url + "&" + sigmobServerQueryString();
        }
    }

    public SigmobRvConfig getRvConfig() {
        if (getCommonConfig() != null) {
            return getCommonConfig().rv_config;
        }
        return null;
    }

    public int getCacheTop() {
        if (getRvConfig() != null) {
            return Wire.get(getRvConfig().cacheTop, 5);
        }
        return 5;
    }

    public long loadRvAdTimeout() {
        if (getRvConfig() != null) {
            int time = Wire.get(getRvConfig().ad_load_timeout, 45);
            if (time < 10) {
                time = 10;
            }
            return time * 1000;
        }
        return 45 * 1000;

    }

    public SigmobDialogSetting getCloseDialogSetting() {
        if (getRvConfig() != null) {
            return getRvConfig().close_dialog_setting;
        }
        return null;
    }

    private SigmobSplashConfig getSplashConfig() {
        if (getCommonConfig() != null) {
            return getCommonConfig().splash_config;
        }
        return null;
    }

    public int getSplashCacheTop() {

        if (getSplashConfig() == null) return 50;

        return Wire.get(getSplashConfig().cacheTop, 50);

    }

    public long getSplashExpiredTime() {
        int material_expired_time = 2;

        if (getSplashConfig() != null) {
            material_expired_time = Wire.get(getSplashConfig().material_expired_time, 2);
        }

        if (material_expired_time < 0) {
            return material_expired_time;
        }
        return (long) material_expired_time * 24 * 60 * 60 * 1000;
    }

    private SigmobNativeConfig getNativeConfig() {
        if (getCommonConfig() != null) {
            return getCommonConfig().native_config;
        }
        return null;
    }

    public int getNativeAdCacheTop() {
        if (getNativeConfig() != null) {
            return Wire.get(getNativeConfig().cacheTop, 50);
        }
        return 50;
    }

    public long loadNativeAdTimeout() {
        SigmobNativeConfig nativeConfig = getNativeConfig();
        if (nativeConfig != null) {
            int time = Wire.get(nativeConfig.ad_load_timeout, 45);
            if (time < 10) {
                time = 10;
            }
            return time * 1000;
        }

        return 45 * 1000;

    }


    public boolean enableAntiFraud() {
        return getCommonConfig() != null && getCommonConfig().anti_fraud_log != null && getCommonConfig().anti_fraud_log.events != null;
    }

    public boolean filterAntiEvent(String key) {
        if (enableAntiFraud() && getCommonConfig().anti_fraud_log.events != null) {
            return getCommonConfig().anti_fraud_log.events.contains(key);
        }
        return false;
    }

    public int getMotionInterval() {
        if (enableAntiFraud() && getCommonConfig().anti_fraud_log.motion_config != null) {
            return Wire.get(getCommonConfig().anti_fraud_log.motion_config.interval, 0);
        }
        return 0;
    }

    public int getMotionCount() {
        if (enableAntiFraud() && getCommonConfig().anti_fraud_log.motion_config != null) {
            return Wire.get(getCommonConfig().anti_fraud_log.motion_config.count, 0);
        }
        return 0;
    }

    public int getMotionQueueMax() {
        if (enableAntiFraud() && getCommonConfig().anti_fraud_log.motion_config != null) {
            if (getCommonConfig().anti_fraud_log.motion_config.queue_max - 50 < getCommonConfig().anti_fraud_log.motion_config.count * 2) {
                return Wire.get(getCommonConfig().anti_fraud_log.motion_config.count, 0) * 2 + 50;
            } else {
                return Wire.get(getCommonConfig().anti_fraud_log.motion_config.queue_max, 0);
            }
        }

        return 0;
    }

    public long getADTrackerExpiredTime() {
        long tracking_expiration_time = 60 * 60 * 24;
        if (getCommonConfig() != null) {
            tracking_expiration_time = Wire.get(getCommonConfig().tracking_expiration_time, 60 * 60 * 24);
        }
        if (tracking_expiration_time < 180) {
            tracking_expiration_time = 180;
        }
        return tracking_expiration_time;
    }

    public int getAdTrackerRetryInterval() {
        int tracking_retry_interval = 180;

        if (getCommonConfig() != null) {
            tracking_retry_interval = Wire.get(getCommonConfig().tracking_retry_interval, 180);
        }

        if (tracking_retry_interval < 10) {
            tracking_retry_interval = 10;
        }
        return tracking_retry_interval;
    }

    public int getMaxSendLogRecords() {

        int max_send_log_records = SigmobCommon.DEFAULT_MAX_SEND_LOG_RECORDS;
        if (getCommonConfig() != null) {
            max_send_log_records = Wire.get(getCommonConfig().max_send_log_records, SigmobCommon.DEFAULT_MAX_SEND_LOG_RECORDS);
        }

        if (max_send_log_records < 10) {
            max_send_log_records = SigmobCommon.DEFAULT_MAX_SEND_LOG_RECORDS;
        }
        return max_send_log_records;
    }

    public int getSendLogInterval() {
        int send_log_interval = 3;
        if (getCommonConfig() != null) {
            send_log_interval = Wire.get(getCommonConfig().send_log_interval, 3);
            if (send_log_interval < 3) {
                send_log_interval = 3;
            }
        }

        return send_log_interval;
    }

    public List<Integer> getLogBlackList() {
        if (getCommonConfig() != null) {
            return getCommonConfig().dclog_blacklist;
        }

        return null;
    }

    public boolean isEnable_debug_level() {
        if (getCommonConfig() != null) {
            return Wire.get(getCommonConfig().enable_debug_level, false);
        }
        return false;
    }

    /**
     * 重复发起请求的间隔时间，在非ready的情况下，用来防止重复发起请求
     *
     * @return
     */
    public long getLoadPeriodTime() {

        if (getCommonConfig() != null) {

            long time = Wire.get(getCommonConfig().load_interval, 0);
            if (time < 1) {
                return 0;
            }
            return time * 1000L;

        }
        return 0;

    }

    public String getNativeAd(){
        SigmobCommon commonConfig = getCommonConfig();
        if (commonConfig != null) {
            SigmobCommonEndpointsConfig endpoints = commonConfig.endpoints;
            if (endpoints != null) {
                String url = Wire.get(endpoints.native_ad, "");
                if (!TextUtils.isEmpty(url)) {
                    if (url.indexOf('?') == -1) {
                        return url + "?" + sigmobServerQueryString();
                    } else {
                        return url + "&" + sigmobServerQueryString();
                    }
                }
            }
        }
        return "";
    }

    public boolean isDisable_up_location() {
        if (getCommonConfig() != null) {
            return Wire.get(getCommonConfig().disable_up_location, false);
        }
        return true;
    }

///////////////////////////////////SigmobAndroid///////////////////////////////////////

    public boolean isDisableUpAppInfo() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().disableUpAppInfo, false);
        }
        return true;
    }

    public int enableReport_log() {
        SigmobAndroid androidConfig = getAndroidConfig();
        if (androidConfig != null) {
            return Wire.get(androidConfig.report_log, 0);
        }
        return 0;
    }


    public List<String> getCanOpenList(){
        SigmobAndroid androidConfig = getAndroidConfig();
        if (androidConfig != null) {
            return androidConfig.open_pkg_list;
        }
        return Collections.emptyList();
    }

    public boolean isEnableWifiScanList() {
        SigmobAndroid sdkConfig = getAndroidConfig();
        if (sdkConfig != null) {
            return Wire.get(sdkConfig.up_wifi_list_interval, 0) >= 60;
        }
        return false;
    }

    public int getDisable_up_OAid() {

        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().disable_up_oaid, 0);
        }
        return 0;
    }

    public boolean isEnable_permission() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().enable_permission, false);

        }
        return false;
    }

    public int getApk_expired_time() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().apk_expired_time, 0);
        }
        return 0;
    }

    public boolean isEnable_report_crash() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().enable_report_crash, false);
        }
        return false;
    }

    public boolean getOaidApiDisable() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().oaid_api_is_disable, false);
        }
        return true;
    }

    public boolean isEnableOkHttp3() {
//        if (getAndroidConfig() != null) {
//            return Wire.get(getAndroidConfig().enable_okhttp3, false);
//        }
        return false;
    }

    public boolean isDisableInstallMonitor() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().disable_install_monitor, false);
        }
        return true;
    }

    public int EnableAppList() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().enable_app_list, 0);
        }
        return 0;
    }

    public boolean isDisableBootMark() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().disable_boot_mark, false);
        }
        return true;
    }


    public WindSDKConfig setOnSDKUpdateListener(OnSDKUpdateListener onSDKUpdateListener) {
        mOnSDKUpdateListener = onSDKUpdateListener;
        return gInstance;
    }

    public boolean enableWebSourceCache() {
        SigmobAndroid androidConfig = getAndroidConfig();
        if (androidConfig != null){
            return Wire.get(androidConfig.use_web_source_cache,false);
        }
        return false;
    }

    public boolean enableExtraDclog() {
        SigmobCommon commonConfig = getCommonConfig();
        if (commonConfig != null){
            return Wire.get(commonConfig.enable_extra_dclog,false);
        }
        return false;
    }
    public int getWebResourceCacheExpireTime(){
        SigmobAndroid androidConfig = getAndroidConfig();
        if (androidConfig != null){
            return Wire.get(androidConfig.web_source_cache_expiration_time,0);
        }
        return 0;
    }

    public interface OnSDKUpdateListener {
        void onUpdate(boolean isOnline);
    }
}
