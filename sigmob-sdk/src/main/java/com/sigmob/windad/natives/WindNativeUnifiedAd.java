package com.sigmob.windad.natives;


import android.os.Handler;
import android.os.Looper;

import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.WindBaseAd;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.common.LoadCacheItem;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmobError;
import com.sigmob.sdk.nativead.SigmobNativeAd;
import com.sigmob.sdk.nativead.SigmobNativeAdLoadListener;
import com.sigmob.windad.WindAdError;

import java.util.List;
import java.util.Map;


public class WindNativeUnifiedAd extends WindBaseAd {

    private WindNativeAdLoadListener nativeAdLoadListener;
    private Handler mHandler;
    private SigmobNativeAd mAdManager;


    public WindNativeUnifiedAd(final WindNativeAdRequest request) {

        super(request, false);
        this.mHandler = new Handler(Looper.getMainLooper());

        mAdManager = new SigmobNativeAd(request, new SigmobNativeAdLoadListener() {
            @Override
            public void onNativeAdLoaded(List<WindNativeAdData> adUnits) {
                mADStatus = AdStatus.AdStatusNone;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (nativeAdLoadListener != null) {
                            nativeAdLoadListener.onAdLoad(adUnits, getPlacementId());
                        }
                    }
                });


            }

            @Override
            public void onNativeAdLoadFail(int error_code, String error_message) {
                mADStatus = AdStatus.AdStatusNone;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (nativeAdLoadListener != null) {
                            WindAdError windAdError = WindAdError.getWindAdError(error_code);
                            if (windAdError == null) {
                                windAdError = WindAdError.ERROR_SIGMOB_REQUEST;
                                windAdError.setErrorMessage(error_code, error_message);
                                windAdError.setMessage(error_message);
                            }

                            nativeAdLoadListener.onAdError(windAdError, request.getPlacementId());
                        }
                    }
                });

            }
        });


    }

    public void setNativeAdLoadListener(WindNativeAdLoadListener nativeAdLoadListener) {
        this.nativeAdLoadListener = nativeAdLoadListener;
    }

    /**
     * load rewarded video Ad
     */
    @Deprecated
    public boolean loadAd() {
        try {
            super.loadAd();
            return privateLoadAd(mRequest.getAdCount());
        } catch (Throwable e) {
            PointEntitySigmobError windError = PointEntitySigmobError.SigmobError(PointCategory.ERROR, WindAdError.ERROR_SIGMOB_REQUEST.getErrorCode(), e.getMessage());
            windError.setAdtype(String.valueOf(AdFormat.UNIFIED_NATIVE));
            windError.setPlacement_id(getPlacementId());
            windError.commit();
            onAdLoadFail(WindAdError.ERROR_SIGMOB_REQUEST);
            return false;
        }
    }

    /**
     * load rewarded video Ad
     */
    public boolean loadAd(int ad_count) {
        try {
            super.loadAd();
            return privateLoadAd(ad_count);
        } catch (Throwable e) {
            PointEntitySigmobError windError = PointEntitySigmobError.SigmobError(PointCategory.ERROR, WindAdError.ERROR_SIGMOB_REQUEST.getErrorCode(), e.getMessage());
            windError.setAdtype(String.valueOf(AdFormat.UNIFIED_NATIVE));
            windError.setPlacement_id(getPlacementId());
            windError.commit();
            onAdLoadFail(WindAdError.ERROR_SIGMOB_REQUEST);
            return false;
        }
    }

    private boolean privateLoadAd(int ad_count) {
        try {

            boolean result = loadAdFilter();
            if (!result) {
                return false;
            }

            mADStatus = AdStatus.AdStatusLoading;
            //加载自渲染广告
            int type =  mAdManager.loadAd(ad_count, getBid_token(), getBidFloor(), getCurrency());
            if (type == 1){
                LoadCacheItem loadCacheItem = LoadCacheItem.loadCacheItem(getPlacementId());

                if (loadCacheItem != null) {
                    sendRequestEvent( loadCacheItem);
                }else{
                    sendRequestEvent();

                }
            }
            return true;
        } catch (Throwable throwable) {
            SigmobLog.i(this.getClass().getSimpleName() + " catch throwable " + throwable);
            if (nativeAdLoadListener != null) {
                WindAdError adError = WindAdError.ERROR_SIGMOB_REQUEST;
                adError.setMessage(throwable.getMessage());
                nativeAdLoadListener.onAdError(adError, getPlacementId());
            }
        }

        return false;
    }

    @Override
    public boolean loadAd(String bid_token) {
        try {

            super.loadAd(bid_token);
            int adcount = mRequest.getAdCount() > 0 ? mRequest.getAdCount() : 1;
            return privateLoadAd(adcount);
        } catch (Throwable e) {
            PointEntitySigmobError windError = PointEntitySigmobError.SigmobError(PointCategory.ERROR, WindAdError.ERROR_SIGMOB_REQUEST.getErrorCode(), e.getMessage());
            windError.setAdtype(String.valueOf(AdFormat.UNIFIED_NATIVE));
            windError.setPlacement_id(getPlacementId());
            windError.commit();
            onAdLoadFail(WindAdError.ERROR_SIGMOB_REQUEST);
            return false;
        }
    }

    public boolean loadAd(String bid_token, int adCount) {
        try {

            super.loadAd(bid_token);
            return privateLoadAd(adCount);
        } catch (Throwable e) {
            PointEntitySigmobError windError = PointEntitySigmobError.SigmobError(PointCategory.ERROR, WindAdError.ERROR_SIGMOB_REQUEST.getErrorCode(), e.getMessage());
            windError.setAdtype(String.valueOf(AdFormat.UNIFIED_NATIVE));
            windError.setPlacement_id(getPlacementId());
            windError.commit();
            onAdLoadFail(WindAdError.ERROR_SIGMOB_REQUEST);
            return false;
        }
    }


    public void destroy() {
        SigmobLog.i(String.format("native ad  %s is Destroy", mRequest != null ? mRequest.getPlacementId() : "null"));

        nativeAdLoadListener = null;
    }

    @Override
    protected void onAdLoadFail(final WindAdError error) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mADStatus = AdStatus.AdStatusNone;

                if (nativeAdLoadListener != null) {
                    SigmobLog.i("onVideoAdLoadFail " + error.toString() + "|" + getPlacementId());
                    nativeAdLoadListener.onAdError(error, getPlacementId());
                }
            }
        });
    }

    @Override
    public String getEcpm() {
        if (mAdManager != null) {
            return mAdManager.getEcpm();
        }
        return null;
    }

    @Override
    protected Map<String, BiddingResponse> getBidInfo() {
        if (mAdManager != null) {
            return mAdManager.getBidInfo();
        }
        return null;
    }

    @Override
    protected void doMacro(String key, String value) {
        if (mAdManager != null) {
            mAdManager.doMacro(key, value);
        }
    }

    public interface WindNativeAdLoadListener {

        void onAdError(final WindAdError error, final String placementId);

        void onAdLoad(final List<WindNativeAdData> adDataList, final String placementId);
    }
}

