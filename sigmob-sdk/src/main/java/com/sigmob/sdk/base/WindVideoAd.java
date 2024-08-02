package com.sigmob.sdk.base;

import static com.czhj.sdk.common.models.AdStatus.AdStatusClose;
import static com.czhj.sdk.common.models.AdStatus.AdStatusLoading;
import static com.czhj.sdk.common.models.AdStatus.AdStatusNone;
import static com.czhj.sdk.common.models.AdStatus.AdStatusPlaying;
import static com.czhj.sdk.common.models.AdStatus.AdStatusReady;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmobError;
import com.sigmob.sdk.videoAd.VideoAdManager;
import com.sigmob.sdk.videoAd.WindAdLoadListener;
import com.sigmob.sdk.videoAd.WindAdRewardListener;
import com.sigmob.sdk.videoAd.WindAdShowListener;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAdRequest;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.rewardVideo.WindRewardInfo;

import java.util.HashMap;
import java.util.Map;


public class WindVideoAd extends WindBaseAd {

    private VideoAdManager mAd;
    private VideoAdManager mPreloadAd;
    private Handler mHandler;
    private LoadAdRequest loadAdRequest;
    private WindAdLoadListener mWindAdLoadListener;
    private boolean isHalfInterstitial;
    private HandleWindAdLoadListener handleRewardVideoAdListener;

    public WindVideoAd(WindAdRequest request, boolean isHalfInterstitial) {

        super(request, isHalfInterstitial);

        AdStackManager.shareInstance().removeHistoryAdAche(getPlacementId(), request.getAdType());

        this.isHalfInterstitial = isHalfInterstitial;

        mAd = new VideoAdManager(isHalfInterstitial);

        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void setAdLoadListener(WindAdLoadListener adLoadListener) {
        this.mWindAdLoadListener = adLoadListener;
    }

    public boolean isReady() {
        if (WindAds.sharedAds().isInit() && !TextUtils.isEmpty(getPlacementId()) && mAd != null) {
            return mADStatus == AdStatusReady && mAd.isReady();
        }
        return false;
    }

    public boolean show(HashMap<String, String> options, final WindAdShowListener windAdShowListener, final WindAdRewardListener windAdRewardListener) {
        try {

//            PointEntitySigmobUtils.SigmobRequestTracking(PointCategory.SHOW, PointCategory.INIT, mRequest, null);

            if (loadAdRequest == null) {

                PointEntitySigmobError windError = PointEntitySigmobError.SigmobError(PointCategory.ERROR, WindAdError.ERROR_SIGMOB_PLACEMENTID_EMPTY.getErrorCode(), "loadAdRequest is null");
                windError.setAdtype(String.valueOf(AdFormat.REWARD_VIDEO));
                windError.setPlacement_id(getPlacementId());
                windError.commit();

                if (windAdShowListener != null) {
                    windAdShowListener.onAdShowError(WindAdError.ERROR_SIGMOB_PLACEMENTID_EMPTY, getPlacementId());
                }
                return false;
            }
            AdStackManager.addShowCount(loadAdRequest.getPlacementId());



            if (options != null) {
                if (options.containsKey(WindAds.AD_SCENE_ID)) {
                    loadAdRequest.setAd_scene_id(options.get(WindAds.AD_SCENE_ID));
                }

                if (options.containsKey(WindAds.AD_SCENE_DESC)) {
                    loadAdRequest.setAd_scene_desc(options.get(WindAds.AD_SCENE_DESC));
                }
            }

            if (mRequest.getAdType() == AdFormat.REWARD_VIDEO) {
                mAd.setWindAdRewardListener(new WindAdRewardListener() {
                    @Override
                    public void onVideoAdRewarded(WindRewardInfo rewardInfo, String placementId) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mAd != null) {
                                    mAd.callBackDc("reward_callback", rewardInfo.getOptions());
                                }
                                if (windAdRewardListener != null) {
                                    SigmobLog.i("onVideoAdRewarded " + placementId);
                                    windAdRewardListener.onVideoAdRewarded(rewardInfo, placementId);
                                }
                            }
                        });
                    }
                });
            }

            mAd.show(loadAdRequest, new WindAdShowListener() {

                @Override
                public void onAdShow(final String placementId) {
                    mADStatus = AdStatusPlaying;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAd != null) {
                                mAd.callBackDc("show_callback", null);
                            }
                            if (windAdShowListener != null) {
                                SigmobLog.i("onVideoAdPlayStart " + "|" + placementId);
                                windAdShowListener.onAdShow(placementId);
                            }
                        }
                    });
                }

                @Override
                public void onVideoAdPlayComplete(final String placementId) {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (windAdShowListener != null) {
                                windAdShowListener.onVideoAdPlayComplete(placementId);
                            }
                        }
                    });
                }

                @Override
                public void onVideoAdPlayEnd(final String placementId) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (windAdShowListener != null) {
                                SigmobLog.i("onVideoAdPlayEnd " + "|" + placementId);
                                windAdShowListener.onVideoAdPlayEnd(placementId);
                            }
                        }
                    });
                }

                @Override
                public void onAdClicked(final String placementId) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAd != null) {
                                mAd.callBackDc("click_callback", null);
                            }

                            if (windAdShowListener != null) {
                                SigmobLog.i("onVideoAdClicked " + "|" + placementId);
                                windAdShowListener.onAdClicked(placementId);
                            }
                        }
                    });
                }

                @Override
                public void onAdClosed(final String placementId) {
                    mADStatus = AdStatusClose;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAd != null){
                                mAd.callBackDc("close_callback",null);
                            }

                            if (mPreloadAd != null) {
                                if (mAd != null) {
                                    mAd.destroy();
                                }
                                mAd = mPreloadAd;
                                mADStatus = AdStatusReady;
                                mPreloadAd = null;
                            }
                            if (windAdShowListener != null) {
                                SigmobLog.i("onVideoAdClosed " + placementId);
                                windAdShowListener.onAdClosed(placementId);
                            }


                        }
                    });
                }

                @Override
                public void onAdShowError(final WindAdError adError, final String placementId) {
                    mADStatus = AdStatusClose;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mPreloadAd != null) {
                                if (mAd != null) {
                                    mAd.destroy();
                                }
                                mAd = mPreloadAd;
                                mADStatus = AdStatusReady;
                                mPreloadAd = null;
                            }
                            if (windAdShowListener != null) {
                                SigmobLog.i("onVideoAdPlayError " + adError.toString() + "|" + placementId);
                                windAdShowListener.onAdShowError(adError, placementId);
                            }
                        }
                    });
                }

            });
        } catch (Throwable throwable) {
            PointEntitySigmobError windError = PointEntitySigmobError.SigmobError(PointCategory.ERROR, WindAdError.ERROR_SIGMOB_REQUEST.getErrorCode(), throwable.getMessage());
            windError.setAdtype(String.valueOf(AdFormat.REWARD_VIDEO));
            windError.setPlacement_id(getPlacementId());
            windError.commit();
            SigmobLog.e("show Ad ", throwable);
        }
        return true;
    }

    public boolean show(HashMap<String, String> options, final WindAdShowListener windAdShowListener) {

        return show(options, windAdShowListener, null);
    }


    private boolean intervalLoadAd() {

        try {

            boolean result = loadAdFilter();
            if (!result) return false;


            if (handleRewardVideoAdListener != null) {
                handleRewardVideoAdListener.destroy();
                handleRewardVideoAdListener = null;
            }
            handleRewardVideoAdListener = new HandleWindAdLoadListener(mWindAdLoadListener);

            if (mAd != null) {
                if (mAd.isReady() && mADStatus == AdStatusReady) {
                    handleRewardVideoAdListener.onAdPreLoadSuccess(getPlacementId());
                    handleRewardVideoAdListener.onAdLoadSuccess(getPlacementId());
                    return true;
                }
            } else {
                mAd = new VideoAdManager(isHalfInterstitial);
            }
            loadAdRequest = new LoadAdRequest(mRequest);

            loadAdRequest.setBidToken(getBid_token());
            loadAdRequest.setBidFloor(getBidFloor());
            loadAdRequest.setCurrency(getCurrency());

            if (mAd != null) {
                mAd.setRewardVideoAdListener(handleRewardVideoAdListener);
            }
            if (mADStatus == AdStatus.AdStatusPlaying) {
                if (mPreloadAd != null) {
                    if (mWindAdLoadListener != null) {
                        SigmobLog.i("onVideoAdLoadSuccess " + "|" + getPlacementId());
                        mWindAdLoadListener.onAdLoadSuccess(getPlacementId());
                    }
                } else {
                    mPreloadAd = new VideoAdManager(isHalfInterstitial);
                    mPreloadAd.setRewardVideoAdListener(handleRewardVideoAdListener);
                    mPreloadAd.loadAd(loadAdRequest);
                    sendRequestEvent();

                }

            } else {
                sendRequestEvent();

                mADStatus = AdStatusLoading;
                mAd.loadAd(loadAdRequest);

            }


        } catch (Throwable e) {
            PointEntitySigmobError windError = PointEntitySigmobError.SigmobError(PointCategory.ERROR, WindAdError.ERROR_SIGMOB_REQUEST.getErrorCode(), e.getMessage());
            windError.setAdtype(String.valueOf(AdFormat.REWARD_VIDEO));
            windError.setPlacement_id(getPlacementId());
            windError.commit();
            if (mWindAdLoadListener != null) {
                SigmobLog.i("onVideoAdLoadError " + "|" + getPlacementId());
                mWindAdLoadListener.onAdLoadError(WindAdError.ERROR_SIGMOB_REQUEST, getPlacementId());
            }
            return false;
        }
        return true;

    }

    @Override
    public boolean loadAd() {
        super.loadAd();
        return intervalLoadAd();
    }

    @Override
    public boolean loadAd(String token) {
        super.loadAd(token);
        return intervalLoadAd();
    }

    public void destroy() {
        SigmobLog.i(String.format("video ad %s is Destroy", mRequest != null ? mRequest.getPlacementId() : "null"));

        if (mAd != null) {
            mAd.destroy();
            mAd = null;
        }
        if (mPreloadAd != null) {
            mPreloadAd.destroy();
            mPreloadAd = null;
        }
        if (handleRewardVideoAdListener != null) {
            handleRewardVideoAdListener.destroy();
            handleRewardVideoAdListener = null;
        }
        mWindAdLoadListener = null;

    }

    @Override
    protected void onAdLoadFail(WindAdError error) {

        if (mADStatus != AdStatusLoading) {
            mADStatus = AdStatusNone;
        }


        if (mWindAdLoadListener != null) {
            mWindAdLoadListener.onAdPreLoadFail(error, getPlacementId());
            mWindAdLoadListener.onAdLoadError(error, getPlacementId());

        }


    }

    @Override
    public String getEcpm() {
        if (mAd != null) {
            return mAd.getEcpm();
        }
        return null;
    }

    @Override
    protected Map<String, BiddingResponse> getBidInfo() {
        if (mAd != null) {
            return mAd.getBidInfo();
        }
        return null;
    }

    @Override
    protected void doMacro(String key, String value) {
        if (mAd != null) {
            mAd.doMacro(key, value);
        }
    }

    private class HandleWindAdLoadListener implements WindAdLoadListener {

        private WindAdLoadListener mWindAdLoadListener;

        HandleWindAdLoadListener(WindAdLoadListener windAdLoadListener) {
            mWindAdLoadListener = windAdLoadListener;
        }

        public void destroy() {
            mWindAdLoadListener = null;
        }

        @Override
        public void onAdLoadSuccess(final String placementId) {

            SigmobLog.i("onVideoAdLoadSuccess " + "|" + placementId + "|" + mADStatus);

            if (mADStatus != AdStatusPlaying) {
                mADStatus = AdStatusReady;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (mWindAdLoadListener != null) {
                        mWindAdLoadListener.onAdLoadSuccess(placementId);
                    }
                }
            });
        }

        @Override
        public void onAdPreLoadSuccess(final String placementId) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (mWindAdLoadListener != null) {
                        SigmobLog.i("onVideoAdPreLoadSuccess " + "|" + placementId);
                        mWindAdLoadListener.onAdPreLoadSuccess(placementId);
                    }
                }
            });
        }

        @Override
        public void onAdPreLoadFail(final WindAdError error, final String placementId) {
            SigmobLog.i("onVideoAdPreLoadFail " + "|" + placementId + "|" + mADStatus +"|"+error.getErrorCode()+"|"+error.getMessage());

            if (mADStatus != AdStatusPlaying) {
                mADStatus = AdStatusNone;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mWindAdLoadListener != null) {
                        mWindAdLoadListener.onAdPreLoadFail(error, placementId);
                    }
                }
            });
        }


        @Override
        public void onAdLoadError(final WindAdError error, final String placementId) {
            SigmobLog.i("onVideoAdLoadError " + "|" + placementId + "|" + mADStatus+"|"+error.getErrorCode()+"|"+error.getMessage());
            if (mADStatus != AdStatusPlaying) {
                mADStatus = AdStatusNone;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mWindAdLoadListener != null) {
                        mWindAdLoadListener.onAdLoadError(error, placementId);
                    }
                }
            });
        }

    }
}

