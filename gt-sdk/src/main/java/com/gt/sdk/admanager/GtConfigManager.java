package com.gt.sdk.admanager;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.models.Config;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.network.SigmobRequestQueue;
import com.czhj.sdk.common.track.TrackManager;
import com.czhj.volley.VolleyError;
import com.czhj.wire.Wire;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.GtConstants;
import com.gt.sdk.base.models.Android;
import com.gt.sdk.base.models.Common;
import com.gt.sdk.base.models.NativeConfig;
import com.gt.sdk.base.models.RvConfig;
import com.gt.sdk.base.models.SdkConfig;
import com.gt.sdk.base.models.SdkConfigResponse;
import com.gt.sdk.base.models.SplashConfig;
import com.gt.sdk.base.models.UrlConfig;
import com.gt.sdk.base.network.GtConfigRequest;
import com.gt.sdk.utils.GtSharedPreUtil;
import com.gt.sdk.utils.GtFileUtil;
import com.gt.sdk.utils.WMLogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class GtConfigManager {
    private static final String SDK_CONFIG_VER = "sdkConfigVer";
    private static final String CONFIG_FILE_NAME = "/config";
    private static GtConfigManager gInstance;
    private static boolean mIsGDPRRegion = false;
    private final Handler mHandler;
    private final Runnable mRefreshRunnable;
    private final String ver = "gt-" + GtConstants.SDK_VERSION;
    private boolean mCurrentAutoRefreshStatus;
    private long mRefreshTimeMillis = 0;
    private SdkConfig mSdkConfig = null;
    private SdkConfig mDefaultSDKConfig = null;
    private static boolean isRetry = false;

    public static String _TIMEOUT = "_timeout";

    /**
     * 默认测试地址,方便自己代码调试ci修改
     */
    private static final String SDK_CONFIG_URL = "https://adstage.sigmob.cn/w/config";

    private String AD_URL = "https://adservice.sigmob.cn/strategy/v6";

    private String SDK_LOG_URL = "https://dc.sigmob.cn/log";

    private GtConfigManager() {
        mHandler = new Handler(Looper.getMainLooper());
        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                scheduleRefreshSDKConfigRunnable();
            }
        };
        mCurrentAutoRefreshStatus = true;
        initDefaultSDKConfig();
        loadFromFile();
    }

    public static GtConfigManager sharedInstance() {
        if (gInstance == null) {
            synchronized (GtConfigManager.class) {
                if (gInstance == null) {
                    gInstance = new GtConfigManager();
                }
            }
        }
        return gInstance;
    }

    public static String getConfigUrl() {
        return SDK_CONFIG_URL + "?" + getServerQueryString();
    }

    public static String getServerQueryString() {
        String sdkVersion = "sdkVersion=" + GtConstants.SDK_VERSION;
        return "appId=" + GtAdSdk.sharedAds().getAppId() + "&" + sdkVersion;
    }

    private void initDefaultSDKConfig() {

        if (mDefaultSDKConfig == null) {

            SdkConfig.Builder sdkConfigBuilder = new SdkConfig.Builder();

            Common.Builder comBuilder = new Common.Builder();

            Android.Builder androidBuilder = new Android.Builder();

            //setup sdk_endPoints builder
            UrlConfig.Builder urlBuilder = new UrlConfig.Builder();
            urlBuilder.ads(AD_URL);
            urlBuilder.log(SDK_LOG_URL);
            comBuilder.urlConfig(urlBuilder.build());

            //setup rv builder
            RvConfig.Builder rvBuilder = new RvConfig.Builder();
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
            mDefaultSDKConfig = sdkConfigBuilder.build();
        }
    }

    public void startUpdate() {
        cancelRefreshTimer();
        if (mHandler != null) {
            mHandler.post(mRefreshRunnable);
        }
    }

    private void cancelRefreshTimer() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRefreshRunnable);
        }
    }

    private void handleUpdateConfig(SdkConfig config) {
        if (config != null && config.common_config != null) {
            mSdkConfig = config;
            mIsGDPRRegion = mSdkConfig.common_config.is_gdpr_region;
            mRefreshTimeMillis = mSdkConfig.common_config.configRefresh * 1000;

            Config.sharedInstance().update(mIsGDPRRegion, isDisableBootMark(), getOaidApiDisable(), getDisable_up_OAid(), getLogUrl(), getSendLogInterval(), getMaxSendLogRecords(), getLog_enc());
            TrackManager.getInstance().setRetryExpiredTime(getTracking_expiration_time());
            TrackManager.getInstance().setRetryInterval(getTracking_retry_interval());
        }
    }

    private void loadFromFile() {

        if (ClientMetadata.getInstance() == null) {
            handleUpdateConfig(mDefaultSDKConfig);
            return;
        }
        SharedPreferences sp = GtSharedPreUtil.getSharedPreferences(ClientMetadata.getInstance().getContext());

        String sdkConfigVer = sp.getString(SDK_CONFIG_VER, null);

        ObjectInputStream objectInputStream = null;

        File file = new File(GtFileUtil.getCachePath() + CONFIG_FILE_NAME);

        if (ver.equals(sdkConfigVer) && file.exists()) {
            try {
                objectInputStream = new ObjectInputStream(new FileInputStream(file));

                SdkConfig sdkConfig = SdkConfig.ADAPTER.decode(objectInputStream);

                handleUpdateConfig(sdkConfig);

            } catch (Throwable e) {
                handleUpdateConfig(mDefaultSDKConfig);
                WMLogUtil.e(e.getMessage());
            } finally {
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (Throwable e) {
                        WMLogUtil.e(e.getMessage());
                    }
                }
            }
        } else {
            handleUpdateConfig(mDefaultSDKConfig);
        }
    }

    private void saveToFile(SdkConfig sdkConfig) {
        if (sdkConfig != null) {

            ObjectOutputStream out = null;

            File file = new File(GtFileUtil.getCachePath() + CONFIG_FILE_NAME);
            if (file.exists()) {
                file.delete();
            }

            file.getParentFile().mkdirs();

            try {
                out = new ObjectOutputStream(new FileOutputStream(file));
                sdkConfig.encode(out);
                SharedPreferences sp = GtSharedPreUtil.getSharedPreferences(ClientMetadata.getInstance().getContext());
                sp.edit().putString(SDK_CONFIG_VER, ver).apply();

            } catch (IOException e) {
                WMLogUtil.e(e.getMessage());
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        WMLogUtil.e(e.getMessage());
                    }
                }
            }
        }
    }

    private void scheduleRefreshSDKConfigRunnable() {

        if (ClientMetadata.getInstance() == null) return;

        if (!ClientMetadata.getInstance().isNetworkConnected(getConfigUrl()) || !PrivacyDataManager.canCollectPersonalInformation()) {
            WMLogUtil.e("Can't load an ad because  is no network or can not CollectPersonalInformation");
            scheduleRefreshTimerIfEnabled();
            return;
        }

        loadConfig();
    }

    private void loadConfig() {

        SigmobRequestQueue queue = Networking.getRequestQueue();

        GtConfigRequest request = new GtConfigRequest(getConfigUrl(), new GtConfigRequest.Listener() {
            @Override
            public void onSuccess(SdkConfigResponse sdkConfigResponse) {
                WMLogUtil.dd(WMLogUtil.TAG, "SdkConfigResponse:" + sdkConfigResponse.toString());
                isRetry = false;
                SdkConfig config = sdkConfigResponse.config;
                if (sdkConfigResponse.code == 0 && config != null) {
                    handleUpdateConfig(config);
                    saveToFile(config);
                } else {
                    WMLogUtil.e("ConfigResponseError:" + sdkConfigResponse.error_message);
                }
                scheduleRefreshTimerIfEnabled();
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                WMLogUtil.e("ConfigResponseError:" + error.toString());
                if (isRetry) {
                    scheduleRefreshTimerIfEnabled();
                } else {
                    isRetry = true;
                    mHandler.post(mRefreshRunnable);
                }
            }
        });

        if (queue == null) {
            WMLogUtil.e("queue is null");
            scheduleRefreshTimerIfEnabled();
            return;
        }

        queue.add(request);

        WMLogUtil.i(WMLogUtil.TAG, "start update sdk config");
    }

    private void scheduleRefreshTimerIfEnabled() {
        cancelRefreshTimer();
        if (mCurrentAutoRefreshStatus) {
            long minRefreshTimeMillis = 30 * 1000;
            mHandler.postDelayed(mRefreshRunnable, Math.max(minRefreshTimeMillis, mRefreshTimeMillis));
        }
    }

    public static boolean isGDPRRegion() {
        return mIsGDPRRegion;
    }

    public SdkConfig getSdkConfig() {
        return mSdkConfig == null ? mDefaultSDKConfig : mSdkConfig;
    }

    public Common getCommonConfig() {
        return getSdkConfig().common_config;
    }

    public Android getAndroidConfig() {
        return getSdkConfig().android_config;
    }

    public String getLogUrl() {

        String log = getCommonConfig().urlConfig.log;

        if (!TextUtils.isEmpty(log)) {

            if (log.indexOf('?') == -1) {
                return log + "?" + getServerQueryString();
            } else {
                return log + "&" + getServerQueryString();
            }
        }

        return SDK_LOG_URL + "?" + getServerQueryString();
    }

    public String getAdUrl() {
        String url = getCommonConfig().urlConfig.ads;

        if (!TextUtils.isEmpty(url)) {
            if (url.indexOf('?') == -1) {
                return url + "?" + getServerQueryString();
            } else {
                return url + "&" + getServerQueryString();
            }
        }

        return AD_URL + "?" + getServerQueryString();
    }

    public int getMaxSendLogRecords() {
        int max_send_log_records = getCommonConfig().max_send_log_records;
        if (max_send_log_records < 10) {
            max_send_log_records = 100;
        }
        return max_send_log_records;
    }

    public boolean getLog_enc() {
        if (getCommonConfig() != null) {
            return getCommonConfig().log_enc;
        }
        return false;
    }

    public int getTracking_expiration_time() {
        if (getCommonConfig() != null) {
            if (getCommonConfig().tracking_expiration_time < 1) {
                return 60 * 60 * 24;
            }
            return getCommonConfig().tracking_expiration_time;
        }
        return 60 * 60 * 24;//s
    }

    public int getTracking_retry_interval() {
        if (getCommonConfig() != null) {
            if (getCommonConfig().tracking_retry_interval < 10) {
                return 180;
            }
            return getCommonConfig().tracking_retry_interval;
        }
        return 180;//s
    }

    public int getSendLogInterval() {
        int send_log_interval = getCommonConfig().send_log_interval;
        if (send_log_interval < 3) {
            send_log_interval = 3;
        }
        return send_log_interval;
    }

    public List<Integer> getLogBlackList() {
        return getCommonConfig().dc_log_blacklist;
    }

    public boolean isEnable_debug_level() {
        if (getSdkConfig() != null) {
            if (getCommonConfig().enable_debug_level != null)
                return getCommonConfig().enable_debug_level;
        }
        return false;
    }

    /**
     * 重复发起请求的间隔时间，在非ready的情况下，用来防止重复发起请求
     *
     * @return
     */
    public long getLoadPeriodTime() {
        Integer time = getCommonConfig().load_interval;

        if (time == null || time < 1) {
            return 0;
        }
        return time * 1000;
    }

    public boolean isDisable_up_location() {
        if (getSdkConfig() != null) {
            if (getCommonConfig().disable_up_location != null)
                return getCommonConfig().disable_up_location;
        }
        return true;
    }

    public boolean isDisableUpAppInfo() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().disableUpAppInfo, false);
        }
        return true;
    }

    public int enableReport_log() {
        if (getSdkConfig() != null && getAndroidConfig() != null) {
            return getAndroidConfig().report_log;
        }
        return 0;
    }

    public boolean isEnableWifiScanList() {
        Android sdkConfig = getAndroidConfig();
        if (sdkConfig != null) {
            return Wire.get(sdkConfig.up_wifi_list_interval, 0) >= 60;
        }
        return false;
    }

    public int getDisable_up_OAid() {
        if (getSdkConfig() != null && getAndroidConfig() != null) {
            return getAndroidConfig().disable_up_oaid;
        }
        return 0;
    }

    public boolean isEnable_permission() {
        if (getSdkConfig() != null && getAndroidConfig() != null) {
            return getAndroidConfig().enable_permission;
        }
        return false;
    }

    public boolean isEnable_report_crash() {
        if (getSdkConfig() != null && getAndroidConfig() != null) {
            return getAndroidConfig().enable_report_crash;
        }
        return false;
    }

    public boolean getOaidApiDisable() {
        if (getSdkConfig() != null && getAndroidConfig() != null) {
            return getAndroidConfig().oaid_api_is_disable;
        }
        return true;
    }

    public boolean isDisableBootMark() {
        if (getSdkConfig() != null && getAndroidConfig() != null) {
            return getAndroidConfig().disable_boot_mark;
        }
        return true;
    }

    public RvConfig getRvConfig() {
        if (getCommonConfig() != null) {
            return getCommonConfig().rv_config;
        }
        return null;
    }

    public int getRvCacheTop() {
        if (getRvConfig() != null) {
            return Wire.get(getRvConfig().cacheTop, 5);
        }
        return 5;
    }

    public long loadRvAdTimeout() {
        if (getRvConfig() != null) {
            int time = Wire.get(getRvConfig().ad_load_timeout, 30);
            if (time < 10) {
                time = 10;
            }
            return time * 1000L;
        }
        return 30 * 1000;
    }

    private SplashConfig getSplashConfig() {
        if (getCommonConfig() != null) {
            return getCommonConfig().splash_config;
        }
        return null;
    }

    public int getSplashCacheTop() {
        if (getSplashConfig() == null) return 50;
        return Wire.get(getSplashConfig().cacheTop, 50);
    }

    public long loadSplashAdTimeout() {
        if (getSplashConfig() != null) {
            int time = Wire.get(getSplashConfig().ad_load_timeout, 5);
            if (time < 5) {
                time = 5;
            }
            return time * 1000L;
        }
        return 5 * 1000;
    }

    private NativeConfig getNativeConfig() {
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
        if (getNativeConfig() != null) {
            int time = Wire.get(getNativeConfig().ad_load_timeout, 30);
            if (time < 10) {
                time = 10;
            }
            return time * 1000L;
        }

        return 30 * 1000;
    }

    public int getApk_expired_time() {
        if (getAndroidConfig() != null) {
            return Wire.get(getAndroidConfig().apk_expired_time, 0);
        }
        return 0;
    }

    public int getAdTrackerMaxRetryNum() {
        return 20;
    }

}
