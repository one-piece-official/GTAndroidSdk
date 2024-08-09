package com.gt.sdk.base;


import android.text.TextUtils;

import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.GtAdSdk;


public abstract class GtBaseAd {

    public AdStatus adStatus = AdStatus.AdStatusNone;

    protected AdRequest mAdRequest;

    protected GtBaseAd(AdRequest adRequest) {
        this.mAdRequest = adRequest;
    }

    public AdError loadAdFilter() {
        if (mAdRequest == null || TextUtils.isEmpty(mAdRequest.getCodeId())) {
            SigmobLog.e("PlacementId with AdRequest can't is null");
            return AdError.ERROR_AD_PLACEMENT_ID_EMPTY;
        }

        if (!GtAdSdk.sharedAds().isInit()) {
            SigmobLog.e("GtAdSdk not initialize");
            return AdError.ERROR_AD_NOT_INIT;
        }

        if (adStatus == AdStatus.AdStatusLoading) {
            SigmobLog.e("Ad is Loading");
            return AdError.ERROR_AD_LOAD_FAIL_LOADING;
        }
        return null;
    }

    protected void sendRequestEvent(AdRequest adRequest) {



    }

}
