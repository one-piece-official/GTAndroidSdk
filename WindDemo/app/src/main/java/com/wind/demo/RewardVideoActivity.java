package com.wind.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.rewardVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardInfo;
import com.sigmob.windad.rewardVideo.WindRewardVideoAd;
import com.sigmob.windad.rewardVideo.WindRewardVideoAdListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RewardVideoActivity extends Activity implements WindRewardVideoAdListener, View.OnClickListener {
    private Button loadAdBtn;
    private Button loadAdBtn2;
    private Button playAdBtn;
    private Button playAdBtn2;

    private TextView logTextView;
    private LinearLayout IdLayout;
    private Map<String, WindRewardVideoAd> rewardAdMap = new HashMap<>();
    private boolean isPlayingLoad;
    private boolean isNewInstance;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String placementId;
            switch (msg.what) {
                case 0x001:
                    placementId = (String) msg.obj;
                    Log.d("WindSDK", "-------handleMessage 0x001-------" + placementId);
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
                    Log.d("WindSDK", "-------handleMessage 0x002-------" + placementId);
//                    WindRewardVideoAd ad = rewardAdMap.get(placementId);
//                    if (ad != null) {
//                        HashMap option = new HashMap();
//                        option.put(WindAds.AD_SCENE_ID, Constants.scene_id);
//                        option.put(WindAds.AD_SCENE_DESC, Constants.scene_desc);
//                        ad.show(option);
//                    }
                    showAd(placementId);
                    break;
            }
        }
    };
    private boolean isCheckReady;

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

    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        String text = (String) button.getText();
        String placementId = text.substring(text.indexOf("-") + 1);
        Log.d("WindSDK", "---------onClick---------" + text);
        if (text.startsWith("LOAD")) {
            loadAd(placementId);
        } else {//SHOW
            showAd(placementId);
        }
    }

    private void loadAd(String placementId) {
        WindRewardVideoAd rewardAd = rewardAdMap.get(placementId);
        Log.d("WindSDK", (rewardAd == null) + "---------loadAd---------" + placementId);
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", Constants.user_id);
        if (rewardAd != null) {
            if (isNewInstance) {
                rewardAd.destroy();
                rewardAdMap.remove(placementId);
                rewardAd = new WindRewardVideoAd(new WindRewardAdRequest(placementId, Constants.user_id, options));
                rewardAd.setWindRewardVideoAdListener(this);
            }
        } else {
            rewardAd = new WindRewardVideoAd(new WindRewardAdRequest(placementId, Constants.user_id, options));
            rewardAd.setWindRewardVideoAdListener(this);
        }

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);
        String BID_DJ = sharedPreferences.getString(Constants.BID_DJ, "-1");
        String BID_BZ = sharedPreferences.getString(Constants.BID_BZ, "CNY");

        rewardAd.setBidFloor(Integer.parseInt(BID_DJ));
        rewardAd.setCurrency(BID_BZ);

        rewardAd.loadAd();

        rewardAdMap.put(placementId, rewardAd);
    }

    private void showAd(String placementId) {
        WindRewardVideoAd rewardAd = rewardAdMap.get(placementId);
        Log.d("WindSDK", "---------showAd---------" + placementId);
        if (rewardAd != null && (!isCheckReady || rewardAd.isReady())) {
            HashMap option = new HashMap();
            option.put(WindAds.AD_SCENE_ID, Constants.scene_id);
            option.put(WindAds.AD_SCENE_DESC, Constants.scene_desc);
            if (!TextUtils.isEmpty(rewardAd.getEcpm())) {
                rewardAd.setBidEcpm(Integer.parseInt(rewardAd.getEcpm()));
            }
            rewardAd.show(option);
        } else {
            logMessage("Ad is not Ready");
            Log.d("WindSDK", "--------请先加载广告--------");
        }
    }

    public void buttonClick(View view) {
        long viewId = view.getId();
        if (viewId == R.id.cleanLog_button) {
            cleanLog();
        } else if (viewId == R.id.notice_win_button) {
            noticeWin();
        } else if (viewId == R.id.notice_loss_button) {
            noticeLoss();
        }
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

        for (WindRewardVideoAd rewardVideoAd : rewardAdMap.values()) {
            rewardVideoAd.sendWinNotificationWithInfo(map);
        }
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

        for (WindRewardVideoAd rewardVideoAd : rewardAdMap.values()) {
            rewardVideoAd.sendLossNotificationWithInfo(map);
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
        isCheckReady = sharedPreferences.getBoolean(Constants.CONF_CHECK_READY, false);

        Log.d("WindSDK", isPlayingLoad + "-----------updatePlacement-----------" + isNewInstance);
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
    public void onRewardAdLoadSuccess(final String placementId) {
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdLoadSuccess [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onRewardAdPreLoadSuccess(final String placementId) {
        WindRewardVideoAd rewardAd = rewardAdMap.get(placementId);
        if (rewardAd != null) {
            Log.d("WindSDK", "onRewardAdPreLoadSuccess: " + rewardAd.getEcpm());
        }
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdPreLoadSuccess [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onRewardAdPreLoadFail(final String placementId) {
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdPreLoadFail [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onRewardAdPlayEnd(final String placementId) {
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdPlayEnd [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onRewardAdPlayStart(final String placementId) {
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdPlayStart [ " + placementId + " ]");
            }
        });

        if (isPlayingLoad) {
            Message message = Message.obtain();
            message.what = 0x001;
            message.obj = placementId;
            handler.sendMessage(message);
        }

    }

    @Override
    public void onRewardAdClicked(final String placementId) {
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdClicked [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onRewardAdRewarded(final WindRewardInfo rewardInfo, final String placementId) {
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdRewarded() called with: reward = [" + rewardInfo.toString() + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    public void onRewardAdClosed(final String placementId) {
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdClosed [" + placementId + "]");
            }
        });
    }

    @Override
    public void onRewardAdLoadError(final WindAdError error, final String placementId) {
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdLoadError() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    public void onRewardAdPlayError(final WindAdError error, final String placementId) {
        RewardVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onRewardAdPlayError() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (WindRewardVideoAd rewardAd : rewardAdMap.values()) {
            if (rewardAd != null) {
                rewardAd.destroy();
            }
        }
    }

}