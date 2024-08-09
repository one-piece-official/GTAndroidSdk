package com.gt.sdk.manager;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.models.AdFormat;
import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.common.utils.ViewUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.WindConstants;
import com.gt.sdk.base.LoadAdRequest;
import com.gt.sdk.base.common.AdSessionManager;
import com.gt.sdk.base.common.AdStackManager;
import com.gt.sdk.base.common.BaseBroadcastReceiver;
import com.gt.sdk.base.common.SessionManager;
import com.gt.sdk.base.common.SplashAdInterstitial;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;
import com.gt.sdk.base.models.point.PointCategory;
import com.gt.sdk.base.network.RequestFactory;
import com.gt.sdk.base.splash.SplashAdView;
import com.gt.sdk.interstitial.InterstitialAdListener;
import com.gt.sdk.interstitial.RewardAdInterstitial;
import com.gt.sdk.utils.PointEntityUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class InterstitialAdManager implements RequestFactory.LoadAdRequestListener, AdStackManager.AdStackStatusListener, RewardAdInterstitial.VideoAdListener {

    private final Runnable timerRunnable;
    private Handler mHandler;
    private AdStatus adStatus;
    private SplashAdView mSplashAdView;
    private BaseAdUnit mAdUnit;
    private int mDuration;
    private InterstitialAdListener interstitialAdListener;
    private final LoadAdRequest mLoadAdRequest;
    private static final int what_timeout = 0x001;
    private final SplashAdInterstitial mSplashAdInterstitial;

    public InterstitialAdManager(final AdRequest request, InterstitialAdListener adListener) {
        mHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                    if (mSplashAdView == null) return;
                    if (mDuration > 0) {
                        mSplashAdView.setDuration(mDuration);
                        mDuration--;
                        mHandler.postDelayed(timerRunnable, 1000);
                    } else {
                        mSplashAdView.setDuration(0);
                    }
                }
            }
        };

        adStatus = AdStatus.AdStatusNone;

        interstitialAdListener = adListener;
        mLoadAdRequest = new LoadAdRequest(request, AdFormat.SPLASH);
        mSplashAdInterstitial = new SplashAdInterstitial(this);
    }

    public boolean isReady() {
        try {
            if (mAdUnit != null) {
                boolean isExist = new File(mAdUnit.getSplashFilePath()).canRead();
                boolean isExpired = mAdUnit.isExpiredAd();
                boolean baseAdUnitValid = mSplashAdInterstitial.baseAdUnitValid(mAdUnit);
                return isExist && !isExpired && baseAdUnitValid;
            }
        } catch (Exception e) {
            SigmobLog.e(e.getMessage());
        }
        return false;
    }

    public void loadAd() {
        adStatus = AdStatus.AdStatusLoading;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == what_timeout) {
                    handleError(AdError.ERROR_AD_LOAD_TIMEOUT, true);
                }
            }
        };

        mHandler.sendEmptyMessageDelayed(what_timeout, 10 * 1000);

        mLoadAdRequest.setLoadId(UUID.randomUUID().toString());

        RequestFactory.LoadAd(mLoadAdRequest, this);
    }

    private void handleError(AdError error, boolean isLoadError) {
        adStatus = AdStatus.AdStatusNone;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (interstitialAdListener != null) {
            if (isLoadError) {
                interstitialAdListener.onInterstitialAdLoadError(mLoadAdRequest.getCodeId(), error);
            } else {
                interstitialAdListener.onInterstitialAdShowError(mLoadAdRequest.getCodeId(), error);
            }
        }

        if (mSplashAdInterstitial != null) {
            mSplashAdInterstitial.onInvalidate(mAdUnit);
        }
    }

    public void showAd(Activity activity) {
        boolean isPortrait = ORIENTATION_PORTRAIT == ClientMetadata.getInstance().getOrientationInt();
        if (!isPortrait) {
            onAdPlayFail(AdError.ERROR_AD_UN_SUPPORT_ORIENTATION);
            return;
        }

        onAdPlayFail(AdError.ERROR_AD_PLAY);
    }

    private void onAdPlayFail(AdError adError) {
        PointEntityUtils.GtError(PointCategory.PLAY, adError, mAdUnit);
        handleError(adError, false);
        destroyAd();
    }

    private boolean createSplashView(Context context, BaseAdUnit adUnit) {
        if (adUnit == null) return false;
        mSplashAdView = new SplashAdView(context.getApplicationContext());
        mSplashAdView.invisibleView();
        return mSplashAdView.loadUI(adUnit);
    }

    private Activity getActivity() {
        return ViewUtil.getActivityFromViewTop(mSplashAdView);
    }

    public void onPause(Activity activity) {
        if (mSplashAdView != null && activity == getActivity()) {
            mSplashAdView.onPause();
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
        }
    }

    public void onResume(Activity activity) {
        if (mSplashAdView != null && activity == getActivity()) {
            mSplashAdView.onResume();
            if (mHandler == null) {
                mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.post(timerRunnable);
        }
    }

    @Override
    public void onSuccess(List<BaseAdUnit> adUnits, LoadAdRequest loadAdRequest) {
        BaseAdUnit adUnit = adUnits.get(0);
        PointEntityUtils.eventRecord(PointCategory.RESPOND, Constants.SUCCESS, adUnit);
        if (!mSplashAdInterstitial.baseAdUnitValid(adUnit)) {
            handleError(AdError.ERROR_AD_INFORMATION_LOSE, true);
            return;
        }

        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdLoadSuccess(mLoadAdRequest.getCodeId());
        }

        mAdUnit = adUnit;
        AdStackManager.shareInstance().cache(mAdUnit, this);
    }

    @Override
    public void onErrorResponse(int code, String message, String request_id, LoadAdRequest loadAdRequest) {

        AdError adError = AdError.getAdError(code);
        if (adError == null) {
            adError = AdError.ERROR_AD_REQUEST;
            adError.setErrorMessage(code, message);
        }

        PointEntityUtils.GtTracking(PointCategory.RESPOND, WindConstants.FAIL, loadAdRequest);

        PointEntityUtils.GtError(PointCategory.REQUEST, null, code, message, loadAdRequest, null, null);

        handleError(adError, true);
    }

    @Override
    public void loadStart(BaseAdUnit adUnit) {//开始下载资源

    }

    @Override
    public void loadEnd(BaseAdUnit adUnit, String error) {//下载资源完毕
        SigmobLog.d(" loadEnd");
        if (TextUtils.isEmpty(error)) {
            PointEntityUtils.GtTracking(PointCategory.READY, null, adUnit, mLoadAdRequest, null);

            if (adStatus != AdStatus.AdStatusLoading) return;

            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }

            adStatus = AdStatus.AdStatusReady;

            GtAdSdk.sharedAds().getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (interstitialAdListener != null) {
                        interstitialAdListener.onInterstitialAdCacheSuccess(mLoadAdRequest.getCodeId());
                    }
                }
            });
        } else {
            AdError adError = AdError.ERROR_AD_FILE_DOWNLOAD;
            adError.setMessage(error);
            handleError(adError, true);
        }
    }

    @Override
    public void onAdShowFailed(BaseAdUnit adUnit, String error) {
        PointEntityUtils.GtError(PointCategory.PLAY, AdError.ERROR_AD_PLAY, mAdUnit);
        handleError(AdError.ERROR_AD_PLAY, false);
        destroyAd();
    }

    @Override
    public void onAdShow(BaseAdUnit adUnit) {
        adStatus = AdStatus.AdStatusPlaying;
        initSessionManager();
        AdStackManager.setPlayAdUnit(adUnit);
        if (mSplashAdView.getDuration() > 0 && mSplashAdView.getDuration() < mDuration) {
            mDuration = mSplashAdView.getDuration();
        }

        if (mSplashAdView != null) {
            mSplashAdView.setDuration(mDuration);
            mSplashAdView.setVisibility(View.VISIBLE);
        }

        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdPlay(mLoadAdRequest.getCodeId());
        }

        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        mHandler.post(timerRunnable);
    }

    public void initSessionManager() {
        SessionManager sessionManager = mAdUnit.getSessionManager();
        if (sessionManager == null) {
            AdSessionManager adSessionManager = new AdSessionManager();
            adSessionManager.createDisplaySession(mAdUnit);
        }
    }

    @Override
    public void onAdClicked(BaseAdUnit adUnit) {
        adStatus = AdStatus.AdStatusClick;
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdClick(mLoadAdRequest.getCodeId());
        }
    }

    @Override
    public void onAdSkip(BaseAdUnit adUnit) {
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdSkip(mLoadAdRequest.getCodeId());
        }
    }

    @Override
    public void onAdPlayEnd(BaseAdUnit adUnit) {
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdPLayEnd(mLoadAdRequest.getCodeId());
        }
    }

    @Override
    public void onAdClose(BaseAdUnit adUnit) {
        adStatus = AdStatus.AdStatusClose;
        if (interstitialAdListener != null) {
            interstitialAdListener.onInterstitialAdClosed(mLoadAdRequest.getCodeId());
        }
        destroyAd();
    }

    @Override
    public void onLandPageShow() {

    }

    @Override
    public void onLandPageClose() {//落地页关闭后通知广告关闭
        BaseBroadcastReceiver.broadcastAction(GtAdSdk.sharedAds().getContext(), mAdUnit.getUuid(), IntentActions.ACTION_SPLASH_CLOSE);
    }

    public void destroyAd() {
        if (mSplashAdInterstitial != null) {
            mSplashAdInterstitial.onInvalidate(mAdUnit);
        }

        if (mAdUnit != null) {
            SessionManager sessionManager = mAdUnit.getSessionManager();
            if (sessionManager != null) {
                sessionManager.endDisplaySession(mAdUnit);
            }
        }

        if (adStatus == AdStatus.AdStatusPlaying) {
            adStatus = AdStatus.AdStatusClose;
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        mAdUnit = null;
        interstitialAdListener = null;
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }
}




