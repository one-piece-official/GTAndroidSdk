package com.sigmob.sdk.splash;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.BaseAdConfig;
import com.sigmob.sdk.base.common.BaseBroadcastReceiver;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.SessionManager;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.IntentActions;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.RequestFactory;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.rewardVideoAd.RequestSceneType;
import com.sigmob.windad.Splash.WindSplashADListener;
import com.sigmob.windad.Splash.WindSplashAdRequest;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SplashAdManager implements RequestFactory.LoadAdRequestListener,
        SplashAdInterstitial.SplashAdInterstitialListener,
        AdStackManager.AdStackStatusListener {


    private final LoadAdRequest mLoadAdRequest;
    private final Runnable timerRunnable;
    private Handler mHandler;
    private SplashAdInterstitial mSplashAdInterstitial;
    private WindSplashADListener mSplashADListener;
    private int mDuration;
    private AdStatus adStatus;
    private SplashAdView mSplashAdView;
    private BaseAdUnit mAdUnit;
    private int mSplashUIType = 0;
    private int clickDuration = 0;

    private static final int what_timeout = 0x20001;

    //    private int mDelayTime = 0;
    public SplashAdManager(final WindSplashAdRequest splashAdRequest,
                           WindSplashADListener splashADListener) {

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

    private Activity getActivity() {

        Activity activityFromView = ViewUtil.getActivityFromViewTop(mSplashAdView);
        return activityFromView;
    }


    public boolean isReady() {
        try {

            if (mAdUnit == null) {
                Object adUnit = FileUtil.readFromCache(SigmobFileUtil.getSplashAdUnitFilePath(mLoadAdRequest.getPlacementId()));
                if (adUnit instanceof BaseAdUnit && ((BaseAdUnit) adUnit).getAd() != null) {
                    FileUtil.deleteFile(SigmobFileUtil.getSplashAdUnitFilePath(mLoadAdRequest.getPlacementId()));
                    mAdUnit = (BaseAdUnit) adUnit;
                }
            }

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

    private boolean createSplashView(Context context, BaseAdUnit adUnit) {

        if (adUnit == null) return false;

        mSplashAdView = new SplashAdView(context.getApplicationContext());

        if (mSplashAdView == null) return false;

        mSplashAdView.invisibleView();
        mSplashAdView.setShowAppLogo(mSplashUIType != 0);


        return mSplashAdView.loadUI(adUnit);

    }


    @Override
    public void onSuccess(List<BaseAdUnit> adUnits, LoadAdRequest loadAdRequest) {

        BaseAdUnit adUnit = adUnits.get(0);
        PointEntitySigmobUtils.eventRecord(PointCategory.RESPOND, Constants.SUCCESS, adUnit);


        if (!mSplashAdInterstitial.baseAdUnitValid(adUnit)) {
            handleError(WindAdError.ERROR_SIGMOB_INFORMATION_LOSE, true);
            return;
        }

        mAdUnit = adUnit;
        if (mSplashAdInterstitial != null) {
            mSplashAdInterstitial.loadInterstitial(null, adUnit);
        }
        AdStackManager.shareInstance().cache(mAdUnit, this);
    }


    private void handleError(WindAdError error, boolean isLoadError) {
        adStatus = AdStatus.AdStatusNone;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }


        if (mLoadAdRequest.getRequest_scene_type() != RequestSceneType.SplashCloseRequest.getValue()) {
            if (mSplashADListener != null) {
                if (isLoadError) {
                    mSplashADListener.onSplashAdLoadFail(error, mLoadAdRequest.getPlacementId());
                } else {
                    mSplashADListener.onSplashAdShowError(error, mLoadAdRequest.getPlacementId());
                }
                mSplashADListener = null;

            }
        }

        if (mSplashAdInterstitial != null) {
            mSplashAdInterstitial.onInvalidate(mAdUnit);
        }


    }

    @Override
    public void onErrorResponse(int error, String message, String request_id, LoadAdRequest loadAdRequest) {

        WindAdError adError = WindAdError.getWindAdError(error);
        if (adError == null) {
            adError = WindAdError.ERROR_SIGMOB_REQUEST;
            adError.setErrorMessage(error, message);
        }

        PointEntitySigmobUtils.SigmobTracking(PointCategory.RESPOND, Constants.FAIL, loadAdRequest);

        PointEntitySigmobUtils.SigmobError(PointCategory.REQUEST, null, error, message,null, loadAdRequest,null,new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {
                    ((PointEntitySigmob) pointEntityBase).setAdx_id(null);
                }
            }
        });


        handleError(adError, true);

    }


    @Override
    public void loadStart(BaseAdUnit adUnit) {

    }

    @Override
    public void loadEnd(BaseAdUnit adUnit, String error) {
        SigmobLog.d(" loadEnd");

        if (TextUtils.isEmpty(error)) {
            PointEntitySigmobUtils.SigmobTracking(PointCategory.READY, null, adUnit, mLoadAdRequest, null);

            if (adStatus == AdStatus.AdStatusClose) {
                SigmobLog.d(" next load");
                FileUtil.writeToCache(adUnit, SigmobFileUtil.getSplashAdUnitFilePath(mLoadAdRequest.getPlacementId()));
            }
            if (adStatus != AdStatus.AdStatusLoading)
                return;

            boolean isPortrait = ORIENTATION_PORTRAIT == ClientMetadata.getInstance().getOrientationInt();

            if (!isPortrait) {
                onAdPlayFailNotSupportOrientation();
                return;
            }

            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }

            adStatus = AdStatus.AdStatusReady;

            WindAds.sharedAds().getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (mSplashADListener != null) {
                        mSplashADListener.onSplashAdLoadSuccess(mLoadAdRequest.getPlacementId());
                    }
                }
            });


        } else {
            onInterstitialFailed(mAdUnit, error);
        }

    }

    public void initSessionManager() {
        SessionManager sessionManager = mAdUnit.getSessionManager();
        if (sessionManager == null) {
            SplashViewAbilitySessionManager splashViewAbilitySessionManager = new SplashViewAbilitySessionManager();
            splashViewAbilitySessionManager.createDisplaySession(mAdUnit);
        }
    }

    public void showSplashAd(ViewGroup viewGroup) {

        boolean isPortrait = ORIENTATION_PORTRAIT == ClientMetadata.getInstance().getOrientationInt();

        if (!isPortrait) {
            onAdPlayFailNotSupportOrientation();
            return;
        }

        if (viewGroup != null) {
            viewGroup.removeAllViews();
            boolean result = createSplashView(viewGroup.getContext(), mAdUnit);
            if (!result) {
                onAdPlayFail();
                return;
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            viewGroup.addView(mSplashAdView, layoutParams);

            mSplashAdInterstitial.showInterstitial(mAdUnit, null);
            mDuration = mSplashAdInterstitial.mAdconfig.getShowDuration();

            mSplashAdView.setDuration(mDuration);
            result = mSplashAdView.loadSplashResource();

            if (result) {
                return;
            }
        }
        onAdPlayFail();
    }


    @Override
    public void onInterstitialFailed(BaseAdUnit adUnit, String error) {
        WindAdError adError = WindAdError.ERROR_SIGMOB_FILE_DOWNLOAD;
        adError.setMessage(error);
        handleError(adError, true);
        PointEntitySigmobUtils.SigmobError(PointCategory.LOAD, adError, mAdUnit);
    }

    @Override
    public void onInterstitialShown(BaseAdUnit adUnit) {
        adStatus = AdStatus.AdStatusPlaying;
        initSessionManager();

        AdStackManager.addAdUnit(adUnit);
        if (mSplashAdView.getDuration() > 0 && mSplashAdView.getDuration() < mDuration) {
            mDuration = mSplashAdView.getDuration();
        }

        if (mSplashAdView != null) {
            mSplashAdView.setDuration(mDuration);
            mSplashAdView.setVisibility(View.VISIBLE);
        }

        if (mSplashADListener != null) {
            mSplashADListener.onSplashAdShow(mLoadAdRequest.getPlacementId());
        }

        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        clickDuration = mDuration;
        mHandler.post(timerRunnable);
    }


    @Override
    public void onStopTime() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    @Override
    public void onInterstitialClicked(BaseAdUnit adUnit) {


        if (mSplashADListener != null)
            mSplashADListener.onSplashAdClick(mLoadAdRequest.getPlacementId());
    }


    @Override
    public void onInterstitialDismissed(BaseAdUnit adUnit) {

        if (mSplashADListener != null)
            mSplashADListener.onSplashAdClose(mLoadAdRequest.getPlacementId());


        adStatus = AdStatus.AdStatusClose;
        onDestroy();

        if (TextUtils.isEmpty(mLoadAdRequest.getBidToken()) && adUnit.bidding_response == null) {
            loadAd(null, 0, WindAds.CNY, 45, true);
        }
    }

    @Override
    public void onInterstitialVOpen(BaseAdUnit mAdUnit) {

    }

    private void onDestroy() {


        if (mSplashAdInterstitial != null) {

            if (mAdUnit != null) {
                SessionManager sessionManager = mAdUnit.getSessionManager();
                if (sessionManager != null) {
                    sessionManager.endDisplaySession();
                }
            }
            mSplashAdInterstitial.onInvalidate(mAdUnit);
        }

        if (adStatus == AdStatus.AdStatusPlaying){

            adStatus = AdStatus.AdStatusClose;
            if (mSplashADListener != null)
                mSplashADListener.onSplashAdClose(mLoadAdRequest.getPlacementId());

        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (mSplashAdView != null && !mLoadAdRequest.isDisableAutoHideAd()) {
            ViewUtil.removeFromParent(mSplashAdView);
            mSplashAdView = null;
        }
        AdStackManager.cleanPlayAdUnit(mAdUnit);

        mAdUnit = null;
        mSplashADListener = null;

    }

    @Override
    public void onAdPlayFail() {
        PointEntitySigmobUtils.SigmobError(PointCategory.PLAY, WindAdError.ERROR_SIGMOB_SPLASH_UNSUPPORT_RESOURCE, mAdUnit);

        handleError(WindAdError.ERROR_SIGMOB_SPLASH_UNSUPPORT_RESOURCE, false);
        onDestroy();
    }

    public void onAdPlayFailNotSupportOrientation() {
        PointEntitySigmobUtils.SigmobError(PointCategory.PLAY, WindAdError.ERROR_SIGMOB_SPLASH_UNSUPPORT_ORIENTATION, mAdUnit);
        handleError(WindAdError.ERROR_SIGMOB_SPLASH_UNSUPPORT_ORIENTATION, false);
        onDestroy();
    }

    @Override
    public void onAdSkip() {
//        if (mSplashAdInterstitial != null) {
//            if (mSplashAdInterstitial.mAdconfig != null)
//                mSplashAdInterstitial.mAdconfig.handleSkip(SDKContext.getApplicationContext(), 0, mAdUnit);
//        }
        mSplashAdView.setDuration(0);
        if (mSplashADListener != null) {
            mSplashADListener.onSplashAdSkip(mLoadAdRequest.getPlacementId());
        }
    }

    @Override
    public void onAdPlay() {

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

    public void destroy() {
        onDestroy();
    }

    @Override
    public void onLandPageShow() {

    }

    @Override
    public void onLandPageClose() {
        BaseBroadcastReceiver.broadcastAction(SDKContext.getApplicationContext(), mAdUnit.getUuid(), IntentActions.ACTION_INTERSTITIAL_DISMISS);
    }


    public String getEcpm() {
        if (mAdUnit != null && mAdUnit.bidding_response != null) {
            return String.valueOf(mAdUnit.bidding_response.ecpm);
        }
        return null;
    }


    public Map<String, BiddingResponse> getBidInfo() {
        if (mAdUnit != null) {
            if (mAdUnit != null && mAdUnit.bidding_response != null) {
                HashMap<String, BiddingResponse> bidInfo = new HashMap<>();
                bidInfo.put(mAdUnit.getRequestId(), mAdUnit.bidding_response);
                return bidInfo;
            }
        }
        return null;
    }


    public void doMacro(String key, String value) {
        if (mAdUnit != null && mAdUnit.bidding_response != null) {
            mAdUnit.getMacroCommon().addMarcoKey(key, value);
        }
    }
}
