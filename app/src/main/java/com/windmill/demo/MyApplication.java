package com.windmill.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.gt.sdk.GtAdSdk;
import com.gt.sdk.GtSdkConfig;
import com.gt.sdk.api.GtCustomController;
import com.gt.sdk.api.GtInitCallback;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.tencent.bugly.crashreport.CrashReport;

public class MyApplication extends MultiDexApplication {

    Boolean isCanUseLocation;
    Boolean isCanUsePhoneState;
    Boolean isCanUseAndroidId;
    Boolean isCanUseWifiState;
    Boolean isCanUseWriteExternal;
    Boolean isCanUseAppList;
    Boolean isCanUsePermissionRecordAudio;
    String getDevImei;
    String getAndroidId;
    String getMacAddress;
    String getDevOaid;
    Location getLocation;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        CrashReport.initCrashReport(getApplicationContext(), "4c41e5eed0", true);//4c41e5eed0//4ee13aff7b

        initSDK();

//        MockFloatWindowManager.getInstance().show(getApplicationContext());
    }

    private void initSDK() {

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);
        String appId = sharedPreferences.getString(Constants.CONF_APP_ID, "");
        boolean isAdult = sharedPreferences.getBoolean(Constants.CONF_ADULT, true);
        boolean isPersonalizedAdvertisingOn = sharedPreferences.getBoolean(Constants.CONF_PERSONALIZED, true);
        boolean isSdkLogEnable = sharedPreferences.getBoolean(Constants.CONF_SDK_LOG, true);
        String gdpr_str = sharedPreferences.getString(Constants.CONF_GDPR, "0");
        int coppa_str = sharedPreferences.getInt(Constants.CONF_COPPA, 0);
        Log.d("lance", "gdpr_str:" + gdpr_str);
        Log.d("lance", "coppa_str:" + coppa_str);

        String custom_info = sharedPreferences.getString(Constants.CONF_CUSTOM_DEVICE_INFO, null);
        Log.d("lance", "------------MyApplication-----------:" + custom_info);
        if (!TextUtils.isEmpty(custom_info)) {
            try {
                JSONObject custom_json = new JSONObject(custom_info);
                isCanUseLocation = custom_json.optBoolean("isCanUseLocation");
                isCanUsePhoneState = custom_json.optBoolean("isCanUsePhoneState");
                isCanUseAndroidId = custom_json.optBoolean("isCanUseAndroidId");
                isCanUseWifiState = custom_json.optBoolean("isCanUseWifiState");
                isCanUseWriteExternal = custom_json.optBoolean("isCanUseWriteExternal");
                isCanUseAppList = custom_json.optBoolean("isCanUseAppList");
                isCanUsePermissionRecordAudio = custom_json.optBoolean("isCanUsePermissionRecordAudio");

                getDevImei = custom_json.optString("getDevImei");
                getAndroidId = custom_json.optString("getAndroidId");
                getMacAddress = custom_json.optString("getMacAddress");
                getDevOaid = custom_json.optString("getDevOaid");

                String location = custom_json.optString("getLocation");
                if (!TextUtils.isEmpty(location)) {
                    String[] split = location.split(",");
                    getLocation = new Location("");
                    getLocation.setLongitude(Double.parseDouble(split[0]));
                    getLocation.setLatitude(Double.parseDouble(split[1]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.d("lance", "------------MyApplication----after-------isCanUseLocation:" + isCanUseLocation + ":isCanUsePhoneState:" + isCanUsePhoneState + ":isCanUseAndroidId:" + isCanUseAndroidId + ":isCanUseWifiState:" + isCanUseWifiState + ":isCanUseWriteExternal:" + isCanUseWriteExternal + ":isCanUseAppList:" + isCanUseAppList + ":isCanUsePermissionRecordAudio:" + isCanUsePermissionRecordAudio + ":getDevImei:" + getDevImei + ":getAndroidId:" + getAndroidId + ":getMacAddress:" + getMacAddress + ":getDevOaid:" + getDevOaid + ":getLocation:" + getLocation);

//        List<PackageInfo> packageInfoList = new ArrayList<>();
//        PackageInfo packageInfo = new PackageInfo();
//        packageInfo.packageName = "com.lance.demo";
//        packageInfoList.add(packageInfo);

        Map<String, String> customData = new HashMap<>();
        customData.put("key", "value");
        GtAdSdk.sharedAds().init(this, new GtSdkConfig.Builder().appId(appId) // 测试aapId，请联系快手平台申请正式AppId，必填
                .appName("appName") // 测试appName，请填写您应用的名称，非必填
                .addCustomData(customData) // 是否展示下载通知栏
                .customController(new GtCustomController() {
                    @Override
                    public boolean canReadLocation() {
                        if (isCanUseLocation != null) {
                            return isCanUseLocation;
                        }
                        return super.canReadLocation();
                    }

                    @Override
                    public Location getLocation() {
                        if (getLocation != null) {
                            return getLocation;
                        }
                        return super.getLocation();
                    }

                    @Override
                    public boolean canUsePhoneState() {
                        if (isCanUsePhoneState != null) {
                            return isCanUsePhoneState;
                        }
                        return super.canUsePhoneState();
                    }

                    @Override
                    public String getImei() {
                        if (!TextUtils.isEmpty(getDevImei)) {
                            return getDevImei;
                        }
                        return super.getImei();
                    }

                    @Override
                    public boolean canUseAndroidId() {
                        if (isCanUseAndroidId != null) {
                            return isCanUseAndroidId;
                        }
                        return super.canUseAndroidId();
                    }

                    @Override
                    public String getAndroidId() {
                        if (!TextUtils.isEmpty(getAndroidId)) {
                            return getAndroidId;
                        }
                        return super.getAndroidId();
                    }

                    @Override
                    public boolean canUseWriteExternal() {
                        if (isCanUseWriteExternal != null) {
                            return isCanUseWriteExternal;
                        }
                        return super.canUseWriteExternal();
                    }

                    @Override
                    public boolean canReadInstalledPackages() {
                        if (isCanUseAppList != null) {
                            return isCanUseAppList;
                        }
                        return super.canReadInstalledPackages();
                    }

                    @Override
                    public List<String> getInstalledPackages() {
                        return super.getInstalledPackages();
                    }

                    @Override
                    public boolean canUseWifiState() {
                        if (isCanUseWifiState != null) {
                            return isCanUseWifiState;
                        }
                        return super.canUseWifiState();
                    }

                    @Override
                    public String getMacAddress() {
                        if (!TextUtils.isEmpty(getMacAddress)) {
                            return getMacAddress;
                        }
                        return super.getMacAddress();
                    }

                    @Override
                    public String getOaid() {
                        if (!TextUtils.isEmpty(getDevOaid)) {
                            return getDevOaid;
                        }
                        return super.getOaid();
                    }
                }).setInitCallback(new GtInitCallback() {
                    @Override
                    public void onSuccess() {
                        // 启动成功后再获取SDK
                        Log.d("lance", "--------------onSuccess-----------");
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        Log.d("lance", "--------------onFail-----------" + code + ":" + msg);
                    }
                }).build());
    }

}
