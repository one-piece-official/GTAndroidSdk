package com.gt.sdk.splash;


import android.app.Activity;
import android.os.Handler;
import android.view.ViewGroup;

import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.common.utils.AdLifecycleManager;
import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.api.SplashAdListener;
import com.gt.sdk.base.GtBaseAd;
import com.gt.sdk.manager.SplashAdManager;

public class SplashAd extends GtBaseAd implements SplashAdListener, AdLifecycleManager.LifecycleListener {

    private SplashAdListener mSplashAdListener;
    private final Handler mHandler;
    private final SplashAdManager adManager;
    private ViewGroup mViewGroup;
    private String codeId;

    public SplashAd(AdRequest adRequest, SplashAdListener adListener) {
        super(adRequest);

        if (adRequest != null) {
            codeId = adRequest.getCodeId();
        }

        mSplashAdListener = adListener;
        adManager = new SplashAdManager(adRequest, this);
        mHandler = adManager.getHandler();
    }

    public boolean loadAd() {

        AdError adError = loadAdFilter();
        if (adError != null) {//过滤无效请求
            onSplashAdLoadFail(codeId, adError);
            return false;
        }

        if (isReady()) {
            onSplashAdLoadSuccess(codeId);
            return true;
        }

        AdLifecycleManager.getInstance().addLifecycleListener(this);

        adStatus = AdStatus.AdStatusLoading;

        sendRequestEvent(mAdRequest);

        adManager.loadAd();
        return true;
    }

    public boolean isReady() {
        return adStatus == AdStatus.AdStatusReady && adManager.isReady();
    }

    public void show(ViewGroup adContainer) {

        if (adContainer == null) {
            onSplashAdShowError(codeId, AdError.ERROR_AD_CONTAINER_IS_NULL);
            return;
        }

        if (!isReady()) {
            onSplashAdShowError(codeId, AdError.ERROR_AD_NOT_READY);
            return;
        }

        mViewGroup = adContainer;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adManager.showSplashAd(mViewGroup);
            }
        });
        adStatus = AdStatus.AdStatusPlaying;
    }

    @Override
    public void onSplashAdShow(String codeId) {
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdShow(codeId);
        }
    }

    @Override
    public void onSplashAdLoadSuccess(String placementId) {
        adStatus = AdStatus.AdStatusReady;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdLoadSuccess(placementId);
        }
    }

    @Override
    public void onSplashAdLoadFail(String codeId, AdError error) {
        adStatus = AdStatus.AdStatusNone;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdLoadFail(codeId, error);
        }
    }

    @Override
    public void onSplashAdShowError(String codeId, AdError error) {
        adStatus = AdStatus.AdStatusNone;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdShowError(codeId, error);
        }
    }

    @Override
    public void onSplashAdClick(String codeId) {
        adStatus = AdStatus.AdStatusClick;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdClick(codeId);
        }
    }

    @Override
    public void onSplashAdClose(String codeId) {
        adStatus = AdStatus.AdStatusClose;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdClose(codeId);
        }
        destroyAd();
    }

    @Override
    public void onCreate(Activity activity) {

    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {
        if (adManager != null) {
            adManager.onPause(activity);
        }
    }

    @Override
    public void onResume(Activity activity) {
        if (adManager != null) {
            adManager.onResume(activity);
        }
    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {

    }

    public void destroyAd() {
        if (adManager != null) {
            adManager.destroyAd();
        }

        if (mViewGroup != null) {
            mViewGroup.removeAllViews();
            mViewGroup = null;
        }

        if (mSplashAdListener != null) {
            mSplashAdListener = null;
        }
    }
}
