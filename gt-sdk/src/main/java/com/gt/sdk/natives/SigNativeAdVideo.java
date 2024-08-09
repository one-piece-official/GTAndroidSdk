package com.gt.sdk.natives;

import static com.sigmob.sdk.videoplayer.VideoAdView.STATE_AUTO_COMPLETE;
import static com.sigmob.sdk.videoplayer.VideoAdView.STATE_ERROR;
import static com.sigmob.sdk.videoplayer.VideoAdView.STATE_NORMAL;
import static com.sigmob.sdk.videoplayer.VideoAdView.STATE_PLAYING;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.common.utils.ImageUtils;
import com.gt.sdk.base.blurkit.BlurKit;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.videoplayer.OnVideoAdViewListener;
import com.gt.sdk.base.videoplayer.VideoAdView;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.blurkit.BlurKit;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.videoplayer.OnVideoAdViewListener;
import com.sigmob.sdk.videoplayer.SigUtils;
import com.sigmob.sdk.videoplayer.VideoAdButton;
import com.sigmob.sdk.videoplayer.VideoAdView;
import com.sigmob.windad.WindAdError;

import java.io.File;
import java.util.LinkedList;


public class SigNativeAdVideo extends SigNativeAdView implements SigAdVideoStatusListener, OnVideoAdViewListener {

    public static LinkedList<ViewGroup> CONTAINER_LIST = new LinkedList<>();
    public static long lastAutoFullscreenTime = 0;
    private VideoAdView mVideoAdView;
    private SigAppView mAppView;
    private SigVideoAdController videoAdController;
    private Bitmap lastBitmap = null;
    private Bitmap lastBlurBitmap = null;
    private ViewGroup videoContainer;

    private Context mContext;
    private long mCurrentPosition;

    public SigNativeAdVideo(Context context) {
        super(context.getApplicationContext());
        mContext = context.getApplicationContext();
        getVideoAdView().setVideoAdViewListener(this);
        videoContainer = new RelativeLayout(mContext);
        getVideoAdView().setVideoAdStatusListener(this);
        BlurKit.init(mContext);
    }


    @Override
    public SigVideoAdController getSigVideoAdController() {
        if (videoAdController == null) {
            videoAdController = new SigVideoAdControllerImpl(getVideoAdView());
        }
        return videoAdController;
    }


    public VideoAdView getVideoAdView() {
        if (mVideoAdView == null) {
            mVideoAdView = new VideoAdView(mContext);
        }
        return mVideoAdView;
    }


    public SigAppView getAppView() {
        if (mAppView == null) {
            mAppView = new SigAppView(mContext);
        }
        return mAppView;
    }

    public void init(SigmobNativeAdRenderListener listener) {
        super.init(listener);
        final BaseAdUnit adUnit = getAdUnit();
        if (adUnit != null) {
            removeAppInfoView(this);
            File file = adUnit.getVideoProxyFile();
            if (file != null && file.exists()) {
                getVideoAdView().setUp(file.getAbsolutePath());
            } else {
                getVideoAdView().setUp(adUnit.getProxyVideoUrl());
            }

            setUIStyle(SigAdStyle.PREVIEW);

            if (TextUtils.isEmpty(adUnit.getVideoThumbUrl())) {
                getVideoAdView().enableVideoThumbImage(true);
            } else {
                ImageManager.with(mContext).load(adUnit.getVideoThumbUrl()).into(getVideoAdView().getThumbView());
            }

            getAppView().initData(adUnit.getIconUrl(), adUnit.getTitle(), adUnit.getCTAText());
            getVideoAdView().setSoundChange(getAdConfig().getPreview_page_video_mute());

        }
    }

    private void removeAppInfoView(ViewGroup adContainer) {
        SigAppInfoView sigAppInfoView = null;

        for (int i = 0; i < adContainer.getChildCount(); i++) {
            View view = adContainer.getChildAt(i);
            if (view instanceof SigAppInfoView) {
                sigAppInfoView = (SigAppInfoView) view;
            }
        }

        if (sigAppInfoView != null) {
            ViewUtil.removeFromParent(sigAppInfoView);
        }
    }


    public boolean isCtaClick(MotionEvent event) {

        if (mAppView != null && mAppView.getParent() != null && mAppView.getVisibility() == VISIBLE) {
            return ViewUtil.isPointInView(mAppView.getCtaView(), event);
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getVideoAdView() != null) {
            getVideoAdView().goOnPlayOnResume();
        }
    }

    @Override
    public boolean onBackPressed() {

        if (CONTAINER_LIST.size() != 0 && getVideoAdView() != null) {//判断条件，因为当前所有goBack都是回到普通窗口
            getVideoAdView().gotoScreenNormal();
            return true;
        } else if (CONTAINER_LIST.size() == 0 && getVideoAdView() != null && getVideoAdView().screen != SCREEN_NORMAL) {//退出直接进入的全屏
            getVideoAdView().clearFloatScreen();
            return true;
        }
        return false;
    }

    @Override
    public void onScreenNormal() {

        ViewGroup vg = (ViewGroup) videoContainer.getParent();
        if (vg != null) {
            vg.removeView(videoContainer);
        }
        Activity activity = SigUtils.scanForActivity(CONTAINER_LIST.getLast().getContext());

        if (activity != null) {
            // 非全屏显示，显示状态栏和导航栏
            activity.getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_VISIBLE);
        }

//        SigUtils.showStatusBar(mContext);
//        SigUtils.showSystemUI(mContext);

        if (getVideoAdView().getVideoHeight() < getVideoAdView().getVideoWidth()) {
            if (activity != null) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
//        showNormalLayout();
        CONTAINER_LIST.getLast().removeAllViews();
        CONTAINER_LIST.getLast().addView(videoContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        CONTAINER_LIST.pop();
    }

//    View.SYSTEM_UI_FLAG_LAYOUT_STABLE：全屏显示时保证尺寸不变。
//    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN：Activity全屏显示，状态栏显示在Activity页面上面。
//    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION：效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION：隐藏导航栏
//    View.SYSTEM_UI_FLAG_FULLSCREEN：Activity全屏显示，且状态栏被隐藏覆盖掉。
//    View.SYSTEM_UI_FLAG_VISIBLE：Activity非全屏显示，显示状态栏和导航栏。
//    View.INVISIBLE：Activity伸展全屏显示，隐藏状态栏。
//    View.SYSTEM_UI_LAYOUT_FLAGS：效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY：必须配合View.SYSTEM_UI_FLAG_FULLSCREEN和View.SYSTEM_UI_FLAG_HIDE_NAVIGATION组合使用,达到的效果是拉出状态栏和导航栏后显示一会儿消失。

    @Override
    public void onScreenFullscreen() {
        ViewGroup vg = (ViewGroup) videoContainer.getParent();
        vg.removeView(videoContainer);
        CONTAINER_LIST.add(vg);
        Activity activity = SigUtils.scanForActivity(vg.getContext());

        if (activity != null) {

//            hideSystemUI(activity);

            vg = (ViewGroup) activity.findViewById(android.R.id.content);

            vg.addView(videoContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            if (getVideoAdView().getVideoHeight() < getVideoAdView().getVideoWidth()) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
//            showFullVideoLayout(activity);
        }
    }

    private void hideSystemUI(Activity activity) {
//        SigUtils.hideStatusBar(activity);
//        SigUtils.hideSystemUI(activity);//华为手机和有虚拟键的手机全屏时可隐藏虚拟键 issue:1326

//            if (Build.VERSION.SDK_INT >= 19) {
//                activity.getWindow().getDecorView().setSystemUiVisibility(
//                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//            }


        // 全屏展示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
            activity.getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            // 全屏显示，隐藏状态栏
            activity.getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private void showNormalLayout() {
        RelativeLayout.LayoutParams mBottomLayoutParams = (RelativeLayout.LayoutParams) getVideoAdView().getBottomLayoutView().getLayoutParams();
        if (mBottomLayoutParams != null) {
            mBottomLayoutParams.setMargins(Dips.asIntPixels(10, mContext), Dips.asIntPixels(0, mContext), Dips.asIntPixels(10, mContext), Dips.asIntPixels(15, mContext));
            getVideoAdView().getBottomLayoutView().setLayoutParams(mBottomLayoutParams);
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getVideoAdView().getTopLayoutView().getLayoutParams();
        if (layoutParams != null) {
            layoutParams.setMargins(Dips.asIntPixels(10, mContext), Dips.asIntPixels(15, mContext), Dips.asIntPixels(10, mContext), 0);
            getVideoAdView().getTopLayoutView().setLayoutParams(layoutParams);
        }
    }

    private void showFullVideoLayout(Activity activity) {
        int right = Dips.asIntPixels(10, mContext);
        int bottom = Dips.asIntPixels(15, mContext);
        int margin = SigUtils.getNavigationBarHeight(activity);
        if (getVideoAdView().getVideoHeight() < getVideoAdView().getVideoWidth()) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            right = right + margin;
        } else {
            bottom += margin;
        }
        RelativeLayout.LayoutParams mBottomLayoutParams = (RelativeLayout.LayoutParams) getVideoAdView().getBottomLayoutView().getLayoutParams();
        if (mBottomLayoutParams != null) {
            mBottomLayoutParams.setMargins(Dips.asIntPixels(10, mContext), 0, right, bottom);
            getVideoAdView().getBottomLayoutView().setLayoutParams(mBottomLayoutParams);
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getVideoAdView().getTopLayoutView().getLayoutParams();
        if (layoutParams != null) {
            layoutParams.setMargins(Dips.asIntPixels(10, mContext), Dips.asIntPixels(35, mContext), right, 0);
            getVideoAdView().getTopLayoutView().setLayoutParams(layoutParams);
        }
    }

    public ViewGroup getVideoContainer() {
        return videoContainer;
    }

    public void autoQuitFullscreen() {
        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 2000
//                && CURRENT_JZVD != null
                && getVideoAdView().state == STATE_PLAYING && getVideoAdView().screen == SCREEN_FULLSCREEN) {
            lastAutoFullscreenTime = System.currentTimeMillis();
            onBackPressed();
        }
    }

    @Override
    public void animateFinish() {
        super.animateFinish();
        //处理videoPlayerView的父容器重新添加他
    }

    @Override
    public void onPaused() {
        super.onPaused();
        if (getVideoAdView() != null) {
            getVideoAdView().goOnPlayOnPause();
        }
    }


    public SigAdVideoStatusListener getSigAdVideoStatusListener() {
        if (videoAdController != null) {
            return videoAdController.getSigAdVideoStatusListener();
        }
        return null;
    }


    @Override
    public void setUIStyle(SigAdStyle style) {
        if (getAdUnit() == null) return;

        super.setUIStyle(style);
        switch (style) {
            case PREVIEW: {

                videoContainer.removeAllViews();
                ViewUtil.removeFromParent(videoContainer);
                ViewUtil.removeFromParent(getVideoAdView());
                addView(getVideoAdView(), new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                if (getVideoAdView().state == STATE_NORMAL) {
                    getVideoAdView().showButton(VideoAdButton.START, true);
                } else {
                    getVideoAdView().showButton(VideoAdButton.START, false);
                }

                getVideoAdView().showButton(VideoAdButton.FULLSCREEN, false);
                getVideoAdView().showButton(VideoAdButton.VOLUME, false);
                getVideoAdView().showButton(VideoAdButton.BIGRETRY, false);
                getVideoAdView().showButton(VideoAdButton.RETRY, false);
                getVideoAdView().showButton(VideoAdButton.BACK, false);

                getVideoAdView().setSoundChange(getAdConfig().getPreview_page_video_mute());

                if (mAppView != null && (getVideoAdView().state == STATE_AUTO_COMPLETE || getVideoAdView().state == STATE_ERROR)) {
                    if (mAppView.getParent() == null) {
                        addAppView();
                    }
                    mAppView.setVisibility(VISIBLE);

                    getVideoAdView().showButton(VideoAdButton.RETRY, true);
                }
                addAppInfoView();

            }
            break;
            case DETAIL_PAGE: {
                if (mAppView.getParent() != null) {
                    mAppView.setVisibility(INVISIBLE);
                }
                if (videoContainer.getChildCount() > 0) {
                    videoContainer.removeAllViews();
                }
                removeView(getVideoAdView());
                videoContainer.addView(getVideoAdView(), new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                getVideoAdView().setSoundChange(getAdConfig().getDetail_page_video_mute() || SDKContext.isSysMute());

                getVideoAdView().showButton(VideoAdButton.START, false);
                getVideoAdView().showButton(VideoAdButton.FULLSCREEN, true);
                getVideoAdView().showButton(VideoAdButton.VOLUME, true);
                getVideoAdView().showButton(VideoAdButton.BIGRETRY, false);
                getVideoAdView().showButton(VideoAdButton.RETRY, false);
                getVideoAdView().showButton(VideoAdButton.BACK, true);
            }
            break;
            case DETAIL_PAGE_END: {
                if (mAppView.getParent() != null) {
                    mAppView.setVisibility(INVISIBLE);
                }
                getVideoAdView().showButton(VideoAdButton.START, false);
                getVideoAdView().showButton(VideoAdButton.FULLSCREEN, false);
                getVideoAdView().showButton(VideoAdButton.VOLUME, false);
                getVideoAdView().showButton(VideoAdButton.RETRY, false);
                getVideoAdView().showButton(VideoAdButton.BIGRETRY, true);
                getVideoAdView().showButton(VideoAdButton.BACK, true);

            }
        }
    }

    @Override
    public void onVideoLoad() {
        if (getSigAdVideoStatusListener() != null) {
            getSigAdVideoStatusListener().onVideoLoad();
        }
    }

    @Override
    public void onVideoError(WindAdError adError) {
        if (getSigAdVideoStatusListener() != null) {
            getSigAdVideoStatusListener().onVideoError(adError);
        }
    }

    @Override
    public void onVideoStart() {
        if (getAdUnit() == null) return;

        if (mAppView != null) {
            ViewUtil.removeFromParent(mAppView);
            getVideoAdView().getAppContainer().setVisibility(INVISIBLE);
        }
        ImageUtils.recycleBitmap(lastBitmap);
        ImageUtils.recycleBitmap(lastBlurBitmap);
        BaseAdUnit adUnit = getAdUnit();
        if (adUnit != null) {
            adUnit.updateRealAdPercent(getVideoAdView().getVideoWidth() * 1.0f / getVideoAdView().getVideoHeight());
        }
        if (mAdStyle == SigAdStyle.DETAIL_PAGE_END) {
            setUIStyle(SigAdStyle.DETAIL_PAGE);
        } else {
            setUIStyle(mAdStyle);
        }

        if (getSigAdVideoStatusListener() != null) {
            getSigAdVideoStatusListener().onVideoStart();
        }

    }

    public int getViewHeight(int width) {
        double percent = getVideoAdView().getVideoWidth() == 0 ? getAdUnit().getAdPercent() : getVideoAdView().getVideoWidth() * 1.0f / getVideoAdView().getVideoHeight();

        int halfScreenHeight = ClientMetadata.getInstance().getDisplayMetrics().heightPixels / 2;
        int height = (int) (width / percent);
        if (height > halfScreenHeight) {
            return halfScreenHeight;
        } else {
            return height;
        }
    }

    @Override
    public void onVideoPause() {

        if (getSigAdVideoStatusListener() != null) {
            getSigAdVideoStatusListener().onVideoPause();
        }
    }

    @Override
    public void onVideoResume() {

        if (getSigAdVideoStatusListener() != null) {
            getSigAdVideoStatusListener().onVideoResume();
        }
    }

    @Override
    public void onProgressUpdate(long current, long duration) {
        if (getSigAdVideoStatusListener() != null) {
            getSigAdVideoStatusListener().onProgressUpdate(current, duration);
        }

        if (current > 0) {
            mCurrentPosition = duration;
        }
    }

    private void addAppView() {
        if (mAppView != null) {
            ViewGroup appContainer = getVideoAdView().getAppContainer();
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            layoutParams.setMargins(0, 0, 0, Dips.asIntPixels(5, mContext));
            layoutParams.addRule(CENTER_IN_PARENT);
            appContainer.addView(mAppView, layoutParams);
        }
    }

    private void showBlurLayout() {

        if (getVideoAdView() != null) {

            Bitmap textureBitmap = getVideoAdView().getTextureBitmap();
            if (textureBitmap != null) {
                Bitmap bitmap = BlurKit.getInstance().blur(textureBitmap, 25);
                getVideoAdView().getBlurImageView().setImageBitmap(bitmap);
                getVideoAdView().getAppContainer().setVisibility(VISIBLE);
                lastBitmap = textureBitmap;
                lastBlurBitmap = bitmap;
            }
        }
    }

    @Override
    public void onVideoCompleted() {
        if (getSigAdVideoStatusListener() != null) {
            getSigAdVideoStatusListener().onVideoCompleted();
        }

        showBlurLayout();
        if (mAdStyle == SigAdStyle.PREVIEW) {
            if (getAppView().getParent() == null) {
                addAppView();
            }
            getAppView().setVisibility(VISIBLE);
            getVideoAdView().showButton(VideoAdButton.RETRY, true);
        } else if (mAdStyle == SigAdStyle.DETAIL_PAGE) {
            setUIStyle(SigAdStyle.DETAIL_PAGE_END);
        }
    }

    @Override
    public void onVideoRestart() {
        if (getSigAdVideoStatusListener() != null) {
            getSigAdVideoStatusListener().onVideoRestart();
        }
    }

    @Override
    public double getVideoDuration() {
        if (getVideoAdView() != null) {
            return getVideoAdView().getDuration() / 1000.f;
        }
        return super.getVideoDuration();
    }

    @Override
    public double getVideoProgress() {

        if (getVideoAdView() != null) {
            return (getVideoAdView().getCurrentPositionWhenPlaying() * 1.0f / getVideoAdView().getDuration()) / 100.f;
        }
        return super.getVideoProgress();
    }

    public int getVideoSurferViewHeight() {
        if (getVideoAdView() != null) {
            return getVideoAdView().getVideoSurferViewHeight();
        }
        return 0;
    }

    @Override
    public void destroy() {
        super.destroy();

        ImageUtils.recycleBitmap(lastBitmap);
        ImageUtils.recycleBitmap(lastBlurBitmap);

        SigVideoAdController sigVideoAdController = getSigVideoAdController();
        if (sigVideoAdController != null) {
            sigVideoAdController.stop();
            sigVideoAdController.destroy();
        }

        if (videoContainer != null) {
            videoContainer.removeAllViews();
            ViewUtil.removeFromParent(videoContainer);
        }


        if (mVideoAdView != null) {
            ViewUtil.removeFromParent(mVideoAdView);
            mVideoAdView.setVideoAdViewListener(null);
            mVideoAdView.setVideoAdStatusListener(null);
            mVideoAdView.setBackClickListener(null);
            mVideoAdView.destroy();
            mVideoAdView = null;
        }
    }

    @Override
    public void reset() {
        super.reset();
        if (getVideoAdView() != null) {
            getVideoAdView().releaseAllVideos();
        }
//        ImageUtils.recycleBitmap(lastBitmap);
//        ImageUtils.recycleBitmap(lastBlurBitmap);
    }

    public void setBackClickListener(View.OnClickListener onClickListener) {
        if (getVideoAdView() != null) {
            getVideoAdView().setBackClickListener(onClickListener);
        }
    }

    private void addAppInfoView() {
        SigAppInfoView sigAppInfoView = getAppInfoView();
        if (sigAppInfoView != null) {
            ViewUtil.removeFromParent(sigAppInfoView);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layout.setMargins(0, 0, 0, Dips.asIntPixels(5, mContext));
            layout.addRule(ALIGN_PARENT_BOTTOM);
            addView(sigAppInfoView, layout);
        }

    }
}
