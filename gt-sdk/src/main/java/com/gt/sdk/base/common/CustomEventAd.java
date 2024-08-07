package com.gt.sdk.base.common;

import com.gt.sdk.base.BaseAdUnit;

public abstract class CustomEventAd {

    public interface CustomAdListener {

        void onAdShowFailed(BaseAdUnit adUnit, String error);

        /*
         * Your custom event subclass should call this method when the interstitial ad is displayed.
         * This method is optional. However, if you call this method, you should ensure that
         * onInterstitialDismissed is called at a later time.
         */
        void onAdShow(BaseAdUnit adUnit);

        /*
         * Your custom event subclass should call this method when a user taps on an interstitial
         * ad. This method is optional.
         */
        void onAdClicked(BaseAdUnit adUnit);

        /*
         * Your custom event subclass should call this method when the interstitial ad is closed.
         * This method is optional.
         */
        void onAdClose(BaseAdUnit adUnit);

    }


    public interface LandPageViewEventAdListener {

        void onLandPageShow();

        void onLandPageClose();
    }

    public interface APKStatusEventAdListener {

        void onInterstitialDownloadAPKStart(boolean result, long downloadId);

        void onInterstitialDownloadAPKEnd(boolean result, long downloadId);

        void onInterstitialDownloadAPKPause(boolean result, long downloadId);

        void onInterstitialInstallAPKStart(boolean result);

        void onInterstitialInstallAPKEnd(boolean result);
    }
}
