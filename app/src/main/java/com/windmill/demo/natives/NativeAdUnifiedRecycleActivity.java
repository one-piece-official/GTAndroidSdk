package com.windmill.demo.natives;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.windmill.demo.Constants;
import com.windmill.demo.R;
import com.windmill.demo.view.ILoadMoreListener;
import com.windmill.demo.view.LoadMoreRecyclerView;
import com.windmill.demo.view.LoadMoreView;
import com.windmill.sdk.WMConstants;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeAdUnifiedRecycleActivity extends AppCompatActivity {

    private static final int LIST_ITEM_COUNT = 10;

    private LoadMoreRecyclerView mListView;

    private MyAdapter myAdapter;

    private WMNativeAd nativeUnifiedAd;

    private String userID;

    private String placementId;

    private List<WMNativeAdData> mData;

    private int adWidth;

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
        userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");
        adWidth = screenWidthAsIntDips(this) - 20;
        Log.d("lance", "---------screenWidthAsIntDips---------" + adWidth);
    }

    public static int screenWidthAsIntDips(Context context) {
        int pixels = context.getResources().getDisplayMetrics().widthPixels;
        float density = context.getResources().getDisplayMetrics().density;
        return (int) ((pixels / density) + 0.5f);
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
        Log.d("lance", "-----------loadListAd-----------");
        Map<String, Object> options = new HashMap<>();
        options.put(WMConstants.AD_WIDTH, adWidth);//针对于模版广告有效、单位dp
        options.put(WMConstants.AD_HEIGHT, WMConstants.AUTO_SIZE);//自适应高度
        options.put("user_id", userID);
        if (nativeUnifiedAd == null) {
            nativeUnifiedAd = new WMNativeAd(this, new WMNativeAdRequest(placementId, userID, 3, options));
        }

        nativeUnifiedAd.loadAd(new WMNativeAd.NativeAdLoadListener() {
            @Override
            public void onError(WindMillError error, String placementId) {
                Log.d("lance", "----------onError----------:" + error.toString() + ":" + placementId);
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

                List<WMNativeAdData> unifiedADData = nativeUnifiedAd.getNativeADDataList();

                if (unifiedADData != null && unifiedADData.size() > 0) {
                    Log.d("lance", "----------onFeedAdLoad----------:" + unifiedADData.size());
                    for (final WMNativeAdData adData : unifiedADData) {

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
            for (WMNativeAdData ad : mData) {
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
        private static final int ITEM_VIEW_TYPE_UNIFIED_AD = 1;
        private static final int ITEM_VIEW_TYPE_EXPRESS_AD = 2;

        private List<WMNativeAdData> mData;

        private Activity mActivity;

        public MyAdapter(Activity activity, List<WMNativeAdData> data) {
            this.mActivity = activity;
            this.mData = data;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d("lance", "-----------onCreateViewHolder-----------");

            switch (viewType) {
                case ITEM_VIEW_TYPE_LOAD_MORE:
                    return new LoadMoreViewHolder(new LoadMoreView(mActivity));
                case ITEM_VIEW_TYPE_UNIFIED_AD:
                    return new UnifiedAdViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.listitem_ad_native, parent, false));
                case ITEM_VIEW_TYPE_EXPRESS_AD:
                    return new ExpressAdViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.listitem_ad_native, parent, false));
                default:
                    return new NormalViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.listitem_normal, parent, false));
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Log.d("lance", "-----------onBindViewHolder-----------");

            if (holder instanceof UnifiedAdViewHolder) {
                WMNativeAdData nativeAdData = mData.get(position);
                final UnifiedAdViewHolder adViewHolder = (UnifiedAdViewHolder) holder;
                bindListener(nativeAdData, holder);
                //将容器和view链接起来
                nativeAdData.connectAdToView(mActivity, adViewHolder.windContainer, adViewHolder.adRender);
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

            } else if (holder instanceof ExpressAdViewHolder) {
                WMNativeAdData nativeAdData = mData.get(position);
                final ExpressAdViewHolder adViewHolder = (ExpressAdViewHolder) holder;
                bindListener(nativeAdData, holder);
                nativeAdData.render();
                View expressAdView = nativeAdData.getExpressAdView();
                //添加进容器
                if (adViewHolder.adContainer != null) {
                    adViewHolder.adContainer.removeAllViews();
                    if (expressAdView != null) {
                        ViewGroup parent = (ViewGroup) expressAdView.getParent();
                        if (parent != null) {
                            parent.removeView(expressAdView);
                        }
                        adViewHolder.adContainer.addView(expressAdView);
                    }
                }
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


        private void bindListener(WMNativeAdData nativeAdData, RecyclerView.ViewHolder adViewHolder) {
            //设置广告交互监听
            nativeAdData.setInteractionListener(new WMNativeAdData.NativeAdInteractionListener() {
                @Override
                public void onADExposed(AdInfo adInfo) {
                    Log.d("lance", "----------onADExposed----------");
                }

                @Override
                public void onADClicked(AdInfo adInfo) {
                    Log.d("lance", "----------onADClicked----------");
                }

                @Override
                public void onADRenderSuccess(AdInfo adInfo, View view, float width, float height) {
                    Log.d("lance", "----------onRenderSuccess----------:" + width + ":" + height);
                }

                @Override
                public void onADError(AdInfo adInfo, WindMillError error) {
                    Log.d("lance", "----------onADError----------:" + error.toString());
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
                        if (adViewHolder instanceof UnifiedAdViewHolder) {
                            ((UnifiedAdViewHolder) adViewHolder).adRender.updateAdAction("开始下载");
                        }
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("lance", "----------onADExposed----------");
                        if (adViewHolder instanceof UnifiedAdViewHolder) {
                            ((UnifiedAdViewHolder) adViewHolder).adRender.updateAdAction("下载中");
                        }
                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("lance", "----------onDownloadActive----------");
                        if (adViewHolder instanceof UnifiedAdViewHolder) {
                            ((UnifiedAdViewHolder) adViewHolder).adRender.updateAdAction("下载暂停");
                        }
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("lance", "----------onDownloadFailed----------");
                        if (adViewHolder instanceof UnifiedAdViewHolder) {
                            ((UnifiedAdViewHolder) adViewHolder).adRender.updateAdAction("重新下载");
                        }
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                        Log.d("lance", "----------onDownloadFinished----------");
                        if (adViewHolder instanceof UnifiedAdViewHolder) {
                            ((UnifiedAdViewHolder) adViewHolder).adRender.updateAdAction("点击安装");
                        }
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
                        Log.d("lance", "----------onInstalled----------");
                        if (adViewHolder instanceof UnifiedAdViewHolder) {
                            ((UnifiedAdViewHolder) adViewHolder).adRender.updateAdAction("点击打开");
                        }
                    }
                });
            }

            //设置dislike弹窗
            nativeAdData.setDislikeInteractionCallback(mActivity, new WMNativeAdData.DislikeInteractionCallback() {
                @Override
                public void onShow() {
                    Log.d("lance", "----------onShow----------");
                }

                @Override
                public void onSelected(int position, String value, boolean enforce) {
                    Log.d("lance", "----------onSelected----------:" + position + ":" + value + ":" + enforce);
                    //用户选择不喜欢原因后，移除广告展示
                    mData.remove(nativeAdData);
                    notifyDataSetChanged();
                }

                @Override
                public void onCancel() {
                    Log.d("lance", "----------onCancel----------");
                }
            });
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
                    WMNativeAdData ad = mData.get(position);
                    if (ad == null) {
                        return ITEM_VIEW_TYPE_NORMAL;
                    } else {
                        if (ad.isExpressAd()) {
                            return ITEM_VIEW_TYPE_EXPRESS_AD;
                        } else {
                            return ITEM_VIEW_TYPE_UNIFIED_AD;
                        }

                    }
                }
            }
            return super.getItemViewType(position);
        }

        private static class ExpressAdViewHolder extends AdViewHolder {
            public ExpressAdViewHolder(View itemView) {
                super(itemView);
            }
        }

        private static class UnifiedAdViewHolder extends AdViewHolder {
            //创建一个装整个自渲染广告的容器
            WMNativeAdContainer windContainer;
            //媒体自渲染的View
            NativeAdDemoRender adRender;

            public UnifiedAdViewHolder(View itemView) {
                super(itemView);
                windContainer = new WMNativeAdContainer(itemView.getContext());
                adRender = new NativeAdDemoRender();
            }
        }

        private static class AdViewHolder extends RecyclerView.ViewHolder {

            FrameLayout adContainer;

            public AdViewHolder(View itemView) {
                super(itemView);
                adContainer = (FrameLayout) itemView.findViewById(R.id.iv_list_item_container);
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