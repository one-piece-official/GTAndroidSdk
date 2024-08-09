package com.windmill.demo.natives;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gt.sdk.AdError;
import com.gt.sdk.base.models.SigImage;
import com.gt.sdk.natives.AdAppInfo;
import com.gt.sdk.natives.NativeADEventListener;
import com.gt.sdk.natives.NativeAdData;
import com.gt.sdk.natives.NativeAdPatternType;
import com.windmill.demo.R;
import com.windmill.sdk.natives.WMImage;
import com.windmill.sdk.natives.WMNativeAdData;
import com.windmill.sdk.natives.WMNativeAdDataType;
import com.windmill.sdk.natives.WMVideoOption;
import com.windmill.sdk.natives.WMViewBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeDemoRender {

    private static final String TAG = "lance";

    private Context context;

    private Map<Integer, View> developViewMap = new HashMap<>();
    private ImageView img_logo;
    private ImageView ad_logo;
    private ImageView img_dislike;
    private TextView text_desc;
    private View mButtonsContainer;
    private Button mPlayButton;
    private Button mPauseButton;
    private Button mStopButton;
    private FrameLayout mMediaViewLayout;
    private ImageView mImagePoster;
    private LinearLayout native_3img_ad_container;
    private ImageView img_1;
    private ImageView img_2;
    private ImageView img_3;
    private TextView text_title;
    private Button mCTAButton;
    private FrameLayout shakeLayout;

    private Activity activity;

    public NativeDemoRender(Activity activity) {
        this.activity = activity;
    }

    /**
     * @param context
     * @param adData
     * @return
     */
    public View createView(Context context, NativeAdData adData) {
        Log.d("lance", "---------createView----------" + adData.hashCode());
        this.context = context;
        View developView = developViewMap.get(adData.hashCode());
        if (developView == null) {
            developView = LayoutInflater.from(context).inflate(R.layout.native_ad_item_normal, null);
            developViewMap.put(adData.hashCode(), developView);
        }
        if (developView.getParent() != null) {
            ((ViewGroup) developView.getParent()).removeView(developView);
        }
        return developView;
    }

    public View renderAdView(NativeAdData adData, NativeADEventListener nativeAdEventListener) {

        View view = createView(activity, adData);
        Log.d("lance", "renderAdView:" + adData.getTitle());

        img_logo = view.findViewById(R.id.img_logo);
        ad_logo = view.findViewById(R.id.channel_ad_logo);
        img_dislike = view.findViewById(R.id.iv_dislike);

        text_desc = view.findViewById(R.id.text_desc);

        mButtonsContainer = view.findViewById(R.id.video_btn_container);
        mPlayButton = view.findViewById(R.id.btn_play);
        mPauseButton = view.findViewById(R.id.btn_pause);
        mStopButton = view.findViewById(R.id.btn_stop);

        mMediaViewLayout = view.findViewById(R.id.media_layout);
        mImagePoster = view.findViewById(R.id.img_poster);
        shakeLayout = view.findViewById(R.id.shake_layout);

        native_3img_ad_container = view.findViewById(R.id.native_3img_ad_container);
        img_1 = view.findViewById(R.id.img_1);
        img_2 = view.findViewById(R.id.img_2);
        img_3 = view.findViewById(R.id.img_3);

        text_title = view.findViewById(R.id.text_title);
        mCTAButton = view.findViewById(R.id.btn_cta);

        //渲染UI
        if (!TextUtils.isEmpty(adData.getIconUrl())) {
            img_logo.setVisibility(View.VISIBLE);
            Glide.with(context.getApplicationContext()).load(adData.getIconUrl()).into(img_logo);
        } else {
//            img_logo.setVisibility(View.GONE);
        }

        if (adData.getAdLogo() != null) {
            ad_logo.setVisibility(View.VISIBLE);
            Glide.with(context.getApplicationContext()).load(adData.getAdLogo()).into(ad_logo);
        } else {
            ad_logo.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(adData.getTitle())) {
            text_title.setText(adData.getTitle());
        } else {
            text_title.setText("点开有惊喜");
        }

        if (!TextUtils.isEmpty(adData.getDesc())) {
            text_desc.setText(adData.getDesc());
        } else {
            text_desc.setText("听说点开它的人都交了好运!");
        }

        //clickViews数量必须大于等于1
        List<View> clickableViews = new ArrayList<>();
        //可以被点击的view, 也可以把convertView放进来意味item可被点击
        clickableViews.add(view);
        ////触发创意广告的view（点击下载或拨打电话）
        List<View> creativeViewList = new ArrayList<>();
        // 所有广告类型，注册mDownloadButton的点击事件
        creativeViewList.add(mCTAButton);
//        clickableViews.add(mDownloadButton);

        List<ImageView> imageViews = new ArrayList<>();
        int patternType = adData.getAdPatternType();
        Log.d("lance", "patternType:" + patternType);
        //ITEM_VIEW_TYPE_LARGE_PIC_AD
        if (patternType == NativeAdPatternType.NATIVE_BIG_IMAGE_AD) {
            // 双图双文、单图双文：注册mImagePoster的点击事件
            mImagePoster.setVisibility(View.VISIBLE);
            mButtonsContainer.setVisibility(View.GONE);
            native_3img_ad_container.setVisibility(View.GONE);
            mMediaViewLayout.setVisibility(View.GONE);
            clickableViews.add(mImagePoster);
            imageViews.add(mImagePoster);
        } else if (patternType == NativeAdPatternType.NATIVE_GROUP_IMAGE_AD) {//IMAGE_MODE_GROUP_IMG
            // 三小图广告：注册native_3img_ad_container的点击事件
            native_3img_ad_container.setVisibility(View.VISIBLE);
            mImagePoster.setVisibility(View.GONE);
            mButtonsContainer.setVisibility(View.GONE);
            mMediaViewLayout.setVisibility(View.GONE);
            clickableViews.add(native_3img_ad_container);
            imageViews.add(img_1);
            imageViews.add(img_2);
            imageViews.add(img_3);
        }

        //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
        //作为creativeViewList传入，点击不进入详情页，直接下载或进入落地页，视频和图文广告均生效
        adData.bindViewForInteraction(view, clickableViews, creativeViewList, img_dislike, nativeAdEventListener);

        List<SigImage> imageList = adData.getImageList();
        if (imageList != null && !imageList.isEmpty()) {
            for (int i = 0; i < imageList.size(); i++) {
                SigImage image = imageList.get(i);
                if (image != null) {
                    Log.d("lance", "-------------imageList--------------:" + image.getWidth() + ":" + image.getHeight() + ":" + image.getImageUrl());
                }
            }
        } else {
            Log.d("lance", "imageList is null or size is 0");
        }

        //需要等到bindViewForInteraction后再去添加media
        if (!imageViews.isEmpty()) {
            adData.bindImageViews(imageViews, 0);
        } else if (patternType == NativeAdPatternType.NATIVE_VIDEO_AD) {

            int videoWidth = adData.getVideoWidth();
            int videoHeight = adData.getVideoHeight();
            Log.d("lance", "-------------getVideoWidth----------" + videoWidth + ":" + videoHeight);

            // 视频广告，注册mMediaView的点击事件
            mImagePoster.setVisibility(View.GONE);
            native_3img_ad_container.setVisibility(View.GONE);
            mMediaViewLayout.setVisibility(View.VISIBLE);
            adData.bindMediaView(mMediaViewLayout, new NativeAdData.NativeADMediaListener() {

                @Override
                public void onVideoLoad() {
                    Log.d("lance", "-------------onVideoLoad--------------");
                }

                @Override
                public void onVideoError(AdError error) {
                    Log.d("lance", "-------------onVideoError--------------:" + error.toString());
                }

                @Override
                public void onVideoStart() {
                    Log.d("lance", "-------------onVideoStart--------------");
                }

                @Override
                public void onVideoPause() {
                    Log.d("lance", "-------------onVideoPause--------------");
                }

                @Override
                public void onVideoResume() {
                    Log.d("lance", "-------------onVideoResume--------------");
                }

                @Override
                public void onVideoCompleted() {
                    Log.d("lance", "-------------onVideoCompleted--------------");
                }
            });

            mButtonsContainer.setVisibility(View.VISIBLE);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v == mPlayButton) {
                        adData.startVideo();
                    } else if (v == mPauseButton) {
                        adData.pauseVideo();
                    } else if (v == mStopButton) {
                        adData.stopVideo();
                    }
                }
            };
            mPlayButton.setOnClickListener(listener);
            mPauseButton.setOnClickListener(listener);
            mStopButton.setOnClickListener(listener);
        }

        View shakeView = adData.getWidgetView(80, 80);
        if (shakeView != null) {
            shakeLayout.addView(shakeView);
        }

        /**
         * 营销组件
         * 支持项目：智能电话（点击跳转拨号盘），外显表单
         *  bindCTAViews 绑定营销组件监听视图，注意：bindCTAViews的视图不可调用setOnClickListener，否则SDK功能可能受到影响
         *  ad.getCTAText 判断拉取广告是否包含营销组件，如果包含组件，展示组件按钮，否则展示download按钮
         */
        String ctaText = adData.getCTAText(); //获取组件文案
        Log.d("lance", "ctaText:" + ctaText);
        updateAdAction(ctaText);

        /**
         * 六要素信息展示
         */
        AdAppInfo appInfo = adData.getAdAppInfo();
        if (appInfo != null) {
            Log.d("lance", "应用名字 = " + appInfo.getAppName());
            Log.d("lance", "应用包名 = " + appInfo.getPackageName());
            Log.d("lance", "应用版本 = " + appInfo.getVersionName());
            Log.d("lance", "开发者 = " + appInfo.getDeveloper());
            Log.d("lance", "应用品牌 = " + appInfo.getAuthorName());
            Log.d("lance", "包大小 = " + appInfo.getAppSize());
            Log.d("lance", "隐私条款链接 = " + appInfo.getPrivacyUrl());
            Log.d("lance", "权限信息链接 = " + appInfo.getPermissionsUrl());
        }

        return view;
    }

    public void updateAdAction(String ctaText) {
        if (!TextUtils.isEmpty(ctaText)) {
            //如果拉取广告包含CTA组件，则渲染该组件
            mCTAButton.setText(ctaText);
            mCTAButton.setVisibility(View.VISIBLE);
        } else {
            mCTAButton.setVisibility(View.INVISIBLE);
        }
    }

    public int dpToPx(Context context, int dp) {
        Resources r = context.getApplicationContext().getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int) px;
    }
}