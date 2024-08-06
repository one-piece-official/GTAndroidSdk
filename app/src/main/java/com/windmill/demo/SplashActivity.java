package com.windmill.demo;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.api.SplashAdListener;
import com.gt.sdk.splash.SplashAd;
import com.windmill.demo.utils.PxUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity implements SplashAdListener {

    /**
     * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
     */
    public boolean canJumpImmediately = false;
    private SplashAd splashAd;
    private ArrayList<String> logs;
    private ViewGroup mViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mViewGroup = findViewById(R.id.splash_container);

        logs = new ArrayList<>();
        logs.add("init SDK appId :" + GtAdSdk.sharedAds().getAppId());

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);

        String userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");

        Map<String, String> options = new HashMap<>();
        options.put("user_id", userID);

        AdRequest adRequest = new AdRequest.Builder()
                .setCodeId("splash_placement_id")
                .setUserID(userID)
                .setWidth(PxUtils.getRealMetrics(this).widthPixels)
                .setHeight(PxUtils.getRealMetrics(this).heightPixels - dipsToIntPixels(100, this))
                .setExtOption(options)
                .build();

        splashAd = new SplashAd(adRequest, this);
        splashAd.loadAd();
    }

    public static int dipsToIntPixels(final float dips, final Context context) {
        return (int) (dips * context.getResources().getDisplayMetrics().density + 0.5f);
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
    public void onSplashAdShow(String placementId) {
        Log.d("lance", "----------onSplashAdShow----------" + placementId);
        logs.add("onSplashAdShow");
    }

    @Override
    public void onSplashAdLoadSuccess(String placementId) {
        Log.d("lance", "----------onSplashAdLoadSuccess----------" + splashAd.isReady() + ":" + placementId);
        Toast.makeText(this, "onSplashAdLoadSuccess:" + splashAd.isReady(), Toast.LENGTH_SHORT).show();
        logs.add("onSplashAdLoadSuccess:" + splashAd.isReady());

        if (splashAd != null && splashAd.isReady()) {
            splashAd.show(mViewGroup);
        }
    }

    @Override
    public void onSplashAdLoadFail(String placementId, AdError error) {
        Log.d("lance", "----------onSplashAdLoadFail----------" + error.toString() + ":" + placementId);
        logs.add("onSplashAdLoadFail: " + error + " placementId: " + placementId);
        jumpMainActivity();
    }

    @Override
    public void onSplashAdShowError(String placementId, AdError error) {
        Log.d("lance", "----------onSplashAdShowError----------" + error.toString() + ":" + placementId);
        logs.add("onSplashAdShowError: " + error + " placementId: " + placementId);
        jumpMainActivity();
    }

    @Override
    public void onSplashAdClick(String placementId) {
        Log.d("lance", "----------onSplashAdClick----------" + placementId);
        logs.add("onSplashAdClick");
    }

    @Override
    public void onSplashAdClose(String placementId) {
        Log.d("lance", "----------onSplashAdClose----------" + placementId);
        logs.add("onSplashAdClose");
        jumpWhenCanClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashAd != null) {
            splashAd.destroyAd();
            splashAd = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
