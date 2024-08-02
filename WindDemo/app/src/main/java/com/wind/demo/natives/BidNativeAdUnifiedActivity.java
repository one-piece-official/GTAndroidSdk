package com.wind.demo.natives;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.natives.NativeADEventListener;
import com.sigmob.windad.natives.WindNativeAdData;
import com.sigmob.windad.natives.WindNativeAdRequest;
import com.sigmob.windad.natives.WindNativeUnifiedAd;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAd;
import com.wind.demo.Constants;
import com.wind.demo.R;
import com.wind.demo.S2SBiddingUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BidNativeAdUnifiedActivity extends Activity implements View.OnClickListener {

    private ViewGroup adContainer;
    private TextView logTextView;
    private LinearLayout IdLayout;
    private boolean isNewInstance;
    private int adWidth;

    private Map<String, WindNativeUnifiedAd> nativeUnifiedAdMap = new HashMap<>();
    private Map<String, List<WindNativeAdData>> unifiedADDataMap = new HashMap<>();
    private EditText bidTokenView;

    //如果是沉浸式的，全屏前就没有状态栏
    public static void hideStatusBar(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    public static void hideSystemUI(Activity context) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        context.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        hideSystemUI(this);
//        hideStatusBar(this);
        setContentView(R.layout.activity_bid_native_ad_unified);

        IdLayout = this.findViewById(R.id.ll_placement);

        adContainer = findViewById(R.id.native_ad_container);
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

        adWidth = screenWidthAsIntDips(this) - 20;

        Log.d("WindSDK", "---------screenWidthAsIntDips---------" + adWidth);
    }

    public static int screenWidthAsIntDips(Context context) {
        int pixels = context.getResources().getDisplayMetrics().widthPixels;
        float density = context.getResources().getDisplayMetrics().density;
        return (int) ((pixels / density) + 0.5f);
    }


    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        String text = (String) button.getText();
        String placementId = text.substring(text.indexOf("-") + 1);
        Log.d("WindSDK", "---------onClick---------" + text);
        if (text.startsWith("getBidToken")) {
            SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

            String appId = sharedPreferences.getString(Constants.CONF_APP_ID, "appId");
            S2SBiddingUtils.requestBiddingToken(this, appId, placementId, Constants.ad_count, new S2SBiddingUtils.RequestTokenCallBack() {
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

    private void loadAd(final String placementId) {
        WindNativeUnifiedAd nativeUnifiedAd = nativeUnifiedAdMap.get(placementId);
        Log.d("WindSDK", (nativeUnifiedAd == null) + "---------loadAd---------" + placementId);
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", Constants.user_id);
        WindNativeAdRequest adRequest = new WindNativeAdRequest(placementId, Constants.user_id,Constants.ad_count,options);

        HashMap<String, Object> extoption = new HashMap<>();
        extoption.put(WindAds.ADX_ID,"999");
        adRequest.setExtOptions(extoption);

        if (nativeUnifiedAd != null) {
            if (isNewInstance) {
                nativeUnifiedAd.destroy();
                nativeUnifiedAdMap.remove(placementId);
                nativeUnifiedAd = new WindNativeUnifiedAd(adRequest);
            }
        } else {
            nativeUnifiedAd = new WindNativeUnifiedAd(adRequest);

        }

        nativeUnifiedAd.setNativeAdLoadListener(new WindNativeUnifiedAd.WindNativeAdLoadListener() {
            @Override
            public void onAdError(final WindAdError error, final String placementId) {
                Log.d("WindSDK", "----------onAdError----------:" + error.toString() + ":" + placementId);
                BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onAdError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                    }
                });
            }

            @Override
            public void onAdLoad(List<WindNativeAdData> list, String s) {
                BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onFeedAdLoad [ " + placementId + " ]");
                        for (WindNativeAdData data: list) {
                            logMessage("video width "+ data.getVideoWidth() + " height "+ data.getVideoHeight());
                        }
                    }
                });


                for (List<WindNativeAdData> adDataList : unifiedADDataMap.values()) {
                    if (adDataList != null && adDataList.size() > 0) {
                        for (WindNativeAdData ad : adDataList) {
                            if (ad != null) {
                                ad.destroy();
                            }
                        }
                    }
                }

                if (list != null && list.size() > 0) {
                    Log.d("WindSDK", "----------onFeedAdLoad----------" + list.size());
                    unifiedADDataMap.put(placementId, list);
                }
            }


        });

        nativeUnifiedAd.loadAd(bidTokenView.getText().toString(), Constants.ad_count);

        nativeUnifiedAdMap.put(placementId, nativeUnifiedAd);
    }

    private void showAd(final String placementId) {
        Log.d("WindSDK", "---------showAd---------" + placementId);
        List<WindNativeAdData> unifiedADDataList = unifiedADDataMap.get(placementId);
        if (unifiedADDataList != null && unifiedADDataList.size() > 0) {
            WindNativeAdData nativeAdData = unifiedADDataList.get(0);

            //创建一个装整个自渲染广告的容器
            NativeAdDemoRender adRender = new NativeAdDemoRender();

            View windContainer = adRender.getNativeAdView(this, nativeAdData, new NativeADEventListener() {
                @Override
                public void onAdExposed() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("AdAppInfo [ " + nativeAdData.getAdAppInfo() + " ]");
                            logMessage("onAdExposed [ " + placementId + " ecpm: ] " +nativeAdData.getEcpm());
                        }
                    });
                }

                @Override
                public void onAdClicked() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onADClicked [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onAdDetailShow() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onAdDetailShow [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onAdDetailDismiss() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onAdDetailDismiss [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onAdError(final WindAdError error) {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onAdError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                        }
                    });
                }
            }, new WindNativeAdData.NativeADMediaListener() {
                @Override
                public void onVideoLoad() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoLoad [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onVideoError(final WindAdError error) {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                        }
                    });
                }

                @Override
                public void onVideoStart() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoStart [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onVideoPause() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoPause [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onVideoResume() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoResume [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onVideoCompleted() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoCompleted [ " + placementId + " ]");
                        }
                    });
                }
            });

            //设置dislike弹窗
            nativeAdData.setDislikeInteractionCallback(this, new WindNativeAdData.DislikeInteractionCallback() {
                @Override
                public void onShow() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onShow [ " + placementId + " ]");
                        }
                    });
                    Log.d("WindSDK", "----------onShow----------");
                }

                @Override
                public void onSelected(final int position, final String value, final boolean enforce) {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onSelected :" + position + ":" + value + ":" + enforce + " [ " + placementId + " ]");
                        }
                    });
                    Log.d("WindSDK", "----------onSelected----------:" + position + ":" + value + ":" + enforce);
                    if (adContainer != null) {
                        adContainer.removeAllViews();
                    }
                }

                @Override
                public void onCancel() {
                    BidNativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onCancel [ " + placementId + " ]");
                        }
                    });
                    Log.d("WindSDK", "----------onCancel----------");
                }
            });


            //媒体最终将要展示广告的容器
            if (adContainer != null) {
                adContainer.removeAllViews();
                adContainer.addView(windContainer);


            }
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

    private static SimpleDateFormat dateFormat = null;

    private static SimpleDateFormat getDateTimeFormat() {

        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss SSS", Locale.CHINA);
        }
        return dateFormat;
    }

    private void cleanLog() {
        logTextView.setText("");
    }

    private void logMessage(String message) {
        Date date = new Date();
        logTextView.append(getDateTimeFormat().format(date) + " " + message + '\n');
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (List<WindNativeAdData> adDataList : unifiedADDataMap.values()) {
            if (adDataList != null && adDataList.size() > 0) {
                for (WindNativeAdData ad : adDataList) {
                    if (ad != null) {
                        ad.destroy();
                    }
                }
            }
        }

        for (WindNativeUnifiedAd unifiedAd : nativeUnifiedAdMap.values()) {
            if (unifiedAd != null) {
                unifiedAd.destroy();
            }
        }
        unifiedADDataMap.clear();
        nativeUnifiedAdMap.clear();
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
                            if (ad_type == 5 && bidType == 1) {

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

        isNewInstance = sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false);

        Log.d("WindSDK", "-----------updatePlacement-----------" + isNewInstance);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}