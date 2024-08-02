package com.windmill.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.windmill.sdk.WMAdnInitConfig;
import com.windmill.sdk.WMAdConfig;
import com.windmill.sdk.WMCustomController;
import com.windmill.sdk.WMNetworkConfig;
import com.windmill.sdk.WMWaterfallFilter;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillConsentStatus;
import com.windmill.sdk.WindMillUserAgeStatus;
import com.windmill.xbid.Actor;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        WindMillAd ads = WindMillAd.sharedAds();

        ads.setLocalStrategyAssetPath(this, "localStrategy");

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);
        String appId = sharedPreferences.getString(Constants.CONF_APP_ID, "");
        boolean isAdult = sharedPreferences.getBoolean(Constants.CONF_ADULT, true);
        boolean isPersonalizedAdvertisingOn = sharedPreferences.getBoolean(Constants.CONF_PERSONALIZED, true);
        boolean isSdkLogEnable = sharedPreferences.getBoolean(Constants.CONF_SDK_LOG, true);
        String gdpr_str = sharedPreferences.getString(Constants.CONF_GDPR, "0");
        int coppa_str = sharedPreferences.getInt(Constants.CONF_COPPA, 0);
        Log.d("lance", "gdpr_str:" + gdpr_str);
        Log.d("lance", "coppa_str:" + coppa_str);
        switch (gdpr_str) {
            case "0":
                ads.setUserGDPRConsentStatus(WindMillConsentStatus.UNKNOWN);
                break;
            case "1":
                ads.setUserGDPRConsentStatus(WindMillConsentStatus.ACCEPT);
                break;
            case "2":
                ads.setUserGDPRConsentStatus(WindMillConsentStatus.DENIED);
                break;
        }

        switch (coppa_str) {
            case 0:
                ads.setIsAgeRestrictedUser(WindMillUserAgeStatus.WindAgeRestrictedStatusUnknown);
                break;
            case 1:
                ads.setIsAgeRestrictedUser(WindMillUserAgeStatus.WindAgeRestrictedStatusYES);
                break;
            case 2:
                ads.setIsAgeRestrictedUser(WindMillUserAgeStatus.WindAgeRestrictedStatusNO);
                break;
        }

        ads.setUserAge(18);
        ads.setAdult(isAdult);
        ads.setPersonalizedAdvertisingOn(isPersonalizedAdvertisingOn);
        ads.setDebugEnable(isSdkLogEnable);

        ads.setSupportMultiProcess(true);

        Set<String> groupList = sharedPreferences.getStringSet(Constants.CONF_GROUP, null);
        if (groupList != null) {
            Log.d("lance", "------initSDK------" + groupList.toString());
            Map<String, String> customMap = new HashMap<>();
            Iterator<String> iterator = groupList.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (!TextUtils.isEmpty(next)) {
                    String[] split = next.split("-");
                    if (split.length == 2) {
                        customMap.put(split[0], split[1]);
                    }
                }
            }
            ads.initCustomMap(customMap); //  App的自定义规则为全局设置，对全部Placement有效
        }

        WMNetworkConfig.Builder builder = new WMNetworkConfig.Builder();

        String initString = sharedPreferences.getString(Constants.INIT_SETTING, "");
        if (!TextUtils.isEmpty(initString)) {
            try {
                JSONObject jsonObject = new JSONObject(initString);
                Log.d("lance", "initString: " + jsonObject.toString());
                Iterator<String> it = jsonObject.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    JSONObject object = jsonObject.getJSONObject(key);
                    String app_id = object.optString("appId");
                    String app_Key = object.optString("appKey");
                    if (!TextUtils.isEmpty(app_id) || !TextUtils.isEmpty(app_Key)) {
                        builder.addInitConfig(new WMAdnInitConfig(Integer.parseInt(key), app_id, app_Key));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean sdkRelease = sharedPreferences.getBoolean(Constants.CONF_SDK_RELEASE, false);
        if (!sdkRelease) {
            try {
                //com.windmill.sdk.b.f:k
                Class cls = Class.forName("com.windmill.sdk.strategy.WMSdkConfig");
                Field f = cls.getDeclaredField("lance");
                f.setAccessible(true);
                f.set(null, "https://adstage.sigmob.cn/w/config");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        /**
//         * 下面构造了过滤表达式例子的含义为：
//         *
//         * 1、针对聚合广告位id:88888888 过滤gdt渠道下的广告位id:123
//         * 2、针对聚合广告位id:88888888 过滤csj渠道下的广告位id:456
//         * 3、针对聚合广告位id:88888888 过滤ks渠道下的广告位id集合:123、456、789
//         * 4、针对聚合广告位id:88888888 过滤bd整个渠道
//         * 5、针对聚合广告位id:88888888 过滤该瀑布流下的123、345、789等三方渠道的广告位id,与渠道无关
//         * 6、针对聚合广告位id:88888888 过滤该瀑布流下的ks、csj、gdt等三方渠道
//         * 7、针对聚合广告位id:88888888 过滤该瀑布流下的客户端bidding、服务端bidding、普通广告源（相当于过滤整个瀑布流）
//         * 8、针对聚合广告位id:88888888 过滤该瀑布流下的客户端bidding的三方广告源
//         * 9、针对聚合广告位id:88888888 过滤该瀑布流下的服务端bidding的三方广告源
//         * 10、针对聚合广告位id:88888888 过滤该瀑布流下的非bidding的普通三方广告源
//         * 11、针对聚合广告位id:88888888 过滤该瀑布流下价格在50-100之间的三方广告源
//         * 12、针对聚合广告位id:88888888 过滤该瀑布流下价格小于等于50的三方广告源
//         * 13、针对聚合广告位id:88888888 过滤该瀑布流下价格大于等于100的三方广告源
//         * 14、针对聚合广告位id:88888888 过滤该瀑布流下价格等于50的三方广告源
//         */
//        WindMillAd.sharedAds().addFilter(new WMWaterfallFilter("88888888")//针对这个聚合广告位进行的过滤
//                .equalTo(WMWaterfallFilter.KEY_CHANNEL_ID, "16")//gdt渠道id
//                .equalTo(WMWaterfallFilter.KEY_ADN_PLACEMENT_ID, "123")//渠道的广告位id
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_CHANNEL_ID, "13")//csj渠道id
//                .and()//与的关系：不开启新的过滤表达式:可写可不写
//                .equalTo(WMWaterfallFilter.KEY_ADN_PLACEMENT_ID, "456")//渠道的广告位id
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_CHANNEL_ID, "19")//快手渠道id
//                .in(WMWaterfallFilter.KEY_ADN_PLACEMENT_ID, Arrays.asList("123", "456", "789"))//渠道的广告位集合
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_CHANNEL_ID, "21")//百度渠道id
//                .or()//或的关系：开启一个新的过滤表达式了
//                .in(WMWaterfallFilter.KEY_ADN_PLACEMENT_ID, Arrays.asList("123", "456", "789"))
//                .or()//或的关系：开启一个新的过滤表达式了
//                .in(WMWaterfallFilter.KEY_CHANNEL_ID, Arrays.asList("19", "13", "16"))
//                .or()//或的关系：开启一个新的过滤表达式了
//                .in(WMWaterfallFilter.KEY_BIDDING_TYPE, Arrays.asList(WMWaterfallFilter.C2S, WMWaterfallFilter.S2S, WMWaterfallFilter.NORMAL))
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_BIDDING_TYPE, WMWaterfallFilter.C2S)
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_BIDDING_TYPE, WMWaterfallFilter.S2S)
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_BIDDING_TYPE, WMWaterfallFilter.NORMAL)
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_E_CPM, new WMWaterfallFilter.PriceInterval().withMinPrice(50).withMaxPrice(100).toString())
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_E_CPM, new WMWaterfallFilter.PriceInterval().withMinPrice(50).toString())
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_E_CPM, new WMWaterfallFilter.PriceInterval().withMaxPrice(100).toString())
//                .or()//或的关系：开启一个新的过滤表达式了
//                .equalTo(WMWaterfallFilter.KEY_E_CPM, new WMWaterfallFilter.PriceInterval().withMinPrice(50).withMaxPrice(50).toString())
//        );

//        WMNetworkConfig.Builder builder = (new WMNetworkConfig.Builder())
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.ADMOB))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.REKLAMUP))
//                .addInitConfig(new WMAdnInitConfig(23, "appId"))
//                .addInitConfig(new WMAdnInitConfig(24, "appId"))
//                .addInitConfig(new WMAdnInitConfig(25, "appId"))
//                .addInitConfig(new WMAdnInitConfig(26, "appId"))//异步
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.VUNGLE, "appId"))//异步
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.UNITYADS, "appId"))//异步
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.IRONSOURCE, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.TOUTIAO, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.GDT, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.KUAISHOU, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.KLEVIN, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.BAIDU, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.GROMORE, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.ADSCOPE, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.QUMENG, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.PANGLE, "appId"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.APPLOVIN, "appKey"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.APPLOVIN_MAX, "appKey"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.MOBVISTA, "appId", "appKey"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.SIGMOB, "appId", "appKey"))
//                .addInitConfig(new WMAdnInitConfig(WMNetworkConfig.TAPTAP, "appId", "appKey"));

        ads.setInitNetworkConfig(builder.build());

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

        Actor.getInstance().registerBiddingInfo(new Actor.XBiddingInfo() {
            @Override
            public Map<String, String> getBidInfoFromYou(int channelId, boolean isWin, Map<String, Object> referBidInfo, Map<String, String> resultBidInfo) {
                Log.d("lance", "----------getBidInfoFromYou----------" + channelId + ":" + isWin + ":" + referBidInfo + ":" + resultBidInfo);
                if (resultBidInfo != null) {
                    resultBidInfo.put("key", "value");
                    return resultBidInfo;
                }
                return null;
            }
        });

        ads.startWithAppId(this, appId, new WMAdConfig.Builder().customController(new WMCustomController() {
            @Override
            public boolean isCanUseLocation() {
                if (isCanUseLocation != null) {
                    return isCanUseLocation;
                }
                return super.isCanUseLocation();
            }

            @Override
            public Location getLocation() {
                if (getLocation != null) {
                    return getLocation;
                }
                return super.getLocation();
            }

            @Override
            public boolean isCanUsePhoneState() {
                if (isCanUsePhoneState != null) {
                    return isCanUsePhoneState;
                }
                return super.isCanUsePhoneState();
            }

            @Override
            public String getDevImei() {
                if (!TextUtils.isEmpty(getDevImei)) {
                    return getDevImei;
                }
                return super.getDevImei();
            }

            @Override
            public boolean isCanUseAndroidId() {
                if (isCanUseAndroidId != null) {
                    return isCanUseAndroidId;
                }
                return super.isCanUseAndroidId();
            }

            @Override
            public String getAndroidId() {
                if (!TextUtils.isEmpty(getAndroidId)) {
                    return getAndroidId;
                }
                return super.getAndroidId();
            }

            @Override
            public String getDevOaid() {
                if (!TextUtils.isEmpty(getDevOaid)) {
                    return getDevOaid;
                }
                return super.getDevOaid();
            }

            @Override
            public boolean isCanUseWifiState() {
                if (isCanUseWifiState != null) {
                    return isCanUseWifiState;
                }
                return super.isCanUseWifiState();
            }

            @Override
            public String getMacAddress() {
                if (!TextUtils.isEmpty(getMacAddress)) {
                    return getMacAddress;
                }
                return super.getMacAddress();
            }

            @Override
            public boolean isCanUseWriteExternal() {
                if (isCanUseWriteExternal != null) {
                    return isCanUseWriteExternal;
                }
                return super.isCanUseWriteExternal();
            }

            @Override
            public boolean isCanUseAppList() {
                if (isCanUseAppList != null) {
                    return isCanUseAppList;
                }
                return super.isCanUseAppList();
            }

            @Override
            public List<PackageInfo> getInstalledPackages() {
                return super.getInstalledPackages();
            }

            @Override
            public boolean isCanUsePermissionRecordAudio() {
                if (isCanUsePermissionRecordAudio != null) {
                    return isCanUsePermissionRecordAudio;
                }
                return super.isCanUsePermissionRecordAudio();
            }
        }).build());
    }

    // Called before we send every request.
    @SuppressLint("MissingPermission")
    private Location getAppLocation() {
        Location lastLocation = null;

        try {
            if (this.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED || this.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
                // Get lat, long failFrom any GPS information that might be currently
                // available
                LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

                for (String provider_name : lm.getProviders(true)) {
                    Location l = lm.getLastKnownLocation(provider_name);
                    if (l == null) {
                        continue;
                    }

                    if (lastLocation == null) {
                        lastLocation = l;
                    } else {
                        if (l.getTime() > 0 && lastLocation.getTime() > 0) {
                            if (l.getTime() > lastLocation.getTime()) {
                                lastLocation = l;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastLocation;
    }

    public static void initPlacementCustomMap(Context context, String placementId) {
        if (!TextUtils.isEmpty(placementId)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("setting", 0);
            Set<String> groupList = sharedPreferences.getStringSet(placementId, null);
            if (groupList != null) {
                Log.d("lance", placementId + "------initPlacementCustomMap------" + groupList);
                Map<String, String> customMap = new HashMap<>();
                Iterator<String> iterator = groupList.iterator();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    if (!TextUtils.isEmpty(next)) {
                        String[] split = next.split("-");
                        if (split.length == 2) {
                            customMap.put(split[0], split[1]);
                        }
                    }
                }
                WindMillAd.sharedAds().initPlacementCustomMap(placementId, customMap);
            }
        }
    }

    public static void filterChannelId(Context context, String placementId) {
        if (!TextUtils.isEmpty(placementId)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("setting", 0);
            String initString = sharedPreferences.getString(Constants.INIT_SETTING, "");
            if (!TextUtils.isEmpty(initString)) {
                try {
                    JSONObject jsonObject = new JSONObject(initString);
                    Iterator<String> it = jsonObject.keys();
                    List<String> ids = new ArrayList<>();
                    while (it.hasNext()) {
                        String key = it.next();
                        JSONObject object = jsonObject.getJSONObject(key);
                        String app_id = object.optString("appId");
                        String app_Key = object.optString("appKey");
                        if (TextUtils.isEmpty(app_id) && TextUtils.isEmpty(app_Key)) {
                            ids.add(key);
                            Log.d("lance", "filterChannelId: " + key);
                        }
                    }
                    WindMillAd.sharedAds().setFilterNetworkFirmIdList(placementId, ids);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
