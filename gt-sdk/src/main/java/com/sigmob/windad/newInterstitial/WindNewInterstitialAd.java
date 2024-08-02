package com.sigmob.windad.newInterstitial;

import com.sigmob.sdk.newInterstitial.WindInterstitialAd;
import com.sigmob.sdk.videoAd.WindAdLoadListener;
import com.sigmob.sdk.videoAd.WindAdShowListener;
import com.sigmob.windad.WindAdError;

import java.util.HashMap;


public class WindNewInterstitialAd extends WindInterstitialAd implements WindAdLoadListener, WindAdShowListener {

    protected WindNewInterstitialAdListener mWindNewInterstitialAdListener;

    public WindNewInterstitialAd(WindNewInterstitialAdRequest request) {
        super(request);
        setAdLoadListener(this);
    }

    public boolean loadAd() {
        return super.loadAd();
    }

    @Override
    public boolean loadAd(String bid_token) {
        return super.loadAd(bid_token);
    }

    public boolean show(HashMap<String, String> options) {
        return super.show(options, this);
    }

    public void setWindNewInterstitialAdListener(WindNewInterstitialAdListener windNewInterstitialAdListener) {
        mWindNewInterstitialAdListener = windNewInterstitialAdListener;
    }

    @Override
    public void destroy() {
        mWindNewInterstitialAdListener = null;
        super.destroy();
    }

    @Override
    public void onAdLoadSuccess(String placementId) {
        if (mWindNewInterstitialAdListener != null) {
            mWindNewInterstitialAdListener.onInterstitialAdLoadSuccess(placementId);
        }

    }

    @Override
    public void onAdPreLoadSuccess(String placementId) {
        if (mWindNewInterstitialAdListener != null) {
            mWindNewInterstitialAdListener.onInterstitialAdPreLoadSuccess(placementId);
        }
    }

    @Override
    public void onAdPreLoadFail(WindAdError error, String placementId) {
        if (mWindNewInterstitialAdListener != null) {
            mWindNewInterstitialAdListener.onInterstitialAdPreLoadFail(placementId);
        }
    }

    @Override
    public void onAdShow(String placementId) {
        if (mWindNewInterstitialAdListener != null) {
            mWindNewInterstitialAdListener.onInterstitialAdShow(placementId);
        }
    }

    @Override
    public void onVideoAdPlayComplete(String placementId) {

    }

    @Override
    public void onVideoAdPlayEnd(String placementId) {

    }


    @Override
    public void onAdClicked(String placementId) {
        if (mWindNewInterstitialAdListener != null) {
            mWindNewInterstitialAdListener.onInterstitialAdClicked(placementId);
        }
    }

    @Override
    public void onAdClosed(String placementId) {
        if (mWindNewInterstitialAdListener != null) {
            mWindNewInterstitialAdListener.onInterstitialAdClosed(placementId);
        }
    }
    

    @Override
    public void onAdLoadError(WindAdError error, String placementId) {
        if (mWindNewInterstitialAdListener != null) {
            mWindNewInterstitialAdListener.onInterstitialAdLoadError(error, placementId);
        }
    }

    @Override
    public void onAdShowError(WindAdError error, String placementId) {
        if (mWindNewInterstitialAdListener != null) {
            mWindNewInterstitialAdListener.onInterstitialAdShowError(error, placementId);
        }
    }
}

