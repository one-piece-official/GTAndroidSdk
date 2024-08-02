package com.sigmob.sigmob.rename;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.sigmob.sigmob.MainActivity;
import com.sigmob.sigmob.R;
import com.xmlywind.windad.Splash.WindSplashAD;
import com.xmlywind.windad.Splash.WindSplashADListener;
import com.xmlywind.windad.Splash.WindSplashAdRequest;
import com.xmlywind.windad.WindAdError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.sigmob.sigmob.Constants.CONF_APP_DESC;
import static com.sigmob.sigmob.Constants.CONF_APP_TITLE;
import static com.sigmob.sigmob.Constants.CONF_HALF_SPLASH;
import static com.sigmob.sigmob.Constants.CONF_JSON;
import static com.sigmob.sigmob.Constants.CONF_SELF_LOGO;

public class SplashRenameActivity extends AppCompatActivity {

    private ArrayList<String> logs = new ArrayList<>();
    private ViewGroup mViewGroup;
    private boolean isLoadAndShow = true;

    private WindSplashAD mWindSplashAD;
    private int userID = 0;

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

        setContentView(R.layout.activity_splash_rename);

        getExtraInfo();

        mViewGroup = findViewById(R.id.splash_container);

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);

        String splash_placement_id = "";

        String configJson = sharedPreferences.getString(CONF_JSON, "");
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

        boolean selfLogo = sharedPreferences.getBoolean(CONF_SELF_LOGO, false);
        boolean halfSplash = sharedPreferences.getBoolean(CONF_HALF_SPLASH, false);
        String appTitle = sharedPreferences.getString(CONF_APP_TITLE, "开心消消乐");
        String appDesc = sharedPreferences.getString(CONF_APP_DESC, "你的快乐由我负责");

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

        mWindSplashAD = new WindSplashAD(this, adRequest, new WindSplashADListener() {
            @Override
            public void onSplashAdClicked() {
                logs.add("onSplashAdClickedX");
            }

            @Override
            public void onSplashAdFailToLoad(WindAdError windAdError, String s) {
                logs.add("onSplashAdFailToLoadX: " + windAdError + " placementId: " + s);
                jumpMainActivity();
            }

            @Override
            public void onSplashAdSuccessLoad() {
                logs.add("onSplashAdSuccessLoadX:" + mWindSplashAD.isReady());
//                if (!isLoadAndShow && mWindSplashAD.isReady()) {
//                    mWindSplashAD.showAd();
//                }
                Toast.makeText(SplashRenameActivity.this, "onSplashAdSuccessLoadX", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSplashAdSuccessPresent() {
                logs.add("onSplashAdSuccessPresentX");

            }

            @Override
            public void onSplashClosed() {
                logs.add("onSplashClosedX");
                jumpWhenCanClick();
            }
        });
        logs.add("isLoadAndShowX:" + isLoadAndShow);
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

    /**
     * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
     */
    public boolean canJumpImmediately = false;

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
}