package com.sigmob.sdk.base.services;

import static com.czhj.sdk.common.Constants.sdf;
import static com.czhj.sdk.common.utils.AppPackageUtil.getPackageManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.IntentUtil;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.utils.Base64Util;
import com.sigmob.sdk.base.utils.WindPrefsUtils;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindCustomController;

import java.util.Date;
import java.util.List;

public class AppInstallService implements ServiceFactory.Service {

    AppInstallReceiver appInstallReceiver;
    ServiceStatus status = ServiceStatus.STOP;


    public AppInstallService() {
        if (appInstallReceiver == null) {
            appInstallReceiver = new AppInstallReceiver();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static boolean isTodaySend(Context context) {

        Date date = new Date();
        String day = sdf.format(date);
        SharedPreferences sp = WindPrefsUtils.getPrefs("BuriedPointConfig");
        String lastDay = sp.getString(WindConstants.LAST_DAY, "");

        return lastDay.equals(day);

    }
    @SuppressLint("SimpleDateFormat")
    public static boolean isCanOpenTodaySend() {

        Date date = new Date();
        String day = sdf.format(date);
        SharedPreferences sp = WindPrefsUtils.getPrefs("BuriedPointConfig");
        String lastDay = sp.getString(WindConstants.CAN_OPEN_LAST_DAY, "");

        return lastDay.equals(day);

    }


    private static List<PackageInfo> getInstallApps(Context context) {
        try {

            List<PackageInfo> packageInfos = getPackageManager(context).getInstalledPackages(0);

            return packageInfos;

        } catch (Throwable e) {

        }
        return null;

    }


    private static void updateAppsinfo() {

        final Context context = SDKContext.getApplicationContext();

        boolean isTodySended = isTodaySend(context);

        if (isTodySended) {
            return;
        }


        ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {

                try {

                    List<PackageInfo> appInfos = getInstallApps(context);

                    if (appInfos == null) return;
                    for (int i = 0; i < appInfos.size(); i++) {
                        PackageInfo packageInfo = appInfos.get(i);
                        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            PointEntitySigmobUtils.appInfoListTracking(packageInfo,0);
                        }
                    }

                    Date date = new Date();
                    String day = sdf.format(date);
                    SharedPreferences sp = WindPrefsUtils.getPrefs("BuriedPointConfig");
                    sp.edit().putString(WindConstants.LAST_DAY, day).apply();
                } catch (Throwable throwable) {
                    SigmobLog.e("update app info", throwable);
                }

            }
        });

    }


    private static void appListHelper() {
        try {

            if (PrivacyManager.getInstance().canCollectPersonalInformation()) {
                WindAdOptions options = WindAds.sharedAds().getOptions();
                boolean isEnableAppList;

                int status = WindSDKConfig.getInstance().EnableAppList();
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
                        boolean isCanUseAppList = true;
                        if (options != null) {
                            WindCustomController customController = options.getCustomController();

                            if (customController != null) {
                                isCanUseAppList = customController.isCanUseAppList();
                            }
                        }

                        isEnableAppList = isCanUseAppList && !WindSDKConfig.getInstance().isDisableUpAppInfo();
                    }
                    break;
                }
                if (isEnableAppList) {
//                    SDKContext.initFastApp(SDKContext.getApplicationContext());
                    updateAppsinfo();
                }
            }
        } catch (Throwable throwable) {

            SigmobLog.e("BuriedPointManager getInstance", throwable);
        }
    }

    public static void appListUpdate() {
        try {
            appListHelper();
        } catch (Throwable t) {

        }

    }

    public static void onCanOpenListSend(){

        Date date = new Date();
        String day = sdf.format(date);
        SharedPreferences sp = WindPrefsUtils.getPrefs("BuriedPointConfig");
        sp.edit().putString(WindConstants.CAN_OPEN_LAST_DAY, day).apply();
    }
    public static void canOpenListUpdate() {

        Context context = SDKContext.getApplicationContext();
        if (context != null &&PrivacyManager.getInstance().canCollectPersonalInformation()) {

            if (!isCanOpenTodaySend()){
                ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
                    @Override
                    public void run() {
                        List<String> canOpenList = WindSDKConfig.getInstance().getCanOpenList();
                        if (!canOpenList.isEmpty()){
                            for (String pkg: canOpenList) {
                                boolean canOpen = IntentUtil.deviceCanHandlePackageName(context, pkg);
                                PointEntitySigmobUtils.canOpenListTracking(pkg,canOpen);
                            }

                            onCanOpenListSend();
                        }

                    }
                });

            }

        }
    }
    @Override
    public boolean startService() {

        if (status != ServiceStatus.RUNNING) {
            registerAPKReceiver(SDKContext.getApplicationContext());
            status = ServiceStatus.RUNNING;
            return true;
        }

        return false;
    }

    @Override
    public ServiceStatus getStatus() {
        return status;
    }

    @Override
    public void stopService() {
        if (status == ServiceStatus.RUNNING) {
            unRegisterAPKReceiver(SDKContext.getApplicationContext());
            status = ServiceStatus.STOP;
        }

    }



    public void registerAPKReceiver(Context context) {

        if (WindSDKConfig.getInstance().isDisableInstallMonitor()) {
            return;
        }
        //registerReceiver
        IntentFilter filter = new IntentFilter();


        filter.addAction(Base64Util.decodeBase64String(WindConstants.INSTALL_APP));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            filter.addAction(Base64Util.decodeBase64String(WindConstants.APP_INSTALL_FAIL));
        }
        filter.addAction(Base64Util.decodeBase64String(WindConstants.APP_ADD));
        filter.addAction(Base64Util.decodeBase64String(WindConstants.APP_REMOVE));
        filter.addAction(Base64Util.decodeBase64String(WindConstants.APP_REPLACE));

        filter.addDataScheme("package"); //This line is very important. Otherwise, broadcast can't be received.这一行很得要

        IntentUtil.registerReceiver(context,appInstallReceiver, filter);

    }

    public void unRegisterAPKReceiver(Context context) {
        //registerReceiver

        context.unregisterReceiver(appInstallReceiver);

    }

    @Override
    public Error getError() {
        return null;
    }
}
