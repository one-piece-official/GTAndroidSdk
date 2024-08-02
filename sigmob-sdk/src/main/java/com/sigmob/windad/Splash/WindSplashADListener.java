package com.sigmob.windad.Splash;

import com.sigmob.windad.WindAdError;

public interface WindSplashADListener {
    /**
     * 开屏广告成功展示
     */
    void onSplashAdShow(final String placementId);

    /**
     * 开屏广告成功加载
     */
    void onSplashAdLoadSuccess(final String placementId);


    /**
     * 开屏广告加载失败
     */
    void onSplashAdLoadFail(final WindAdError error, final String placementId);


    /**
     * 开屏广告展示失败
     */
    void onSplashAdShowError(final WindAdError error,final String placementId);

    /**
     * 开屏广告被点击
     */

    void onSplashAdClick(final String placementId);


    /**
     * 开屏广告关闭
     */
    void onSplashAdClose(final String placementId);


    void onSplashAdSkip(final String placementId);
}
