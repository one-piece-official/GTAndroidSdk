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
import com.windmill.sdk.WMNetworkConfig;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.models.AdInfo;
import com.windmill.sdk.reward.WMRewardAd;
import com.windmill.sdk.reward.WMRewardAdListener;
import com.windmill.sdk.reward.WMRewardAdRequest;
import com.windmill.sdk.reward.WMRewardInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RewardVideoActivity extends AppCompatActivity implements WMRewardAdListener, View.OnClickListener {

    private TextView logTextView;
    private String userID;
    private LinearLayout IdLayout;
    private Map<String, WMRewardAd> rewardAdMap = new HashMap<>();
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
//                    WMRewardAd rewardAd = rewardAdMap.get(placementId);
//                    if (rewardAd != null) {
//                        rewardAd.loadAd();
//                    }
                    loadAd(placementId);

//                    Message message = Message.obtain();
//                    message.what = 0x002;
//                    message.obj = placementId;
//                    handler.sendMessageDelayed(message, 3000);
                    break;
                case 0x002:
                    placementId = (String) msg.obj;
                    Log.d("lance", "-------handleMessage 0x002-------" + placementId);
//                    WMRewardAd ad = rewardAdMap.get(placementId);
//                    if (ad != null) {
//                        HashMap option = new HashMap();
//                        option.put(WMConstants.AD_SCENE_ID, "567");
//                        option.put(WMConstants.AD_SCENE_DESC, "转盘抽奖");
//                        ad.show(RewardVideoActivity.this, option);
//                    }
                    showAd(placementId);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_video);

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

        WindMillAd.sharedAds().reportSceneExposure("456", "激励场景");
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
        WMRewardAd rewardAd = rewardAdMap.get(placementId);
        Log.d("lance", (rewardAd == null) + "---------loadAd---------" + placementId);
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", userID);
        if (rewardAd != null) {
            Map<String, Object> op = new HashMap<>();
            op.put("user_id", "update request option");
            rewardAd.getRequest().setOptions(op);

            if (isNewInstance) {
                rewardAd.destroy();
                rewardAdMap.remove(placementId);
                rewardAd = new WMRewardAd(this, new WMRewardAdRequest(placementId, userID, options));
                rewardAd.setRewardedAdListener(this);
            }
        } else {
            rewardAd = new WMRewardAd(this, new WMRewardAdRequest(placementId, userID, options));
            rewardAd.setRewardedAdListener(this);
        }

        rewardAd.loadAd();

        rewardAdMap.put(placementId, rewardAd);
    }

    private void showAd(String placementId) {
        WMRewardAd rewardAd = rewardAdMap.get(placementId);
        Log.d("lance", "---------showAd---------" + placementId);
        if (rewardAd != null) {
            List<AdInfo> adInfoList = rewardAd.checkValidAdCaches();
            if (adInfoList != null && adInfoList.size() > 0) {
                for (int i = 0; i < adInfoList.size(); i++) {
                    AdInfo adInfo = adInfoList.get(i);
                    Log.d("lance", "---------showAd-----adInfo----:" + adInfo.toString());
                }
            }
        }
        if (rewardAd != null && rewardAd.isReady()) {
            HashMap option = new HashMap();
            option.put(WMConstants.AD_SCENE_ID, "567");
            option.put(WMConstants.AD_SCENE_DESC, "转盘抽奖");
            rewardAd.show(this, option);
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
                            if (ad_type == 1 && bidType == 0) {

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
    public void onVideoAdLoadSuccess(final String placementId) {
        Log.d("lance", "----------onVideoAdLoadSuccess----------" + placementId);
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdLoadSuccess [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onVideoAdPlayEnd(AdInfo adInfo) {
        Log.d("lance", "----------onVideoAdPlayEnd----------" + adInfo.toString());
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdPlayEnd [ " + adInfo.getPlacementId() + " ]");
            }
        });
    }

    @Override
    public void onVideoAdPlayStart(final AdInfo adInfo) {
        Log.d("lance", "----------onVideoAdPlayStart----------" + adInfo.toString());
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdPlayStart [ " + adInfo.getPlacementId() + " ]");
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
    public void onVideoAdClicked(AdInfo adInfo) {
        Log.d("lance", "----------onVideoAdClicked----------" + adInfo.toString());
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdClicked [ " + adInfo.getPlacementId() + " ]");
            }
        });
    }

    @Override
    public void onVideoRewarded(AdInfo adInfo, WMRewardInfo rewardInfo) {
        Log.d("lance", "----------onVideoRewarded----------" + adInfo.toString() + ":" + rewardInfo.toString());
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoRewarded() called with: reward = [" + rewardInfo.toString() + "], placementId = [" + adInfo.getPlacementId() + "]");
            }
        });
    }

    @Override
    public void onVideoAdClosed(AdInfo adInfo) {
        Log.d("lance", "----------onVideoAdClosed----------" + adInfo.toString());
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdClosed [" + adInfo.getPlacementId() + "]");
            }
        });
    }

    @Override
    public void onVideoAdLoadError(final WindMillError error, final String placementId) {
        Log.d("lance", "----------onVideoAdLoadError----------" + error.toString() + ":" + placementId);
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdLoadError() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    public void onVideoAdPlayError(final WindMillError error, final String placementId) {
        Log.d("lance", "----------onVideoAdPlayError----------" + error.toString() + ":" + placementId);
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdPlayError() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (WMRewardAd rewardAd : rewardAdMap.values()) {
            if (rewardAd != null) {
                rewardAd.destroy();
            }
        }
    }

}