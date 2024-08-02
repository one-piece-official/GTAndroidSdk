package com.windmill.demo;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.banner.WMBannerAdListener;
import com.windmill.sdk.banner.WMBannerAdRequest;
import com.windmill.sdk.banner.WMBannerView;
import com.windmill.sdk.models.AdInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BannerActivity extends AppCompatActivity implements View.OnClickListener, WMBannerAdListener {

    private ViewGroup adContainer;
    private TextView logTextView;
    private String userID;
    private LinearLayout IdLayout;
    private Map<String, WMBannerView> bannerAdMap = new HashMap<>();
    private boolean isNewInstance;
    private int adWidth = 0;
    private int adHeight = 0;
    private EditText editTextWidth, editTextHeight; // 编辑框输入的宽高

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        IdLayout = this.findViewById(R.id.ll_placement);
        adContainer = findViewById(R.id.banner_ad_container);

        editTextWidth = (EditText) findViewById(R.id.editWidth);
        editTextHeight = (EditText) findViewById(R.id.editHeight);

        logTextView = this.findViewById(R.id.logView);
        logTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        logTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        updatePlacement();

        WindMillAd.sharedAds().reportSceneExposure("000", "Banner场景");
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        String text = (String) button.getText();
        String placementId = text.substring(text.indexOf("-") + 1);
        Log.d("lance", "---------onClick---------" + text);
        if (text.startsWith("LOAD")) {
            loadAd(placementId);
        } else {//SHOW
            showAd(placementId);
        }
    }

    private void loadAd(String placementId) {
        MyApplication.initPlacementCustomMap(this, placementId);
        MyApplication.filterChannelId(this, placementId);
        adWidth = Integer.valueOf(editTextWidth.getText().toString());
        adHeight = Integer.valueOf(editTextHeight.getText().toString());
        WMBannerView bannerView = bannerAdMap.get(placementId);
        Log.d("lance", (bannerView == null) + "---------loadAd---------" + placementId + ":" + adWidth + ":" + adHeight);
        Map<String, Object> options = new HashMap<>();
        options.put(WMConstants.AD_WIDTH, adWidth);//针对于模版广告有效、单位dp
        options.put(WMConstants.AD_HEIGHT, adHeight);//针对于模版广告有效、单位dp
        options.put("user_id", userID);
        if (bannerView != null) {
            if (isNewInstance) {
                bannerView.destroy();
                bannerAdMap.remove(placementId);
                bannerView = new WMBannerView(this);
                bannerView.setAdListener(this);
            }
        } else {
            bannerView = new WMBannerView(this);
            bannerView.setAdListener(this);
        }

        bannerView.setAutoAnimation(true);
        bannerView.loadAd(new WMBannerAdRequest(placementId, userID, options));

        bannerAdMap.put(placementId, bannerView);
    }

    private void showAd(String placementId) {
        WMBannerView bannerView = bannerAdMap.get(placementId);
        Log.d("lance", "---------showAd---------" + placementId);
        if (bannerView != null) {
            Log.d("lance", "---------getLoadFailMessages---------" + bannerView.getLoadFailMessages());
            List<AdInfo> adInfoList = bannerView.checkValidAdCaches();
            if (adInfoList != null && adInfoList.size() > 0) {
                for (int i = 0; i < adInfoList.size(); i++) {
                    AdInfo adInfo = adInfoList.get(i);
                    Log.d("lance", "---------showAd-----adInfo----:" + adInfo.toString());
                }
            }
        }
        if (bannerView != null && bannerView.isReady()) {
            //媒体最终将要展示广告的容器
            if (adContainer != null) {
                adContainer.removeAllViews();
                adContainer.addView(bannerView);
            }
        } else {
            logMessage("Ad is not Ready");
            Log.d("lance", "--------请先加载广告--------");
        }
    }

    private void updatePlacement() {

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, "");

        IdLayout.removeAllViews();

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
                            if (ad_type == 7 && bidType == 0) {

                                String adSlotId = slotId.optString("adSlotId");

                                LinearLayout ll = new LinearLayout(this);
                                ll.setOrientation(LinearLayout.HORIZONTAL);
                                ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                                Button loadB = new Button(this);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(0, 5, 0, 5);
                                layoutParams.weight = 1;
                                loadB.setLayoutParams(layoutParams);
                                loadB.setOnClickListener(this);
                                loadB.setText("LOAD-" + adSlotId);
                                loadB.setTextSize(12);
                                ll.addView(loadB);

                                Button playB = new Button(this);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.setMargins(0, 5, 0, 5);
                                params.weight = 1;
                                playB.setLayoutParams(params);
                                playB.setOnClickListener(this);
                                playB.setText("PLAY-" + adSlotId);
                                playB.setTextSize(12);
                                ll.addView(playB);

                                IdLayout.addView(ll);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        isNewInstance = sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false);
        userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");

        Log.d("lance", userID + "-----------updatePlacement-----------" + isNewInstance);
    }

    public void buttonClick(View view) {
        if (view.getId() == R.id.cleanLog_button) {
            cleanLog();
        }
    }

    private void cleanLog() {
        logTextView.setText("");
    }

    private static SimpleDateFormat dateFormat = null;

    private static SimpleDateFormat getDateTimeFormat() {

        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss SSS", Locale.CHINA);
        }
        return dateFormat;
    }

    private void logMessage(String message) {
        Date date = new Date();
        logTextView.append(getDateTimeFormat().format(date) + " " + message + '\n');
    }

    @Override
    public void onAdLoadSuccess(String placementId) {
        Log.d("lance", "----------onAdLoadSuccess----------" + placementId);
        BannerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onAdLoadSuccess [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onAdLoadError(WindMillError error, String placementId) {
        Log.d("lance", "----------onAdLoadError----------" + error.toString() + ":" + placementId);
        BannerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onAdLoadError() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    public void onAdShown(AdInfo adInfo) {
        Log.d("lance", "----------onAdShown----------" + adInfo.toString());
        BannerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onAdShown [ " + adInfo.getPlacementId() + " ]");
            }
        });
    }

    @Override
    public void onAdClicked(AdInfo adInfo) {
        Log.d("lance", "----------onAdClicked----------" + adInfo.toString());
        BannerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onAdClicked [ " + adInfo.getPlacementId() + " ]");
            }
        });
    }

    @Override
    public void onAdClosed(AdInfo adInfo) {
        Log.d("lance", "----------onAdClosed----------" + adInfo.toString());
        BannerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onAdClosed [" + adInfo.getPlacementId() + "]");
            }
        });

        if (adContainer != null) {
            adContainer.removeAllViews();
        }
    }

    @Override
    public void onAdAutoRefreshed(AdInfo adInfo) {
        Log.d("lance", "----------onAdAutoRefreshed----------" + adInfo.toString());
        BannerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onAdAutoRefreshed [" + adInfo.getPlacementId() + "]");
            }
        });
    }

    @Override
    public void onAdAutoRefreshFail(WindMillError error, String placementId) {
        Log.d("lance", "----------onAdAutoRefreshFail----------" + error.toString() + ":" + placementId);
        BannerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onAdAutoRefreshFail() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (WMBannerView bannerView : bannerAdMap.values()) {
            if (bannerView != null) {
                bannerView.destroy();
            }
        }
    }
}