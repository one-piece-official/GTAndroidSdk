package com.sigmob.sdk.videoAd;

import android.os.Bundle;

import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.common.AdActivity;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.CreativeType;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.TransparentAdActivity;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;

import java.util.Map;

public class VideoInterstitial extends com.sigmob.sdk.base.common.VideoInterstitial {

    private RewardVideoAdBroadcastReceiver mRewardVideoBroadcastReceiver;
    private boolean isHalfInterstitial;

    public VideoInterstitial(CustomEventInterstitialListener customEventInterstitialListener, boolean isHalfInterstitial) {
        super(customEventInterstitialListener);
        this.isHalfInterstitial = isHalfInterstitial;
    }

    @Override
    public void loadInterstitial(
            Map<String, Object> localExtras,
            BaseAdUnit adUnit) {
        super.loadInterstitial(localExtras, adUnit);

    }

    @Override
    public void showInterstitial(BaseAdUnit baseAdUnit, Bundle option) {
        super.showInterstitial(baseAdUnit, option);

        if (mCustomEventInterstitialListener instanceof VideoInterstitialListener) {
            mRewardVideoBroadcastReceiver = new RewardVideoAdBroadcastReceiver(baseAdUnit,
                    (VideoInterstitialListener) mCustomEventInterstitialListener,
                    baseAdUnit.getUuid());
            mRewardVideoBroadcastReceiver.register(mRewardVideoBroadcastReceiver);
        }

        String ad_class = BaseAdActivity.REWARD;
        if (baseAdUnit.getAd_type() == AdFormat.REWARD_VIDEO || baseAdUnit.getAd_type() == AdFormat.FULLSCREEN_VIDEO) {
            if (baseAdUnit.getCreativeType() == CreativeType.CreativeTypeMRAID.getCreativeType()) {
                ad_class = BaseAdActivity.MRAID;
            } else if (baseAdUnit.getCreativeType() == CreativeType.CreativeTypeMRAIDTWO.getCreativeType()) {
                ad_class = BaseAdActivity.MRAID_TWO;
            }
        }

        if (option != null) {
            option.putBoolean(WindConstants.IS_HALF_INTERSTITIAL, isHalfInterstitial);
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

        if (baseAdUnit.getAd_type() == AdFormat.FULLSCREEN_VIDEO && (baseAdUnit.getMaterial().theme_data == 1 || isHalfInterstitial)
                || baseAdUnit.getCreativeType() == CreativeType.CreativeTypeMRAIDTWO.getCreativeType()
                && (baseAdUnit.getMaterial().theme_data == 1 || baseAdUnit.getTemplateType() == 1)) {
            AdActivity.startActivity(SDKContext.getApplicationContext(), TransparentAdActivity.class, baseAdUnit.getUuid(), option, ad_class);
        } else {
            AdActivity.startActivity(SDKContext.getApplicationContext(), AdActivity.class, baseAdUnit.getUuid(), option, ad_class);
        }
    }

    @Override
    public void onInvalidate(BaseAdUnit baseAdUnit) {
        if (mRewardVideoBroadcastReceiver != null) {
            mRewardVideoBroadcastReceiver.unregister(mRewardVideoBroadcastReceiver);
            mRewardVideoBroadcastReceiver = null;
        }
        super.onInvalidate(baseAdUnit);

    }


    public interface VideoInterstitialListener extends CustomEventInterstitialListener {
        void onVideoComplete(BaseAdUnit adUnit);

        void onVideoClose(BaseAdUnit adUnit);

        void onVideoPlayFail(BaseAdUnit adUnit, String message);

        void onVideoSkip(BaseAdUnit adUnit);

        void onVideoPlay(BaseAdUnit adUnit);

    }
}
