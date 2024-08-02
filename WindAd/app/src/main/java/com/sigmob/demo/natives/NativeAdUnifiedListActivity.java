package com.sigmob.demo.natives;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.sigmob.demo.Constants;
import com.sigmob.demo.R;
import com.sigmob.demo.view.ILoadMoreListener;
import com.sigmob.demo.view.LoadMoreListView;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.WindNativeAdData;
import com.sigmob.windad.natives.WindNativeAdRequest;
import com.sigmob.windad.natives.WindNativeUnifiedAd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeAdUnifiedListActivity extends AppCompatActivity {

    private static final int LIST_ITEM_COUNT = 10;
    private LoadMoreListView mListView;
    private MyAdapter myAdapter;
    private WindNativeUnifiedAd windNativeUnifiedAd;
    private int userID = 0;
    private String placementId;

    private List<WindNativeAdData> mData;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_ad_unified_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        updatePlacement();
        initListView();
    }

    private void updatePlacement() {

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        placementId = sharedPreferences.getString(Constants.CONF_UNIFIED_NATIVE_PLACEMENTID, Constants.native_unified_placement_id);

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
        userID++;
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", String.valueOf(userID));
        if (windNativeUnifiedAd == null) {
            windNativeUnifiedAd = new WindNativeUnifiedAd(new WindNativeAdRequest(placementId, String.valueOf(userID), 3, options));
        }

        windNativeUnifiedAd.loadAd(new WindNativeUnifiedAd.WindNativeAdLoadListener() {
            @Override
            public void onError(WindAdError error, String placementId) {
                Log.d("WindSDK", "onError:" + error.toString() + ":" + placementId);
                Toast.makeText(NativeAdUnifiedListActivity.this, "onError:" + error.toString(), Toast.LENGTH_SHORT).show();
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
        }
        mData = null;
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

            View nativeAdView = adViewHolder.adRender.getNativeAdView(mActivity, adData);


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