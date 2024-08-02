package com.sigmob.sigmob.natives;

import static com.sigmob.sigmob.Constants.CONF_JSON;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sigmob.sigmob.R;
import com.sigmob.sigmob.view.ILoadMoreListener;
import com.sigmob.sigmob.view.LoadMoreRecyclerView;
import com.sigmob.sigmob.view.LoadMoreView;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.NativeADData;
import com.sigmob.windad.natives.WindNativeAdContainer;
import com.sigmob.windad.natives.WindNativeAdRequest;
import com.sigmob.windad.natives.WindNativeUnifiedAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeAdUnifiedRecycleActivity extends AppCompatActivity {

    private static final int LIST_ITEM_COUNT = 10;

    private LoadMoreRecyclerView mListView;

    private MyAdapter myAdapter;

    private WindNativeUnifiedAd windNativeUnifiedAd;

    private int userID = 0;

    private String placementId;

    private List<NativeADData> mData;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_ad_unified_recycle);
        updatePlacement();
        initListView();
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
        mListView = (LoadMoreRecyclerView) findViewById(R.id.unified_native_ad_recycle);
        mListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
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

        windNativeUnifiedAd.loadAd(new WindNativeUnifiedAd.NativeAdLoadListener() {
            @Override
            public void onError(WindAdError error, String placementId) {
                Log.d("WindSDK", "onError:" + error.toString() + ":" + placementId);
                Toast.makeText(NativeAdUnifiedRecycleActivity.this, "onError:" + error.toString(), Toast.LENGTH_SHORT).show();
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }
            }

            @Override
            public void onFeedAdLoad(String placementId) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }

                List<NativeADData> unifiedADData = windNativeUnifiedAd.getNativeADDataList();

                if (unifiedADData != null && unifiedADData.size() > 0) {
                    Log.d("WindSDK", "onFeedAdLoad:" + unifiedADData.size());
                    for (final NativeADData adData : unifiedADData) {

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
            for (NativeADData ad : mData) {
                if (ad != null) {
                    ad.destroy();
                }
            }
        }
        mData = null;
    }

    private static class MyAdapter extends RecyclerView.Adapter {

        private static final int FOOTER_VIEW_COUNT = 1;

        private static final int ITEM_VIEW_TYPE_LOAD_MORE = -1;
        private static final int ITEM_VIEW_TYPE_NORMAL = 0;
        private static final int ITEM_VIEW_TYPE_AD = 1;

        private List<NativeADData> mData;

        private Activity mActivity;

        public MyAdapter(Activity activity, List<NativeADData> data) {
            this.mActivity = activity;
            this.mData = data;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_VIEW_TYPE_LOAD_MORE:
                    return new LoadMoreViewHolder(new LoadMoreView(mActivity));
                case ITEM_VIEW_TYPE_AD:
                    return new AdViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.listitem_ad_native, parent, false));
                default:
                    return new NormalViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.listitem_normal, parent, false));
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AdViewHolder) {
                final AdViewHolder adViewHolder = (AdViewHolder) holder;
                bindData(adViewHolder, mData.get(position));
            } else if (holder instanceof NormalViewHolder) {
                NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
                normalViewHolder.idle.setText("Recycler item " + position);
                holder.itemView.setBackgroundColor(getColorRandom());
            } else if (holder instanceof LoadMoreViewHolder) {
                LoadMoreViewHolder loadMoreViewHolder = (LoadMoreViewHolder) holder;
                loadMoreViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        private int getColorRandom() {
            int a = Double.valueOf(Math.random() * 255).intValue();
            int r = Double.valueOf(Math.random() * 255).intValue();
            int g = Double.valueOf(Math.random() * 255).intValue();
            int b = Double.valueOf(Math.random() * 255).intValue();
            return Color.argb(a, r, g, b);
        }

        private void bindData(final AdViewHolder adViewHolder, final NativeADData adData) {
            //将容器和view链接起来
            adData.connectAdToView(mActivity, adViewHolder.windContainer, adViewHolder.adRender);

            //设置dislike弹窗
            adData.setDislikeInteractionCallback(mActivity, new NativeADData.DislikeInteractionCallback() {
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
                if (adViewHolder.windContainer != null) {
                    ViewGroup parent = (ViewGroup) adViewHolder.windContainer.getParent();
                    if (parent != null) {
                        parent.removeView(adViewHolder.windContainer);
                    }
                    adViewHolder.adContainer.addView(adViewHolder.windContainer);
                }
            }
        }

        @Override
        public int getItemCount() {
            int count = mData == null ? 0 : mData.size();
            return count + FOOTER_VIEW_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (mData != null) {
                int count = mData.size();
                if (position >= count) {
                    return ITEM_VIEW_TYPE_LOAD_MORE;
                } else {
                    NativeADData ad = mData.get(position);
                    if (ad == null) {
                        return ITEM_VIEW_TYPE_NORMAL;
                    } else {
                        return ITEM_VIEW_TYPE_AD;
                    }
                }

            }
            return super.getItemViewType(position);
        }

        private static class AdViewHolder extends RecyclerView.ViewHolder {

            FrameLayout adContainer;
            //创建一个装整个自渲染广告的容器
            WindNativeAdContainer windContainer;
            //媒体自渲染的View
            NativeAdDemoRender adRender;

            public AdViewHolder(View itemView) {
                super(itemView);
                adContainer = (FrameLayout) itemView.findViewById(R.id.iv_list_item_container);
                windContainer = new WindNativeAdContainer(itemView.getContext());
                adRender = new NativeAdDemoRender();
            }
        }

        private static class NormalViewHolder extends RecyclerView.ViewHolder {
            TextView idle;

            public NormalViewHolder(View itemView) {
                super(itemView);
                idle = (TextView) itemView.findViewById(R.id.text_idle);
            }
        }

        private static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
            TextView mTextView;
            ProgressBar mProgressBar;

            public LoadMoreViewHolder(View itemView) {
                super(itemView);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                mTextView = (TextView) itemView.findViewById(R.id.tv_load_more_tip);
                mProgressBar = (ProgressBar) itemView.findViewById(R.id.pb_load_more_progress);
            }
        }
    }
}