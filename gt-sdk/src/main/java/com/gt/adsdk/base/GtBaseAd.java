package com.gt.adsdk.base;

import static com.czhj.sdk.common.models.AdStatus.AdStatusLoading;
import static com.czhj.sdk.common.models.AdStatus.AdStatusNone;
import static com.czhj.sdk.common.models.AdStatus.AdStatusReady;

import android.text.TextUtils;


import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.adsdk.AdError;
import com.gt.adsdk.AdRequest;
import com.gt.adsdk.GtAdSdk;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmobRequest;
import com.sigmob.windad.WindAdRequest;


public abstract class GtBaseAd {

    public AdStatus adStatus = AdStatusNone;

    protected AdRequest mAdRequest;

    protected GtBaseAd(AdRequest adRequest) {
        this.mAdRequest = adRequest;
    }

    public boolean loadAdFilter() {
        AdError adError = null;

        if (mAdRequest == null || TextUtils.isEmpty(mAdRequest.getCodeId())) {
            SigmobLog.e("PlacementId with AdRequest can't is null");
            adError = AdError.ERROR_AD_PLACEMENT_ID_EMPTY;
        } else {
            if (!GtAdSdk.sharedAds().isInit()) {
                SigmobLog.e("GtAdSdk not initialize");
                adError = AdError.ERROR_AD_NOT_INIT;
            } else if (adStatus != AdStatusReady) {
                if (adStatus == AdStatusLoading) {
                    SigmobLog.e("Ad is Loading");
                    adError = AdError.ERROR_AD_LOAD_FAIL_LOADING;
                }
            }
        }

        if (adError != null) {
            onAdFilterLoadFail(adError);
            return false;
        }
        return true;
    }

    protected void sendRequestEvent(AdRequest adRequest) {

    }

    protected abstract void onAdFilterLoadFail(AdError adError);

}
