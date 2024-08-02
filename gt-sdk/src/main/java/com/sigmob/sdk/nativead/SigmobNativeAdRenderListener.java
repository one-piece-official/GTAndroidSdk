package com.sigmob.sdk.nativead;

import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.windad.natives.WindNativeAdData;

public interface SigmobNativeAdRenderListener {

    BaseAdUnit getAdUnit();

    WindNativeAdData getNativeAdUnit();

    NativeAdConfig getAdConfig();

    SigAppInfoView getAppInfoView();
}
