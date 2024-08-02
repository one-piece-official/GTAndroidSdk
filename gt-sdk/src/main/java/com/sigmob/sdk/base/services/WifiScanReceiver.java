package com.sigmob.sdk.base.services;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Base64;

import com.czhj.sdk.common.utils.DeviceUtils;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntityAntiSpan;
import com.sigmob.sdk.base.mta.PointType;

import java.util.List;


public class WifiScanReceiver extends BroadcastReceiver {
    private List<ScanResult> wifiScanList;
    private long updateTime = 0;

    @Override
    @SuppressLint("MissingPermission")
    public void onReceive(Context context, Intent intent) {

        try {
            WifiManager wifiManager = DeviceUtils.getWifiManager(context);

            if (wifiManager == null) return;
            SigmobLog.d("private :use_wifi");

            switch (intent.getAction()) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION: {
                    List<ScanResult> scanResults = wifiManager.getScanResults();

                    int up_wifi_list_interval = WindSDKConfig.getInstance().getAndroidConfig().up_wifi_list_interval;
                    if (up_wifi_list_interval >= 60 && updateTime + up_wifi_list_interval * 1000 < System.currentTimeMillis() && !scanResults.isEmpty()) {

                        updateTime = System.currentTimeMillis();

                        wifiScanList = scanResults;
                        String nameList = "";
                        String macList = "";

                        for (int i = 0; i < wifiScanList.size(); i++) {
                            ScanResult result = wifiScanList.get(i);
                            nameList = nameList + Base64.encodeToString(result.SSID.getBytes(), Base64.NO_WRAP);
                            macList = macList + result.BSSID;

                            if (i != wifiScanList.size() - 1) {
                                nameList = nameList + ",";
                                macList = macList + ",";
                            }
                        }
                        SigmobLog.d("name List " + nameList);
                        SigmobLog.d("mac List " + macList);
                        wifiListSend(nameList, macList);
                    }

                }
                break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION: {

                    scanWifiList(context);

                }
                break;

            }

        } catch (Throwable throwable) {

            SigmobLog.e("WifiScanReceiver error", throwable);
        }
    }

    private void wifiListSend(String nameList, String macList) {

        PointEntityAntiSpan pointEntityBase = new PointEntityAntiSpan();

        pointEntityBase.setCategory(PointCategory.WIFI_LIST);
        pointEntityBase.setAc_type(PointType.ANTI_SPAM);
        pointEntityBase.setWifi_id_list(nameList);
        pointEntityBase.setWifi_mac_list(macList);

        pointEntityBase.commit();
    }
    
    @SuppressLint("MissingPermission")
    public void scanWifiList(Context context) {
        try {
            WifiManager wifi = DeviceUtils.getWifiManager(context);


            if (wifi == null || wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED || WindSDKConfig.getInstance().getAndroidConfig().up_wifi_list_interval < 60)
                return;
            SigmobLog.d("private :use_wifi ");

            boolean scanResult = wifi.startScan();

            SigmobLog.i("scanResult " + scanResult);

        } catch (Throwable e) {

        }
    }


}