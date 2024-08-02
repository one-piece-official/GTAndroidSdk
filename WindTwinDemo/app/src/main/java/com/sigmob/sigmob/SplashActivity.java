package com.sigmob.sigmob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sigmob.windad.Splash.WindSplashAD;
import com.sigmob.windad.Splash.WindSplashADListener;
import com.sigmob.windad.Splash.WindSplashAdRequest;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindAgeRestrictedUserStatus;
import com.sigmob.windad.WindConsentStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity implements WindSplashADListener {

    /**
     * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
     */
    public boolean canJumpImmediately = false;
    private WindSplashAD mWindSplashAD;
    private ArrayList<String> logs;
    private ViewGroup mViewGroup;
    private boolean isLoadAndShow = true;
    private int userID = 0;

    private void initSDK() {
        WindAds ads = WindAds.sharedAds();

        //enable or disable debug log

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);
        String appId = sharedPreferences.getString(Constants.CONF_APP_ID, "");
        String appKey = sharedPreferences.getString(Constants.CONF_APP_KEY, "");

        logs = new ArrayList<String>();
        logs.add("init SDK appId :" + appId + " appKey: " + appKey);

        boolean isUseMediation = sharedPreferences.getBoolean(Constants.USE_MEDIATION, false);
        boolean isGdpr = sharedPreferences.getBoolean(Constants.CONF_GDPR, false);
        boolean isAdult = sharedPreferences.getBoolean(Constants.CONF_ADULT, true);
        boolean isPersonalizedAdvertisingOn = sharedPreferences.getBoolean(Constants.CONF_PERSONALIZED, true);

        ads.setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.NO);
        ads.setUserAge(18);

        ads.setUserGDPRConsentStatus(isGdpr ? WindConsentStatus.ACCEPT : WindConsentStatus.DENIED);
        ads.setAdult(isAdult);
        ads.setPersonalizedAdvertisingOn(isPersonalizedAdvertisingOn);
        ads.setUserGDPRConsentStatus(isGdpr ? WindConsentStatus.ACCEPT : WindConsentStatus.DENIED);

        WindAdOptions options = new WindAdOptions(appId, appKey, isUseMediation);
        ads.startWithOptions(this, options);
    }

    private void getExtraInfo() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        isLoadAndShow = intent.getBooleanExtra("isLoadAndShow", true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        getExtraInfo();

        mViewGroup = findViewById(R.id.splash_container);

        initSDK();

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);

        String splash_placement_id = "";

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, "");
        Log.d("WindSDK", "configJson:" + configJson);
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

        boolean selfLogo = sharedPreferences.getBoolean(Constants.CONF_SELF_LOGO, false);
        boolean halfSplash = sharedPreferences.getBoolean(Constants.CONF_HALF_SPLASH, false);
        String appTitle = sharedPreferences.getString(Constants.CONF_APP_TITLE, "开心消消乐");
        String appDesc = sharedPreferences.getString(Constants.CONF_APP_DESC, "你的快乐由我负责");

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
        options.put("user_id", String.valueOf(userID));

        WindSplashAdRequest adRequest = new WindSplashAdRequest(splash_placement_id, String.valueOf(userID), options, 5, appTitle, appDesc, true);

        mWindSplashAD = new WindSplashAD(this, adRequest, this);
        logs.add("isLoadAndShow:" + isLoadAndShow);
        if (isLoadAndShow) {
            mWindSplashAD.loadAdAndShow(mViewGroup);
        } else {

            findViewById(R.id.splash_holder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWindSplashAD.loadAdOnly();
                }
            });

            findViewById(R.id.app_logo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isLoadAndShow && mWindSplashAD.isReady()) {
                        mWindSplashAD.showAd(mViewGroup);
                    }
                }
            });
        }

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

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String[] list = logs.toArray(new String[logs.size()]);

        intent.putExtra("logs", list);
        this.startActivity(intent);
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.e("WindSDK", "onPause:" + canJumpImmediately);
        canJumpImmediately = false;

    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.e("WindSDK", "onResume:" + canJumpImmediately);
        if (canJumpImmediately) {
            jumpWhenCanClick();
        }
        canJumpImmediately = true;
    }


    @Override
    public void onSplashAdSuccessPresent() {
        logs.add("onSplashAdSuccessPresent");
    }

    @Override
    public void onSplashAdSuccessLoad() {
        logs.add("onSplashAdSuccessLoad:" + mWindSplashAD.isReady());
//        if (!isLoadAndShow && mWindSplashAD.isReady()) {
//            mWindSplashAD.showAd();
//        }
        Toast.makeText(SplashActivity.this, "onSplashAdSuccessLoad", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSplashAdFailToLoad(WindAdError error, String placementId) {
        logs.add("onSplashAdFailToLoad: " + error + " placementId: " + placementId);
        jumpMainActivity();
    }

    @Override
    public void onSplashAdClicked() {
        logs.add("onSplashAdClicked");
    }

    @Override
    public void onSplashClosed() {
        logs.add("onSplashClosed");
        jumpWhenCanClick();
    }
}
