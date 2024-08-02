package com.sigmob.sdk.videoAd;

import com.sigmob.windad.WindAdError;

public interface WindAdLoadListener {


    /**
     * 广告请求成功
     *
     * @param placementId
     */
    void onAdPreLoadSuccess(String placementId);

    /**
     * 加载成功
     *
     * @param placementId
     */
    void onAdLoadSuccess(String placementId);


    /**
     * 广告请求失败
     *
     * @param placementId
     */
    void onAdPreLoadFail(WindAdError error, String placementId);


    /**
     * 加载广告错误回调
     *
     * @param error
     * @param placementId
     */
    void onAdLoadError(WindAdError error, String placementId);

}

