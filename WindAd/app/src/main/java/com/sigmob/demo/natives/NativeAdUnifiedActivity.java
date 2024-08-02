package com.sigmob.demo.natives;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sigmob.demo.Constants;
import com.sigmob.demo.R;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.WindNativeAdData;
import com.sigmob.windad.natives.WindNativeAdRequest;
import com.sigmob.windad.natives.WindNativeUnifiedAd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeAdUnifiedActivity extends AppCompatActivity {

    private ViewGroup adContainer;
    private Button loadAdBtn;
    private Button playAdBtn;
    private WindNativeUnifiedAd windNativeUnifiedAd;
    private int userID = 0;
    private String placementId;
    private List<WindNativeAdData> unifiedADDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_ad_unified);
        adContainer = findViewById(R.id.native_ad_container);
        loadAdBtn = this.findViewById(R.id.load_native_button);
        playAdBtn = this.findViewById(R.id.show_native_button);
        updatePlacement();
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.load_native_button:
                //加载原生广告
                loadNativeAd();
                break;
            case R.id.show_native_button:
                //展示原生广告
                showNativeAd();
                break;
        }
    }

    private void loadNativeAd() {
        Log.d("WindSDK", "-----------loadNativeAd-----------");
        userID++;
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", String.valueOf(userID));
        if (windNativeUnifiedAd == null) {
            windNativeUnifiedAd = new WindNativeUnifiedAd( new WindNativeAdRequest(placementId, String.valueOf(userID), 3, options));
        }

        windNativeUnifiedAd.loadAd(new WindNativeUnifiedAd.WindNativeAdLoadListener() {
            @Override
            public void onError(WindAdError error, String placementId) {
                Log.d("WindSDK", "onError:" + error.toString() + ":" + placementId);
                Toast.makeText(NativeAdUnifiedActivity.this, "onError", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoad(List<WindNativeAdData> unifiedADData,String placementId) {
                Toast.makeText(NativeAdUnifiedActivity.this, "onFeedAdLoad", Toast.LENGTH_SHORT).show();
                unifiedADDataList = unifiedADData;

            }
        });
    }

    private void showNativeAd() {
        Log.d("WindSDK", "-----------showNativeAd-----------:" + placementId);
        if (unifiedADDataList != null && unifiedADDataList.size() > 0) {
            WindNativeAdData nativeADData = unifiedADDataList.get(0);
            //媒体自渲染的View
            NativeAdDemoRender adRender = new NativeAdDemoRender();

            View nativeAdView = adRender.getNativeAdView(this, nativeADData);
            //设置dislike弹窗
            nativeADData.setDislikeInteractionCallback(this, new WindNativeAdData.DislikeInteractionCallback() {
                @Override
                public void onShow() {
                    Log.d("WindSDK", "onShow");
                }

                @Override
                public void onSelected(int position, String value, boolean enforce) {
                    Log.d("WindSDK", "onSelected: " + position + ":" + value + ":" + enforce);
                    if (adContainer.getTag() != null && adContainer.getTag().equals(placementId)) {
                        adContainer.removeAllViews();
                    }

                }

                @Override
                public void onCancel() {
                    Log.d("WindSDK", "onCancel");
                }
            });

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            //媒体最终将要展示广告的容器
            if (adContainer.getTag() != null && adContainer.getTag().equals(placementId)) {
                adContainer.removeAllViews();
                adContainer.addView(nativeAdView,lp);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unifiedADDataList != null && unifiedADDataList.size() > 0) {
            for (WindNativeAdData ad : unifiedADDataList) {
                if (ad != null) {
                    ad.destroy();
                }
            }
        }
        if (windNativeUnifiedAd != null) {
            windNativeUnifiedAd.destroy();
        }
    }

    private void updatePlacement() {

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        placementId = sharedPreferences.getString(Constants.CONF_UNIFIED_NATIVE_PLACEMENTID, Constants.native_unified_placement_id);

        loadAdBtn.setText("加载自渲染广告:" + placementId);
        playAdBtn.setText("展示自渲染广告: " + placementId);
        Toast.makeText(this, "updatePlacement", Toast.LENGTH_SHORT).show();
    }
}