package com.wind.demo.natives;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.NativeADEventListener;
import com.sigmob.windad.natives.WindNativeAdData;
import com.sigmob.windad.natives.WindNativeAdRequest;
import com.sigmob.windad.natives.WindNativeUnifiedAd;
import com.wind.demo.Constants;
import com.wind.demo.R;
import com.wind.demo.S2SBiddingUtils;
import com.wind.demo.view.ILoadMoreListener;
import com.wind.demo.view.LoadMoreListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeAdUnifiedListActivity extends Activity {

    private static final int LIST_ITEM_COUNT = 10;
    private LoadMoreListView mListView;
    private MyAdapter myAdapter;
    private WindNativeUnifiedAd windNativeUnifiedAd;
    private String placementId;

    private List<WindNativeAdData> mData;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mBidType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_ad_unified_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mBidType = getIntent().getIntExtra("bidType", 0);

        updatePlacement();
        initListView();
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
                            int bidType = slotId.optInt("bidType", 0);
                            if (ad_type == 5 && bidType == mBidType) {
                                if (TextUtils.isEmpty(placementId)) {
                                    placementId = slotId.optString("adSlotId");
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(this, "updatePlacement", Toast.LENGTH_SHORT).show();
    }

    private void initListView() {
        mListView = (LoadMoreListView) findViewById(R.id.unified_native_ad_list);
        mData = new ArrayList<>();
        myAdapter = new MyAdapter(this, mData);
        mListView.setAdapter(myAdapter);
        mListView.setLoadMoreListener(new ILoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadListAd();
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadListAd();
            }
        }, 500);
    }

    /**
     * 加载feed广告
     */
    private void loadListAd() {
        Log.d("WindSDK", "-----------loadListAd-----------");
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", Constants.user_id);
        if (windNativeUnifiedAd == null) {
            windNativeUnifiedAd = new WindNativeUnifiedAd(new WindNativeAdRequest(placementId, Constants.user_id, options));
        }

        windNativeUnifiedAd.setNativeAdLoadListener(new WindNativeUnifiedAd.WindNativeAdLoadListener() {
            @Override
            public void onAdError(WindAdError error, String placementId) {
                Log.d("WindSDK", "onAdError:" + error.toString() + ":" + placementId);
                Toast.makeText(NativeAdUnifiedListActivity.this, "onAdError:" + error.toString(), Toast.LENGTH_SHORT).show();
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }
            }

            @Override
            public void onAdLoad(List<WindNativeAdData> unifiedADData, String placementId) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }

                if (unifiedADData != null && unifiedADData.size() > 0) {
                    Log.d("WindSDK", "onFeedAdLoad:" + unifiedADData.size());
                    for (final WindNativeAdData adData : unifiedADData) {

                        for (int i = 0; i < LIST_ITEM_COUNT; i++) {
                            mData.add(null);
                        }

                        int count = mData.size();
                        mData.set(count - 1, adData);
                    }

                    myAdapter.notifyDataSetChanged();
                }
            }
        });

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }


        if (mBidType == 0) {
            windNativeUnifiedAd.loadAd(Constants.ad_count);

        } else {
            SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

            String appId = sharedPreferences.getString(Constants.CONF_APP_ID, "appId");
            int adcount = sharedPreferences.getInt(Constants.CONF_AD_COUNT, 1);

            S2SBiddingUtils.requestBiddingToken(this, appId, placementId, adcount, new S2SBiddingUtils.RequestTokenCallBack() {
                @Override
                public void onSuccess(String token) {
                    windNativeUnifiedAd.loadAd(token, adcount);
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mData != null) {
            for (WindNativeAdData ad : mData) {
                if (ad != null) {
                    ad.destroy();
                }
            }
            mData.clear();
        }
    }

    private static class MyAdapter extends BaseAdapter {

        private static final int ITEM_VIEW_TYPE_NORMAL = 0;
        private static final int ITEM_VIEW_TYPE_AD = 1;
        private List<WindNativeAdData> mData;
        private Activity mActivity;

        public MyAdapter(Activity activity, List<WindNativeAdData> data) {
            this.mActivity = activity;
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData.size(); // for test
        }

        @Override
        public WindNativeAdData getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //信息流广告的样式，有大图、小图、组图和视频，通过ad.getImageMode()来判断
        @Override
        public int getItemViewType(int position) {
            WindNativeAdData ad = getItem(position);
            if (ad == null) {
                return ITEM_VIEW_TYPE_NORMAL;
            } else {
                return ITEM_VIEW_TYPE_AD;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WindNativeAdData ad = getItem(position);
            switch (getItemViewType(position)) {
                case ITEM_VIEW_TYPE_AD:
                    return getADView(convertView, parent, ad);
                default:
                    return getNormalView(convertView, parent, position);
            }
        }

        //渲染视频广告，以视频广告为例，以下说明
        @SuppressWarnings("RedundantCast")
        private View getADView(View convertView, ViewGroup parent, final WindNativeAdData ad) {
            final AdViewHolder adViewHolder;
            try {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mActivity).inflate(R.layout.listitem_ad_native, parent, false);
                    adViewHolder = new AdViewHolder(convertView);
                    convertView.setTag(adViewHolder);
                } else {
                    adViewHolder = (AdViewHolder) convertView.getTag();
                }
                //绑定广告数据、设置交互回调
                bindData(adViewHolder, ad);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        /**
         * 非广告list
         *
         * @param convertView
         * @param parent
         * @param position
         * @return
         */
        @SuppressWarnings("RedundantCast")
        @SuppressLint("SetTextI18n")
        private View getNormalView(View convertView, ViewGroup parent, int position) {
            NormalViewHolder normalViewHolder;
            if (convertView == null) {
                normalViewHolder = new NormalViewHolder();
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.listitem_normal, parent, false);
                normalViewHolder.idle = (TextView) convertView.findViewById(R.id.text_idle);
                convertView.setTag(normalViewHolder);
            } else {
                normalViewHolder = (NormalViewHolder) convertView.getTag();
            }
            normalViewHolder.idle.setText("ListView item " + position);
            return convertView;
        }

        private void bindData(final AdViewHolder adViewHolder, final WindNativeAdData adData) {
            //将容器和view链接起来

            View nativeAdView = adViewHolder.adRender.getNativeAdView(mActivity, adData, new NativeADEventListener() {
                @Override
                public void onAdExposed() {
                    Log.d("WindSDK", "onAdExposed");

                }

                @Override
                public void onAdClicked() {
                    Log.d("WindSDK", "onAdClicked");

                }

                @Override
                public void onAdDetailShow() {
                    Log.d("WindSDK", "onAdDetailShow");

                }

                @Override
                public void onAdDetailDismiss() {
                    Log.d("WindSDK", "onAdDetailDismiss");

                }

                @Override
                public void onAdError(WindAdError error) {
                    Log.d("WindSDK", "onAdError :" + error);

                }
            }, new WindNativeAdData.NativeADMediaListener() {
                @Override
                public void onVideoLoad() {
                    Log.d("WindSDK", "onVideoLoad");

                }

                @Override
                public void onVideoError(WindAdError error) {
                    Log.d("WindSDK", "onVideoError :" + error);

                }

                @Override
                public void onVideoStart() {
                    Log.d("WindSDK", "onVideoStart");

                }

                @Override
                public void onVideoPause() {
                    Log.d("WindSDK", "onVideoPause");

                }

                @Override
                public void onVideoResume() {
                    Log.d("WindSDK", "onVideoResume");

                }

                @Override
                public void onVideoCompleted() {
                    Log.d("WindSDK", "onVideoCompleted");

                }
            });


            //设置dislike弹窗
            adData.setDislikeInteractionCallback(mActivity, new WindNativeAdData.DislikeInteractionCallback() {
                @Override
                public void onShow() {
                    Log.d("WindSDK", "onShow");
                }

                @Override
                public void onSelected(int position, String value, boolean enforce) {
                    Log.d("WindSDK", "onSelected: " + position + ":" + value + ":" + enforce);
                    //用户选择不喜欢原因后，移除广告展示
                    mData.remove(adData);
                    adData.destroy();
                    notifyDataSetChanged();
                }

                @Override
                public void onCancel() {
                    Log.d("WindSDK", "onCancel");
                }
            });
            //添加进容器
            if (adViewHolder.adContainer != null) {
                adViewHolder.adContainer.removeAllViews();
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

                adViewHolder.adContainer.addView(nativeAdView, lp);

            }
        }

        private static class AdViewHolder {

            FrameLayout adContainer;

            //媒体自渲染的View
            NativeAdDemoRender adRender;

            public AdViewHolder(View convertView) {
                adContainer = (FrameLayout) convertView.findViewById(R.id.iv_list_item_container);
                adRender = new NativeAdDemoRender();
            }
        }

        private static class NormalViewHolder {
            TextView idle;
        }
    }
}