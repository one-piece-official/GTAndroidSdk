package com.wind.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindAgeRestrictedUserStatus;
import com.sigmob.windad.WindConsentStatus;
import com.sigmob.windad.WindCustomController;
//import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import leakcanary.EventListener;
import leakcanary.LeakCanary;

public class MyApplication extends MultiDexApplication {


    @Override
    public void onCreate() {
        super.onCreate();

//        CrashReport.initCrashReport(getApplicationContext(), "4c41e5eed0", true);//4c41e5eed0//4ee13aff7b
        List<EventListener> eventListeners = LeakCanary.getConfig().getEventListeners();
        ArrayList list = new ArrayList(eventListeners);
        list.add(new SentryLeakUploader());
        LeakCanary.Config config = LeakCanary.getConfig().newBuilder().eventListeners(list).build();
        LeakCanary.setConfig(config);
//        new DoKit.Builder(this)
//                .productId("需要使用平台功能的话，需要到DoKit.cn平台申请id")
//                .build();

        initSDK();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }

    private String loadOaidCertPem(Context context) {
        try {

            InputStream is;
            String defaultPemCert = context.getPackageName() + ".cert.pem";
            AssetManager assetManager = context.getAssets();

            is = assetManager.open(defaultPemCert);

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
            return builder.toString();

        } catch (Throwable th) {

        }
        return "";
    }

    private void initSDK() {
        WindAds ads = WindAds.sharedAds();

        //enable or disable debug log

        final SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);

        Constants.scene_id = sharedPreferences.getString(Constants.CONF_SCENE_ID, "");
        Constants.scene_desc = sharedPreferences.getString(Constants.CONF_SCENE_DESC, "");

        Constants.user_age = sharedPreferences.getString(Constants.CONF_AGE, "");
        Constants.user_id = sharedPreferences.getString(Constants.CONF_USER_ID, "");
        Constants.native_image_res = sharedPreferences.getBoolean(Constants.CONF_NATIVE_IMAGE_RES, false);


        boolean custom_device = sharedPreferences.getBoolean(Constants.CONF_CUSTOM_DEVICE, false);
        String appId = sharedPreferences.getString(Constants.CONF_APP_ID, "");
        String appKey = sharedPreferences.getString(Constants.CONF_APP_KEY, "");


        WindAds.setOAIDCertPem(loadOaidCertPem(this));


        boolean isAdult = sharedPreferences.getBoolean(Constants.CONF_ADULT, true);
        boolean isPersonalizedAdvertisingOn = sharedPreferences.getBoolean(Constants.CONF_PERSONALIZED, true);
        String gdpr_str = sharedPreferences.getString(Constants.CONF_GDPR, "0");
        int coppa_str = sharedPreferences.getInt(Constants.CONF_COPPA, 0);
        int config_url = sharedPreferences.getInt(Constants.CONF_URL, 0);

        Log.d("WindSDK", "gdpr_str:" + gdpr_str);
        Log.d("WindSDK", "coppa_str:" + coppa_str);

        switch (gdpr_str) {
            case "0":
                ads.setUserGDPRConsentStatus(WindConsentStatus.UNKNOWN);
                break;
            case "1":
                ads.setUserGDPRConsentStatus(WindConsentStatus.ACCEPT);
                break;
            case "2":
                ads.setUserGDPRConsentStatus(WindConsentStatus.DENIED);
                break;
        }

        switch (coppa_str) {
            case 0:
                ads.setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.Unknown);
                break;
            case 1:
                ads.setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.YES);
                break;
            case 2:
                ads.setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.NO);
                break;
        }

        int age = -1;
        try {
            age = Integer.parseInt(Constants.user_age);
        } catch (Throwable th) {

        }


        ads.setUserAge(age);
        ads.setAdult(isAdult);
        ads.setPersonalizedAdvertisingOn(isPersonalizedAdvertisingOn);

        WindAdOptions options = new WindAdOptions(appId, appKey);

        HashMap<String, String> extData = new HashMap<>();
        switch (config_url) {
            case 0: {
                if (extData != null) {
                    extData.remove(ConfigurationFragment.SIGDEMO_CONF_URL);
                }
            }
            break;
            case 1: {
                if (extData != null) {
                    extData.put(ConfigurationFragment.SIGDEMO_CONF_URL, ConfigurationFragment.CONFIG_URL_NORMAL);
                }
            }
            break;
            case 2: {
                if (extData != null) {
                    extData.put(ConfigurationFragment.SIGDEMO_CONF_URL, ConfigurationFragment.CONFIG_URL_TEST);
                }
            }
            break;
            case 3: {
                if (extData != null) {
                    extData.put(ConfigurationFragment.SIGDEMO_CONF_URL, ConfigurationFragment.CONFIG_URL_MOCK);
                }
            }
            break;
        }


        if (custom_device) {
            options.setCustomController(new WindCustomController() {
                @Override
                public boolean isCanUseLocation() {
                    return sharedPreferences.getBoolean(Constants.CONF_CAN_LOCATION, true);

                }

                @Override
                public Location getLocation() {
                    String str = sharedPreferences.getString(Constants.CONF_CUSTOM_LOCATION, "");
                    if (!TextUtils.isEmpty(str)) {
                        String[] list = str.split(",");
                        if (list.length == 2) {
                            Location location = new Location("");
                            location.setLatitude(Integer.parseInt(list[0]));
                            location.setLongitude(Integer.parseInt(list[1]));

                            return location;
                        }
                    }
                    return null;

                }

                @Override
                public boolean isCanUsePhoneState() {
                    return sharedPreferences.getBoolean(Constants.CONF_CAN_PHONESTATE, true);

                }

                @Override
                public String getDevImei() {
                    return sharedPreferences.getString(Constants.CONF_CUSTOM_IMEI, "");
                }

                @Override
                public boolean isCanUseAndroidId() {
                    return sharedPreferences.getBoolean(Constants.CONF_CAN_ANDROIDID, true);

                }

                @Override
                public String getAndroidId() {
                    return sharedPreferences.getString(Constants.CONF_CUSTOM_ANDROIDID, "");
                }

                @Override
                public String getDevOaid() {
                    return sharedPreferences.getString(Constants.CONF_CUSTOM_OAID, "");
                }

                @Override
                public boolean isCanUseAppList() {
                    return sharedPreferences.getBoolean(Constants.CONF_CAN_APPLIST, true);
                }
                @Override
                public List<PackageInfo> getInstallPackageInfoList() {

                    Context applicationContext = getApplicationContext();
                    if (applicationContext != null){
                        PackageManager packageManager = applicationContext.getPackageManager();
                        return packageManager.getInstalledPackages(0);
                    }
                    return super.getInstallPackageInfoList();
                }
            });

        }

        options.setExtData(extData);

        ads.startWithOptions(this, options);
    }

}