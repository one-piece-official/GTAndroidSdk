package com.sigmob.windad.rewardVideo;

import com.sigmob.sdk.base.WindVideoAd;
import com.sigmob.sdk.videoAd.WindAdLoadListener;
import com.sigmob.sdk.videoAd.WindAdRewardListener;
import com.sigmob.sdk.videoAd.WindAdShowListener;
import com.sigmob.windad.WindAdError;

import java.util.HashMap;


public class WindRewardVideoAd extends WindVideoAd implements WindAdLoadListener, WindAdShowListener, WindAdRewardListener {

    protected WindRewardVideoAdListener mWindRewardedVideoAdListener;

    public WindRewardVideoAd(WindRewardAdRequest request) {
        super(request, false);
        setAdLoadListener(this);
    }

    /**
     * load rewarded video Ad
     */
    public boolean loadAd() {
        return super.loadAd();
    }

    @Override
    public boolean loadAd(String bid_token) {
        return super.loadAd(bid_token);
    }

    public boolean show(HashMap<String, String> options) {

        return super.show(options, this, this);
    }

    public void setWindRewardVideoAdListener(WindRewardVideoAdListener windRewardedVideoAdListener) {
        mWindRewardedVideoAdListener = windRewardedVideoAdListener;
    }


    @Override
    public void destroy() {
        mWindRewardedVideoAdListener = null;
        super.destroy();
    }

    @Override
    public void onAdLoadSuccess(String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdLoadSuccess(placementId);
        }

    }

    @Override
    public void onAdPreLoadSuccess(String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdPreLoadSuccess(placementId);
        }
    }

    @Override
    public void onAdPreLoadFail(WindAdError error, String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdPreLoadFail(placementId);
        }
    }

    @Override
    public void onAdShow(String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdPlayStart(placementId);
        }
    }

    @Override
    public void onVideoAdPlayComplete(String placementId) {

    }

    @Override
    public void onVideoAdPlayEnd(String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdPlayEnd(placementId);
        }
    }

    @Override
    public void onAdClicked(String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdClicked(placementId);
        }
    }

    @Override
    public void onAdClosed(String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdClosed(placementId);
        }
    }

    @Override
    public void onAdLoadError(WindAdError error, String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdLoadError(error, placementId);
        }
    }

    @Override
    public void onAdShowError(WindAdError error, String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdPlayError(error, placementId);
        }
    }

    @Override
    public void onVideoAdRewarded(WindRewardInfo rewardInfo, String placementId) {
        if (mWindRewardedVideoAdListener != null) {
            mWindRewardedVideoAdListener.onRewardAdRewarded(rewardInfo, placementId);
        }
    }
}

