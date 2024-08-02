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
import com.sigmob.windad.WindAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends Activity implements WindSplashADListener {

    /**
     * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
     */
    public boolean canJumpImmediately = false;
    private WindSplashAD splashAd;
    private ArrayList<String> logs;
    private ViewGroup mViewGroup;
    private boolean isLoadAndShow = true;
    private boolean closeSplash;
    private boolean disable_autoHide;


    private void getExtraInfo() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        isLoadAndShow = intent.getBooleanExtra("isLoadAndShow", true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashAd != null) {
            splashAd.destroy();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        getExtraInfo();

        mViewGroup = findViewById(R.id.splash_container);

        logs = new ArrayList<>();
        logs.add("init SDK appId :" + WindAds.sharedAds().getAppId() + " appKey: " + WindAds.sharedAds().getAppKey());

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
                            int bidType = slotId.optInt("bidType", -1);

                            if (ad_type == 2 && bidType != 1) {
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
        closeSplash = sharedPreferences.getBoolean(Constants.CONF_CLOSE_SPLASH, false);
        disable_autoHide = sharedPreferences.getBoolean(Constants.CONF_DISABLE_AUTOHIDEAD, false);

        /**
         * 是否需要使用外部的容器
         */
        if (!halfSplash) {
            mViewGroup = findViewById(android.R.id.content);
        }

        Map<String, Object> options = new HashMap<>();
        options.put("user_id", Constants.user_id);

        WindSplashAdRequest adRequest = new WindSplashAdRequest(splash_placement_id, Constants.user_id, options);
        adRequest.setDisableAutoHideAd(disable_autoHide);

        splashAd = new WindSplashAD(adRequest, this);
        logs.add("isLoadAndShow:" + isLoadAndShow);

        String BID_DJ = sharedPreferences.getString(Constants.BID_DJ, "-1");
        String BID_BZ = sharedPreferences.getString(Constants.BID_BZ, "CNY");

        splashAd.setBidFloor(Integer.parseInt(BID_DJ));
        splashAd.setCurrency(BID_BZ);

        if (isLoadAndShow) {
            splashAd.loadAndShow(mViewGroup);
        } else {

            findViewById(R.id.splash_holder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    splashAd.loadAd();
                }
            });

            findViewById(R.id.app_logo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(splashAd.getEcpm())) {
                        splashAd.setBidEcpm(Integer.parseInt(splashAd.getEcpm()));
                    }

                    if (!isLoadAndShow && splashAd.isReady()) {
                        splashAd.show(mViewGroup);
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
        logs.add("onSplashAdSuccessPresent");
    }

    @Override
    public void onSplashAdLoadSuccess(String placementId) {
        logs.add("onSplashAdSuccessLoad:" + splashAd.isReady());
//        if (!isLoadAndShow && mWindSplashAD.isReady()) {
//            mWindSplashAD.showAd();
//        }
        Toast.makeText(SplashActivity.this, "onSplashAdSuccessLoad", Toast.LENGTH_LONG).show();
        noticeWin();
    }


    @Override
    public void onSplashAdLoadFail(WindAdError error, String placementId) {
        logs.add("onSplashAdFailToLoad: " + error + " placementId: " + placementId);
        jumpMainActivity();
    }

    @Override
    public void onSplashAdShowError(WindAdError error, String placementId) {
        logs.add("onSplashAdFailToPresent: " + error + " placementId: " + placementId);
        jumpMainActivity();

    }

    @Override
    public void onSplashAdClick(String placementId) {
        logs.add("onSplashAdClicked");
        noticeLoss();
    }


    @Override
    public void onSplashAdClose(String placementId) {
        logs.add("onSplashAdClose");


        if (mViewGroup != null) {
            mViewGroup.removeAllViews();
        }

        if (!disable_autoHide) {
            if (closeSplash) {
                jumpMainActivity();
            } else {
                jumpWhenCanClick();
            }
        }

    }

    @Override
    public void onSplashAdSkip(String placementId) {
        logs.add("onSplashAdSkip");
    }


    private void noticeWin() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        String AUCTION_PRICE = sharedPreferences.getString(Constants.BID_JS, "-1");
        String HIGHEST_LOSS_PRICE = sharedPreferences.getString(Constants.BID_CGJ, "-1");
        String CURRENCY = sharedPreferences.getString(Constants.BID_BZ, "CNY");

        Log.d("WindSDK", "---------noticeWin---------" + AUCTION_PRICE + ":" + HIGHEST_LOSS_PRICE + ":" + CURRENCY);

        Map<String, Object> map = new HashMap<>();
        map.put(WindAds.AUCTION_PRICE, Integer.parseInt(AUCTION_PRICE));
        map.put(WindAds.HIGHEST_LOSS_PRICE, Integer.parseInt(HIGHEST_LOSS_PRICE));
        map.put(WindAds.CURRENCY, CURRENCY);

        splashAd.sendWinNotificationWithInfo(map);
    }

    private void noticeLoss() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        String AUCTION_PRICE = sharedPreferences.getString(Constants.BID_JS, "-1");
        String CURRENCY = sharedPreferences.getString(Constants.BID_BZ, "CNY");
        String LOSS_REASON = sharedPreferences.getString(Constants.BID_SB, "-1");
        String ADN_ID = sharedPreferences.getString(Constants.BID_JSF, "-1");

        Log.d("WindSDK", "---------noticeLoss---------" + AUCTION_PRICE + ":" + LOSS_REASON + ":" + CURRENCY + ":" + ADN_ID);

        Map<String, Object> map = new HashMap<>();
        map.put(WindAds.AUCTION_PRICE, Integer.parseInt(AUCTION_PRICE));
        map.put(WindAds.CURRENCY, CURRENCY);
        map.put(WindAds.LOSS_REASON, Integer.parseInt(LOSS_REASON));
        map.put(WindAds.ADN_ID, ADN_ID);

        splashAd.sendLossNotificationWithInfo(map);
    }

}
