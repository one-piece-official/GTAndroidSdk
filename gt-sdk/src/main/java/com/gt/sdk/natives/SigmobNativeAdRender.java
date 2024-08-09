package com.gt.sdk.natives;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.TouchLocation;
import com.czhj.sdk.common.utils.ViewUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.AdError;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.base.common.ADEvent;
import com.gt.sdk.base.common.AdSessionManager;
import com.gt.sdk.base.common.AdStackManager;
import com.gt.sdk.base.common.BaseAdConfig;
import com.gt.sdk.base.common.SessionManager;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.SigImage;
import com.gt.sdk.base.view.DownloadDialog;
import com.gt.sdk.manager.DeviceContextManager;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SigmobNativeAdRender implements SigEmptyView.AdVisibilityStatusChangeListener, View.OnTouchListener, SigmobNativeAdRenderListener, NativeAdInterstitial.NativeAdInterstitialListener, View.OnClickListener, NativeAdData.DislikeInteractionCallback {

    private static final int APP_INFO_VIEW = 1;
    private static final int CREATIVE_VIEW = 2;
    private static final int BTN_VIEW = 3;
    private static final int MOTION_VIEW = 4;
    protected NativeAdData mNativeAdUnit;
    protected BaseAdUnit mAdUnit;
    protected SigAdVideoEventListener videoAdEventListener;
    private SigNativeAdVideo sigAdView;
    private SigEmptyView sigEmptyView;
    private List<View> mClickViewList = new ArrayList<>();
    private HashSet<View> mCreativeViews = new HashSet<>();
    private NativeAdInterstitial nativeAdInterstitial;
    private SigAppInfoView mSigAppInfoView;
    private MotionEvent down;
    private NativeAdConfig adConfig;
    private NativeADEventListener adInteractionListener;
    private boolean mIsVisible;
    private boolean misShowDetailPage;
    private ViewGroup sigAdViewParentVew;
    private ViewGroup.LayoutParams sigAdViewParentVewLP;
    private DownloadDialog downloadDialog;
    private boolean isElementDialogShow;
    private boolean isImpressioned;
    private boolean isUsePause;
    private int beginTime;
    private boolean isFirstPlay = true;
    private boolean isPaused = false;
    private boolean mStartImpression;
    private boolean isAdShow;
    private boolean isClicked;
    private List<View> mImageLists;
    private long lastDownEventTime;
    private List<View> mCreativeViewList = new ArrayList<>();
    private View mDislikeView;
    private boolean disLikeDialog;
    private NativeAdData.DislikeInteractionCallback mDislikeInteractionCallback;
    private boolean showWithoutAppInfo;
    private SigmobMotionView mWidgetView;
    private static Stack<SigmobMotionView> widgetViews = new Stack<>();

    public SigmobNativeAdRender() {

    }

    public static void setViewSize(View view, int width, int height) {
        if (view.getParent() instanceof FrameLayout) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
            lp.width = width;
            lp.height = height;
            view.setLayoutParams(lp);
            view.requestLayout();
        } else if (view.getParent() instanceof RelativeLayout) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.width = width;
            lp.height = height;
            view.setLayoutParams(lp);
            view.requestLayout();
        } else if (view.getParent() instanceof LinearLayout) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            lp.width = width;
            lp.height = height;
            view.setLayoutParams(lp);
            view.requestLayout();
        }
    }

    public SessionManager getSessionManager() {
        if (mAdUnit == null) return null;
        SessionManager sessionManager = mAdUnit.getSessionManager();
        if (sessionManager == null) {
            sessionManager = new AdSessionManager();
            sessionManager.createDisplaySession(mAdUnit);
        }
        return sessionManager;
    }

    @Override
    public BaseAdUnit getAdUnit() {
        return mAdUnit;
    }

    @Override
    public NativeAdData getNativeAdUnit() {
        return mNativeAdUnit;
    }

    public void initAdData(final BaseAdUnit adUnit, NativeAdData nativeAdUnit) {
        mAdUnit = adUnit;
        mNativeAdUnit = nativeAdUnit;
    }

    public SigAppInfoView getAppInfoView() {
        if (!showWithoutAppInfo && mSigAppInfoView == null) {
            Map<String, String> adPrivacy = mAdUnit.getAdPrivacy();
            if (adPrivacy != null) {
                mSigAppInfoView = new SigAppInfoView(getContext());
                mSigAppInfoView.setOnTouchListener(this);
                mSigAppInfoView.initData(mAdUnit.getAppVersion(), mAdUnit.getCompanyName());
            }
        }

        return mSigAppInfoView;
    }

    private Context getContext() {
        return GtAdSdk.sharedAds().getContext();
    }

    public boolean isValid() {
        if (nativeAdInterstitial != null) {
            return nativeAdInterstitial.baseAdUnitValid(mAdUnit);
        }
        return false;
    }

    public void updateViewClickListener(List<View> viewList, View.OnTouchListener onTouchListener) {
        if (viewList != null) {
            for (View view : viewList) {
                if (view != null) {
                    view.setOnClickListener(null);
                    view.setOnTouchListener(null);
                    view.setOnTouchListener(onTouchListener);
                }

            }
        }

    }

    public void updateViewClickListener(HashSet<View> viewList, View.OnTouchListener onTouchListener) {
        if (viewList != null) {
            for (View view : viewList) {
                if (view != null) {
                    view.setOnClickListener(null);
                    view.setOnTouchListener(null);
                    view.setOnTouchListener(onTouchListener);
                }
            }
        }
    }

    private SigNativeAdVideo getSigAdView(ViewGroup view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            View tmpView = view.getChildAt(i);
            if (tmpView instanceof SigNativeAdVideo) {
                return (SigNativeAdVideo) tmpView;
            }
        }
        return null;
    }

    private SigEmptyView getSigEmptyView(ViewGroup view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            View tmpView = view.getChildAt(i);
            if (tmpView instanceof SigEmptyView) {
                return (SigEmptyView) tmpView;
            }
        }
        return null;
    }

    public void bindMediaViewWithoutAppInfo(ViewGroup mediaLayout, NativeAdData.NativeADMediaListener nativeADMediaListener) {
        showWithoutAppInfo = true;
        bindMediaView(mediaLayout, nativeADMediaListener);
    }

    public void bindMediaView(final ViewGroup mediaContainer, final NativeAdData.NativeADMediaListener sigAdVideoEventListener) {

        if (mediaContainer == null) return;
        final SigNativeAdVideo tempSigAdView = ViewUtil.findViewByClass(mediaContainer, SigNativeAdVideo.class);
        if (tempSigAdView != null) {
            sigAdView = tempSigAdView;
        } else {
            //防止二次渲染
            ViewUtil.removeFromParent(sigAdView);
            sigAdView = new SigNativeAdVideo(mediaContainer.getContext().getApplicationContext());
        }

        if (tempSigAdView == null) {
            ViewUtil.removeFromParent(sigAdView);
            mediaContainer.addView(sigAdView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        if (!mCreativeViews.contains(sigAdView)) {
            mCreativeViews.add(sigAdView);
        }

        if (!mAdUnit.equals(sigAdView.getAdUnit())) {
            sigAdView.init(this);
        }
        final SigVideoAdController sigVideoAdController = sigAdView.getSigVideoAdController();
        if (sigVideoAdController != null) {
            sigVideoAdController.setAdVideoStatusListener(new SigAdVideoStatusListener() {

                @Override
                public void onVideoRestart() {

                }

                @Override
                public void onVideoLoad() {
                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoLoad();
                    }
                }

                @Override
                public void onVideoError(AdError error) {
                    isUsePause = false;
                    isPaused = false;

                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoError(error);
                    }
                }


                @Override
                public void onVideoStart() {

                    View adContainer = getAdContainer();
                    if (adContainer != null) {
                        mAdUnit.setAdSize(adContainer.getWidth(), adContainer.getHeight());
                    }
                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoStart();
                    }
                    isUsePause = false;
                    isPaused = false;

                    getSessionManager().recordDisplayEvent(mAdUnit, ADEvent.VIDEO_PLAY_START);
                }

                @Override
                public void onVideoPause() {
                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoPause();
                    }
                    isPaused = true;
                    getSessionManager().recordDisplayEvent(mAdUnit, ADEvent.AD_PLAY_PAUSE);
                }

                @Override
                public void onVideoResume() {
                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoResume();
                    }
                    isPaused = false;
                    isUsePause = false;
                }

                @Override
                public void onProgressUpdate(long current, long duration) {

//                    mAdUnit.getVideoCommon().end_time = sigVideoAdController.getCurrentPosition();
//
//                    NativeAdConfig adConfig = getAdConfig();
//                    if (adConfig != null) {
//                        List<FractionalProgressAdTracker> trackers = adConfig.getUntriggeredTrackersBefore(current, duration);
//                        for (FractionalProgressAdTracker adTracker : trackers) {
//                            getSessionManager().recordDisplayEvent(adTracker.getEvent(), sigVideoAdController.getCurrentPosition());
//                            adTracker.setTracked();
//                        }
//                    }

                }

                @Override
                public void onVideoCompleted() {
                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoCompleted();
                    }

                    getSessionManager().recordDisplayEvent(mAdUnit, ADEvent.VIDEO_PLAY_END);

                    isPaused = false;
                    isUsePause = false;
                }
            });
        }
    }

    public void bindImageViews(List<ImageView> viewList, int resId) {

        List<SigImage> sigImages = getAdUnit().getImageUrlList();

        if (viewList != null && sigImages != null) {

            int size = Math.min(viewList.size(), sigImages.size());

            for (int i = 0; i < size; i++) {
                SigImage sigImage = sigImages.get(i);
                ImageView imageView = viewList.get(i);

                if (!mCreativeViews.contains(imageView)) {
                    mCreativeViews.add(imageView);
                }
                if (mImageLists == null) {
                    mImageLists = new ArrayList<>();
                } else {
                    mImageLists.clear();
                }
                mImageLists.add(imageView);

                AdStackManager.getImageManger().load(sigImage.getImageUrl()).placeholder(resId).error(resId).into(imageView);
            }
        }
    }

    public NativeAdInterstitial getNativeAdInterstitial() {
        if (nativeAdInterstitial == null) {
            nativeAdInterstitial = new NativeAdInterstitial(this);
        }
        return nativeAdInterstitial;
    }


    public void registerViewForInteraction(View view, List<View> clickViewList,//创意按钮
                                           List<View> creativeViewList,//cta按钮
                                           final View disLikeView, NativeADEventListener nativeADEventListener) {

        if (view == null) {
            if (nativeADEventListener != null) {
                AdError adError = AdError.ERROR_AD_CONTAINER_IS_NULL;
                nativeADEventListener.onAdRenderFail(adError);
            }
            return;
        }

        if (!(view instanceof ViewGroup)) {
            if (nativeADEventListener != null) {
                AdError adError = AdError.ERROR_AD_CONTAINER_NOT_VIEW_GROUP;
                nativeADEventListener.onAdRenderFail(adError);
            }
            return;
        }

        Activity topActivity = DeviceContextManager.getTopActivity();
        if (topActivity == null) {
            DeviceContextManager.setTopActivity(ViewUtil.getActivityFromView(view));
        }

        if (disLikeView != null) {
            disLikeView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        onSelected(0, "close", true);
                    }
                    return true;
                }
            });
        }
        ViewGroup adContainer = (ViewGroup) view;

        ViewTreeObserver viewTreeObserver = adContainer.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                adContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mAdUnit != null) {
                    int[] position = new int[2];
                    adContainer.getLocationOnScreen(position);
                }
            }
        });

        final SigmobMotionView motionView = ViewUtil.findViewByClass(adContainer, SigmobMotionView.class);
        if (motionView != null && motionView != mWidgetView) {
            ViewUtil.removeFromParent(motionView);
        }

        getAdConfig().setOnAdClickListener(new BaseAdConfig.OnSigAdClickListener() {
            @Override
            public void onAdClick(boolean isRecord) {

                if (adInteractionListener != null) {
                    adInteractionListener.onAdClicked();
                }
                if (!isRecord) {
                    return;
                }
                SessionManager sessionManager = getSessionManager();
                if (sessionManager != null) {
                    sessionManager.recordDisplayEvent(mAdUnit, ADEvent.AD_CLICK);
                }

            }
        });
        if (creativeViewList != null) {
            mClickViewList = creativeViewList;
        }

        if (mCreativeViewList != null) {
            mCreativeViews.removeAll(mCreativeViewList);
        }
        if (clickViewList != null) {
            mCreativeViewList = clickViewList;
        }

        adInteractionListener = nativeADEventListener;
//        getNativeAdInterstitial().loadInterstitial(null, mAdUnit);

        AdStackManager.shareInstance().cache(mAdUnit, null);
        ////防止二次渲染
        if (sigEmptyView != null) {
            ViewUtil.removeFromParent(sigEmptyView);
            adContainer.addView(sigEmptyView, new ViewGroup.LayoutParams(0, 0));
        } else {

            sigEmptyView = getSigEmptyView(adContainer);

            if (sigEmptyView == null) {
                sigEmptyView = new SigEmptyView(getContext());
                adContainer.addView(sigEmptyView, new ViewGroup.LayoutParams(0, 0));
            }
        }

        mCreativeViews.addAll(mCreativeViewList);

        sigEmptyView.setAdVisibilityStatusChangeListener(this);
        sigEmptyView.startAutoCheck(getAdConfig().getImpPercent(), getAdConfig().getImpTime());

    }

    private DownloadDialog.onPrivacyClickListener onPrivacyClickListener = new DownloadDialog.onPrivacyClickListener() {

        @Override
        public void onCloseClick() {
            if (downloadDialog != null) {
                downloadDialog.dismiss();
                downloadDialog.destroy();
                downloadDialog = null;
            }
            isElementDialogShow = false;
            /**
             * 四要素消失的时候恢复视频，如果非endCard页面
             */
//            if (sigAdView != null) {
//                getVideoAdController().start();
//            }
            getSessionManager().recordDisplayEvent(mAdUnit, ADEvent.AD_FOUR_ELEMENTS_CLOSE);

        }

        @Override
        public void onButtonClick(String clickCoordinate) {
            if (mAdUnit != null) {
                if (isElementDialogShow) {
                    getAdConfig().handleUrlAction(mAdUnit, "");
                }
            }
        }

        @Override
        public void onShowSuccess() {
            /**
             * 四要素出来的时候暂停视频，如果非endCard页面
             */
            if (sigAdView != null) {
                getVideoAdController().pause();
            }
//            if (!isElementDialogShow) {
//                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_APPINFO;
//                mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_PREVIEW;
//                mAdUnit.getClickCommon().is_final_click = false;
//                getSessionManager().recordDisplayEvent(ADEvent.AD_CLICK, 0);
//            }
            getSessionManager().recordDisplayEvent(mAdUnit, ADEvent.AD_FOUR_ELEMENTS_SHOW);
        }
    };

    private void openFourElements() {
        try {
            Activity activity = ViewUtil.getActivityFromView(getAdContainer());
            if (downloadDialog == null && activity != null) {
                downloadDialog = new DownloadDialog(activity, mAdUnit);
                downloadDialog.setOnPrivacyClickListener(onPrivacyClickListener);
            }

            if (downloadDialog != null && downloadDialog.isRenderSuccess() && !isElementDialogShow) {
                downloadDialog.show();
                isElementDialogShow = true;
            }
        } catch (Exception e) {
            SigmobLog.e("openFourElements fail:" + e.getMessage());
        }
    }

    private View getAdContainer() {

        if (sigEmptyView != null) {

            View parentView = (View) sigEmptyView.getParent();
            if (parentView != null) {
                return parentView;
            }
        }
        return null;
    }

    public int getAdViewWidth() {

        View adContainer = getAdContainer();

        if (sigAdView != null && adContainer != null) {
            int width = adContainer.getWidth();
            if (width > 0) {
                return width;
            }
        }

        return 1280;
    }

    public View getAdView() {
        return sigAdView;
    }

    public SigVideoAdController getVideoAdController() {

        if (getAdView() != null) {
            return sigAdView.getSigVideoAdController();
        }
        return null;
    }

    public double getVideoDuration() {
        SigVideoAdController videoAdController = getVideoAdController();
        if (videoAdController != null) {
            return videoAdController.getVideoDuration();
        }
        return 0;
    }

    public double getVideoProgress() {
        SigVideoAdController videoAdController = getVideoAdController();
        if (videoAdController != null) {
            return videoAdController.getVideoProgress();
        }

        return 0;
    }

    public int getAdViewHeight() {
        View adContainer = getAdContainer();

        if (getAdView() != null && adContainer != null) {
            if (adContainer.getWidth() > 0) {
                int height = (int) (adContainer.getWidth() / mAdUnit.getAdPercent());
                return height;
            }
        }
        return 720;
    }

    public void unRegisterViewForInteraction() {


        updateViewClickListener(mCreativeViews, null);
        updateViewClickListener(mClickViewList, null);


        if (mCreativeViews != null) {
            mCreativeViews.clear();
        }
        if (mClickViewList != null) {
            mClickViewList.clear();
        }
        if (mCreativeViewList != null) {
            mCreativeViewList.clear();
        }
//        mCreativeViews = null;
//        mClickViewList = null;
//        mCreativeViewList = null;
        adInteractionListener = null;
        videoAdEventListener = null;

    }

    public void destroy() {
        SigmobLog.i(String.format("native ad data %s is Destroy", mAdUnit != null ? mAdUnit.getLogId() : "null"));

        unRegisterViewForInteraction();
        if (mImageLists != null) {
            mImageLists.clear();
        }

        if (sigAdView != null) {
            ViewUtil.removeFromParent(sigAdView);
            sigAdView.destroy();
            sigAdView = null;
        }
        if (mAdUnit != null && mAdUnit.getSessionManager() != null) {
            getSessionManager().endDisplaySession(mAdUnit);
        }
        if (mWidgetView != null) {
            mWidgetView.destroy();
            widgetViews.remove(mWidgetView);
            ViewUtil.removeFromParent(mWidgetView);
            mWidgetView = null;
        }

        AdStackManager.cleanPlayAdUnit(mAdUnit);
        if (downloadDialog != null) {
            downloadDialog.dismiss();
            downloadDialog.destroy();
            downloadDialog = null;
        }

        mDislikeInteractionCallback = null;

        onPrivacyClickListener = null;
        if (sigEmptyView != null) {
            sigEmptyView.setAdVisibilityStatusChangeListener(null);
            ViewUtil.removeFromParent(sigEmptyView);
            sigEmptyView = null;
        }


        if (nativeAdInterstitial != null) {
            nativeAdInterstitial.onInvalidate(mAdUnit);
            nativeAdInterstitial = null;
        }
        if (mAdUnit != null) {
            mAdUnit.destroy();
        }

    }

    @Override
    public void onAdViewImpression(boolean isValidImpression) {

        if (isImpressioned) return;

        if (!misShowDetailPage) {
            if (isValidImpression) {

                if (adInteractionListener != null) {
                    adInteractionListener.onAdExposed();
                }
                getSessionManager().recordDisplayEvent(mAdUnit, ADEvent.AD_NATIVE_SHOW);
            }
        }

        isImpressioned = isValidImpression;

    }

    @Override
    public void onAdViewPauseImpression() {

        if (mStartImpression) {
            if (mWidgetView != null) {
                mWidgetView.onPause();
                widgetViews.remove(mWidgetView);
            }
            SigVideoAdController videoAdController = getVideoAdController();
            if (videoAdController != null) {
                videoAdController.pause();
            }
            SigmobLog.d("-----------onAdViewPauseImpression---------");
            mStartImpression = false;
        }
    }

    @Override
    public void onAdViewStartImpression() {


        if (mWidgetView != null && !widgetViews.isEmpty()) {
            SigmobMotionView pop = widgetViews.lastElement();
            if (pop != null && pop == mWidgetView) {
                mWidgetView.onStart();
            }
        }

        if (!misShowDetailPage && !mStartImpression) {
            mStartImpression = true;
            if (mWidgetView != null && !widgetViews.contains(mWidgetView)) {
                widgetViews.push(mWidgetView);
                mWidgetView.onStart();
            }
            if (sigAdView != null && sigAdView.getParent() != null) {
                NativeAdConfig adConfig = getAdConfig();
                if (!isUsePause && adConfig != null) {
                    getVideoAdController().start();
                }
                SigmobLog.d("------------onAdViewStartImpression------------");
            }
        }
    }

    private boolean isAdVisible() {

        boolean isExpired = mAdUnit.isExpiredAd();

        return isExpired;
    }

    @Override
    public void onAdViewRemoved() {

        if (!misShowDetailPage && getContext() != null && isAdShow) {
            if (mCreativeViews != null) {
                mCreativeViews.clear();
            }
            isAdShow = false;

            if (mWidgetView != null) {
                mWidgetView.onPause();
                widgetViews.remove(mWidgetView);
            }
            SigmobLog.d("------------onAdViewRemoved----------" + this.hashCode());
            SigVideoAdController controller = getVideoAdController();
            if (controller != null) {
                controller.pause();
            }
            mStartImpression = false;

            if (sigEmptyView != null && sigEmptyView.hasWindowFocus()) {
                isImpressioned = false;
            }
            SessionManager sessionManager = getSessionManager();
            if (sessionManager != null) {
                sessionManager.recordDisplayEvent(mAdUnit, ADEvent.AD_NATIVE_HIDE);
            }

            if (!isFirstPlay && !isPaused) {
//                if (sessionManager != null) {
//                    sessionManager.recordDisplayEvent(ADEvent.AD_VIDEO_LINK, 0);
//                }
            }
        }

    }

    @Override
    public void onAdViewShow() {

        if (isAdShow) return;

        if (!misShowDetailPage) {
            isAdShow = true;
            SigmobLog.d("----------onAdViewShow------------" + this.hashCode());
            isUsePause = false;
            mStartImpression = false;


            //广告展示进行stopvideo，保持iOS一致
            View adContainer = getAdContainer();
            if (adContainer != null) {
                getAdConfig().initFourElements(ViewUtil.getActivityFromViewTop(adContainer), mAdUnit, onPrivacyClickListener);

                adContainer.setOnTouchListener(this);
            }
            if (sigAdView != null && !mCreativeViews.contains(sigAdView)) {
                mCreativeViews.add(sigAdView);
            } else if (mImageLists != null) {
                if (mCreativeViews.isEmpty()) {
                    mCreativeViews.addAll(mImageLists);
                } else {
                    for (View imageView : mImageLists) {
                        if (!mCreativeViews.contains(imageView)) {
                            mCreativeViews.add(imageView);
                        }
                    }
                }
            }
            if (mWidgetView != null && !mCreativeViews.contains(mWidgetView)) {
                mCreativeViews.add(mWidgetView);
            }
            mCreativeViews.addAll(mCreativeViewList);

            updateViewClickListener(mClickViewList, this);

            SessionManager sessionManager = getSessionManager();
            if (sessionManager != null) {
                sessionManager.recordDisplayEvent(mAdUnit, ADEvent.AD_SHOW);
            }

        }
    }

    @Override
    public NativeAdConfig getAdConfig() {
        if (adConfig == null && mAdUnit != null) {
            adConfig = (NativeAdConfig) mAdUnit.getAdConfig();
        }
        return adConfig;
    }

    private boolean isShowDetailPage() {
        return false;//!TextUtils.isEmpty(mAdUnit.getHtmlData()) || !TextUtils.isEmpty(mAdUnit.getHtmlUrl());
    }

    private View getCtaClickView(List<View> viewList, MotionEvent event) {

        if (viewList == null) return null;

        for (View view : viewList) {
            if (ViewUtil.isPointInView(view, event)) {
                return view;
            }
        }
        return null;
    }

    private View getCtaClickView(HashSet<View> viewList, MotionEvent event) {

        if (viewList == null) return null;

        for (View view : viewList) {
            if (ViewUtil.isPointInView(view, event)) {
                return view;
            }
        }
        return null;
    }

    private boolean isSigAdViewCtaClick(MotionEvent event) {
        if (sigAdView != null) {
            return sigAdView.isCtaClick(event);
        }
        return false;
    }

    private void handClick(int viewType, MotionEvent event) {

        boolean isMaterialClick = false;
        AdStackManager.addAdUnit(mAdUnit);
        TouchLocation uplocation = null;
        TouchLocation downlocation = null;
        if (event != null) {
            uplocation = TouchLocation.getTouchLocation(getAdContainer(), event);
            downlocation = TouchLocation.getTouchLocation(getAdContainer(), down);
        }

        switch (viewType) {
            case APP_INFO_VIEW: {
                SessionManager sessionManager = getSessionManager();
                if (sessionManager != null) {
                    sessionManager.recordDisplayEvent(mAdUnit, ADEvent.AD_CLICK);
                }
                openFourElements();
                return;
            }
            case CREATIVE_VIEW:
            case MOTION_VIEW:
            case BTN_VIEW: {
                getAdConfig().handleUrlAction(mAdUnit, "");
            }
            break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event == null) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (lastDownEventTime > 0) {
                long offset = System.currentTimeMillis() - lastDownEventTime;
                if (offset < 500) {
                    return false;
                }
            }
        }

        if (mAdUnit != null) {
            try {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (v == mSigAppInfoView) {
                        SigmobLog.d("click mSigAppInfoView");
                        handClick(APP_INFO_VIEW, event);
                        return true;
                    }

                    SigmobLog.d("click " + v);
                    View ctaClickView = null;
                    if (mClickViewList != null) {
                        //cta 按钮点击
                        ctaClickView = mClickViewList.contains(v) ? v : getCtaClickView(mClickViewList, event);
                        if (ctaClickView != null) {
                            SigmobLog.d("click ctaClickView");
                            handClick(BTN_VIEW, event);
                            return true;
                        }
                    }

                    if (mCreativeViews != null) {
                        //创意点击
                        ctaClickView = mCreativeViews.contains(v) ? v : getCtaClickView(mCreativeViews, event);
                        if (ctaClickView != null) {
                            SigmobLog.d("click mCreativeViews");
                            //预览页视频cta按钮检查
                            boolean isSigbtn = sigAdView != null && sigAdView.isCtaClick(event);
                            handClick(isSigbtn ? BTN_VIEW : CREATIVE_VIEW, event);
                            return true;
                        }
                    }
                } else {
                    lastDownEventTime = System.currentTimeMillis();
                    down = event;
                    return true;
                }
            } catch (Throwable th) {
                SigmobLog.e("onTouch error", th);
            }
        }

        return false;
    }

    public String getCTAText() {
        return getAdUnit().getCTAText();
    }

    public void startVideo() {

        if (misShowDetailPage) return;

        SigVideoAdController videoAdController = getVideoAdController();
        if (videoAdController != null) {
            videoAdController.start();
        }
    }

    public void pauseVideo() {
        if (misShowDetailPage) return;

        SigVideoAdController videoAdController = getVideoAdController();
        if (videoAdController != null) {
            isUsePause = true;
            videoAdController.pause();
        }
    }

    @Override
    public void onAdShowFailed(BaseAdUnit adUnit, String error) {

    }

    @Override
    public void onAdShow(BaseAdUnit adUnit) {
        if (sigAdView != null) {
            View adContainer = (View) sigAdView.getParent();
            ViewGroup.LayoutParams layoutParams = adContainer.getLayoutParams();
            SigmobLog.d("visibilityStatusChange() called with: isVisible = [" + layoutParams.width + ":" + layoutParams.height + "]");
        }
    }

    @Override
    public void onAdClicked(BaseAdUnit adUnit) {

    }

    @Override
    public void onAdClose(BaseAdUnit adUnit) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onAdDetailShow() {
        SigmobLog.d("----------onAdDetailShow----------");

        if (getAdConfig() != null) {
            getAdConfig().handleDetailAdShow(getContext(), mAdUnit);
        }
        misShowDetailPage = true;
        if (adInteractionListener != null) {
            adInteractionListener.onAdDetailShow();
        }
    }

    @Override
    public void onAdDetailClick() {
//        if (getAdConfig() != null) {
//            getAdConfig().handleFeedClick(getContext(), mAdUnit);
//        }
//        if (adInteractionListener != null) {
//            adInteractionListener.onAdClicked();
//        }
    }

    @Override
    public void onAdDetailDismiss() {

        misShowDetailPage = false;


        if (adInteractionListener != null) {
            adInteractionListener.onAdDetailDismiss();
        }
        if (getAdConfig() != null) {
            getAdConfig().handleDetailAdClose(getContext(), mAdUnit);
        }

        retryAddIntoParent();

        if (mSigAppInfoView != null) {
            mSigAppInfoView.setVisibility(View.VISIBLE);
        }
        if (nativeAdInterstitial != null) {
            nativeAdInterstitial.onInvalidate(mAdUnit);
        }

        //广告展示进行stopvideo，保持iOS一致
        View adContainer = getAdContainer();
        if (adContainer != null) {
            getAdConfig().initFourElements(ViewUtil.getActivityFromViewTop(adContainer), mAdUnit, onPrivacyClickListener);
        }
    }

    /**
     * 重新添加进父容器
     */
    public void retryAddIntoParent() {

        if (sigAdViewParentVew != null && sigAdView != null) {
            ViewParent parent = sigAdView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(sigAdView);
            }
            sigAdViewParentVew.removeAllViews();

            if (sigAdViewParentVewLP != null) {
                sigAdViewParentVew.addView(sigAdView, sigAdViewParentVewLP);
            } else {
                sigAdViewParentVew.addView(sigAdView);
            }
            if (!mCreativeViews.contains(sigAdView)) {
                mCreativeViews.add(sigAdView);
            }
        }
        sigAdViewParentVew = null;
        sigAdViewParentVewLP = null;
    }


    public void resumeVideo() {
        if (misShowDetailPage) return;

        SigVideoAdController videoAdController = getVideoAdController();
        if (videoAdController != null) {
            videoAdController.resume();
        }
    }

    public void stopVideo() {
        if (misShowDetailPage) return;
        SigVideoAdController videoAdController = getVideoAdController();
        if (videoAdController != null) {
            videoAdController.stop();
        }
    }

    public void setVideoMute(boolean mute) {
        if (misShowDetailPage) return;

        SigVideoAdController videoAdController = getVideoAdController();
        if (videoAdController != null) {
            videoAdController.setMute(mute);
        }
    }

    @Override
    public void onLandPageShow() {
        if (getAdConfig() != null) {
            getAdConfig().handleLandPageShow(getContext(), mAdUnit);
        }
    }

    @Override
    public void onLandPageClose() {
        if (getAdConfig() != null) {
            getAdConfig().handleLandPageClose(getContext(), mAdUnit);
        }
    }

    public void setDislikeInterActionCallBack(NativeAdData.DislikeInteractionCallback dislikeInteractionCallback) {
        mDislikeInteractionCallback = dislikeInteractionCallback;
    }

    @Override
    public void onShow() {
        if (mDislikeInteractionCallback != null) {
            mDislikeInteractionCallback.onShow();
        }
    }

    @Override
    public void onSelected(int position, String value, boolean enforce) {
        if (mDislikeInteractionCallback != null) {
            mDislikeInteractionCallback.onSelected(position, value, enforce);
        }
    }

    @Override
    public void onCancel() {
        if (mDislikeInteractionCallback != null) {
            mDislikeInteractionCallback.onCancel();
        }
    }

    public View getWidgetView(int width, int height) {

        if (mWidgetView == null && mAdUnit != null) {

            SigmobMotionView sigmobMotionView = new SigmobMotionView(getContext());
            boolean result = sigmobMotionView.initWidgetView(138757, mAdUnit.getSensitivity());
            if (!result) {
                return null;
            }
            sigmobMotionView.setMotionActionListener(new MotionActionListener() {
                @Override
                public void onAction() {
                    handClick(MOTION_VIEW, null);
                }

            });
            int width_px = Dips.dipsToIntPixels(width, getContext());
            int height_px = Dips.dipsToIntPixels(height, getContext());

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width_px, height_px);
            layoutParams.gravity = Gravity.CENTER;
            sigmobMotionView.setLayoutParams(layoutParams);
            mWidgetView = sigmobMotionView;
        }
        return mWidgetView;
    }
}
