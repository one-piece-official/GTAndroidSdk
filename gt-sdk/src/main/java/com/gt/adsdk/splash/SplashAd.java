package com.gt.adsdk.splash;

import static com.czhj.sdk.common.models.AdStatus.AdStatusLoading;
import static com.czhj.sdk.common.models.AdStatus.AdStatusReady;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.common.utils.AdLifecycleManager;
import com.gt.adsdk.AdRequest;
import com.gt.adsdk.api.SplashAdListener;
import com.gt.adsdk.base.GtBaseAd;
import com.gt.adsdk.base.SplashAdManager;
import com.sigmob.windad.WindAdError;

public class SplashAd extends GtBaseAd implements SplashAdListener, AdLifecycleManager.LifecycleListener {

    private SplashAdListener mSplashAdListener;
    private Handler mHandler;
    private SplashAdManager mSplashAd;

    public SplashAd(AdRequest adRequest, SplashAdListener adListener) {

        super(adRequest);

        mSplashAdListener = adListener;

        mHandler = new Handler(Looper.getMainLooper());

        mSplashAd = new SplashAdManager(adRequest, this);

    }

    public boolean loadAd() {

        if (!loadAdFilter()) {//过滤无效请求
            return false;
        }

        AdLifecycleManager.getInstance().addLifecycleListener(this);

        adStatus = AdStatusLoading;

        if (!mSplashAd.isReady()) {
            sendRequestEvent();
        }

        mSplashAd.loadAd(getBid_token(), getBidFloor(), getCurrency(), mFetchDelay, false);
        return true;
    }

    public boolean isReady() {
        return adStatus == AdStatusReady && mSplashAd.isReady();
    }

    public void show(ViewGroup adContainer) {

        if (adStatus != AdStatusReady) {
            onSplashError(WindAdError.ERROR_SIGMOB_SPLASH_NOT_READY, getPlacementId());
            return;
        }
        if (adContainer == null) {
            WindAdError adError = WindAdError.ERROR_SIGMOB_ADCONTAINER_IS_NULL;
            onSplashAdShowError(adError, getPlacementId());
            return;
        }

        mViewGroup = adContainer;

        privateShow();
    }

    private void privateShow() {


        if (mSplashAd == null) {
            onSplashError(WindAdError.ERROR_SIGMOB_SPLASH_NOT_READY, getPlacementId());
            return;
        }

        initView();

        if (splashLY != null) {
            splashLY.setVisibility(View.VISIBLE);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSplashAd.showSplashAd(splashLY);
            }
        });

        adStatus = AdStatus.AdStatusPlaying;
    }

    @Override
    public void onSplashAdShow(String placementId) {

    }

    @Override
    public void onSplashAdLoadSuccess(String placementId) {

    }

    @Override
    public void onSplashAdLoadFail(String placementId, WindAdError error) {

    }

    @Override
    public void onSplashAdShowError(String placementId, WindAdError error) {

    }

    @Override
    public void onSplashAdClick(String placementId) {

    }

    @Override
    public void onSplashAdClose(String placementId) {

    }

    @Override
    public void onCreate(Activity activity) {

    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {

    }

    public void destroy() {
    }
}
