package com.sigmob.sigmob;

import static com.sigmob.sigmob.Constants.CONF_ADULT;
import static com.sigmob.sigmob.Constants.CONF_APP_ID;
import static com.sigmob.sigmob.Constants.CONF_APP_KEY;
import static com.sigmob.sigmob.Constants.CONF_GDPR;
import static com.sigmob.sigmob.Constants.CONF_PERSONALIZED;
import static com.sigmob.sigmob.Constants.USE_MEDIATION;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.didichuxing.doraemonkit.DoKit;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindAgeRestrictedUserStatus;
import com.sigmob.windad.WindConsentStatus;
import com.tencent.bugly.crashreport.CrashReport;


public class MyApplication extends MultiDexApplication {


    @Override
    public void onCreate() {
        super.onCreate();
        initSDK();
        CrashReport.initCrashReport(getApplicationContext(), "4c41e5eed0", true);//4c41e5eed0//4ee13aff7b

        new DoKit.Builder(this)
                .productId("需要使用平台功能的话，需要到DoKit.cn平台申请id")
                .build();
    }

    private void initSDK() {

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);
        String appId = sharedPreferences.getString(CONF_APP_ID, "");
        String appKey = sharedPreferences.getString(CONF_APP_KEY, "");

        boolean isUseMediation = sharedPreferences.getBoolean(USE_MEDIATION, false);
        boolean isGdpr = sharedPreferences.getBoolean(CONF_GDPR, false);
        boolean isAdult = sharedPreferences.getBoolean(CONF_ADULT, false);
        boolean isPersonalizedAdvertisingOn = sharedPreferences.getBoolean(CONF_PERSONALIZED, false);
        /**
         * 初始化SigMob
         */
        WindAds ads = WindAds.sharedAds();
        ads.setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.NO);
        ads.setUserAge(18);

        ads.setUserGDPRConsentStatus(isGdpr ? WindConsentStatus.ACCEPT : WindConsentStatus.DENIED);
        ads.setAdult(isAdult);
        ads.setPersonalizedAdvertisingOn(isPersonalizedAdvertisingOn);
        WindAdOptions options = new WindAdOptions(appId, appKey, isUseMediation);
        ads.startWithOptions(this, options);
        /**
         * 初始化rename SigMob
         */
        com.xmlywind.windad.WindAds windAds = com.xmlywind.windad.WindAds.sharedAds();

        windAds.setUserGDPRConsentStatus(isGdpr ? com.xmlywind.windad.WindConsentStatus.ACCEPT : com.xmlywind.windad.WindConsentStatus.DENIED);
        windAds.setAdult(isAdult);
        windAds.setPersonalizedAdvertisingOn(isPersonalizedAdvertisingOn);

        windAds.startWithOptions(this, new com.xmlywind.windad.WindAdOptions(appId, appKey, isUseMediation));

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);

    }
}
