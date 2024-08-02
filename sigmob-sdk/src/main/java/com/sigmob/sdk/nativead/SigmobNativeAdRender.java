package com.sigmob.sdk.nativead;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.common.utils.TouchLocation;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.OnSigAdClickListener;
import com.sigmob.sdk.base.common.SessionManager;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.ClickCommon;
import com.sigmob.sdk.base.models.SigImage;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.models.rtb.AdPrivacy;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.base.views.DownloadDialog;
import com.sigmob.sdk.splash.SplashAdView;
import com.sigmob.sdk.videoAd.FractionalProgressAdTracker;
import com.sigmob.sdk.videoAd.InterActionType;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.NativeADEventListener;
import com.sigmob.windad.natives.WindNativeAdData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SigmobNativeAdRender implements SigEmptyView.AdVisibilityStatusChangeListener,
        View.OnTouchListener, SigmobNativeAdRenderListener,
        NativeAdInterstitial.NativeAdInterstitialListener, View.OnClickListener, WindNativeAdData.DislikeInteractionCallback {

    private static final int APP_INFO_VIEW = 1;
    private static final int CREATIVE_VIEW = 2;
    private static final int BTN_VIEW = 3;

    private static final int MOTION_VIEW = 4;
    protected WindNativeAdData mNativeAdUnit;
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
    private Bitmap mAdLogo;
    private DownloadDialog downloadDialog;
    private boolean isElementDialogShow;
    private SigmobAdDislike mDislikeDialog;
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
    private WindNativeAdData.DislikeInteractionCallback mDislikeInteractionCallback;
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
            sessionManager = new NativeAdViewAbilitySessionManager();
            sessionManager.createDisplaySession(mAdUnit);
        }
        return sessionManager;
    }


    @Override
    public BaseAdUnit getAdUnit() {
        return mAdUnit;
    }

    @Override
    public WindNativeAdData getNativeAdUnit() {
        return mNativeAdUnit;
    }

    public void initAdData(final BaseAdUnit adUnit, WindNativeAdData nativeAdUnit) {
        mAdUnit = adUnit;
        mNativeAdUnit = nativeAdUnit;

        AdStackManager.getImageManger().getBitmap(getAdUnit().getAd_source_logo(), new ImageManager.BitmapLoadedListener() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                mAdLogo = bitmap;
            }

            @Override
            public void onBitmapLoadFailed() {
            }
        });
    }

    public SigAppInfoView getAppInfoView() {


        if (!showWithoutAppInfo && mSigAppInfoView == null) {
            AdPrivacy adPrivacy = mAdUnit.getadPrivacy();
            if (adPrivacy != null) {
                mSigAppInfoView = new SigAppInfoView(getContext());

                mSigAppInfoView.setOnTouchListener(this);
                mSigAppInfoView.initData(mAdUnit.getAppVersion(), mAdUnit.getCompanyName());
            }
        }

        return mSigAppInfoView;
    }

    private Context getContext() {
        return SDKContext.getApplicationContext();
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
                if (view != null){
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

    public void bindMediaViewWithoutAppInfo(ViewGroup mediaLayout, WindNativeAdData.NativeADMediaListener nativeADMediaListener) {

        showWithoutAppInfo = true;
        bindMediaView(mediaLayout, nativeADMediaListener);
    }

    public void bindMediaView(final ViewGroup mediaContainer, final WindNativeAdData.NativeADMediaListener sigAdVideoEventListener) {

        if (mediaContainer == null) return;
        final SigNativeAdVideo tempSigAdView = ViewUtil.findViewByClass(mediaContainer,SigNativeAdVideo.class);
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

                    getSessionManager().recordDisplayEvent(ADEvent.AD_NATIVE_VIDEO_RESTART, 0);

                }

                @Override
                public void onVideoLoad() {
                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoLoad();
                    }

                }

                @Override
                public void onVideoError(WindAdError error) {
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
                    mAdUnit.getVideoCommon().video_time = sigVideoAdController.getVideoDuration();
                    mAdUnit.getVideoCommon().is_first = 1;
                    mAdUnit.getVideoCommon().is_last = 0;
                    mAdUnit.getVideoCommon().end_time = 0;
                    mAdUnit.getVideoCommon().is_auto_play = getAdConfig().isAutoPlay() ? 1 : 2;

                    if (misShowDetailPage) {
                        mAdUnit.getVideoCommon().scene = 3;
                    } else {
                        mAdUnit.getVideoCommon().scene = 1;
                    }

                    if (isFirstPlay) {
                        mAdUnit.getVideoCommon().type = 1;
                        isFirstPlay = false;
                    } else {
                        mAdUnit.getVideoCommon().type = 3;
                    }

                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoStart();
                    }
                    isUsePause = false;
                    isPaused = false;

                    getSessionManager().recordDisplayEvent(ADEvent.AD_VIDEO_START, 0);
                }

                @Override
                public void onVideoPause() {
                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoPause();
                    }
                    isPaused = true;
                    getSessionManager().recordDisplayEvent(ADEvent.AD_VIDEO_PAUSE, 0);
                    getSessionManager().recordDisplayEvent(ADEvent.AD_VIDEO_LINK, 0);

                }

                @Override
                public void onVideoResume() {
                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoResume();
                    }
                    isPaused = false;
                    mAdUnit.getVideoCommon().type = 2;
                    mAdUnit.getVideoCommon().is_first = 0;

                    mAdUnit.getVideoCommon().begin_time = sigVideoAdController.getCurrentPosition();
                    isUsePause = false;

                    getSessionManager().recordDisplayEvent(ADEvent.AD_VIDEO_START, sigVideoAdController.getCurrentPosition());
                }

                @Override
                public void onProgressUpdate(long current, long duration) {

                    mAdUnit.getVideoCommon().end_time = sigVideoAdController.getCurrentPosition();

                    NativeAdConfig adConfig = getAdConfig();
                    if (adConfig != null) {
                        List<FractionalProgressAdTracker> trackers = adConfig.getUntriggeredTrackersBefore(current, duration);

                        for (FractionalProgressAdTracker adTracker : trackers) {
                            getSessionManager().recordDisplayEvent(adTracker.getEvent(), sigVideoAdController.getCurrentPosition());
                            adTracker.setTracked();
                        }
                    }
                }

                @Override
                public void onVideoCompleted() {
                    if (sigAdVideoEventListener != null) {
                        sigAdVideoEventListener.onVideoCompleted();
                    }
                    mAdUnit.getVideoCommon().end_time = sigVideoAdController.getVideoDuration();
                    mAdUnit.getVideoCommon().is_last = 1;

                    getSessionManager().recordDisplayEvent(ADEvent.AD_COMPLETE, sigVideoAdController.getCurrentPosition());
                    getSessionManager().recordDisplayEvent(ADEvent.AD_VIDEO_LINK, 0);

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

                AdStackManager.getImageManger().load(sigImage.getImageUrl())
                        .placeholder(resId).error(resId).into(imageView);
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
                                           final View disLikeView,
                                           NativeADEventListener nativeADEventListener) {

        if (view == null) {
            if (nativeADEventListener != null) {
                WindAdError adError = WindAdError.ERROR_SIGMOB_ADCONTAINER_IS_NULL;
                nativeADEventListener.onAdError(adError);
            }
            return;
        }


        if (!(view instanceof ViewGroup)) {
            if (nativeADEventListener != null) {
                WindAdError adError = WindAdError.ERROR_SIGMOB_ADCONTAINER_NOT_VIEWGROUP;
                nativeADEventListener.onAdError(adError);
            }
            return;
        }


        Activity topActivity = SDKContext.getTopActivity();
        if (topActivity == null) {
            SDKContext.setTopActivity(ViewUtil.getActivityFromView(view));
        }

        if (disLikeView != null) {

            if (mDislikeDialog == null) {
                mDislikeDialog = new DisLikeDialog(disLikeView.getContext(), mAdUnit);
                mDislikeDialog.setDislikeInteractionCallback(this);
            }
            disLikeView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {

                        if (mDislikeDialog != null) {
                            mDislikeDialog.showDislikeDialog();
                        }
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
                    mAdUnit.getClickCommon().adarea_x = String.valueOf(Dips.pixelsToIntDips(position[0], getContext()));
                    mAdUnit.getClickCommon().adarea_y = String.valueOf(Dips.pixelsToIntDips(position[1], getContext()));
                    mAdUnit.getClickCommon().adarea_w = String.valueOf(Dips.pixelsToIntDips(adContainer.getWidth(), getContext()));
                    mAdUnit.getClickCommon().adarea_h = String.valueOf(Dips.pixelsToIntDips(adContainer.getHeight(), getContext()));
                }
            }
        });

        final SigmobMotionView motionView = ViewUtil.findViewByClass(adContainer, SigmobMotionView.class);
        if (motionView != null && motionView != mWidgetView) {
            ViewUtil.removeFromParent(motionView);
        }

        getAdConfig().setOnAdClickListener(new OnSigAdClickListener() {
            @Override
            public void onAdClick(boolean isRecord, ClickUIType type) {

                if (adInteractionListener != null) {
                    adInteractionListener.onAdClicked();
                }
                if (!isRecord) {
                    return;
                }
                SessionManager sessionManager = getSessionManager();
                if (sessionManager != null) {
                    sessionManager.recordDisplayEvent(ADEvent.AD_CLICK, 0);
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
        getNativeAdInterstitial().loadInterstitial(null, mAdUnit);

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
            mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_PREVIEW;

//            if (sigAdView != null) {
//                getVideoAdController().start();
//            }
            getSessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_CLOSE, 0);

        }

        @Override
        public void onButtonClick(String url, String clickCoordinate) {//执行点击逻辑
            if (mAdUnit != null) {
                mAdUnit.getClickCommon().sld = "0";
                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
                mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_APPINFO;
                mAdUnit.getClickCommon().is_final_click = true;
                if (isElementDialogShow) {
                    getAdConfig().handleUrlFourAction(ClickUIType.PREVIEW, url, clickCoordinate, true);
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
            getSessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_SHOW, 0);
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
                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_SCENE_APPINFO;

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
//
//        if (getNativeAdUnit().getAdViewModel() == ADVIEW_MODEL_VIDEO) {
//            if (sigAdView == null) {
//                sigAdView = SigAdViewCachePool.getSigNativeAdVideo(getContext());
//                sigAdView.init(this);
//                if (!mCreativeViews.contains(sigAdView)) {
//                    mCreativeViews.add(sigAdView);
//                }
//                addAppInfoView(sigAdView);
//
//            }
//            return sigAdView;
//        }
//        return null;

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
        SigmobLog.i(String.format("native ad data %s is Destroy", mAdUnit != null ? mAdUnit.getVid() : "null"));

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
            getSessionManager().recordDisplayEvent(ADEvent.AD_HIDE, 0);
            getSessionManager().endDisplaySession();
        }
        if (mWidgetView != null) {
            mWidgetView.destroy();
            widgetViews.remove(mWidgetView);
            ViewUtil.removeFromParent(mWidgetView);
            mWidgetView = null;
        }

        AdStackManager.shareInstance().removeHistoryAdCache(mAdUnit);
        AdStackManager.cleanPlayAdUnit(mAdUnit);
        if (downloadDialog != null) {
            downloadDialog.dismiss();
            downloadDialog.destroy();
            downloadDialog = null;
        }
        if (mDislikeDialog != null && mDislikeDialog instanceof DisLikeDialog) {
            mDislikeDialog.setDislikeInteractionCallback(null);
            DisLikeDialog dialog = (DisLikeDialog) mDislikeDialog;
            dialog.dismiss();
            dialog.destroy();
            mDislikeDialog = null;
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
        if(mAdUnit != null){
            mAdUnit.destroy();
        }

    }

    @Override
    public void onAdViewImpression(boolean isValidImpression) {

        if (isImpressioned) return;

        if (!misShowDetailPage) {
            AdStackManager.shareInstance().removeHistoryAdCache(mAdUnit);

            if (isValidImpression) {

                if (adInteractionListener != null) {
                    adInteractionListener.onAdExposed();
                }
                getSessionManager().recordDisplayEvent(ADEvent.AD_NATIVE_SHOW, 0);

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


        if (mWidgetView != null && !widgetViews.isEmpty() ){
            SigmobMotionView pop = widgetViews.lastElement();
            if ( pop != null && pop == mWidgetView) {
                mWidgetView.onStart();
            }
        }

        if (!misShowDetailPage && !mStartImpression) {
            mStartImpression = true;
            if(mWidgetView != null &&  !widgetViews.contains(mWidgetView)) {
                widgetViews.push(mWidgetView);
                mWidgetView.onStart();
            }
            if (sigAdView != null && sigAdView.getParent() != null) {
                NativeAdConfig adConfig = getAdConfig();
                if(!isUsePause  && adConfig != null && adConfig.isAutoPlay()){
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
                sessionManager.recordDisplayEvent(ADEvent.AD_HIDE, 0);
            }

            if (!isFirstPlay && !isPaused) {
                if (sessionManager != null) {
                    sessionManager.recordDisplayEvent(ADEvent.AD_VIDEO_LINK, 0);
                }
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
                sessionManager.recordDisplayEvent(ADEvent.AD_START, 0);
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
        return  false;//!TextUtils.isEmpty(mAdUnit.getHtmlData()) || !TextUtils.isEmpty(mAdUnit.getHtmlUrl());
    }

    public void showLandPage() {

        //需要拿到过度的View
        Bundle bundle = new Bundle();
        if (sigAdView != null && getAdConfig().isUse_na_video_component()) {
            ViewAttr attr = new ViewAttr();
            int[] location = new int[2];
            sigAdView.getLocationOnScreen(location);
            attr.setX(location[0]);
            attr.setY(location[1]);
            attr.setWidth(sigAdView.getMeasuredWidth());
            attr.setHeight(sigAdView.getMeasuredHeight());
            bundle.putParcelable("attr", attr);
            /**
             * 进入模版页之前需要记录父容器及属性
             */
            sigAdViewParentVew = (ViewGroup) sigAdView.getParent();
            sigAdViewParentVewLP = sigAdView.getLayoutParams();
            SigmobNativeAdLandViewController.setSigAdView(sigAdView);

            SigMacroCommon macroCommon = mAdUnit.getMacroCommon();
            if (macroCommon != null) {
                macroCommon.addMarcoKey(SigMacroCommon._SCENE_, "3");
            }
            misShowDetailPage = true;

        }
        if (mSigAppInfoView != null) {
            mSigAppInfoView.setVisibility(View.INVISIBLE);
        }

        getNativeAdInterstitial().showInterstitial(mAdUnit, bundle);
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
            mAdUnit.getClickCommon().down = downlocation;
            mAdUnit.getClickCommon().up = downlocation;
        }

        mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_PREVIEW;
        switch (viewType) {
            case APP_INFO_VIEW: {
                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_APPINFO;
                mAdUnit.getClickCommon().is_final_click = false;
                SessionManager sessionManager = getSessionManager();
                if (sessionManager != null) {
                    sessionManager.recordDisplayEvent(ADEvent.AD_CLICK, 0);
                }
                openFourElements();

                return;
            }
            case CREATIVE_VIEW: {
                boolean showable = isShowDetailPage();
                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_MATERIAL;
                if (showable) {
                    mAdUnit.getClickCommon().is_final_click = false;
                    getSessionManager().recordDisplayEvent(ADEvent.AD_CLICK, 0);
                    showLandPage();
                    return;
                }
                isMaterialClick = true;
            }
            case MOTION_VIEW:{
                if (!isMaterialClick) {
                    mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_COMPONENT;
                    isMaterialClick = true;
                }
            }
            case BTN_VIEW: {

                if (!isMaterialClick) {
                    mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
                }

                if (!showWithoutAppInfo && mNativeAdUnit.getAdPatternType() == SigmobNativeAd.ADVIEW_MODEL_VIDEO) {
                    mAdUnit.getClickCommon().is_final_click = true;
                    getAdConfig().handleClick(getContext(), downlocation, uplocation, ClickUIType.PREVIEW, mAdUnit);
                } else {
                    //非视频下载类型cta点击或者无详情页先进入四要素
                    if (mAdUnit.getInteractionType() == InterActionType.DownloadType) {
                        mAdUnit.getClickCommon().is_final_click = false;
                        getAdConfig().handleClick(getContext(), downlocation, uplocation, ClickUIType.PREVIEW, mAdUnit);
                    } else {
                        mAdUnit.getClickCommon().is_final_click = true;
                        getAdConfig().handleClick(getContext(), downlocation, uplocation, ClickUIType.PREVIEW, mAdUnit);
                    }
                }
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

                    mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_PREVIEW;
                    mAdUnit.getClickCommon().sld = "0";
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

                            mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_PREVIEW;
                            mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
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

    public Bitmap getAdLogo() {
        return mAdLogo;
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
    public void onInterstitialFailed(BaseAdUnit adUnit, String error) {

    }

    @Override
    public void onInterstitialShown(BaseAdUnit adUnit) {
        if (sigAdView != null) {

            View adContainer = (View) sigAdView.getParent();
            ViewGroup.LayoutParams layoutParams = adContainer.getLayoutParams();
            SigmobLog.d("visibilityStatusChange() called with: isVisible = [" + layoutParams.width + ":" + layoutParams.height + "]");

//            if (layoutParams != null && layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT){
//                if ( sigAdView.getVideoSurferViewHeight()>0){
//                    layoutParams.height =  sigAdView.getVideoSurferViewHeight();
//                }
//            }
        }
    }

    @Override
    public void onInterstitialClicked(BaseAdUnit adUnit) {
//        if (adInteractionListener != null) {
//            adInteractionListener.onAdClicked(getNativeAdUnit());
//        }
    }

    @Override
    public void onInterstitialDismissed(BaseAdUnit adUnit) {

    }

    @Override
    public void onInterstitialVOpen(BaseAdUnit mAdUnit) {

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
//        AdStackManager.cleanPlayAdUnit(mAdUnit);

    }

    public void setDislikeInterActionCallBack(WindNativeAdData.DislikeInteractionCallback dislikeInteractionCallback) {
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


    public String getEcpm() {
        if (mAdUnit != null &&  mAdUnit.bidding_response != null) {
            return String.valueOf(mAdUnit.bidding_response.ecpm);
        }
        return "";
    }

    public View getWidgetView(int width,int height) {

        if (mWidgetView == null && mAdUnit != null) {

            SigmobMotionView sigmobMotionView = new SigmobMotionView(getContext());
            boolean result = sigmobMotionView.initWidgetView((int) mAdUnit.getWidgetId(0),
                    mAdUnit.getSensitivity(),
                    mAdUnit.getClickCommon());
            if (!result) {
                return null;
            }
            sigmobMotionView.setMotionActionListener(new MotionActionListener() {
                @Override
                public void onAction() {
                    handClick(MOTION_VIEW,null);
                }

            });
            int width_px = Dips.dipsToIntPixels(width,getContext());
            int height_px = Dips.dipsToIntPixels(height,getContext());

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width_px, height_px);
            layoutParams.gravity = Gravity.CENTER;
            sigmobMotionView.setLayoutParams(layoutParams);
            mWidgetView = sigmobMotionView;
        }
        return mWidgetView;
    }
}
