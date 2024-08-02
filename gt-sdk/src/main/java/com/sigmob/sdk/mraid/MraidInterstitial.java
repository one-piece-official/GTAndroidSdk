package com.sigmob.sdk.mraid;

import android.os.Bundle;
import android.text.TextUtils;

import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.CreativeType;
import com.sigmob.sdk.base.common.LoadAdsInterstitial;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.mraid2.MRAID2ControllerCache;
import com.sigmob.sdk.mraid2.Mraid2Controller;

import java.util.List;
import java.util.Map;

public class MraidInterstitial extends LoadAdsInterstitial {


    protected MraidInterstitial(CustomEventInterstitialListener customEventInterstitialListener) {
        super(customEventInterstitialListener);
    }

    protected static boolean MRAIDTypeCheck(MaterialMeta material) {
        return (material.creative_type == CreativeType.CreativeTypeMRAID.getCreativeType() &&
                (!TextUtils.isEmpty(material.html_url) || material.html_snippet != null && material.html_snippet.size() > 10));
    }

    public static boolean isCridValid(BaseAdUnit adUnit) {
        MaterialMeta material = adUnit.getMaterial();

        if (material == null || TextUtils.isEmpty(adUnit.getCrid()))
            return false;
        return true;
    }

    protected boolean baseAdUnitValid(BaseAdUnit adUnit) {

        return isCridValid(adUnit);
    }

    @Override
    public void loadInterstitial(
            Map<String, Object> localExtras,
            BaseAdUnit adUnit) {
        super.loadInterstitial(localExtras, adUnit);

    }


    @Override
    public void showInterstitial(BaseAdUnit adUnit, Bundle option) {
        if (adUnit.getCreativeType() == CreativeType.CreativeTypeMRAIDTWO.getCreativeType()) {
            List<BaseAdUnit> adUnitList = AdStackManager.getAdUnitList(adUnit.getUuid());

            if (adUnitList == null) {
                MRAID2ControllerCache.getInstance().addController(adUnit.getUuid(), new Mraid2Controller(SDKContext.getApplicationContext(), adUnitList));

            }
        }
        super.showInterstitial(adUnit, option);
    }

    @Override
    protected void preloadAds(CustomEventInterstitialListener customEventInterstitialListener) {
        mCustomEventInterstitialListener = customEventInterstitialListener;

//        if (mCustomEventInterstitialListener != null) {
//            mCustomEventInterstitialListener.onInterstitialLoadStart(mLoadAdUnit);
//        }

    }

    @Override
    public void onInvalidate(BaseAdUnit baseAdUnit) {
        super.onInvalidate(baseAdUnit);

    }
}
