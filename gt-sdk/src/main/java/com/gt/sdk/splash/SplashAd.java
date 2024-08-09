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

    private Handler mHandler;
    private SplashAdManager mAdManager;
    private ViewGroup mViewGroup;
    private String codeId;

    private SplashAdListener splashAdListener;

    public SplashAd(AdRequest adRequest) {
        super(adRequest);
        init(adRequest, null);
    }

    public SplashAd(AdRequest adRequest, SplashAdListener adListener) {
        super(adRequest);
        init(adRequest, adListener);
    }

    private void init(AdRequest adRequest, SplashAdListener adListener) {
        if (adRequest != null) {
            codeId = adRequest.getCodeId();
        }
        splashAdListener = adListener;
        mAdManager = new SplashAdManager(adRequest, this);
        mHandler = mAdManager.getHandler();
    }

    public void setSplashAdListener(SplashAdListener splashAdListener) {
        this.splashAdListener = splashAdListener;
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

        mAdManager.loadAd();
        return true;
    }

    public boolean isReady() {
        return adStatus == AdStatus.AdStatusReady && mAdManager.isReady();
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
                mAdManager.showSplashAd(mViewGroup);
            }
        });
        adStatus = AdStatus.AdStatusPlaying;
    }

    @Override
    public void onSplashAdShow(String codeId) {
        if (splashAdListener != null) {
            splashAdListener.onSplashAdShow(codeId);
        }
    }

    @Override
    public void onSplashAdLoadSuccess(String placementId) {
        adStatus = AdStatus.AdStatusReady;
        if (splashAdListener != null) {
            splashAdListener.onSplashAdLoadSuccess(placementId);
        }
    }

    @Override
    public void onSplashAdLoadFail(String codeId, AdError error) {
        adStatus = AdStatus.AdStatusNone;
        if (splashAdListener != null) {
            splashAdListener.onSplashAdLoadFail(codeId, error);
        }
    }

    @Override
    public void onSplashAdShowError(String codeId, AdError error) {
        adStatus = AdStatus.AdStatusNone;
        if (splashAdListener != null) {
            splashAdListener.onSplashAdShowError(codeId, error);
        }
    }

    @Override
    public void onSplashAdClick(String codeId) {
        adStatus = AdStatus.AdStatusClick;
        if (splashAdListener != null) {
            splashAdListener.onSplashAdClick(codeId);
        }
    }

    @Override
    public void onSplashAdClose(String codeId) {
        adStatus = AdStatus.AdStatusClose;
        if (splashAdListener != null) {
            splashAdListener.onSplashAdClose(codeId);
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
        if (mAdManager != null) {
            mAdManager.onPause(activity);
        }
    }

    @Override
    public void onResume(Activity activity) {
        if (mAdManager != null) {
            mAdManager.onResume(activity);
        }
    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {

    }

    public void destroyAd() {
        if (mAdManager != null) {
            mAdManager.destroyAd();
        }

        if (mViewGroup != null) {
            mViewGroup.removeAllViews();
            mViewGroup = null;
        }

        if (splashAdListener != null) {
            splashAdListener = null;
        }
    }
}
