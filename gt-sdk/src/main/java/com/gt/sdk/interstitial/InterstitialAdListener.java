package com.gt.sdk.interstitial;


import com.gt.sdk.AdError;

public interface InterstitialAdListener {

    void onInterstitialAdLoadSuccess(final String codeId);

    void onInterstitialAdCacheSuccess(final String codeId);

    void onInterstitialAdShow(final String codeId);

    void onInterstitialAdClick(final String codeId);

    void onInterstitialAdClosed(final String codeId);

    void onInterstitialAdLoadError(final String codeId, final AdError error);

    void onInterstitialAdShowError(final String codeId, final AdError error);

}