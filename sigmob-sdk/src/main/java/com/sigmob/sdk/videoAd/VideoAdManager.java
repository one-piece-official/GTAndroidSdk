package com.sigmob.sdk.videoAd;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.common.track.AdTracker;
import com.czhj.sdk.common.track.BaseMacroCommon;
import com.czhj.sdk.common.track.TrackManager;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.VolleyError;
import com.sigmob.sdk.Sigmob;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.AdListCacheManager;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.CreativeType;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.mta.PointEntitySigmobError;
import com.sigmob.sdk.base.network.RequestFactory;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;
import com.sigmob.sdk.rewardVideoAd.RequestSceneType;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.rewardVideo.WindRewardInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoAdManager implements VideoInterstitial.VideoInterstitialListener,
        RequestFactory.LoadAdRequestListener,
        AdStackManager.AdStackStatusListener {

    private final Handler mHandler;
    private WindAdLoadListener windAdLoadListener;
    private int rewardStatus;
    private AdStatus mAdStatus;
    private List<BaseAdUnit> mLoadAdUnitList;
    private BaseAdUnit mPlayingAdUnit;
    private long lastLoadAdTime;
    private LoadAdRequest mLoadAdRequest;
    private final int MSG_LOAD = 0x2001;
    private String mPlacementId;
    private VideoInterstitial interstitial;
    private WindAdShowListener mWindAdShowListener;
    private WindAdRewardListener mWindAdRewardListener;
    private String mRequestId = null;
    private String preLoadRequestId = null;

    private boolean isHalfInterstitial;

    public VideoAdManager(boolean isHalfInterstitial) {
        this.isHalfInterstitial = isHalfInterstitial;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_LOAD: {//总的广告加载时常
                        if (mAdStatus == AdStatus.AdStatusLoading) {
                            mHandler.removeMessages(MSG_LOAD);
                            notifyLoadResult(WindAdError.ERROR_SIGMOB_AD_TIME_OUT);

                        }
                    }
                    break;
                }
            }
        };
    }

    private boolean isExpiredAd() {

        if (lastLoadAdTime == 0 || mLoadAdUnitList == null || mLoadAdUnitList.isEmpty()) {
            return false;
        }
        BaseAdUnit baseAdUnit = mLoadAdUnitList.get(0);
        return baseAdUnit != null && baseAdUnit.isExpiredAd();
    }

    private boolean checkAdUnit(BaseAdUnit adUnit) {


        try {

            if (adUnit.getPlayMode() != WindConstants.PLAY_MODE_PRELOAD) return true;

            boolean isEndcardReady = adUnit.isEndCardIndexExist();

            if (!isEndcardReady) {
                SigmobLog.e("endIndex file not ready");
            }

            return isEndcardReady;

        } catch (Throwable e) {
            return false;
        }
    }

    public boolean isReady() {
        if (mLoadAdUnitList != null && mLoadAdUnitList.size() > 0) {
            BaseAdUnit adUnit = mLoadAdUnitList.get(0);
            if (adUnit == null
                    || isExpiredAd()
                    || !checkAdUnit(adUnit)
                    || interstitial == null
                    || !interstitial.baseAdUnitValid(adUnit)) {
                if (adUnit != null) {
                    AdStackManager.shareInstance().removeHistoryAdCache(adUnit);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private VideoInterstitial showAdCheck() {


        boolean result = false;
        WindAdError adError = WindAdError.ERROR_SIGMOB_AD_PLAY_CHECK_FAIL;
        BaseAdUnit adUnit = null;
        if (mAdStatus == AdStatus.AdStatusPlaying) {
            adError.setMessage(WindAdError.ERROR_SIGMOB_AD_PLAY_HAS_PLAYING.getMessage());
        } else if (mLoadAdUnitList == null || mLoadAdUnitList.get(0) == null) {
            adError.setMessage("not ready adUnit");
        } else if (mAdStatus != AdStatus.AdStatusReady) {
            adError.setMessage("ad status is not ready");
        } else if (interstitial == null) {
            adError.setMessage("interstitial object is null");
        } else {
            adUnit = mLoadAdUnitList.get(0);
            if (isExpiredAd()) {
                adError.setMessage("ad unit is expired");
            } else if (!checkAdUnit(adUnit)) {
                adError.setMessage("check ad unit endcard is invalid");
            } else if (!interstitial.baseAdUnitValid(adUnit)) {
                adError.setMessage("check ad unit info is invalid");
            } else {
                result = true;

            }
        }

        if (!result) {
            notifyPlayErrorResult(adError);
            PointEntitySigmobUtils.SigmobError(PointCategory.PLAY, adError, adUnit, mLoadAdRequest);
            return null;
        }

        return interstitial;

    }

    public void callBackDc(String category,Map<String,String> option){
        WindSDKConfig instance = WindSDKConfig.getInstance();
        if (instance.enableExtraDclog()){
            PointEntitySigmobUtils.SigmobTracking(category,null,mPlayingAdUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                @Override
                public void onAddExtra(Object pointEntityBase) {
                    if (pointEntityBase instanceof PointEntitySigmob){
                        PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                        if(option != null){
                            entitySigmob.getOptions().putAll(option);
                        }

                    }
                }
            });
        }

    }

    public void show(final LoadAdRequest loadAdRequest, WindAdShowListener windAdShowListener) {

        mWindAdShowListener = windAdShowListener;
        mLoadAdRequest = loadAdRequest;


        VideoInterstitial interstitial = showAdCheck();
        if (interstitial == null) {
            return;
        }


        mPlayingAdUnit = mLoadAdUnitList.get(0);
        mRequestId = mPlayingAdUnit.getRequestId();
        mPlayingAdUnit.setLoad_id(mLoadAdRequest.getLoadId());
        mPlayingAdUnit.setBid_token(mLoadAdRequest.getBidToken());
        mPlayingAdUnit.setAd_scene_id(mLoadAdRequest.getAdSceneId());
        mPlayingAdUnit.setAd_scene_desc(mLoadAdRequest.getAdSceneDesc());


        BaseMacroCommon baseMacroCommon = mPlayingAdUnit.getMacroCommon();
        if (baseMacroCommon instanceof SigMacroCommon) {
            if (!TextUtils.isEmpty(mLoadAdRequest.getAdSceneDesc())) {
                ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ADSCENE_, mLoadAdRequest.getAdSceneDesc());
            }

            if (!TextUtils.isEmpty(mLoadAdRequest.getAdSceneId())) {
                ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ADSCENEID_, mLoadAdRequest.getAdSceneId());
            }

            if (!TextUtils.isEmpty(mPlayingAdUnit.getVideo_url())) {
                ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VMD5_,
                        AdStackManager.shareInstance().getVideoFileMD5(mPlayingAdUnit.getVideoPath()));
                try {
                    String url = URLEncoder.encode(mPlayingAdUnit.getVideo_url(), "UTF-8");
                    ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VURL_, url);
                } catch (UnsupportedEncodingException e) {
                    SigmobLog.e(e.getMessage());
                }
            }

        }

        if (mPlayingAdUnit.getCreativeType() == CreativeType.CreativeTypeMRAIDTWO.getCreativeType()) {//代表2.0

            AdListCacheManager manager = new AdListCacheManager(mLoadAdUnitList, this, mLoadAdRequest);
            boolean cache = manager.cacheList(true);
            if (!cache) {
                Map<String, Object> localExtra = new HashMap<>();
                interstitial.loadInterstitial(localExtra, mPlayingAdUnit);
            }
        } else {//之前原有的逻辑

            if (mPlayingAdUnit.getPlayMode() == WindConstants.PLAY_MODE_STREAM) {

                Map<String, Object> localExtra = new HashMap<>();
                interstitial.loadInterstitial(localExtra, mPlayingAdUnit);
                AdStackManager.shareInstance().cache(mPlayingAdUnit, this);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(WindConstants.ENABLEKEEPON, mLoadAdRequest.isEnable_keep_on() || WindSDKConfig.getInstance().isScreenKeep());
        bundle.putBoolean(WindConstants.ENABLESCREENLOCKDISPLAYAD, mLoadAdRequest.isEnable_screen_lock_displayad() || WindSDKConfig.getInstance().isLockPlay());


        interstitial.showInterstitial(mPlayingAdUnit, bundle);

        lastLoadAdTime = 0;
        mAdStatus = AdStatus.AdStatusPlaying;

    }

    public void loadAd(LoadAdRequest request) {


        mAdStatus = AdStatus.AdStatusLoading;

        if (request == null || TextUtils.isEmpty(request.getPlacementId())) {

            SigmobLog.e("loadAd error " + "loadAdRequest or placementId is null");
            notifyLoadResult(WindAdError.ERROR_SIGMOB_PLACEMENTID_EMPTY);
            return;
        }
        try {
            if (Sigmob.getInstance() != null) {
                WindAdError sigmobError = Sigmob.getInstance().getSigMobError();
                if (sigmobError != null) {
                    SigmobLog.e("check loadAd error " + sigmobError.toString());
                    PointEntitySigmobError error = PointEntitySigmobError.SigmobError(PointCategory.LOAD, sigmobError.getErrorCode(), sigmobError.getMessage());
                    error.commit();
                    notifyLoadResult(sigmobError);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(request.getBidToken())) {
            clearLoadAdUnit();
        }
        mPlacementId = request.getPlacementId();
        mLoadAdRequest = request;

        if (interstitial == null) {
            interstitial = new VideoInterstitial(this, isHalfInterstitial);
        }


        if (isReady()) {
            interstitial.loadInterstitial(null, mLoadAdUnitList.get(0));
            SigmobLog.d("adsRequest isReady  placementId = [" + mPlacementId + "]");
            notifyPreLoadSuccess();
            notifyLoadResult(null);
            return;
        }
        if (!TextUtils.isEmpty(mRequestId)) {
            AdStackManager.removeBidResponse(mRequestId);
        }
        if (!TextUtils.isEmpty(preLoadRequestId)) {
            AdStackManager.removeBidResponse(preLoadRequestId);
        }

        request.setLastCampid(AdStackManager.shareInstance().getLast_campid());

        request.setLastCrid(AdStackManager.shareInstance().getLast_crid());

        mHandler.sendEmptyMessageDelayed(MSG_LOAD, WindSDKConfig.getInstance().loadRvAdTimeout());

        adsRequest(request, RequestSceneType.NormalRequest, this);
    }

    private void adsRequest(final LoadAdRequest loadAdRequest, RequestSceneType sceneType, RequestFactory.LoadAdRequestListener listener) {

        final String mPlacementId = loadAdRequest.getPlacementId();

        clearLoadAdUnit();

        loadAdRequest.setRequest_scene_type(sceneType.getValue());

        if (sceneType != RequestSceneType.NormalRequest) {
            PointEntitySigmobUtils.SigmobTracking(PointCategory.REQUEST, PointCategory.PLAY, null, null, loadAdRequest, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                @Override
                public void onAddExtra(Object pointEntityBase) {
                    if (pointEntityBase instanceof PointEntitySigmob) {
                        PointEntitySigmob entity = (PointEntitySigmob) pointEntityBase;
                        entity.setAdx_id(null);
                    }
                }
            });
        }


        boolean isExpiredAd = isExpiredAd();
        loadAdRequest.setExpired(isExpiredAd);

        RequestFactory.LoadAd(loadAdRequest, listener);
        SigmobLog.d("adsRequest loadAdRequest = [" + loadAdRequest + "], placementId = [" + mPlacementId + "]");

    }

    @Override
    public void onSuccess(final List<BaseAdUnit> adUnits, final LoadAdRequest loadAdRequest) {
        BaseAdUnit adUnit = null;
        try {
            if (adUnits != null && adUnits.size() > 0) {

                adUnit = adUnits.get(0);

                mLoadAdUnitList = adUnits;
                mRequestId = adUnit.getRequestId();

                PointEntitySigmobUtils.SigmobTracking(PointCategory.RESPOND, Constants.SUCCESS, null, loadAdRequest, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        if (pointEntityBase instanceof PointEntitySigmob) {
                            PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                            HashMap<String, String> extData = new HashMap<>();
                            extData.put("ad_count", String.valueOf(adUnits.size()));
                            entitySigmob.setOptions(extData);
                        }
                    }
                });

                if (interstitial == null) {
                    interstitial = new VideoInterstitial(this, isHalfInterstitial);
                }

                if (!interstitial.baseAdUnitValid(adUnit)) {
                    notifyLoadResult(WindAdError.ERROR_SIGMOB_INFORMATION_LOSE);
                    return;
                }

                notifyPreLoadSuccess();

                AdStackManager.addAdUnitList(adUnits);

                for (BaseAdUnit ad : mLoadAdUnitList) {
                    if (ad.getAd_source_channel().equalsIgnoreCase(WindConstants.SIGMOB_CHANNEL)) {
                        AdStackManager.shareInstance().addHistoryAdCache(ad);
                    }
                }


                /* 0 代表 预加载,1 代表填充立即返回广告，并同时预加载，2: 代表广告填充立即返回广告，但不进行预加载处理*/
                if (adUnit.getPlayMode() != WindConstants.PLAY_MODE_PRELOAD) {//流播放

                    lastLoadAdTime = adUnit.getCreate_time();
                    notifyLoadResult(null);
                }

                if (adUnit.getCreativeType() == CreativeType.CreativeTypeMRAIDTWO.getCreativeType()) {//代表2.0

                    AdListCacheManager manager = new AdListCacheManager(adUnits, this, mLoadAdRequest);
                    boolean cache = manager.cacheList(false);
                    if (cache) {
                        Map<String, Object> localExtra = new HashMap<>();
                        interstitial.loadInterstitial(localExtra, adUnit);
                    }
                } else {//之前原有的逻辑

                    if (adUnit.getPlayMode() != WindConstants.PLAY_MODE_STREAM) {//预加载

                        Map<String, Object> localExtra = new HashMap<>();
                        interstitial.loadInterstitial(localExtra, adUnit);
                        AdStackManager.shareInstance().cache(adUnit, this);
                    }
                }
            } else {

                WindAdError adError = WindAdError.ERROR_SIGMOB_INFORMATION_LOSE;
                notifyPreLoadFail(WindAdError.ERROR_SIGMOB_INFORMATION_LOSE);

                PointEntitySigmobUtils.SigmobError(PointCategory.RESPOND, Constants.FAIL,
                        adError.getErrorCode(), adError.getMessage(), loadAdRequest);

                notifyLoadResult(WindAdError.ERROR_SIGMOB_INFORMATION_LOSE);
            }
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());

            clearLoadAdUnit();

            WindAdError adError = WindAdError.ERROR_SIGMOB_INFORMATION_LOSE;
            adError.setMessage(e.getMessage());

            PointEntitySigmobUtils.SigmobError(PointCategory.RESPOND, Constants.FAIL,
                    adError.getErrorCode(), adError.getMessage(), loadAdRequest);

            notifyLoadResult(adError);
        }
    }

    @Override
    public void onErrorResponse(int error, String message, String request_id, LoadAdRequest loadAdRequest) {

        PointEntitySigmobUtils.SigmobTracking(PointCategory.RESPOND, Constants.FAIL, loadAdRequest);

        PointEntitySigmobUtils.SigmobError(PointCategory.RESPOND, Constants.FAIL, error, message,
                loadAdRequest);

        WindAdError adError = WindAdError.getWindAdError(error);

        if (adError == null) {
            adError = WindAdError.ERROR_SIGMOB_REQUEST;
            adError.setErrorMessage(error, message);
        }
        notifyPreLoadFail(adError);

        notifyLoadResult(adError);
    }


    @Override
    public void loadStart(BaseAdUnit adUnit) {
        PointEntitySigmobUtils.SigmobTracking(PointCategory.LOADSTART, null, adUnit, null, mLoadAdRequest, null);

        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_LOAD);
    }

    @Override
    public void loadEnd(BaseAdUnit adUnit, String error) {
        SigmobLog.d("onInterstitialLoaded() called");

        SigmobTrackingRequest.sendTrackings(adUnit, TextUtils.isEmpty(error) ? ADEvent.AD_LOAD_SUCCESS : ADEvent.AD_LOAD_FAILURE);

        mHandler.removeMessages(MSG_LOAD);
        PointEntitySigmobUtils.SigmobTracking(PointCategory.LOADEND,
                TextUtils.isEmpty(error) ? Constants.SUCCESS : Constants.FAIL, adUnit, null, mLoadAdRequest, null);

        if (adUnit != null && adUnit.getPlayMode() == WindConstants.PLAY_MODE_PRELOAD) {

            if (TextUtils.isEmpty(error)) {

                lastLoadAdTime = adUnit.getCreate_time();

                notifyLoadResult(null);
            } else {
                WindAdError adError = WindAdError.ERROR_SIGMOB_FILE_DOWNLOAD;
                adError.setMessage(error);
                logError(adUnit, PointCategory.LOAD, adUnit.getAd_type(), adUnit.getAdslot_id(), adUnit.getLoad_id(), adError);

                notifyLoadResult(adError);
                clearLoadAdUnit();
            }

        }

    }


    @Override
    public void onInterstitialFailed(BaseAdUnit adUnit, String error) {

        mHandler.removeMessages(MSG_LOAD);

        SigmobLog.d("onInterstitialFailed() called with: errorCode = [" + error + "]");

        PointEntitySigmobUtils.SigmobTracking(PointCategory.LOADEND, Constants.FAIL, adUnit, null, mLoadAdRequest, null);
        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_LOAD_FAILURE);

        clearLoadAdUnit();
        if (adUnit != null && adUnit.getPlayMode() == WindConstants.PLAY_MODE_PRELOAD) {

            WindAdError adError = WindAdError.ERROR_SIGMOB_FILE_DOWNLOAD;
            adError.setMessage(error);
            logError(adUnit, PointCategory.LOAD, adUnit.getAd_type(), adUnit.getAdslot_id(), adUnit.getLoad_id(), adError);

            notifyLoadResult(adError);
        }

    }

    @Override
    public void onInterstitialShown(BaseAdUnit adUnit) {
        SigmobLog.d("onInterstitialShown() called");


        rewardStatus = 0;
        clearLoadAdUnit();

        if (mWindAdShowListener != null) {
            mWindAdShowListener.onAdShow(mPlacementId);
        }
        if (adUnit != null) {


            if (!adUnit.getDisableAutoLoad() && TextUtils.isEmpty(mLoadAdRequest.getBidToken()) && adUnit.bidding_response == null) {

                SigmobLog.d(adUnit.getPlayMode() + " adsRequest onInterstitialShown: " + mLoadAdRequest.getBidToken());
                if (adUnit.getPlayMode() == WindConstants.PLAY_MODE_PRELOAD && TextUtils.isEmpty(mLoadAdRequest.getBidToken())) {

                    mLoadAdRequest.setLastCampid(adUnit.getCamp_id());
                    mLoadAdRequest.setLastCrid(adUnit.getCrid());

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            preloadAdsRequest(mLoadAdRequest);
                        }
                    });
                }
            }

        }
    }

    @Override
    public void onInterstitialClicked(BaseAdUnit adUnit) {
        SigmobLog.d("onInterstitialClicked() called");

        if (mWindAdShowListener != null) {
            mWindAdShowListener.onAdClicked(mPlacementId);
        }
    }


    @Override
    public void onInterstitialDismissed(BaseAdUnit adUnit) {
        SigmobLog.d("onInterstitialDismissed() called");


        if (mAdStatus == AdStatus.AdStatusClose)
            return;

        mAdStatus = AdStatus.AdStatusClose;
        if (interstitial != null) {
            interstitial.onInvalidate(adUnit);
        }
        if (rewardStatus == 1) {
            rewardStatus = 0;
            if (mWindAdRewardListener != null) {
                HashMap option = new HashMap();
                option.put(WindAds.TRANS_ID,mRequestId);
                option.put(WindAds.SERVER_ARRIVED,Constants.FAIL);
                option.put(WindAds.REWARD_TYPE,Constants.SUCCESS);
                mWindAdRewardListener.onVideoAdRewarded(new WindRewardInfo(true,option), mPlacementId);
            }
        }


        if (mWindAdShowListener != null) {
            mWindAdShowListener.onAdClosed(mPlacementId);
        }

        //广告关闭时候清除上一次播放的广告id
//        AdStackManager.shareInstance().setLast_campid("");
//        AdStackManager.shareInstance().setLast_crid("");
    }

    @Override
    public void onInterstitialVOpen(BaseAdUnit adUnit) {

        if (adUnit != null) {
            AdStackManager.shareInstance().setLast_campid(adUnit.getCamp_id());
            AdStackManager.shareInstance().setLast_crid(adUnit.getCrid());
            clearLoadAdUnit();

        }
    }

    @Override
    public void onVideoComplete(BaseAdUnit adUnit) {

        SigmobLog.d("onVideoComplete() called");
        rewardStatus = 1;
        if (mWindAdShowListener != null) {
            mWindAdShowListener.onVideoAdPlayComplete(mPlacementId);
        }

        sendSendReward(adUnit);

    }

    private void sendSendReward(BaseAdUnit baseAdUnit) {

        if (baseAdUnit != null) {
            String url = baseAdUnit.getRvCallBackUrl();
            if (!TextUtils.isEmpty(url)) {
                AdTracker tracker = new AdTracker(AdTracker.MessageType.TRACKING_URL, url, "reward_server", baseAdUnit.getRequestId());

                tracker.setRetryNum(baseAdUnit.getTrackingRetryNum());
                SigmobTrackingRequest.sendTracking(tracker, baseAdUnit, false, true, true, new TrackManager.Listener() {
                    @Override
                    public void onSuccess(AdTracker tracker, NetworkResponse response) {
                        if (rewardStatus == 1) {
                            rewardStatus = 2;
                            if (mWindAdRewardListener != null) {
                                HashMap option = new HashMap();
                                option.put(WindAds.TRANS_ID,mRequestId);
                                option.put(WindAds.SERVER_ARRIVED,Constants.SUCCESS);
                                option.put(WindAds.REWARD_TYPE,Constants.SUCCESS);
                                mWindAdRewardListener.onVideoAdRewarded(new WindRewardInfo(true,option), mPlacementId);
                            }
                        }

                    }

                    @Override
                    public void onErrorResponse(AdTracker tracker, VolleyError error) {
                        if (rewardStatus == 1) {
                            rewardStatus = 2;
                            if (mWindAdRewardListener != null) {
                                HashMap option = new HashMap();
                                option.put(WindAds.TRANS_ID,mRequestId);
                                option.put(WindAds.SERVER_ARRIVED,Constants.FAIL);
                                option.put(WindAds.REWARD_TYPE,Constants.SUCCESS);
                                mWindAdRewardListener.onVideoAdRewarded(new WindRewardInfo(true,option), mPlacementId);
                            }
                        }
                    }
                });
                return;
            }
        }
        rewardStatus = 2;
        if (mWindAdRewardListener != null) {
            HashMap option = new HashMap();
            option.put(WindAds.TRANS_ID,mRequestId);
            option.put(WindAds.SERVER_ARRIVED,Constants.FAIL);
            option.put(WindAds.REWARD_TYPE,Constants.FAIL);
            mWindAdRewardListener.onVideoAdRewarded(new WindRewardInfo(true,option), mPlacementId);
        }
    }

    @Override
    public void onVideoPlayFail(BaseAdUnit adUnit, String message) {
        if (adUnit != null && adUnit.getAd_source_channel().equalsIgnoreCase(WindConstants.SIGMOB_CHANNEL)) {
            AdStackManager.shareInstance().removeHistoryAdCache(adUnit);
        }

        WindAdError adError = WindAdError.ERROR_SIGMOB_AD_PLAY;
        adError.setMessage(message);
        notifyPlayErrorResult(adError);

        logError(adUnit, PointCategory.PLAY, mLoadAdRequest.getAdType(), mPlacementId, mLoadAdRequest.getLoadId(), adError);

        SigmobLog.d("onVideoPlayFail() called");
    }

    @Override
    public void onVideoPlay(BaseAdUnit adUnit) {
        SigmobLog.d("onVideoPlay() called");

    }

    @Override
    public void onVideoSkip(BaseAdUnit adUnit) {
        SigmobLog.d("onVideoSkip() called");
        boolean isSkip = true;
    }

    @Override
    public void onVideoClose(BaseAdUnit adUnit) {
        if (mWindAdShowListener != null) {
            mWindAdShowListener.onVideoAdPlayEnd(mPlacementId);
        }

        if (!adUnit.getDisableAutoLoad() && TextUtils.isEmpty(mLoadAdRequest.getBidToken()) && adUnit.bidding_response == null) {
            SigmobLog.d(adUnit.getPlayMode() + " adsRequest onVideoClose: " + mLoadAdRequest.getBidToken());
            if (adUnit.getPlayMode() != WindConstants.PLAY_MODE_PRELOAD && TextUtils.isEmpty(mLoadAdRequest.getBidToken())) {
                mLoadAdRequest.setLastCampid(adUnit.getCamp_id());
                mLoadAdRequest.setLastCrid(adUnit.getCrid());
                preloadAdsRequest(mLoadAdRequest);
            }
        }
    }


    private void preloadAdsRequest(LoadAdRequest loadAdRequest) {

        adsRequest(loadAdRequest, RequestSceneType.AutoNextPreload, new RequestFactory.LoadAdRequestListener() {
            @Override
            public void onSuccess(final List<BaseAdUnit> adUnits, LoadAdRequest loadAdRequest) {
                if (adUnits != null && adUnits.size() > 0) {

                    BaseAdUnit adUnit = adUnits.get(0);

                    mLoadAdUnitList = adUnits;

                    preLoadRequestId = adUnit.getRequestId();
                    PointEntitySigmobUtils.SigmobTracking(PointCategory.RESPOND, Constants.SUCCESS, null, loadAdRequest, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {
                            if (pointEntityBase instanceof PointEntitySigmob) {
                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                HashMap<String, String> extData = new HashMap<>();
                                extData.put("ad_count", String.valueOf(adUnits.size()));
                                entitySigmob.setOptions(extData);
                            }
                        }
                    });

                    if (!interstitial.baseAdUnitValid(adUnit)) {
                        notifyLoadResult(WindAdError.ERROR_SIGMOB_INFORMATION_LOSE);
                        return;
                    }

//                    notifyPreLoadSuccess();

                    if (adUnit.getAd_source_channel().equalsIgnoreCase(WindConstants.SIGMOB_CHANNEL)) {
                        AdStackManager.shareInstance().addHistoryAdCache(adUnit);
                    }


                    if (adUnit.getPlayMode() != WindConstants.PLAY_MODE_PRELOAD) {//流播放

                        lastLoadAdTime = adUnit.getCreate_time();
                    }

                    AdStackManager.addAdUnitList(adUnits);

                    AdStackManager.AdStackStatusListener listener = new AdStackManager.AdStackStatusListener() {
                        @Override
                        public void loadStart(BaseAdUnit adUnit) {
                            PointEntitySigmobUtils.SigmobTracking(PointCategory.LOADSTART, null, adUnit, null, mLoadAdRequest, null);

                            SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_LOAD);
                        }

                        @Override
                        public void loadEnd(BaseAdUnit adUnit, String error) {
                            SigmobTrackingRequest.sendTrackings(adUnit, TextUtils.isEmpty(error) ? ADEvent.AD_LOAD_SUCCESS : ADEvent.AD_LOAD_FAILURE);

                            mHandler.removeMessages(MSG_LOAD);
                            PointEntitySigmobUtils.SigmobTracking(PointCategory.LOADEND,
                                    TextUtils.isEmpty(error) ? Constants.SUCCESS : Constants.FAIL,
                                    adUnit, null, mLoadAdRequest, null);

                            if (adUnit != null && adUnit.getPlayMode() == WindConstants.PLAY_MODE_PRELOAD) {

                                if (TextUtils.isEmpty(error)) {
                                    lastLoadAdTime = adUnit.getCreate_time();

                                } else {

                                    WindAdError adError = WindAdError.ERROR_SIGMOB_FILE_DOWNLOAD;
                                    adError.setMessage(error);
                                    logError(adUnit, PointCategory.LOAD, adUnit.getAd_type(), adUnit.getAdslot_id(), adUnit.getLoad_id(), adError);

                                    clearLoadAdUnit();
                                }

                            }

                        }
                    };

                    if (adUnit.getCreativeType() == CreativeType.CreativeTypeMRAIDTWO.getCreativeType()) {//代表2.0
                        AdListCacheManager manager = new AdListCacheManager(adUnits, listener, mLoadAdRequest);
                        boolean cache = manager.cacheList(false);
                        if (cache) {
                            Map<String, Object> localExtra = new HashMap<>();
                            interstitial.loadInterstitial(localExtra, adUnit);
                        }
                    } else {//之前原有的逻辑
                        if (adUnit.getPlayMode() != WindConstants.PLAY_MODE_STREAM) {//预加载
                            Map<String, Object> localExtra = new HashMap<>();
                            interstitial.loadInterstitial(localExtra, adUnit);
                            AdStackManager.shareInstance().cache(adUnit, listener);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse(int error, String message, String request_id, LoadAdRequest loadAdRequest) {
                PointEntitySigmobUtils.SigmobTracking(PointCategory.RESPOND, Constants.FAIL, loadAdRequest);

                PointEntitySigmobUtils.SigmobError(PointCategory.RESPOND, Constants.FAIL, error, message,
                        loadAdRequest);

            }
        });
    }

    private void logError(BaseAdUnit adUnit, String cate, final int adType, final String placementId, final String load_id, WindAdError adError) {


        PointEntitySigmobUtils.SigmobError(cate, adError, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmobError) {
                    PointEntitySigmobError sigmobError = (PointEntitySigmobError) pointEntityBase;
                    sigmobError.setLoad_id(load_id);
                    sigmobError.setPlacement_id(placementId);
                    sigmobError.setAdtype(String.valueOf(adType));
                }
            }
        });

    }


    private void notifyPlayErrorResult(final WindAdError error) {

        if (mAdStatus != AdStatus.AdStatusLoading){
            clearLoadAdUnit();
            mAdStatus = AdStatus.AdStatusNone;
        }
        if (mWindAdShowListener != null && error != null) {
            mWindAdShowListener.onAdShowError(error, mPlacementId);
        }
    }

    private void notifyPreLoadSuccess() {

        if (mAdStatus != AdStatus.AdStatusLoading) return;

        if (windAdLoadListener != null) {
            windAdLoadListener.onAdPreLoadSuccess(mPlacementId);
        }

    }

    private void notifyPreLoadFail(final WindAdError error) {

        if (mAdStatus != AdStatus.AdStatusLoading) return;


        if (windAdLoadListener != null) {
            windAdLoadListener.onAdPreLoadFail(error, mPlacementId);
        }

    }


    private void notifyLoadResult(final WindAdError error) {

        if (mAdStatus != AdStatus.AdStatusLoading) return;

        if (error == null) {
            mAdStatus = AdStatus.AdStatusReady;

            if (mLoadAdUnitList != null && mLoadAdUnitList.size() > 0) {
                PointEntitySigmobUtils.SigmobTracking(PointCategory.READY, null, mLoadAdUnitList.get(0), mLoadAdRequest, null);
            }
        } else {
            mAdStatus = AdStatus.AdStatusNone;
        }
        if (windAdLoadListener != null) {
            if (error != null) {
                windAdLoadListener.onAdLoadError(error, mPlacementId);
            } else {
                windAdLoadListener.onAdLoadSuccess(mPlacementId);
            }
        }
    }

    public void setRewardVideoAdListener(WindAdLoadListener windAdLoadListener) {
        this.windAdLoadListener = windAdLoadListener;
    }

    public void setWindAdRewardListener(WindAdRewardListener windAdRewardListener) {
        this.mWindAdRewardListener = windAdRewardListener;
    }

    private void clearLoadAdUnit() {
        if (mLoadAdUnitList != null && mLoadAdUnitList.size() > 0) {
           for (BaseAdUnit baseAdUnit : mLoadAdUnitList) {
               if (baseAdUnit != null) {
                   if (baseAdUnit.getAd_source_channel().equalsIgnoreCase(WindConstants.SIGMOB_CHANNEL)) {
                       AdStackManager.shareInstance().removeHistoryAdCache(baseAdUnit);
                   }
                   AdStackManager.cleanPlayAdUnit(baseAdUnit);
               }
           }
        }

        mLoadAdUnitList = null;
    }

    /**
     * 对象销毁时，移除缓存的广告
     */
    public void destroy() {

        if (!TextUtils.isEmpty(mRequestId)) {
            AdStackManager.removeBidResponse(mRequestId);
        }

        if (!TextUtils.isEmpty(preLoadRequestId)) {
            AdStackManager.removeBidResponse(preLoadRequestId);
        }
        clearLoadAdUnit();
        mWindAdShowListener = null;
        windAdLoadListener = null;
        mWindAdRewardListener = null;
        mPlayingAdUnit = null;
    }

    public String getEcpm() {
        if (mLoadAdUnitList != null && mLoadAdUnitList.size() > 0) {
            if (mLoadAdUnitList.get(0) != null && mLoadAdUnitList.get(0).bidding_response != null) {
                return String.valueOf(mLoadAdUnitList.get(0).bidding_response.ecpm);
            }
        }
        return null;
    }

    public Map<String, BiddingResponse> getBidInfo() {
        if (mLoadAdUnitList != null && mLoadAdUnitList.size() > 0) {
            BaseAdUnit baseAdUnit = mLoadAdUnitList.get(0);
            if (baseAdUnit != null && baseAdUnit.bidding_response != null) {
                HashMap<String, BiddingResponse> bidInfo = new HashMap<>();
                bidInfo.put(baseAdUnit.getRequestId(), baseAdUnit.bidding_response);
                return bidInfo;
            }
        }
        return null;
    }


    public void doMacro(String key, String value) {
        if (mLoadAdUnitList != null && mLoadAdUnitList.size() > 0) {
            BaseAdUnit baseAdUnit = mLoadAdUnitList.get(0);
            if (baseAdUnit != null && baseAdUnit.bidding_response != null) {
                baseAdUnit.getMacroCommon().addMarcoKey(key, value);
            }
       }
    }
}
