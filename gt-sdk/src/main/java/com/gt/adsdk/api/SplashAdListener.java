package com.gt.adsdk.api;

import com.gt.adsdk.AdError;

public interface SplashAdListener {

    /**
     * 开屏广告成功加载
     */
    void onSplashAdLoadSuccess(final String placementId);


    /**
     * 开屏广告加载失败
     */
    void onSplashAdLoadFail(final String placementId, final AdError error);

    /**
     * 开屏广告成功展示
     */
    void onSplashAdShow(final String placementId);

    /**
     * 开屏广告展示失败
     */
    void onSplashAdShowError(final String placementId, final AdError error);

    /**
     * 开屏广告被点击
     */

    void onSplashAdClick(final String placementId);

    /**
     * 开屏广告关闭
     */
    void onSplashAdClose(final String placementId);

}
