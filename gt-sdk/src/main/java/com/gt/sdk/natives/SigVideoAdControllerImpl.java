package com.gt.sdk.natives;


import com.gt.sdk.base.videoplayer.VideoAdView;

import java.lang.ref.WeakReference;

public class SigVideoAdControllerImpl implements SigVideoAdController {

    private WeakReference<VideoAdView> videoAdViewWeakReference;
    private SigAdVideoStatusListener mAdVideoStatusListener;

    public SigVideoAdControllerImpl(VideoAdView adView) {
        videoAdViewWeakReference = new WeakReference<>(adView);
    }

    private VideoAdView getVideoAdView() {
        return videoAdViewWeakReference.get();
    }

    @Override
    public void setMute(boolean isMute) {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            videoAdView.setSoundChange(isMute);

        }
    }

    @Override
    public void pause() {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            videoAdView.goOnPlayOnPause();

        }
    }

    @Override
    public void resume() {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            videoAdView.goOnPlayOnResume();

        }
    }

    @Override
    public void start() {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            videoAdView.goOnPlayOnResume();

        }
    }

    @Override
    public void stop() {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            videoAdView.releaseAllVideos();
        }
    }

    @Override
    public void setAdVideoStatusListener(SigAdVideoStatusListener adVideoStatusListener) {
        this.mAdVideoStatusListener = adVideoStatusListener;
    }

    public void setHolderImage(int resId) {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            videoAdView.setHolderImageResource(resId);
        }
    }


    @Override
    public void startPreloading() {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            videoAdView.startPreloading();
        }
    }

    @Override
    public int getVideoDuration() {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            return videoAdView.getDuration() == 0 ? 0 : (int) (videoAdView.getDuration() / 1000);
        }
        return 0;
    }

    @Override
    public int getVideoProgress() {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            return videoAdView.getCurrentPositionWhenPlaying() == 0 ? 0 : (int) (videoAdView.getCurrentPositionWhenPlaying() * 100 / videoAdView.getDuration());
        }
        return 0;
    }

    @Override
    public SigAdVideoStatusListener getSigAdVideoStatusListener() {
        return mAdVideoStatusListener;
    }


    public void destroy() {
        VideoAdView videoAdView = videoAdViewWeakReference.get();
        if (videoAdView != null) {
            videoAdView.destroy();
        }
        videoAdViewWeakReference.clear();
        mAdVideoStatusListener = null;
    }

    @Override
    public int getCurrentPosition() {
        VideoAdView videoAdView = getVideoAdView();
        if (videoAdView != null) {
            return (int) (videoAdView.getCurrentPositionWhenPlaying() / 1000);
        }
        return 0;
    }

}
