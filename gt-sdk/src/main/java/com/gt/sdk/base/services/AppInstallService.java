package com.gt.sdk.base.services;

import static com.czhj.sdk.common.utils.AppPackageUtil.getPackageManager;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Build;


import com.czhj.sdk.common.utils.IntentUtil;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.WindConstants;
import com.gt.sdk.utils.Base64Util;

import java.util.List;

public class AppInstallService implements ServiceFactory.Service {

    AppInstallReceiver appInstallReceiver;
    ServiceStatus status = ServiceStatus.STOP;

    public AppInstallService() {
        if (appInstallReceiver == null) {
            appInstallReceiver = new AppInstallReceiver();
        }
    }

    private static List<PackageInfo> getInstallApps(Context context) {
        try {
            return getPackageManager(context).getInstalledPackages(0);
        } catch (Throwable ignored) {

        }
        return null;
    }

    @Override
    public boolean startService() {

        if (status != ServiceStatus.RUNNING) {
            registerAPKReceiver(GtAdSdk.sharedAds().getContext());
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
            unRegisterAPKReceiver(GtAdSdk.sharedAds().getContext());
            status = ServiceStatus.STOP;
        }
    }

    public void registerAPKReceiver(Context context) {

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

        IntentUtil.registerReceiver(context, appInstallReceiver, filter);
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
