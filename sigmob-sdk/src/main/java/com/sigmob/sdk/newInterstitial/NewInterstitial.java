package com.sigmob.sdk.newInterstitial;

import android.os.Bundle;

import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.common.AdActivity;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.CreativeType;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.TransparentAdActivity;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.mraid.MraidInterstitial;
import com.sigmob.sdk.videoAd.BaseAdActivity;

import java.util.Map;

public class NewInterstitial extends MraidInterstitial {

    private NewInterstitialAdBroadcastReceiver mNewInterstitialAdBroadcastReceiver;

    protected NewInterstitial(CustomEventInterstitialListener customEventInterstitialListener) {
        super(customEventInterstitialListener);
    }

    private static boolean MRAID2TypeCheck(MaterialMeta material) {
        return material.creative_type == CreativeType.CreativeTypeMRAIDTWO.getCreativeType();
    }

    private static boolean NewInterstitialTypeCheck(MaterialMeta material) {
        return (material.creative_type == CreativeType.CreativeTypeNewInterstitial.getCreativeType()
                && material.template_id != 0);
    }

    public static boolean checkAdValid(BaseAdUnit baseAdUnit) {

        boolean adCheck = isCridValid(baseAdUnit);
        MaterialMeta material = baseAdUnit.getMaterial();
        boolean creativeCheck = MRAID2TypeCheck(material) || NewInterstitialTypeCheck(material);

        return adCheck && creativeCheck;
    }

    public boolean baseAdUnitValid(BaseAdUnit baseAdUnit) {

        boolean adCheck = super.baseAdUnitValid(baseAdUnit);
        MaterialMeta material = baseAdUnit.getMaterial();
        boolean creativeCheck = MRAID2TypeCheck(material) || NewInterstitialTypeCheck(material);

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
        if (mCustomEventInterstitialListener instanceof InterstitialListener) {
            mNewInterstitialAdBroadcastReceiver = new NewInterstitialAdBroadcastReceiver(baseAdUnit,
                    (InterstitialListener) mCustomEventInterstitialListener,
                    baseAdUnit.getUuid());
            mNewInterstitialAdBroadcastReceiver.register(mNewInterstitialAdBroadcastReceiver);
        }

        String ad_class = BaseAdActivity.NEW_INTERSTITIAL;
        if (baseAdUnit.getAd_type() == AdFormat.NEW_INTERSTITIAL) {
            if (baseAdUnit.getCreativeType() == CreativeType.CreativeTypeMRAIDTWO.getCreativeType()) {
                ad_class = BaseAdActivity.MRAID_TWO;
            } else if (baseAdUnit.getCreativeType() == CreativeType.CreativeTypeNewInterstitial.getCreativeType()) {
                ad_class = BaseAdActivity.NEW_INTERSTITIAL;
            }
        }
        String placementId = baseAdUnit.getAdslot_id();
        PointEntitySigmobUtils.SigmobTracking(PointCategory.VOPEN, null, baseAdUnit, null, null, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {

                if (pointEntityBase instanceof PointEntitySigmob){
                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;

                    Map<String, String> options = entitySigmob.getOptions();
                    if (options != null){
                        options.put("show_count", String.valueOf(AdStackManager.getShowCount(placementId)));
                        AdStackManager.cleanShowCount(placementId);
                    }
                }
            }
        });

        if (baseAdUnit.getMaterial().theme_data == 1 || baseAdUnit.getTemplateType() == 1) {
            AdActivity.startActivity(SDKContext.getApplicationContext(), TransparentAdActivity.class, baseAdUnit.getUuid(), option, ad_class);
        } else {
            AdActivity.startActivity(SDKContext.getApplicationContext(), AdActivity.class, baseAdUnit.getUuid(), option, ad_class);
        }
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


    public interface InterstitialListener extends CustomEventInterstitialListener {

        void onAdClose(BaseAdUnit adUnit);

        void onAdShowFail(BaseAdUnit adUnit, String message);

        void onAdSkip(BaseAdUnit adUnit);

        void onAdShow(BaseAdUnit adUnit);

    }

    @Override
    public void onInvalidate(BaseAdUnit baseAdUnit) {
        AdStackManager.cleanPlayAdUnit(baseAdUnit);
        super.onInvalidate(baseAdUnit);
    }
}
