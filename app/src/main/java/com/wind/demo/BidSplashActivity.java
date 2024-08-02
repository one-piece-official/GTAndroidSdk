package com.wind.demo;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sigmob.windad.Splash.WindSplashAD;
import com.sigmob.windad.Splash.WindSplashADListener;
import com.sigmob.windad.Splash.WindSplashAdRequest;
import com.sigmob.windad.WindAdError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BidSplashActivity extends Activity implements WindSplashADListener {

    /**
     * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
     */
    public boolean canJumpImmediately = false;
    private WindSplashAD splashAd;
    private ArrayList<String> logs = new ArrayList<>();
    private ViewGroup mViewGroup;
    private boolean isLoadAndShow = true;


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

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);

        String splash_placement_id = "";

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, Constants.TEST_CONF_JSON);
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
                            int bidType = slotId.optInt("bidType", -1);

                            if (ad_type == 2 && bidType == 1) {
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

        boolean halfSplash = sharedPreferences.getBoolean(Constants.CONF_HALF_SPLASH, false);

        /**
         * 是否需要使用外部的容器
         */
        if (!halfSplash) {
            mViewGroup = findViewById(android.R.id.content);
        }
        final String appId = sharedPreferences.getString(Constants.CONF_APP_ID, "6878");

        Map<String, Object> options = new HashMap<>();
        options.put("user_id", Constants.user_id);

        WindSplashAdRequest adRequest = new WindSplashAdRequest(splash_placement_id, Constants.user_id, options);

        splashAd = new WindSplashAD(adRequest, this);
        logs.add("isLoadAndShow:" + isLoadAndShow);
        if (isLoadAndShow) {

            S2SBiddingUtils.requestBiddingToken(this, appId, splash_placement_id, 1, new S2SBiddingUtils.RequestTokenCallBack() {
                @Override
                public void onSuccess(String token) {
                    splashAd.loadAndShow(token, mViewGroup);
                }
            });
        }

        final String finalSplash_placement_id = splash_placement_id;
        findViewById(R.id.splash_holder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                S2SBiddingUtils.requestBiddingToken(v.getContext(), appId, finalSplash_placement_id, 1, new S2SBiddingUtils.RequestTokenCallBack() {
                    @Override
                    public void onSuccess(String token) {
                        splashAd.loadAd(token);
                    }
                });
            }
        });

        findViewById(R.id.app_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (splashAd.isReady()) {
                    splashAd.show(mViewGroup);
                }
            }
        });
    }

    private void jumpWhenCanClick() {
        if (canJumpImmediately) {
            jumpMainActivity();
        } else {
            canJumpImmediately = true;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashAd != null) {
            splashAd.destroy();
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
    public void onSplashAdShow(String placementId) {
        logs.add("onSplashAdShow : " + " placementId: " + placementId);
    }

    @Override
    public void onSplashAdLoadSuccess(String placementId) {
        logs.add("onSplashAdLoadSuccess: " + " placementId: " + placementId + "ready status" + splashAd.isReady());
//        if (!isLoadAndShow && mWindSplashAD.isReady()) {
//            mWindSplashAD.showAd();
//        }
        Toast.makeText(BidSplashActivity.this, "onSplashAdLoadSuccess", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSplashAdLoadFail(WindAdError error, String placementId) {
        logs.add("onSplashAdFailToLoad: " + error + " placementId: " + placementId);
        Toast.makeText(BidSplashActivity.this, "onSplashAdLoadFail" + error, Toast.LENGTH_LONG).show();
        jumpMainActivity();

    }

    @Override
    public void onSplashAdShowError(WindAdError error, String placementId) {
        logs.add("onSplashAdFailToPresent: " + error + " placementId: " + placementId);
        jumpMainActivity();
    }

    @Override
    public void onSplashAdClick(String placementId) {
        logs.add("onSplashAdClick :" + " placementId: " + placementId);
    }

    @Override
    public void onSplashAdClose(String placementId) {
        logs.add("onSplashAdClose :" + " placementId: " + placementId);
        jumpWhenCanClick();
    }

    @Override
    public void onSplashAdSkip(String placementId) {
        logs.add("onSplashSkip :" + " placementId: " + placementId);

    }
}
