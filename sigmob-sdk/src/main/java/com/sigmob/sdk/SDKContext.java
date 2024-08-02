package com.sigmob.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.AdLifecycleManager;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.AdActivity;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.IntentUtil;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.db.SQLiteHelper;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntityActive;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindCustomController;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class SDKContext {

    private static WeakReference<Activity> mActivityWeakReference;
    private static Context mContext;

    private static AdLifecycleManagerListener adLifecycleManagerListener;
    private static String fastAppPackageName;
    private static WeakReference<Activity> lastActivity;

    private static boolean hasAdLoaded = false;

    private static boolean canUseAndroidID = false;
    private static boolean canUseAndroid = false;
    private static DeviceContext deviceContext;

    public static void initFastApp(final Context context) {
        try {
            if (TextUtils.isEmpty(fastAppPackageName)) {
                Uri uri = Uri.parse("hap://app/");
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                final PackageManager packageManager = context.getPackageManager();
                final List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
                if (!activities.isEmpty()) {
                    ResolveInfo resolveInfo = activities.get(0);
                    fastAppPackageName = resolveInfo.activityInfo.packageName;
                }
            }

        } catch (NullPointerException e) {

        }
    }


    public static void setHasAdLoaded(boolean hasAdLoaded) {
        SDKContext.hasAdLoaded = hasAdLoaded;
    }

    public static String getFastAppPackageName() {
        return fastAppPackageName;
    }

    public synchronized static void init(Context context) {


        if (mContext == null) {
            mContext = context.getApplicationContext();

            initFastApp(mContext);
            SigmobFileUtil.initSDKCacheFolder(mContext, WindConstants.SDK_FOLDER);

            WindSDKConfig.getInstance().loadFromFile();
            initNetworking(mContext);
            SQLiteHelper.initialize(mContext);

            ClientMetadata.getInstance().initialize(mContext);

            updateAppList();

        }
    }

    private static void updateAppList(){
        WindAdOptions options = WindAds.sharedAds().getOptions();
        if (options != null && options.getCustomController() != null){
            WindCustomController customController = options.getCustomController();
            if (customController != null && !customController.isCanUseAppList()){
                List<PackageInfo> installPackageInfoList = customController.getInstallPackageInfoList();
                if (installPackageInfoList != null){
                    for (PackageInfo info: installPackageInfoList) {
                        PointEntitySigmobUtils.appInfoListTracking(info,2);

                    }
                }
            }
        }
    }


    public static DeviceContext getDeviceContext() {

        if (deviceContext != null || mContext == null) {
            return deviceContext;
        }
        deviceContext = new DeviceContext() {

            @Override
            public String getAndroidId() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                if ((controller == null || controller.isCanUseAndroidId()) && hasAdLoaded && WindAds.sharedAds().isPersonalizedAdvertisingOn()) {
                    return ClientMetadata.getInstance().getAndroidId();
                } else if (controller != null && !controller.isCanUseAndroidId()) {
                    return controller.getAndroidId();
                }
                return null;
            }

            @Override
            public String getImei() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                if ((controller == null || controller.isCanUsePhoneState()) && hasAdLoaded && WindAds.sharedAds().isPersonalizedAdvertisingOn()) {
                    return ClientMetadata.getInstance().getDeviceId();
                } else if (controller != null && !controller.isCanUsePhoneState()) {
                    return controller.getDevImei();
                }
                return null;
            }

            @Override
            public String getImei1() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                if ((controller == null || controller.isCanUsePhoneState()) && hasAdLoaded && WindAds.sharedAds().isPersonalizedAdvertisingOn()) {
                    return ClientMetadata.getInstance().getDeviceId(0);
                } else if (controller != null && !controller.isCanUsePhoneState()) {
                    return controller.getDevImei();
                }
                return null;
            }

            @Override
            public boolean isCustomPhoneState() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                return (controller == null || controller.isCanUsePhoneState());
            }

            @Override
            public boolean isCustomAndroidId() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                return (controller == null || controller.isCanUseAndroidId());
            }

            @Override
            public boolean isCustomOaId() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                return !(controller == null || TextUtils.isEmpty(controller.getDevOaid()));
            }

            @Override
            public String getImei2() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                if ((controller == null || controller.isCanUsePhoneState()) && hasAdLoaded && WindAds.sharedAds().isPersonalizedAdvertisingOn()) {
                    return ClientMetadata.getInstance().getDeviceId(1);
                } else if (controller != null && !controller.isCanUsePhoneState()) {
                    return controller.getDevImei();
                }
                return null;
            }

            @Override
            public String getOaid() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                if (controller == null || TextUtils.isEmpty(controller.getDevOaid())) {
                    return ClientMetadata.getInstance().getOAID();
                } else {
                    return controller.getDevOaid();
                }
            }

            @Override
            public Location getLocation() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                if ((controller == null || controller.isCanUseLocation())) {
                    return ClientMetadata.getInstance().getLocation();
                } else if (!controller.isCanUseLocation()) {
                    return controller.getLocation();
                }
                return null;
            }

            @Override
            public String getCarrier() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                if ((controller == null || controller.isCanUsePhoneState())) {
                    return ClientMetadata.getInstance().getNetworkOperatorForUrl();
                }
                return null;
            }

            @Override
            public String getCarrierName() {
                WindCustomController controller = WindAds.sharedAds().getOptions().getCustomController();
                if ((controller == null || controller.isCanUsePhoneState())) {
                    return ClientMetadata.getInstance().getNetworkOperatorName();
                }
                return null;
            }
        };
        return deviceContext;
    }

    /**
     * 初始化网络监听
     */
    private static void initNetworking(Context context) {
        Networking.AddSigmobServerURL(WindSDKConfig.getConfigUrl());
        Networking.AddSigmobServerURL(WindSDKConfig.getInstance().getAdsUrl());
        Networking.AddSigmobServerURL(WindSDKConfig.getInstance().getHbAdsUrl());
        Networking.AddSigmobServerURL(WindSDKConfig.getInstance().getLogUrl());

        Networking.initializeV2(context);
        Networking.initializeSigRequestQueue(context);

    }

    public static Activity getLastActivity() {
        if (lastActivity != null) {
            return lastActivity.get();
        }
        return null;
    }

    public static void startSession() {
        if (adLifecycleManagerListener == null) {
            adLifecycleManagerListener = new AdLifecycleManagerListener();
        }
    }




    private static class AdLifecycleManagerListener implements AdLifecycleManager.LifecycleListener {
        private String active_id;
        private long session_start;
        private Map<String, String> map = new HashMap<>();
        private String topActivity;
        private int mActivityCount;
        private boolean isAppAlive = true;  //judge is app alive;
        private boolean isSwitchActivity = false;  // judge is switch activity failFrom top to other in the stack of activity
        private boolean isAppExit = false;  //some times app have cleard the stack of activity but app is not exit . this boolean can help to static realive;
        private boolean isAdShow;

        AdLifecycleManagerListener() {

            AdLifecycleManager.getInstance().initialize(SDKContext.getApplication());

            session_start = System.currentTimeMillis();
            active_id = UUID.randomUUID().toString();
            //session_start打点
            SigmobLog.i("session_start: " + session_start + ":" + active_id);
            PointEntityActive.ActiveTracking(PointCategory.SESSION_START, active_id, "0", String.valueOf(session_start));

            AdLifecycleManager.getInstance().addLifecycleListener(this);

            try {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction( new String(Base64.decode(WindConstants.SCREEN_ON, Base64.NO_WRAP)));
                intentFilter.addAction( new String(Base64.decode(WindConstants.SCREEN_OFF,Base64.NO_WRAP)));
                intentFilter.addAction( new String(Base64.decode(WindConstants.USER_PRESENT,Base64.NO_WRAP)));
                IntentUtil.registerReceiver(mContext,mScreenStatusReceiver, intentFilter);
            }catch (Throwable t){
                SigmobLog.e("register screen status receiver error" + t.getMessage());
            }
        }

        private BroadcastReceiver mScreenStatusReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = android.util.Base64.encodeToString(intent.getAction().getBytes(), Base64.NO_WRAP);
                switch (action){
                    case WindConstants.SCREEN_ON:{
                        SigmobLog.d("screen on");
                        PointEntitySigmobUtils.eventRecord(PointCategory.SCREEN_ON,null,null);
                    }
                    break;
                    case WindConstants.SCREEN_OFF:
                        SigmobLog.d("screen off");
                        PointEntitySigmobUtils.eventRecord(PointCategory.LOCKED,null,null);

                        break;
                    case WindConstants.USER_PRESENT:
                        SigmobLog.d("screen user present");
                        PointEntitySigmobUtils.eventRecord(PointCategory.UNLOCK,null,null);

                        break;
                }
            }
        };

        public void onCreate(Activity activity) {
            topActivity = activity.getClass().getSimpleName();
            map.put(topActivity, topActivity);
            isAppAlive = true;
            isSwitchActivity = false;
        }

        public void onStart(Activity activity) {

            mActivityCount++;
        }

        public void onPause(Activity activity) {

        }

        public void onResume(Activity activity) {
            if (activity instanceof AdActivity) {
                isAdShow = true;
            } else {
                if (!isAdShow) {
                    lastActivity = new WeakReference<>(activity);
                }
            }

            isSwitchActivity = !activity.getClass().getSimpleName().equals(topActivity);
            topActivity = activity.getClass().getSimpleName();
            if (isSwitchActivity) {
                mActivityWeakReference = new WeakReference<>(activity);
            }
            if (!isAppAlive || isAppExit) {
                isAppExit = false;
                //app进入活动状态
                active_id = UUID.randomUUID().toString();
                session_start = System.currentTimeMillis();
                isAppAlive = true;
                SigmobLog.i("onActivityResumed session_start: " + session_start + ":" + active_id);
                PointEntityActive.ActiveTracking(PointCategory.SESSION_START, active_id, "0", String.valueOf(session_start));
            }
        }

        public void onRestart(Activity activity) {

        }
        private void openPkgTrack() {

            BaseAdUnit clickAdUnit = AdStackManager.getClickAdUnit();
            if (clickAdUnit != null) {
                SigmobTrackingRequest.sendTrackings(clickAdUnit, ADEvent.OPEN_PKG);
                PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_PKG, Constants.SUCCESS, clickAdUnit);
            }

            AdStackManager.setClickAdUnit(null);

        }

        public void onStop(Activity activity) {
            mActivityCount--;
            if (activity.getClass().getSimpleName().equals(topActivity)) {
                openPkgTrack();
                if (!isSwitchActivity || map.size() == 1) {
                    long session_end = System.currentTimeMillis();
                    long duration = (session_end - session_start) / 1000;
                    //用户切换到后台
                    SigmobLog.i("onActivityStopped session_end: " + session_end + ":" + active_id + ":" + duration);
                    PointEntityActive.ActiveTracking(PointCategory.SESSION_END, active_id, String.valueOf(duration), String.valueOf(session_end));
                    //when upload compelete,reset timestart
                    session_start = System.currentTimeMillis();
                    isAppAlive = false;
                }
            }
        }

        public int getActivityCount() {
            return mActivityCount;
        }

        public void onDestroy(Activity activity) {
            if (activity instanceof AdActivity) {
                isAdShow = false;
            }
            map.remove(activity.getClass().getSimpleName());

            if (map.size() == 0 && isAppAlive) {
                long session_end = System.currentTimeMillis();
                long duration = (session_end - session_start) / 1000;
                //用户切换到后台
                SigmobLog.i("onActivityDestroyed session_end: " + session_end + ":" + active_id + ":" + duration);
                PointEntityActive.ActiveTracking(PointCategory.SESSION_END, active_id, String.valueOf(duration), String.valueOf(session_end));
                //when upload compelete,reset timestart
                session_start = System.currentTimeMillis();
                isAppAlive = false;
            }
            if (map.size() == 0) {
                isAppExit = true;
            }
        }

        public void onBackPressed(Activity activity) {

        }

        public void onActivityResult(Activity activity, int reqCode, int resCode, Intent data) {

        }
    }

    public static Context getApplicationContext() {
        return mContext;
    }

    public static boolean isSysMute() {
        if (mContext != null) {
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
            }
        }
        return false;
    }

    public static Application getApplication() {

        if (mContext instanceof Application) {
            return (Application) mContext;
        }
        return null;
    }


    public static void setTopActivity(Activity activity) {
        if (activity != null) {
            mActivityWeakReference = new WeakReference<>(activity);
        }
    }

    public static Activity getTopActivity() {
        if (mActivityWeakReference != null) {
            return mActivityWeakReference.get();
        }
        return null;
    }

}
