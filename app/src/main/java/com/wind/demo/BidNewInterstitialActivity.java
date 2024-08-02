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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAd;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdListener;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BidNewInterstitialActivity extends Activity implements WindNewInterstitialAdListener, View.OnClickListener {


    private TextView logTextView;
    private LinearLayout IdLayout;
    private Map<String, WindNewInterstitialAd> interstitialAdMap = new HashMap<>();
    private boolean isPlayingLoad;
    private boolean isNewInstance;
    private EditText bidTokenView;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String placementId;
            switch (msg.what) {
                case 0x001:
                    placementId = (String) msg.obj;
                    Log.d("WindSDK", "-------handleMessage 0x001-------" + placementId);

                    loadAd(placementId);

//                    Message message = Message.obtain();
//                    message.what = 0x002;
//                    message.obj = placementId;
//                    handler.sendMessageDelayed(message, 3000);
                    break;
                case 0x002:
                    placementId = (String) msg.obj;
                    Log.d("WindSDK", "-------handleMessage 0x002-------" + placementId);
                    showAd(placementId);
                    break;
            }
        }
    };
    private boolean isCheckReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid_interstitial);

        IdLayout = this.findViewById(R.id.ll_placement);

        bidTokenView = this.findViewById(R.id.bidTokenText);

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
        String text = button.getText().toString();
        String placementId = text.substring(text.indexOf("-") + 1);
        Log.d("WindSDK", "---------onClick---------" + text);
        if (text.startsWith("getBidToken")) {
            SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

            String appId = sharedPreferences.getString(Constants.CONF_APP_ID, "6878");
            S2SBiddingUtils.requestBiddingToken(this, appId, placementId, 1, new S2SBiddingUtils.RequestTokenCallBack() {
                @Override
                public void onSuccess(String token) {
                    bidTokenView.setText(token);
                }
            });
        } else if (text.startsWith("LOAD")) {
            loadAd(placementId);
        } else {//SHOW
            showAd(placementId);
        }
    }

    private void loadAd(String placementId) {
        WindNewInterstitialAd interstitialAd = interstitialAdMap.get(placementId);
        Log.d("WindSDK", (interstitialAd == null) + "---------loadAd---------" + placementId);
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", Constants.user_id);

        if (interstitialAd != null) {
            if (isNewInstance) {
                interstitialAd.destroy();
                interstitialAdMap.remove(placementId);
                interstitialAd = new WindNewInterstitialAd(new WindNewInterstitialAdRequest(placementId, Constants.user_id, null));
                interstitialAd.setWindNewInterstitialAdListener(this);
            }
        } else {
            interstitialAd = new WindNewInterstitialAd(new WindNewInterstitialAdRequest(placementId, Constants.user_id, null));
            interstitialAd.setWindNewInterstitialAdListener(this);
        }

        String token = bidTokenView.getText().toString();

        interstitialAd.loadAd(token);

        interstitialAdMap.put(placementId, interstitialAd);

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.CONF_BID_TOKEN, token);
        editor.apply();
    }

    private void showAd(String placementId) {
        WindNewInterstitialAd interstitialAd = interstitialAdMap.get(placementId);
        Log.d("WindSDK", "---------showAd---------" + placementId);
        if (interstitialAd != null && (!isCheckReady || interstitialAd.isReady())) {
            HashMap option = new HashMap();
            option.put(WindAds.AD_SCENE_ID, "567");
            option.put(WindAds.AD_SCENE_DESC, "转盘抽奖");
            interstitialAd.show(option);
        } else {
            logMessage("Ad is not Ready");
            Log.d("WindSDK", "--------请先加载广告--------");
        }
    }

    public void buttonClick(View view) {
        if (view.getId() == R.id.cleanLog_button) {
            cleanLog();
        }
    }

    private void updatePlacement() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, Constants.TEST_CONF_JSON);

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
                            if (ad_type == 6 && bidType == 1) {

                                String adSlotId = slotId.optString("adSlotId");

                                LinearLayout ll = new LinearLayout(this);
                                ll.setOrientation(LinearLayout.VERTICAL);
                                ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


                                Button bidToken = new Button(this);
                                LinearLayout.LayoutParams bidLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                bidLP.setMargins(0, 5, 0, 5);
                                bidLP.weight = 1;
                                bidToken.setLayoutParams(bidLP);
                                bidToken.setOnClickListener(this);
                                bidToken.setText("getBidToken-" + adSlotId);
                                bidToken.setTextSize(12);
                                ll.addView(bidToken);

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

//        bidTokenView.setText(sharedPreferences.getString(Constants.CONF_BID_TOKEN, ""));
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
    public void onInterstitialAdLoadSuccess(final String placementId) {
        BidNewInterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdLoadSuccess [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdPreLoadSuccess(final String placementId) {
        WindNewInterstitialAd interstitialAd = interstitialAdMap.get(placementId);
        if (interstitialAd != null) {
            Log.d("WindSDK", "onInterstitialAdPreLoadSuccess: " + interstitialAd.getEcpm());
        }
        BidNewInterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPreLoadSuccess [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdPreLoadFail(final String placementId) {
        BidNewInterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPreLoadFail [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdShow(final String placementId) {
        BidNewInterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdShow [ " + placementId + " ]");
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
    public void onInterstitialAdClicked(final String placementId) {
        BidNewInterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdClicked [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdClosed(final String placementId) {
        BidNewInterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdClosed [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdLoadError(final WindAdError error, final String placementId) {
        BidNewInterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdLoadError() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    public void onInterstitialAdShowError(final WindAdError error, final String placementId) {
        BidNewInterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdShowError() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (WindNewInterstitialAd interstitialAd : interstitialAdMap.values()) {
            if (interstitialAd != null) {
                interstitialAd.destroy();
            }
        }
    }

}