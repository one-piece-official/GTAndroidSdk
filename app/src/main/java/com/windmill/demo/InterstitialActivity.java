package com.windmill.demo;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.interstitial.WMInterstitialAd;
import com.windmill.sdk.interstitial.WMInterstitialAdListener;
import com.windmill.sdk.interstitial.WMInterstitialAdRequest;
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

public class InterstitialActivity extends AppCompatActivity implements WMInterstitialAdListener, View.OnClickListener {

    private TextView logTextView;
    private String userID;
    private LinearLayout IdLayout;
    private Map<String, WMInterstitialAd> interstitialAdMap = new HashMap<>();
    private boolean isPlayingLoad;
    private boolean isNewInstance;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String placementId;
            switch (msg.what) {
                case 0x001:
                    placementId = (String) msg.obj;
                    Log.d("lance", "-------handleMessage 0x001-------" + placementId);

                    loadAd(placementId);

//                    Message message = Message.obtain();
//                    message.what = 0x002;
//                    message.obj = placementId;
//                    handler.sendMessageDelayed(message, 3000);
                    break;
                case 0x002:
                    placementId = (String) msg.obj;
                    Log.d("lance", "-------handleMessage 0x002-------" + placementId);
                    showAd(placementId);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        IdLayout = this.findViewById(R.id.ll_placement);

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

        WindMillAd.sharedAds().reportSceneExposure("123", "插屏场景");
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
        WMInterstitialAd interstitialAd = interstitialAdMap.get(placementId);
        Log.d("lance", (interstitialAd == null) + "---------loadAd---------" + placementId);
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", userID);

        if (interstitialAd != null) {
            Map<String, Object> op = new HashMap<>();
            op.put("user_id", "update request option");
            interstitialAd.getRequest().setOptions(op);

            if (isNewInstance) {
                interstitialAd.destroy();
                interstitialAdMap.remove(placementId);
                interstitialAd = new WMInterstitialAd(this, new WMInterstitialAdRequest(placementId, userID, options));
                interstitialAd.setInterstitialAdListener(this);
            }
        } else {
            interstitialAd = new WMInterstitialAd(this, new WMInterstitialAdRequest(placementId, userID, options));
            interstitialAd.setInterstitialAdListener(this);
        }

        interstitialAd.loadAd();

        interstitialAdMap.put(placementId, interstitialAd);
    }

    private void showAd(String placementId) {
        WMInterstitialAd interstitialAd = interstitialAdMap.get(placementId);
        Log.d("lance", "---------showAd---------" + placementId);
        if (interstitialAd != null) {
            List<AdInfo> adInfoList = interstitialAd.checkValidAdCaches();
            if (adInfoList != null && adInfoList.size() > 0) {
                for (int i = 0; i < adInfoList.size(); i++) {
                    AdInfo adInfo = adInfoList.get(i);
                    Log.d("lance", "---------showAd-----adInfo----:" + adInfo.toString());
                }
            }
        }
        if (interstitialAd != null && interstitialAd.isReady()) {
            HashMap option = new HashMap();
            option.put(WMConstants.AD_SCENE_ID, "567");
            option.put(WMConstants.AD_SCENE_DESC, "转盘抽奖");
            interstitialAd.show(this, option);
        } else {
            logMessage("Ad is not Ready");
            Log.d("lance", "--------请先加载广告--------");
        }
    }

    public void buttonClick(View view) {
        if (view.getId() == R.id.cleanLog_button) {
            cleanLog();
        }
    }

    private void updatePlacement() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, "");
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
                            if (ad_type == 4 && bidType == 0) {

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

        isPlayingLoad = sharedPreferences.getBoolean(Constants.CONF_PLAYING_LOAD, false);
        isNewInstance = sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false);
        userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");

        Log.d("lance", isPlayingLoad + "-----------updatePlacement-----------" + isNewInstance);
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
    public void onInterstitialAdLoadSuccess(final String placementId) {
        Log.d("lance", "----------onInterstitialAdLoadSuccess----------" + placementId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdLoadSuccess [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdPlayStart(AdInfo adInfo) {
        Log.d("lance", "----------onInterstitialAdPlayStart----------" + adInfo.toString());
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPlayStart [ " + adInfo.getPlacementId() + " ]");
            }
        });

        if (isPlayingLoad) {
            Message message = Message.obtain();
            message.what = 0x001;
            message.obj = adInfo.getPlacementId();
            handler.sendMessage(message);
        }
    }

    @Override
    public void onInterstitialAdPlayEnd(AdInfo adInfo) {
        Log.d("lance", "----------onInterstitialAdPlayEnd----------" + adInfo.toString());
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPlayEnd [ " + adInfo.getPlacementId() + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdClicked(AdInfo adInfo) {
        Log.d("lance", "----------onInterstitialAdClicked----------" + adInfo.toString());
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdClicked [ " + adInfo.getPlacementId() + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdClosed(AdInfo adInfo) {
        Log.d("lance", "----------onInterstitialAdClosed----------" + adInfo.toString());
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdClosed [ " + adInfo.getPlacementId() + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdLoadError(final WindMillError error, final String placementId) {
        Log.d("lance", "----------onInterstitialAdLoadError----------" + error.toString() + ":" + placementId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdLoadError() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    public void onInterstitialAdPlayError(final WindMillError error, final String placementId) {
        Log.d("lance", "----------onInterstitialAdPlayError----------" + error.toString() + ":" + placementId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPlayError() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (WMInterstitialAd interstitialAd : interstitialAdMap.values()) {
            if (interstitialAd != null) {
                interstitialAd.destroy();
            }
        }
    }

}