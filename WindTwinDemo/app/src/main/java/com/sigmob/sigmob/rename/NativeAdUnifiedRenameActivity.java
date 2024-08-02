package com.sigmob.sigmob.rename;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sigmob.sigmob.R;
import com.xmlywind.windad.WindAdError;
import com.xmlywind.windad.natives.NativeADData;
import com.xmlywind.windad.natives.WindNativeAdContainer;
import com.xmlywind.windad.natives.WindNativeAdRequest;
import com.xmlywind.windad.natives.WindNativeUnifiedAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sigmob.sigmob.Constants.CONF_JSON;

public class NativeAdUnifiedRenameActivity extends AppCompatActivity {

    private ViewGroup adContainer;
    private ViewGroup adContainer2;
    private Button loadAdBtn;
    private Button playAdBtn;
    private Button loadAdBtn2;
    private Button playAdBtn2;
    private int userID = 0;
    private String placementId;
    private String placementId2;
    private WindNativeUnifiedAd windNativeUnifiedAd;
    private Map<String, List<NativeADData>> unifiedADDataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_ad_unified_rename);
        adContainer = findViewById(R.id.native_ad_container);
        adContainer2 = findViewById(R.id.native_ad_container2);
        loadAdBtn = this.findViewById(R.id.load_native_button);
        playAdBtn = this.findViewById(R.id.show_native_button);
        loadAdBtn2 = this.findViewById(R.id.load_native_button2);
        playAdBtn2 = this.findViewById(R.id.show_native_button2);
        updatePlacement();
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.load_native_button:
                //加载原生广告
                loadNativeAd(placementId);
                break;
            case R.id.show_native_button:
                //展示原生广告
                showNativeAd(placementId);
                break;
            case R.id.load_native_button2:
                //加载原生广告
                loadNativeAd(placementId2);
                break;
            case R.id.show_native_button2:
                //展示原生广告
                showNativeAd(placementId2);
                break;
        }
    }

    private void loadNativeAd(String placementId) {
        Log.d("WindSDK", "-----------loadNativeAd-----------:" + placementId);
        userID++;
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", String.valueOf(userID));

        windNativeUnifiedAd = new WindNativeUnifiedAd( new WindNativeAdRequest(placementId, String.valueOf(userID), 3, options));

        windNativeUnifiedAd.loadAd(new WindNativeUnifiedAd.NativeAdLoadListener() {
            @Override
            public void onError(WindAdError error, String placementId) {
                Log.d("WindSDK", "onError:" + error.toString() + ":" + placementId);
                Toast.makeText(NativeAdUnifiedRenameActivity.this, "onError", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFeedAdLoad(String placementId) {
                Toast.makeText(NativeAdUnifiedRenameActivity.this, "onFeedAdLoad", Toast.LENGTH_SHORT).show();
                List<NativeADData> unifiedADData = windNativeUnifiedAd.getNativeADDataList();
                if (unifiedADData != null && unifiedADData.size() > 0) {
                    Log.d("WindSDK", "onFeedAdLoad" + unifiedADData.size());
                    unifiedADDataMap.put(placementId, unifiedADData);
                }
            }
        });
    }

    private void showNativeAd(final String placementId) {
        Log.d("WindSDK", "-----------showNativeAd-----------:" + placementId);
        List<NativeADData> unifiedADDataList = unifiedADDataMap.get(placementId);
        if (unifiedADDataList != null && unifiedADDataList.size() > 0) {
            NativeADData nativeADData = unifiedADDataList.get(0);
            //创建一个装整个自渲染广告的容器
            WindNativeAdContainer windContainer = new WindNativeAdContainer(this);
            //媒体自渲染的View
            NativeAdDemoRenameRender adRender = new NativeAdDemoRenameRender();
            //将容器和view链接起来
            nativeADData.connectAdToView(this, windContainer, adRender);
            //设置dislike弹窗
            nativeADData.setDislikeInteractionCallback(this, new NativeADData.DislikeInteractionCallback() {
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
                    if (adContainer2.getTag() != null && adContainer2.getTag().equals(placementId)) {
                        adContainer2.removeAllViews();
                    }
                }

                @Override
                public void onCancel() {
                    Log.d("WindSDK", "onCancel");
                }
            });

            //媒体最终将要展示广告的容器
            if (adContainer.getTag() != null && adContainer.getTag().equals(placementId)) {
                adContainer.removeAllViews();
                adContainer.addView(windContainer);
            }
            //媒体最终将要展示广告的容器
            if (adContainer2.getTag() != null && adContainer2.getTag().equals(placementId)) {
                adContainer2.removeAllViews();
                adContainer2.addView(windContainer);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (List<NativeADData> adDataList : unifiedADDataMap.values()) {
            if (adDataList != null && adDataList.size() > 0) {
                for (NativeADData ad : adDataList) {
                    if (ad != null) {
                        ad.destroy();
                    }
                }
            }
        }

        if (windNativeUnifiedAd != null) {
            windNativeUnifiedAd.destroy();
        }
    }

    private void updatePlacement() {

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        String configJson = sharedPreferences.getString(CONF_JSON, "");
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
                                if (TextUtils.isEmpty(placementId)) {
                                    placementId = slotId.optString("adSlotId");
                                } else if (TextUtils.isEmpty(placementId2)) {
                                    placementId2 = slotId.optString("adSlotId");
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(placementId)) {
            adContainer.setTag(placementId);
        }

        if (!TextUtils.isEmpty(placementId2)) {
            adContainer2.setTag(placementId2);
        }

        loadAdBtn.setText("load " + placementId);
        loadAdBtn2.setText("load " + placementId2);
        playAdBtn.setText("show " + placementId);
        playAdBtn2.setText("show " + placementId2);
        Toast.makeText(this, "updatePlacement", Toast.LENGTH_SHORT).show();
    }
}