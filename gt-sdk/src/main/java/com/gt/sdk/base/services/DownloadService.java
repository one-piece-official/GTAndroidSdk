package com.gt.sdk.base.services;

import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;

import com.gt.sdk.GtAdSdk;
import com.gt.sdk.base.common.IntentUtil;

public class DownloadService implements ServiceFactory.Service {

    DownloadCompleteReceiver downloadCompleteReceiver;
    ServiceStatus status = ServiceStatus.STOP;

    public DownloadService() {
        if (downloadCompleteReceiver == null) {
            downloadCompleteReceiver = new DownloadCompleteReceiver();
        }
    }

    @Override
    public boolean startService() {
        registerAPKReceiver(GtAdSdk.sharedAds().getContext());
        status = ServiceStatus.RUNNING;
        return false;
    }

    @Override
    public ServiceStatus getStatus() {
        return null;
    }

    @Override
    public void stopService() {
        unRegisterAPKReceiver(GtAdSdk.sharedAds().getContext());
        status = ServiceStatus.STOP;
    }

    public void registerAPKReceiver(Context context) {
        //registerReceiver
        IntentFilter downloadFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        IntentUtil.registerReceiver(context, downloadCompleteReceiver, downloadFilter, true);
    }

    public void unRegisterAPKReceiver(Context context) {
        //registerReceiver

        context.unregisterReceiver(downloadCompleteReceiver);

    }

    @Override
    public Error getError() {
        return null;
    }
}
