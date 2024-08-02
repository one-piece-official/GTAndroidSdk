package com.sigmob.sdk.base.common;

import com.sigmob.sdk.base.models.BaseAdUnit;

public abstract class CustomEventInterstitial implements Interstitial {


    public interface CustomEventInterstitialListener {


//        void onInterstitialLoadStart(BaseAdUnit adUnit);
//
//        /*
//         * Your custom event subclass must call this method when it successfully loads an ad.
//         * Failure to do so will disrupt the mediation waterfall and cause future ad requests to
//         * stall.
//         */
//        void onInterstitialLoaded(BaseAdUnit adUnit);

        /*
         * Your custom event subclass must call this method when it fails to load an ad.
         * Failure to do so will disrupt the mediation waterfall and cause future ad requests to
         * stall.
         */
        void onInterstitialFailed(BaseAdUnit adUnit, String error);

        /*
         * Your custom event subclass should call this method when the interstitial ad is displayed.
         * This method is optional. However, if you call this method, you should ensure that
         * onInterstitialDismissed is called at a later time.
         */
        void onInterstitialShown(BaseAdUnit adUnit);

        /*
         * Your custom event subclass should call this method when a user taps on an interstitial
         * ad. This method is optional.
         */
        void onInterstitialClicked(BaseAdUnit adUnit);


        /*
         * Your custom event subclass should call this method when the interstitial ad is closed.
         * This method is optional.
         */
        void onInterstitialDismissed(BaseAdUnit adUnit);


        void onInterstitialVOpen(BaseAdUnit mAdUnit);
    }


    public interface LandPageViewEventInterstitialListener {

        void onLandPageShow();

        void onLandPageClose();
    }

    public interface APKStatusEventInterstitialListener {

        void onInterstitialDownloadAPKStart(boolean result,long downloadId);

        void onInterstitialDownloadAPKEnd(boolean result,long downloadId);

        void onInterstitialDownloadAPKPause(boolean result,long downloadId);

        void onInterstitialInstallAPKStart(boolean result);

        void onInterstitialInstallAPKEnd(boolean result);
    }
}
