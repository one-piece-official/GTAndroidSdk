package com.windmill.demo.natives;


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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.windmill.demo.Constants;
import com.windmill.demo.MyApplication;
import com.windmill.demo.R;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.models.AdInfo;
import com.windmill.sdk.natives.WMNativeAd;
import com.windmill.sdk.natives.WMNativeAdContainer;
import com.windmill.sdk.natives.WMNativeAdData;
import com.windmill.sdk.natives.WMNativeAdDataType;
import com.windmill.sdk.natives.WMNativeAdRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NativeAdUnifiedActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewGroup adContainer;
    private String userID;
    private TextView logTextView;
    private LinearLayout IdLayout;
    private boolean isNewInstance;
    private int AD_COUNT = 3;
    private int adWidth;
    private WMNativeAd nativeUnifiedAd;

    private Map<String, WMNativeAd> nativeUnifiedAdMap = new HashMap<>();
    private Map<String, List<WMNativeAdData>> unifiedADDataMap = new HashMap<>();

    //如果是沉浸式的，全屏前就没有状态栏
    public static void hideStatusBar(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    public static void hideSystemUI(Activity context) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        context.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        hideSystemUI(this);
//        hideStatusBar(this);
        setContentView(R.layout.activity_native_ad_unified);

        IdLayout = this.findViewById(R.id.ll_placement);

        adContainer = findViewById(R.id.native_ad_container);

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

        Log.d("lance", "---------screenWidthAsIntDips---------" + adWidth);

        WindMillAd.sharedAds().reportSceneExposure("111", "原生场景");
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
        nativeUnifiedAd = nativeUnifiedAdMap.get(placementId);
        Log.d("lance", (nativeUnifiedAd == null) + "---------loadAd---------" + placementId);
        Map<String, Object> options = new HashMap<>();
        options.put(WMConstants.AD_WIDTH, adWidth);//针对于模版广告有效、单位dp
        options.put(WMConstants.AD_HEIGHT, WMConstants.AUTO_SIZE);//自适应高度
        options.put("user_id", userID);
        if (nativeUnifiedAd != null) {
            Map<String, Object> op = new HashMap<>();
            op.put("user_id", "update request option");
            nativeUnifiedAd.getRequest().setOptions(op);

            if (isNewInstance) {
                nativeUnifiedAd.destroy();
                nativeUnifiedAdMap.remove(placementId);
                nativeUnifiedAd = new WMNativeAd(this, new WMNativeAdRequest(placementId, userID, AD_COUNT, options));
            }
        } else {
            nativeUnifiedAd = new WMNativeAd(this, new WMNativeAdRequest(placementId, userID, AD_COUNT, options));

        }

        nativeUnifiedAd.loadAd(new WMNativeAd.NativeAdLoadListener() {
            @Override
            public void onError(WindMillError error, String placementId) {
                Log.d("lance", "----------onError----------:" + error.toString() + ":" + placementId);
                NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                    }
                });
            }

            @Override
            public void onFeedAdLoad(String placementId) {
                NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onFeedAdLoad [ " + placementId + " ]");
                    }
                });

                WMNativeAd unifiedAd = nativeUnifiedAdMap.get(placementId);
                if (unifiedAd != null) {
                    List<WMNativeAdData> unifiedADData = unifiedAd.getNativeADDataList();
                    if (unifiedADData != null && unifiedADData.size() > 0) {
                        Log.d("lance", "----------onFeedAdLoad----------" + unifiedADData.size());
                        unifiedADDataMap.put(placementId, unifiedADData);
                    }
                }
            }
        });

        nativeUnifiedAdMap.put(placementId, nativeUnifiedAd);
    }

    private void showAd(final String placementId) {
        Log.d("lance", "---------showAd---------" + placementId);
        if (nativeUnifiedAd != null) {
            Log.d("lance", "---------getLoadFailMessages---------" + nativeUnifiedAd.getLoadFailMessages());
            List<AdInfo> adInfoList = nativeUnifiedAd.checkValidAdCaches();
            if (adInfoList != null && adInfoList.size() > 0) {
                for (int i = 0; i < adInfoList.size(); i++) {
                    AdInfo adInfo = adInfoList.get(i);
                    Log.d("lance", "---------showAd-----adInfo----:" + adInfo.toString());
                }
            }
        }

        List<WMNativeAdData> unifiedADDataList = unifiedADDataMap.get(placementId);
        if (unifiedADDataList != null && unifiedADDataList.size() > 0) {
            WMNativeAdData nativeAdData = unifiedADDataList.get(0);
            Log.d("lance", "-----------isExpressAd-----------:" + nativeAdData.isExpressAd());

            bindListener(nativeAdData);

            if (nativeAdData.isExpressAd()) {//模版广告
                nativeAdData.render();//onRenderSuccess
//                View expressAdView = nativeAdData.getExpressAdView();
//                //媒体最终将要展示广告的容器
//                if (adContainer != null) {
//                    adContainer.removeAllViews();
//                    adContainer.addView(expressAdView);
//                }
            } else {//自渲染广告
                //创建一个装整个自渲染广告的容器
                WMNativeAdContainer windContainer = new WMNativeAdContainer(this);
                //媒体自渲染的View
                NativeAdDemoRender adRender = new NativeAdDemoRender();
                //将容器和view链接起来
                nativeAdData.connectAdToView(this, windContainer, adRender);

                //媒体最终将要展示广告的容器
                if (adContainer != null) {
                    adContainer.removeAllViews();
                    adContainer.addView(windContainer);
                }
            }
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

    private void bindListener(WMNativeAdData nativeAdData) {
        //设置广告交互监听
        nativeAdData.setInteractionListener(new WMNativeAdData.NativeAdInteractionListener() {
            @Override
            public void onADExposed(AdInfo adInfo) {
                Log.d("lance", "----------onADExposed----------" + adInfo.toString());
                NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onADExposed()");
                    }
                });
            }

            @Override
            public void onADClicked(AdInfo adInfo) {
                Log.d("lance", "----------onADClicked----------" + adInfo.toString());
                NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onADClicked()");
                    }
                });
            }

            @Override
            public void onADRenderSuccess(AdInfo adInfo, View view, float width, float height) {
                Log.d("lance", "----------onRenderSuccess----------" + adInfo.toString());
                NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onRenderSuccess()");
                    }
                });
                //媒体最终将要展示广告的容器
                if (adContainer != null) {
                    adContainer.removeAllViews();
                    adContainer.addView(view);
                }
            }

            @Override
            public void onADError(AdInfo adInfo, WindMillError error) {
                Log.d("lance", "----------onADError----------" + adInfo.toString() + ":" + error.toString());
                NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onADError() called with: error = [" + error + "]");
                    }
                });
            }

        });

        //设置media监听
        if (nativeAdData.getAdPatternType() == WMNativeAdDataType.NATIVE_VIDEO_AD) {
            nativeAdData.setMediaListener(new WMNativeAdData.NativeADMediaListener() {
                @Override
                public void onVideoLoad() {
                    Log.d("lance", "----------onVideoLoad----------");
                }

                @Override
                public void onVideoError(WindMillError error) {
                    Log.d("lance", "----------onVideoError----------:" + error.toString());
                }

                @Override
                public void onVideoStart() {
                    Log.d("lance", "----------onVideoStart----------");
                }

                @Override
                public void onVideoPause() {
                    Log.d("lance", "----------onVideoPause----------");
                }

                @Override
                public void onVideoResume() {
                    Log.d("lance", "----------onVideoResume----------");
                }

                @Override
                public void onVideoCompleted() {
                    Log.d("lance", "----------onVideoCompleted----------");
                }
            });
        }

        if (nativeAdData.getInteractionType() == WMConstants.INTERACTION_TYPE_DOWNLOAD) {
            nativeAdData.setDownloadListener(new WMNativeAdData.AppDownloadListener() {
                @Override
                public void onIdle() {
                    Log.d("lance", "----------onIdle----------");
                }

                @Override
                public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                    Log.d("lance", "----------onDownloadActive----------");
                }

                @Override
                public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                    Log.d("lance", "----------onDownloadPaused----------");
                }

                @Override
                public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                    Log.d("lance", "----------onDownloadFailed----------");
                }

                @Override
                public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                    Log.d("lance", "----------onDownloadFinished----------");
                }

                @Override
                public void onInstalled(String fileName, String appName) {
                    Log.d("lance", "----------onInstalled----------");
                }
            });
        }

        //设置dislike弹窗
        nativeAdData.setDislikeInteractionCallback(this, new WMNativeAdData.DislikeInteractionCallback() {
            @Override
            public void onShow() {
                Log.d("lance", "----------onShow----------");
            }

            @Override
            public void onSelected(int position, String value, boolean enforce) {
                Log.d("lance", "----------onSelected----------:" + position + ":" + value + ":" + enforce);
                if (adContainer != null) {
                    adContainer.removeAllViews();
                }
            }

            @Override
            public void onCancel() {
                Log.d("lance", "----------onCancel----------");
            }
        });
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

        for (List<WMNativeAdData> adDataList : unifiedADDataMap.values()) {
            if (adDataList != null && adDataList.size() > 0) {
                for (WMNativeAdData ad : adDataList) {
                    if (ad != null) {
                        ad.destroy();
                    }
                }
            }
        }

        for (WMNativeAd unifiedAd : nativeUnifiedAdMap.values()) {
            if (unifiedAd != null) {
                unifiedAd.destroy();
            }
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
                            if (ad_type == 5 && bidType == 0) {

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

        Log.d("lance", "-----------updatePlacement-----------" + isNewInstance);
    }

}