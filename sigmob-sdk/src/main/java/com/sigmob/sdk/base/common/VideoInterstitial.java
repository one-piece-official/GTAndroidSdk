package com.sigmob.sdk.base.common;

import android.os.Bundle;
import android.text.TextUtils;

import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.mraid.MraidInterstitial;

public abstract class VideoInterstitial extends MraidInterstitial {

    protected VideoInterstitial(CustomEventInterstitialListener customEventInterstitialListener) {
        super(customEventInterstitialListener);
    }

    private static boolean tarTypeCheck(MaterialMeta material) {
        return material.creative_type == CreativeType.CreativeTypeVideo_Tar.getCreativeType()
                && !TextUtils.isEmpty(material.endcard_md5)
                && !TextUtils.isEmpty(material.endcard_url)
                && !TextUtils.isEmpty(material.video_url);
    }

    private static boolean htmlTypeCheck(MaterialMeta material) {
        return (material.creative_type == CreativeType.CreativeTypeVideo_Html_Snippet.getCreativeType()
                || material.creative_type == CreativeType.CreativeTypeVideo_transparent_html.getCreativeType())
                && material.html_snippet != null && material.html_snippet.size() > 10
                && !TextUtils.isEmpty(material.video_url);
    }

    private static boolean htmlURLTypeCheck(MaterialMeta material) {
        return material.creative_type == CreativeType.CreativeTypeVideo_EndCardURL.getCreativeType()
                && !TextUtils.isEmpty(material.html_url)
                && !TextUtils.isEmpty(material.video_url);

    }

    public static boolean checkAdValid(BaseAdUnit baseAdUnit) {

        boolean adCheck = isCridValid(baseAdUnit);
        MaterialMeta material = baseAdUnit.getMaterial();

        boolean creativeCheck;
        if (baseAdUnit.getCreativeType() == CreativeType.CreativeTypeMRAID.getCreativeType()) {
            creativeCheck = MRAIDTypeCheck(material);
        } else {
            creativeCheck = tarTypeCheck(material) || htmlTypeCheck(material) || htmlURLTypeCheck(material);
        }

        return adCheck && creativeCheck;
    }

    public boolean baseAdUnitValid(BaseAdUnit baseAdUnit) {

        boolean adCheck = super.baseAdUnitValid(baseAdUnit);
        MaterialMeta material = baseAdUnit.getMaterial();

        boolean creativeCheck;
        if (baseAdUnit.getCreativeType() == CreativeType.CreativeTypeMRAID.getCreativeType()) {
            creativeCheck = MRAIDTypeCheck(material);
        } else if (baseAdUnit.getCreativeType() == CreativeType.CreativeTypeMRAIDTWO.getCreativeType()) {
            creativeCheck = true;
        } else {
            creativeCheck = tarTypeCheck(material) || htmlTypeCheck(material) || htmlURLTypeCheck(material);
        }

        return adCheck && creativeCheck;
    }

    @Override
    protected void preloadAds(CustomEventInterstitialListener customEventInterstitialListener) {

        super.preloadAds(customEventInterstitialListener);
    }

    @Override
    public void showInterstitial(BaseAdUnit baseAdUnit, Bundle option) {
        AdStackManager.setPlayAdUnit(baseAdUnit);
        super.showInterstitial(baseAdUnit, option);
    }

//    @Override
//    public void updateSuccess(BaseAdUnit adUnit) {
//
//        if (mCustomEventInterstitialListener != null) {
//            mCustomEventInterstitialListener.onInterstitialLoaded(adUnit);
//        }
//    }
//
//    @Override
//    public void updateFailed(BaseAdUnit adUnit, WindAdError error, String message) {
//        if (mCustomEventInterstitialListener != null) {
//            mCustomEventInterstitialListener.onInterstitialFailed(adUnit, message);
//        }
//    }


    @Override
    public void onInvalidate(BaseAdUnit baseAdUnit) {
        AdStackManager.cleanPlayAdUnit(baseAdUnit);
        super.onInvalidate(baseAdUnit);
    }
}
