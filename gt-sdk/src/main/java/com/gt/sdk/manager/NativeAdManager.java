package com.gt.sdk.manager;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

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
import com.gt.sdk.interstitial.RewardAdInterstitial;
import com.gt.sdk.natives.NativeAdData;
import com.gt.sdk.natives.NativeAdInterstitial;
import com.gt.sdk.natives.NativeAdLoadListener;
import com.gt.sdk.natives.NativeAdUnitObject;
import com.gt.sdk.utils.PointEntityUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NativeAdManager implements RequestFactory.LoadAdRequestListener, AdStackManager.AdStackStatusListener, NativeAdInterstitial.NativeAdInterstitialListener {

    private Handler mHandler;
    private AdStatus adStatus;
    private BaseAdUnit mAdUnit;
    private NativeAdLoadListener interstitialAdListener;
    private final LoadAdRequest mLoadAdRequest;
    private static final int what_timeout = 0x001;
    private final NativeAdInterstitial nativeAdInterstitial;

    public NativeAdManager(final AdRequest request, NativeAdLoadListener adListener) {
        mHandler = new Handler(Looper.getMainLooper());
        adStatus = AdStatus.AdStatusNone;

        interstitialAdListener = adListener;
        mLoadAdRequest = new LoadAdRequest(request, AdFormat.SPLASH);
        nativeAdInterstitial = new NativeAdInterstitial(this);
    }

    public boolean isReady() {
        try {
            if (mAdUnit != null) {
                boolean isExist = new File(mAdUnit.getSplashFilePath()).canRead();
                boolean isExpired = mAdUnit.isExpiredAd();
                boolean baseAdUnitValid = nativeAdInterstitial.baseAdUnitValid(mAdUnit);
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

        sendRequestEvent(mLoadAdRequest);

        RequestFactory.LoadAd(mLoadAdRequest, this);
    }

    private void sendRequestEvent(LoadAdRequest adRequest) {
        PointEntityUtils.GtTracking(PointCategory.REQUEST, "", adRequest);
    }

    private void handleError(AdError error, boolean isLoadError) {
        adStatus = AdStatus.AdStatusNone;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (interstitialAdListener != null) {
            interstitialAdListener.onAdError(mLoadAdRequest.getCodeId(), error);
        }

        if (nativeAdInterstitial != null) {
            nativeAdInterstitial.onInvalidate(mAdUnit);
        }
    }

    public void onPause(Activity activity) {

    }

    public void onResume(Activity activity) {

    }

    private ArrayList<NativeAdData> adNativeAdDataList;

    @Override
    public void onSuccess(List<BaseAdUnit> adUnits, LoadAdRequest loadAdRequest) {
        BaseAdUnit adUnit = adUnits.get(0);
        PointEntityUtils.eventRecord(PointCategory.RESPOND, Constants.SUCCESS, adUnit);
        if (!nativeAdInterstitial.baseAdUnitValid(adUnit)) {
            handleError(AdError.ERROR_AD_INFORMATION_LOSE, true);
            return;
        }
        mAdUnit = adUnit;
        adNativeAdDataList = new ArrayList<>();
        NativeAdData ad = new NativeAdUnitObject(mAdUnit);
        adNativeAdDataList.add(ad);
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
                        interstitialAdListener.onAdLoad(mLoadAdRequest.getCodeId(), adNativeAdDataList);
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

        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
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
    }


    @Override
    public void onAdClose(BaseAdUnit adUnit) {
        adStatus = AdStatus.AdStatusClose;
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
        if (nativeAdInterstitial != null) {
            nativeAdInterstitial.onInvalidate(mAdUnit);
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

    @Override
    public void onAdDetailShow() {

    }

    @Override
    public void onAdDetailClick() {

    }

    @Override
    public void onAdDetailDismiss() {

    }
}




