package com.sigmob.sdk.base.services;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.czhj.sdk.common.utils.IntentUtil;
import com.sigmob.sdk.SDKContext;

public class WifiScanService implements ServiceFactory.Service {

    WifiScanReceiver wifiScanReceiver;
    ServiceStatus status = ServiceStatus.STOP;

    public WifiScanService() {
        wifiScanReceiver = new WifiScanReceiver();
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
        //registerReceiver
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        IntentUtil.registerReceiver(context,wifiScanReceiver, filter);

    }

    public void unRegisterAPKReceiver(Context context) {
        //registerReceiver

        context.unregisterReceiver(wifiScanReceiver);

    }

    @Override
    public Error getError() {
        return null;
    }
}
