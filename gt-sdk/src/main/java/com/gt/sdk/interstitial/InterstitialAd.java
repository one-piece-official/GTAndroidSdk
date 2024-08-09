package com.gt.sdk.interstitial;

import static com.czhj.sdk.common.models.AdStatus.AdStatusClick;
import static com.czhj.sdk.common.models.AdStatus.AdStatusClose;
import static com.czhj.sdk.common.models.AdStatus.AdStatusLoading;
import static com.czhj.sdk.common.models.AdStatus.AdStatusNone;
import static com.czhj.sdk.common.models.AdStatus.AdStatusReady;

import android.app.Activity;
import android.os.Handler;

import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.common.utils.AdLifecycleManager;
import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.base.GtBaseAd;
import com.gt.sdk.manager.InterstitialAdManager;

public class InterstitialAd extends GtBaseAd implements InterstitialAdListener, AdLifecycleManager.LifecycleListener {

    private InterstitialAdListener interstitialAdListener;
    private String codeId;
    private Handler mHandler;
    private InterstitialAdManager adManager;

    public InterstitialAd(AdRequest adRequest) {
        super(adRequest);
        init(adRequest, null);
    }

    public InterstitialAd(AdRequest adRequest, InterstitialAdListener adListener) {
        super(adRequest);
        init(adRequest, adListener);
    }

    private void init(AdRequest adRequest, InterstitialAdListener adListener) {
        if (adRequest != null) {
            codeId = adRequest.getCodeId();
        }

        if (adListener != null) {
            interstitialAdListener = adListener;
        }

        adManager = new InterstitialAdManager(adRequest, this);
        mHandler = adManager.getHandler();
    }

    public void setInterstitialAdListener(InterstitialAdListener interstitialAdListener) {
        this.interstitialAdListener = interstitialAdListener;
    }

    public boolean loadAd() {
        AdError adError = loadAdFilter();
        if (adError != null) {//过滤无效请求
            onInterstitialAdLoadError(codeId, adError);
            return false;
        }

        if (isReady()) {
            onInterstitialAdLoadSuccess(codeId);
            onInterstitialAdCacheSuccess(codeId);
            return true;
        }

        AdLifecycleManager.getInstance().addLifecycleListener(this);

        adStatus = AdStatusLoading;

        sendRequestEvent(mAdRequest);

        adManager.loadAd();
        return true;
    }

    public boolean isReady() {
        return adStatus == AdStatusReady && adManager.isReady();
    }

    public void show(Activity activity) {

        if (activity == null) {
            onInterstitialAdShowError(codeId, AdError.ERROR_AD_ACTIVITY_IS_NULL);
            return;
        }

        if (!isReady()) {
            onInterstitialAdShowError(codeId, AdError.ERROR_AD_NOT_READY);
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adManager.showAd(activity);
            }
        });
        adStatus = AdStatus.AdStatusPlaying;
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

        if (interstitialAdListener != null) {
            interstitialAdListener = null;
        }
    }

    @Override
    public void onInterstitialAdLoadSuccess(String codeId) {
        adStatus = AdStatusReady;
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdLoadSuccess(codeId);
        }
    }

    @Override
    public void onInterstitialAdCacheSuccess(String codeId) {

    }

    @Override
    public void onInterstitialAdPlay(String codeId) {
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdPlay(codeId);
        }
    }

    @Override
    public void onInterstitialAdSkip(String codeId) {
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdSkip(codeId);
        }
    }

    @Override
    public void onInterstitialAdPLayEnd(String codeId) {
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdPLayEnd(codeId);
        }
    }

    @Override
    public void onInterstitialAdClick(String codeId) {
        adStatus = AdStatusClick;
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdClick(codeId);
        }
    }

    @Override
    public void onInterstitialAdLoadError(String codeId, AdError error) {
        adStatus = AdStatusNone;
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdLoadError(codeId, error);
        }
    }

    @Override
    public void onInterstitialAdShowError(String codeId, AdError error) {
        adStatus = AdStatusNone;
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdShowError(codeId, error);
        }
    }

    @Override
    public void onInterstitialAdClosed(String codeId) {
        adStatus = AdStatusClose;
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdClosed(codeId);
        }
        destroyAd();
    }
}
