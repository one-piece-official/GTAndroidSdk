package com.gt.adsdk.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.czhj.sdk.common.models.AdStatus;
import com.gt.adsdk.AdRequest;
import com.gt.adsdk.api.SplashAdListener;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.RequestFactory;
import com.sigmob.sdk.rewardVideoAd.RequestSceneType;
import com.sigmob.sdk.splash.SplashAdInterstitial;
import com.sigmob.windad.WindAdError;

public class SplashAdManager {

    public SplashAdManager(final AdRequest splashAdRequest,
                           SplashAdListener splashADListener) {

        timerRunnable = new Runnable() {

            @Override
            public void run() {

                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);

                    if (mSplashAdView == null) return;
                    if (mDuration > 0) {
                        mSplashAdView.setDuration(mDuration);
                        mDuration--;
//                        if (autoClickMode != 2 && autoClickTimeRatio > 0 && ((clickDuration - mDuration) * 100 / clickDuration) >= autoClickTimeRatio) {
//                            SigmobLog.e("performAdClick: " + (clickDuration - mDuration) * 100 / clickDuration);
//                            mSplashAdView.performAdClick();
//                        }
                        mHandler.postDelayed(timerRunnable, 1000);
                    } else {
                        mSplashAdView.setDuration(0);

                    }
                }

            }
        };


        adStatus = AdStatus.AdStatusNone;

        mLoadAdRequest = new LoadAdRequest(splashAdRequest);
        mSplashADListener = splashADListener;
//        mDelayTime=delayTime;
        mSplashAdInterstitial = new SplashAdInterstitial(this);

    }

    public boolean isReady() {
    }

    public void loadAd(String bidToken, int bidFloor, String currency, int fetchTime, boolean isPreload) {

        if (!isPreload) {
            adStatus = AdStatus.AdStatusLoading;
            if (isReady()) {
                if (mSplashAdInterstitial != null) {
                    mSplashAdInterstitial.loadInterstitial(null, mAdUnit);
                    AdStackManager.shareInstance().cache(mAdUnit, this);
                    return;
                }
            }

            mLoadAdRequest.setRequest_scene_type(RequestSceneType.NormalRequest.getValue());

        } else {
            mLoadAdRequest.setRequest_scene_type(RequestSceneType.SplashCloseRequest.getValue());
            PointEntitySigmobUtils.SigmobTracking(PointCategory.REQUEST, PointCategory.PLAY, null,null,mLoadAdRequest,new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                @Override
                public void onAddExtra(Object pointEntityBase) {
                    if (pointEntityBase instanceof PointEntitySigmob) {
                        ((PointEntitySigmob) pointEntityBase).setAdx_id(null);
                    }
                }
            });

        }

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    case what_timeout: {
                        WindAdError adError = WindAdError.ERROR_SIGMOB_SPLASH_TIMEOUT;
                        PointEntitySigmobUtils.SigmobError(PointCategory.REQUEST, null,
                                adError.getErrorCode(),
                                adError.getMessage(),
                                null,
                                mLoadAdRequest,null,new PointEntitySigmobUtils.OnPointEntityExtraInfo() {

                                    @Override
                                    public void onAddExtra(Object pointEntityBase) {

                                        if (pointEntityBase instanceof PointEntitySigmob) {
                                            ((PointEntitySigmob) pointEntityBase).setAdx_id(null);
                                        }
                                    }
                                });


                        handleError(adError, true);

                    }
                    break;
                    default:
                        break;
                }
            }
        };

        mHandler.sendEmptyMessageDelayed(what_timeout, fetchTime * 1000);


        mLoadAdRequest.setBidToken(bidToken);
        mLoadAdRequest.setBidFloor(bidFloor);
        mLoadAdRequest.setCurrency(currency);
        RequestFactory.LoadAd(mLoadAdRequest, this);
    }

}
