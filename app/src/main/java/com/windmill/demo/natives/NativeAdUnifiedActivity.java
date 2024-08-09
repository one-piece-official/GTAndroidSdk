package com.windmill.demo.natives;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.natives.NativeADEventListener;
import com.gt.sdk.natives.NativeAdData;
import com.gt.sdk.natives.NativeAdLoadListener;
import com.gt.sdk.natives.NativeUnifiedAd;
import com.windmill.demo.Constants;
import com.windmill.demo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NativeAdUnifiedActivity extends AppCompatActivity implements View.OnClickListener, NativeAdLoadListener {

    private ViewGroup adContainer;
    private String userID;
    private TextView logTextView;
    private LinearLayout IdLayout;
    private boolean isNewInstance;
    private NativeUnifiedAd nativeUnifiedAd;

    private final Map<String, NativeUnifiedAd> nativeUnifiedAdMap = new HashMap<>();
    private final Map<String, List<NativeAdData>> unifiedADDataMap = new HashMap<>();

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

    private void loadAd(String codeId) {
        nativeUnifiedAd = nativeUnifiedAdMap.get(codeId);
        Log.d("lance", (nativeUnifiedAd == null) + "---------loadAd---------" + codeId);

        Map<String, String> options = new HashMap<>();
        options.put("user_id", userID);
        AdRequest adRequest = new AdRequest.Builder().setCodeId("123456").setExtOption(options).build();

        if (nativeUnifiedAd != null) {
            if (isNewInstance) {
                nativeUnifiedAd.destroyAd();
                nativeUnifiedAdMap.remove(codeId);
                nativeUnifiedAd = new NativeUnifiedAd(adRequest, this);
            }
        } else {
            nativeUnifiedAd = new NativeUnifiedAd(adRequest, this);
        }
        nativeUnifiedAdMap.put(codeId, nativeUnifiedAd);
        nativeUnifiedAd.loadAd();
    }

    private void showAd(final String placementId) {
        Log.d("lance", "---------showAd---------" + placementId);

        List<NativeAdData> unifiedADDataList = unifiedADDataMap.get(placementId);
        if (unifiedADDataList != null && !unifiedADDataList.isEmpty()) {
            NativeAdData nativeAdData = unifiedADDataList.get(0);
            View view = buildView(nativeAdData);
            //媒体最终将要展示广告的容器
            if (adContainer != null) {
                adContainer.removeAllViews();
                adContainer.addView(view);
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

    private View buildView(NativeAdData nativeAdData) {
        //设置广告交互监听
        nativeAdData.setDislikeInteractionCallback(this, new NativeAdData.DislikeInteractionCallback() {

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
        //媒体自渲染的View
        NativeDemoRender adRender = new NativeDemoRender(this);
        View view = adRender.renderAdView(nativeAdData, new NativeADEventListener() {
            @Override
            public void onAdExposed() {
                Log.d("lance", "----------onAdExposed----------");
                NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onAdExposed()");
                    }
                });
            }

            @Override
            public void onAdClicked() {
                Log.d("lance", "----------onAdClicked----------");
                NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onAdClicked()");
                    }
                });
            }

            @Override
            public void onAdDetailShow() {
                Log.d("lance", "----------onAdDetailShow----------");
            }

            @Override
            public void onAdDetailDismiss() {
                Log.d("lance", "----------onAdDetailDismiss----------");
            }

            @Override
            public void onAdRenderFail(AdError error) {
                Log.d("lance", "----------onAdRenderFail----------" + error.toString());
                NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onAdRenderFail() called with: error = [" + error + "]");
                    }
                });
            }
        });
        return view;
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

        for (List<NativeAdData> adDataList : unifiedADDataMap.values()) {
            if (adDataList != null && !adDataList.isEmpty()) {
                for (NativeAdData ad : adDataList) {
                    if (ad != null) {
                        ad.destroy();
                    }
                }
            }
        }

        for (NativeUnifiedAd unifiedAd : nativeUnifiedAdMap.values()) {
            if (unifiedAd != null) {
                unifiedAd.destroyAd();
            }
        }
    }

    private void updatePlacement() {

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        try {
            String adSlotId = "123456";

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        isNewInstance = sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false);
        userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");

        Log.d("lance", "-----------updatePlacement-----------" + isNewInstance);
    }

    @Override
    public void onAdError(String codeId, AdError error) {
        Log.d("lance", "----------onAdError----------:" + error.toString() + ":" + codeId);
        NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onAdError() called with: error = [" + error + "], codeId = [" + codeId + "]");
            }
        });
    }

    @Override
    public void onAdLoad(String codeId, List<NativeAdData> adDataList) {
        NativeAdUnifiedActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onAdLoad [ " + codeId + " ]");
            }
        });

        if (adDataList != null && !adDataList.isEmpty()) {
            Log.d("lance", "----------onAdLoad----------" + adDataList.size());
            unifiedADDataMap.put(codeId, adDataList);
        }
    }
}