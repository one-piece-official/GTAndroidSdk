package com.gt.sdk.natives;


import com.gt.sdk.base.models.BaseAdUnit;

public interface SigmobNativeAdRenderListener {

    BaseAdUnit getAdUnit();

    NativeAdData getNativeAdUnit();

    NativeAdConfig getAdConfig();

    SigAppInfoView getAppInfoView();
}
