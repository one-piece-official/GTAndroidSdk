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

import com.gt.adsdk.AdRequest;
import com.gt.adsdk.api.SplashAdListener;
import com.gt.adsdk.splash.SplashAd;
import com.sigmob.windad.WindAdError;
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

public class SplashActivity extends AppCompatActivity implements SplashAdListener {

    /**
     * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
     */
    public boolean canJumpImmediately = false;
    private SplashAd splashAd;
    private ArrayList<String> logs;
    private ViewGroup mViewGroup;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mViewGroup = findViewById(R.id.splash_container);

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);

        userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");

        Map<String, String> options = new HashMap<>();
        options.put("user_id", userID);

        AdRequest adRequest = new AdRequest.Builder()
                .setCodeId("splash_placement_id")
                .setUserID(userID)
                .setWidth(PxUtils.getRealMetrics(this).widthPixels)
                .setHeight(PxUtils.getRealMetrics(this).heightPixels - dipsToIntPixels(100, this))
                .setExtOption(options).build();

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
    }

    @Override
    public void onSplashAdLoadFail(String placementId, WindAdError error) {
        Log.d("lance", "----------onSplashAdLoadFail----------" + error.toString() + ":" + placementId);
        logs.add("onSplashAdLoadFail: " + error + " placementId: " + placementId);
        jumpMainActivity();
    }

    @Override
    public void onSplashAdShowError(String placementId, WindAdError error) {
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
            splashAd.destroy();
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
