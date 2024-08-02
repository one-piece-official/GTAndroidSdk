package com.sigmob.sdk;


import android.text.TextUtils;
import android.util.Base64;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.ThreadPool.RepeatingHandlerRunnable;
import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.common.exceptions.CrashHandler;
import com.czhj.sdk.common.models.BidRequest;
import com.czhj.sdk.common.mta.PointEntityCommon;
import com.czhj.sdk.common.network.BuriedPointRequest;
import com.czhj.sdk.common.network.JsonRequest;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.track.AdTracker;
import com.czhj.sdk.common.track.TrackManager;
import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.VolleyError;
import com.czhj.volley.VolleyLog;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.AdLoadCheckUtil;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.mta.PointEntitySigmobCrash;
import com.sigmob.sdk.base.network.AdsRequest;
import com.sigmob.sdk.base.services.AppInstallService;
import com.sigmob.sdk.base.services.ServiceFactory;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindCustomController;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;

public class Sigmob {

    private static Sigmob gshareInstance = null;
    private static boolean isInited;

    private WindAdError sigmobError;
    private RepeatingHandlerRunnable repeatingHandlerRunnable = null;

    private Sigmob() {

    }

    public static SigMacroCommon macroCommon;

    public SigMacroCommon getMacroCommon() {
        if (macroCommon == null) {
            macroCommon = new SigMacroCommon();
        }
        return macroCommon;
    }


    public void init() {
        VolleyLog.DEBUG = WindConstants.IS_DEBUG;

        WindAdOptions options = WindAds.sharedAds().getOptions();
        WindCustomController customController = WindAds.sharedAds().getOptions().getCustomController();
        final int status = WindSDKConfig.getInstance().EnableAppList();
        final boolean disableUpAppInfo = WindSDKConfig.getInstance().isDisableUpAppInfo();

        int canUseAppList = 0;

        boolean isCanUseAppList = true;
        if (options != null) {
            if (customController != null) {
                isCanUseAppList = customController.isCanUseAppList();
                canUseAppList = isCanUseAppList ? 1 : 2;
            }
        }
        boolean isEnableAppList;
        switch (status) {
            case 1: {
                isEnableAppList = true;
            }
            break;
            case 2: {
                isEnableAppList = false;
            }
            break;
            default: {
                isEnableAppList = isCanUseAppList && !disableUpAppInfo;
            }
            break;
        }

        final boolean hasQueryPackagePermission = AdLoadCheckUtil.hasQueryPackagePermission();

        final int finalCanUseAppList = canUseAppList;
        final boolean finalIsEnableAppList = isEnableAppList;
        PointEntitySigmobUtils.SigmobInit(PointCategory.INIT, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {

                if (pointEntityBase instanceof PointEntityCommon) {
                    PointEntityCommon entityInit = (PointEntityCommon) pointEntityBase;
                    HashMap<String, String> options = new HashMap<>();
                    options.put("is_minor", PrivacyManager.getInstance().isAdult() ? Constants.FAIL : Constants.SUCCESS);
                    options.put("is_unpersonalized", PrivacyManager.getInstance().isPersonalizedAdvertisingOn() ? Constants.FAIL : Constants.SUCCESS);
                    options.put("canUseAppList", String.valueOf(finalCanUseAppList));
                    options.put("appListPermission", hasQueryPackagePermission ? Constants.SUCCESS : Constants.FAIL);
                    options.put("disableUpAppInfo", disableUpAppInfo ? Constants.SUCCESS : Constants.FAIL);
                    options.put("EnableAppList", String.valueOf(status));
                    options.put("uploadAppList", finalIsEnableAppList ? Constants.SUCCESS : Constants.FAIL);
                    options.put("common_version", String.valueOf(WindAds.sharedAds().getCommonVersion()));

                    entityInit.setOptions(options);
                }
            }
        });


        if (WindSDKConfig.getInstance().isEnable_permission()) {
            PointEntitySigmobUtils.SigmobTracking(PointCategory.PERMISSION, PointCategory.INIT, null,
                    new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {
                            if (pointEntityBase instanceof PointEntitySigmob) {
                                PointEntitySigmob entitySigMob = (PointEntitySigmob) pointEntityBase;
                                entitySigMob.setPermission(ClientMetadata.getInstance().getPermission(SDKContext.getApplicationContext()));
                            }
                        }
                    });
        }

        AdStackManager.initHttpProxyCacheServer();

        ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                PointEntitySigmobUtils.TrackApp(PointCategory.APP);

                sigmobError = AdLoadCheckUtil.doCheck();

                loadGDPRRegionStatus();
            }
        });


        TrackManager.getInstance().setSigmobTrackListener(new TrackManager.Listener() {
            @Override
            public void onSuccess(AdTracker tracker, NetworkResponse response) {
                PointEntitySigmobUtils.eventTracking(tracker, tracker.getUrl(), null, response, null);

            }

            @Override
            public void onErrorResponse(AdTracker tracker, VolleyError error) {
                PointEntitySigmobUtils.eventTracking(tracker, tracker.getUrl(), null, error);
            }
        });


        ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                clearAdCache();
            }
        });

        TrackManager.getInstance().startRetryTracking();

        isInited = true;
    }

    private void clearAdCache() {

        AdStackManager.clearCacheFiles();

        AdStackManager.deleteCacheTmpfiles();

        AdStackManager.clearNativeAdCache();
        AdStackManager.clearVideoAdCache();
        AdStackManager.clearSplashAd();
        AdStackManager.clearDownloadAPK();
        AdStackManager.cleanWebSourceCache();
    }

    public static BidRequest createRequest() {

        BidRequest.Builder builder = new BidRequest.Builder();

        try {


            builder = AdsRequest.createBidRequest(null);

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

        return builder.build();
    }

    public String getSDKToken() {
        SDKContext.setHasAdLoaded(true);
        BidRequest request = createRequest();
        String s = "2.01|" + deflateAndBase64(request.encode());
        PointEntitySigmobUtils.SigmobTracking("token_request", null, null, null);
        SigmobLog.d("getSDKToken: " + s);
        return s;
    }

    /**
     * 采用gzip压缩，然后base64编码
     *
     * @param str
     * @return
     */
    private String deflateAndBase64(byte[] str) {

        if (str == null || str.length == 0) {
            return null;
        }
        // 使用 deflate 压缩字符串
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream deflater = new DeflaterOutputStream(out);
        try {
            deflater.write(str);
            deflater.flush();
            deflater.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 将压缩后的二进制数据base64成普通文本
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    /**
     * 初始化定位监听
     */
    private static void updateLocationMonitor() {
        //TODO:初始化定位监听
        try {

            boolean canCollectPersonalInformation = PrivacyManager.getInstance().canCollectPersonalInformation();
            boolean disable_localization = WindSDKConfig.getInstance().isDisable_up_location();
            ServiceFactory.updateService(ServiceFactory.LocationServiceName, canCollectPersonalInformation
                    && !disable_localization);

        } catch (Throwable throwable) {
            SigmobLog.e("updateLocationMonitor fail", throwable);
        }
    }

    /**
     * 初始化定位监听
     */
    private static void initAppInstallService() {
        //TODO:初始化定位监听
        try {
            ServiceFactory.updateService(ServiceFactory.AppInstallServiceName, true);
        } catch (Throwable throwable) {
            SigmobLog.e("initAppInstallService fail", throwable);
        }
    }

    /**
     * 初始化定位监听
     */
    private static void updateWifiScanService() {
        //TODO:初始化定位监听
        try {
            boolean canCollectPersonalInformation = PrivacyManager.getInstance().canCollectPersonalInformation();
            boolean isEnableWifiScanList = WindSDKConfig.getInstance().isEnableWifiScanList();

            ServiceFactory.updateService(ServiceFactory.WifiScanServiceName, canCollectPersonalInformation && isEnableWifiScanList);

        } catch (Throwable throwable) {
            SigmobLog.e("updateWifiScanService fail", throwable);
        }
    }

    /**
     * 初始化定位监听
     */
    private static void initDownloadService() {
        //TODO:初始化定位监听
        try {
            ServiceFactory.updateService(ServiceFactory.DownloadServiceName, true);
        } catch (Throwable throwable) {
            SigmobLog.e("initDownloadService fail", throwable);
        }
    }


    public static synchronized Sigmob getInstance() {
        if (gshareInstance == null) {

            synchronized (Sigmob.class) {

                gshareInstance = new Sigmob();
            }
        }
        return gshareInstance;
    }

    private void loadGDPRRegionStatus() {
        JsonRequest gdprRegionRequest = new JsonRequest(WindSDKConfig.getGDPRRegionURL(), new JsonRequest.Listener() {
            @Override
            public void onSuccess(JSONObject response) {
                if (response != null) {
                    try {
                        Boolean isGDPRRegion = response.getBoolean(Constants.IS_REQUEST_IN_EEA_OR_UNKNOWN);
                        PrivacyManager.getInstance().setExtGDPRRegion(isGDPRRegion);

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                startServices();
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                startServices();
                PrivacyManager.getInstance().setExtGDPRRegion(null);

            }
        }, 0);

        Networking.getSigRequestQueue().add(gdprRegionRequest);
    }


    private void sendCrashLog(final File file, String crash) {


        PointEntitySigmobCrash sigmobCrash = PointEntitySigmobCrash.WindCrash(crash);

        if (file != null) {
            try {
                String name = file.getName().replace(".log", "");
                sigmobCrash.setCrashTime(Long.parseLong(name));
            } catch (Throwable th) {
                SigmobLog.e("set crash time fail", th);
            }
        }

        sigmobCrash.sendServe(new BuriedPointRequest.RequestListener() {
            @Override
            public void onSuccess() {
                if (file != null) {
                    file.delete();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void reportCrashLogs() {
        try {
            File[] crashFiles = SigmobFileUtil.getCrashFiles();
            if (crashFiles != null) {

                for (File file : crashFiles) {
                    String crashLog = FileUtil.readFileToString(file);
                    if (!TextUtils.isEmpty(crashLog)) {
                        sendCrashLog(file, crashLog);
                    }
                }
            }
        } catch (Throwable e) {
            SigmobLog.e("send crash Log fail", e);
        }

    }

    private void startServices() {

        initDownloadService();
//        initAppInstallService();
        SDKContext.startSession();

//        startSendSchedule();
//        WindGDPRStatusTrack(WindAds.sharedAds().getUserGDPRConsentStatus().getValue());


        WindSDKConfig.getInstance().setOnSDKUpdateListener(new WindSDKConfig.OnSDKUpdateListener() {
            @Override
            public void onUpdate(boolean isOnline) {

                updateLocationMonitor();
                updateWifiScanService();

                if (isOnline){
                    AppInstallService.appListUpdate();
                    initAppInstallService();
                    AppInstallService.canOpenListUpdate();
                }

            }
        }).startUpdate();


        if (WindSDKConfig.getInstance().isEnable_report_crash()) {

            String[] words = WindAds.class.getName().split("\\.");
            if (words.length <= 2) return;
            final String pkgPrefix = words[0] + "." + words[1] + ".";
            CrashHandler.getInstance().add(new CrashHandler.CrashHandlerListener() {
                @Override
                public void reportCrash(String crash) {
                    if (!TextUtils.isEmpty(crash) &&
                            (crash.contains(pkgPrefix)
                                    || crash.contains("com.czhj."))) {

                        String crashString = String.format("crashTime %d, appId %s, sdkVersion %s, commonVersion %d crashLog: %s",
                                System.currentTimeMillis(),
                                WindAds.sharedAds().getAppId(), WindConstants.SDK_VERSION,
                                WindAds.sharedAds().getCommonVersion(), crash);


                        SigmobLog.e("crashLog " + crashString);
                        File crashPath = SigmobFileUtil.createCrashPath();
                        if (crashPath != null) {
                            FileUtil.writeToBuffer(crashString.getBytes(), crashPath.getAbsolutePath());
                        }

                        sendCrashLog(crashPath, crashString);
                    }
                }
            });
            reportCrashLogs();
        }


        clearImageCache();


    }

    private void clearImageCache() {
        ImageManager.with(SDKContext.getApplicationContext()).clearCache();
    }

//    void startSendSchedule() {
//        int send_log_interval = WindSDKConfig.getInstance().getSendLogInterval();
//        send_log_interval = send_log_interval * 1000;
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//        executor.scheduleAtFixedRate(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        BuriedPointManager buriedPointManager = SDKContext.getBuriedPointManager();
//                        if (buriedPointManager != null){
//                            buriedPointManager.sendPoint(WindSDKConfig.getInstance().getLogUrl());
//                        }
//                    }
//                },
//                0,
//                send_log_interval,
//                TimeUnit.MILLISECONDS);
//    }


    public WindAdError getSigMobError() {
        return sigmobError;
    }


}
