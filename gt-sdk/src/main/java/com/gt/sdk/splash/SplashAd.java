package com.gt.sdk.splash;

import static com.czhj.sdk.common.models.AdStatus.AdStatusClick;
import static com.czhj.sdk.common.models.AdStatus.AdStatusClose;
import static com.czhj.sdk.common.models.AdStatus.AdStatusLoading;
import static com.czhj.sdk.common.models.AdStatus.AdStatusNone;
import static com.czhj.sdk.common.models.AdStatus.AdStatusReady;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.common.utils.AdLifecycleManager;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.api.SplashAdListener;
import com.gt.sdk.base.GtBaseAd;
import com.gt.sdk.admanager.SplashAdManager;

public class SplashAd extends GtBaseAd implements SplashAdListener, AdLifecycleManager.LifecycleListener {

    private SplashAdListener mSplashAdListener;
    private final Handler mHandler;
    private final SplashAdManager adManager;
    private ViewGroup mViewGroup;
    private String placementId;
    private boolean isCloseToOut = false;

    public SplashAd(AdRequest adRequest, SplashAdListener adListener) {
        super(adRequest);

        if (adRequest != null) {
            placementId = adRequest.getCodeId();
        }

        mSplashAdListener = adListener;
        adManager = new SplashAdManager(adRequest, this);
        mHandler = adManager.getHandler();

    }

    public boolean loadAd() {

        if (!loadAdFilter()) {//过滤无效请求
            return false;
        }

        AdLifecycleManager.getInstance().addLifecycleListener(this);

        adStatus = AdStatusLoading;

        if (!adManager.isReady()) {
            sendRequestEvent(mAdRequest);
        }

        adManager.loadAd();
        return true;
    }

    public boolean isReady() {
        return adStatus == AdStatusReady && adManager.isReady();
    }

    public void show(ViewGroup adContainer) {

        if (adStatus != AdStatusReady || adManager == null) {
            onSplashError(AdError.ERROR_AD_NOT_READY, placementId);
            return;
        }
        if (adContainer == null) {
            onSplashAdShowError(placementId, AdError.ERROR_AD_CONTAINER_IS_NULL);
            return;
        }

        mViewGroup = adContainer;

        privateShow();
    }

    private void privateShow() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adManager.showSplashAd(mViewGroup);
            }
        });

        adStatus = AdStatus.AdStatusPlaying;
    }

    private void onSplashError(AdError error, final String placementId) {
        SigmobLog.e("onSplashError: " + error + " :placementId: " + placementId);
        if (!isCloseToOut) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSplashAdListener != null) {
                        isCloseToOut = true;
                        mSplashAdListener.onSplashAdLoadFail(placementId, error);
                    }
                }
            });
        }
    }

    @Override
    protected void onAdFilterLoadFail(AdError adError) {
        adStatus = AdStatusNone;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdLoadFail(placementId, adError);
        }
    }

    @Override
    public void onSplashAdShow(String placementId) {
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdShow(placementId);
        }
    }

    @Override
    public void onSplashAdLoadSuccess(String placementId) {
        adStatus = AdStatusReady;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdLoadSuccess(placementId);
        }
    }

    @Override
    public void onSplashAdLoadFail(String placementId, AdError error) {
        adStatus = AdStatusNone;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdLoadFail(placementId, error);
        }
    }

    @Override
    public void onSplashAdShowError(String placementId, AdError error) {
        adStatus = AdStatusNone;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdShowError(placementId, error);
        }
    }

    @Override
    public void onSplashAdClick(String placementId) {
        adStatus = AdStatusClick;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdClick(placementId);
        }
    }

    @Override
    public void onSplashAdClose(String placementId) {
        adStatus = AdStatusClose;
        if (mSplashAdListener != null) {
            mSplashAdListener.onSplashAdClose(placementId);
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
            mHandler.removeCallbacksAndMessages(null);
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
