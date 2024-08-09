package com.gt.sdk.base.common;

import android.os.Bundle;

import com.gt.sdk.base.models.BaseAdUnit;


public abstract class LoadAdsInterstitial extends CustomEventAd {

    protected abstract boolean baseAdUnitValid(BaseAdUnit adUnit);

    protected abstract void showInterstitial(BaseAdUnit baseAdUnit, Bundle option);

    protected abstract void onInvalidate(BaseAdUnit baseAdUnit);

}
