package com.windmill.demo;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.windmill.demo.splash.SplashEyeAdHolder;
import com.windmill.demo.splash.SplashZoomOutManager;
import com.windmill.demo.utils.PxUtils;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.models.AdInfo;
import com.windmill.sdk.splash.IWMSplashEyeAd;
import com.windmill.sdk.splash.WMSplashAd;
import com.windmill.sdk.splash.WMSplashAdListener;
import com.windmill.sdk.splash.WMSplashAdRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity implements WMSplashAdListener {

    /**
     * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
     */
    public boolean canJumpImmediately = false;
    private WMSplashAd splashAd;
    private ArrayList<String> logs;
    private ViewGroup mViewGroup;
    private String userID;

    private void initSDK() {
//        WindMillAd ads = WindMillAd.sharedAds();

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);
        String appId = sharedPreferences.getString(Constants.CONF_APP_ID, "");
        logs = new ArrayList<>();
        logs.add("init SDK appId :" + appId);

//        boolean isAdult = sharedPreferences.getBoolean(Constants.CONF_ADULT, true);
//        boolean isPersonalizedAdvertisingOn = sharedPreferences.getBoolean(Constants.CONF_PERSONALIZED, true);
//        boolean isSdkLogEnable = sharedPreferences.getBoolean(Constants.CONF_SDK_LOG, true);
//        String gdpr_str = sharedPreferences.getString(Constants.CONF_GDPR, "0");
//        int coppa_str = sharedPreferences.getInt(Constants.CONF_COPPA, 0);
//        Log.d("lance", "gdpr_str:" + gdpr_str);
//        Log.d("lance", "coppa_str:" + coppa_str);
//
//        switch (gdpr_str) {
//            case "0":
//                ads.setUserGDPRConsentStatus(WindMillConsentStatus.UNKNOWN);
//                break;
//            case "1":
//                ads.setUserGDPRConsentStatus(WindMillConsentStatus.ACCEPT);
//                break;
//            case "2":
//                ads.setUserGDPRConsentStatus(WindMillConsentStatus.DENIED);
//                break;
//        }
//
//        switch (coppa_str) {
//            case 0:
//                ads.setIsAgeRestrictedUser(WindMillUserAgeStatus.WindAgeRestrictedStatusUnknown);
//                break;
//            case 1:
//                ads.setIsAgeRestrictedUser(WindMillUserAgeStatus.WindAgeRestrictedStatusYES);
//                break;
//            case 2:
//                ads.setIsAgeRestrictedUser(WindMillUserAgeStatus.WindAgeRestrictedStatusNO);
//                break;
//        }
//
//        ads.setUserAge(18);
//        ads.setAdult(isAdult);
//        ads.setPersonalizedAdvertisingOn(isPersonalizedAdvertisingOn);
//        ads.setDebugEnable(isSdkLogEnable);
//
//        ads.startWithAppId(this, appId);
    }

    public static int dipsToIntPixels(final float dips, final Context context) {
        return (int) (dips * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mViewGroup = findViewById(R.id.splash_container);

        initSDK();

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);

        String splash_placement_id = "";

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, "");
        Log.d("lance", "configJson:" + configJson);
        if (!TextUtils.isEmpty(configJson)) {
            try {
                JSONObject jsonObject = new JSONObject(configJson);
                JSONObject dataJson = jsonObject.getJSONObject("data");
                JSONArray array = dataJson.optJSONArray("slotIds");
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject slotId = array.getJSONObject(i);
                        if (slotId != null) {
                            int ad_type = slotId.optInt("adType", -1);
                            if (ad_type == 2) {
                                if (TextUtils.isEmpty(splash_placement_id)) {
                                    splash_placement_id = slotId.optString("adSlotId");
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        WindMillAd.sharedAds().reportSceneExposure("789", "开屏场景");

        boolean selfLogo = sharedPreferences.getBoolean(Constants.CONF_SELF_LOGO, false);
        boolean halfSplash = sharedPreferences.getBoolean(Constants.CONF_HALF_SPLASH, false);
        String appTitle = sharedPreferences.getString(Constants.CONF_APP_TITLE, "开心消消乐");
        String appDesc = sharedPreferences.getString(Constants.CONF_APP_DESC, "你的快乐由我负责");
        userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");

        /**
         * 是否现实自定义的Logo
         * 一旦使用自定义logo相当于mViewGroup = null
         */
        if (!selfLogo) {
            appTitle = "";
            appDesc = "";
        }
        /**
         * 是否需要使用外部的容器
         */
        if (halfSplash) {
            mViewGroup = null;
        }

        Map<String, Object> options = new HashMap<>();
        options.put("user_id", userID);
        options.put(WMConstants.AD_WIDTH, PxUtils.getRealMetrics(this).widthPixels);//针对于穿山甲、GroMore、AdScope开屏有效、单位px
//        options.put(WMConstants.AD_HEIGHT, this.getResources().getDisplayMetrics().heightPixels);//针对于穿山甲、GroMore、AdScope开屏有效、单位px
        options.put(WMConstants.AD_HEIGHT, PxUtils.getRealMetrics(this).heightPixels - dipsToIntPixels(100, this));//针对于穿山甲、GroMore、AdScope开屏有效、单位px

        WMSplashAdRequest adRequest = new WMSplashAdRequest(splash_placement_id, userID, options, appTitle, appDesc, true);

        splashAd = new WMSplashAd(this, adRequest, this);
        Log.d("lance", "------------start--------loadAd-------" + System.currentTimeMillis());
        splashAd.loadAdAndShow(mViewGroup);
    }

    private void jumpWhenCanClick() {
        if (canJumpImmediately) {
            jumpMainActivity();
        } else {
            canJumpImmediately = true;
        }
    }

    /**
     * 不可点击的开屏，使用该jump方法，而不是用jumpWhenCanClick
     */
    private void jumpMainActivity() {

        if (SplashEyeAdHolder.splashEyeAd != null) {
            try {
                SplashZoomOutManager zoomOutManager = SplashZoomOutManager.getInstance(getApplicationContext());
                zoomOutManager.setSplashInfo(SplashEyeAdHolder.splashEyeAd.getSplashView(), getWindow().getDecorView());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String[] list = logs.toArray(new String[logs.size()]);
        intent.putExtra("logs", list);
        Log.d("lance", "------------startActivity---------------" + System.currentTimeMillis());
        startActivity(intent);

        overridePendingTransition(0, 0);

        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJumpImmediately = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJumpImmediately) {
            jumpWhenCanClick();
        }
        canJumpImmediately = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashAd != null) {
            splashAd.destroy();
            splashAd = null;
        }
    }

    @Override
    public void onSplashAdSuccessPresent(AdInfo adInfo) {
        Log.d("lance", "----------onSplashAdSuccessPresent----------" + adInfo.toString());
        logs.add("onSplashAdSuccessPresent");
    }

    @Override
    public void onSplashAdSuccessLoad(String placementId) {
        Log.d("lance", "----------onSplashAdSuccessLoad----------" + splashAd.isReady() + ":" + placementId);
        Toast.makeText(this, "onSplashAdSuccessLoad:" + splashAd.isReady(), Toast.LENGTH_SHORT).show();
        logs.add("onSplashAdSuccessLoad:" + splashAd.isReady());
    }

    @Override
    public void onSplashAdFailToLoad(WindMillError error, String placementId) {
        Log.d("lance", "------------start--------loadAd---fail----" + System.currentTimeMillis());
        Log.d("lance", "----------onSplashAdFailToLoad----------" + error.toString() + ":" + placementId);
        logs.add("onSplashAdFailToLoad: " + error + " placementId: " + placementId);
        jumpMainActivity();
    }

    @Override
    public void onSplashAdClicked(AdInfo adInfo) {
        Log.d("lance", "----------onSplashAdClicked----------" + adInfo.toString());
        logs.add("onSplashAdClicked");
    }

    @Override
    public void onSplashClosed(AdInfo adInfo, IWMSplashEyeAd splashEyeAd) {
        Log.d("lance", "----------onSplashClosed----------" + adInfo.toString());
        logs.add("onSplashClosed");
        SplashEyeAdHolder.splashEyeAd = splashEyeAd;
        jumpWhenCanClick();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
