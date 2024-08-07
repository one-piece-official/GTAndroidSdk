package com.gt.sdk.utils;

import android.text.TextUtils;

import com.czhj.sdk.common.track.AdTracker;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.VolleyError;
import com.gt.sdk.AdError;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.GtSdkConfig;
import com.gt.sdk.api.GtCustomController;
import com.gt.sdk.base.BaseAdUnit;
import com.gt.sdk.base.LoadAdRequest;
import com.gt.sdk.base.point.GtPointEntityAd;
import com.gt.sdk.base.point.GtPointEntityAdError;
import com.gt.sdk.base.point.PointType;


import org.json.JSONObject;

public class PointEntityUtils {

    public static void GtTracking(String category, String sub_category, BaseAdUnit adUnit) {
        GtTracking(category, sub_category, adUnit, null);
    }

    public static void GtTracking(String category, String sub_category, BaseAdUnit adUnit, OnPointEntityExtraInfo onPointEntityExtraInfo) {
        GtTracking(category, sub_category, adUnit, null, onPointEntityExtraInfo);
    }

    public static void GtTracking(String category, String sub_category, LoadAdRequest adRequest) {
        GtTracking(category, sub_category, null, adRequest, null);
    }

    public static void GtTracking(String category, String sub_category, LoadAdRequest adRequest, OnPointEntityExtraInfo onPointEntityExtraInfo) {
        GtTracking(category, sub_category, null, adRequest, onPointEntityExtraInfo);
    }

    public static void GtTracking(String category, String sub_category, BaseAdUnit adUnit, LoadAdRequest adRequest, OnPointEntityExtraInfo onPointEntityExtraInfo) {

        GtPointEntityAd pointEntity = new GtPointEntityAd();
        pointEntity.setAc_type(PointType.GT_COMMON);
        pointEntity.setCategory(category);
        pointEntity.setSub_category(sub_category);

        updateADunitInfo(adUnit, pointEntity);

        updateLoadRequestInfo(adRequest, pointEntity);

        if (onPointEntityExtraInfo != null) {
            onPointEntityExtraInfo.onAddExtra(pointEntity);
        }

        pointEntity.commit();
    }

    public static void eventRecord(String category, final String sub_category, BaseAdUnit adUnit) {
        PointEntityUtils.GtTracking(category, sub_category, adUnit, null);
    }

    public static void updateADunitInfo(BaseAdUnit adUnit, GtPointEntityAd pointEntity) {
        if (adUnit != null) {
            try {
                pointEntity.setAdType(String.valueOf(adUnit.getAd_type()));
                pointEntity.setCampaign_id(adUnit.getCamp_id());
                pointEntity.setCreative_id(adUnit.getCrid());
                pointEntity.setRequest_id(adUnit.getRequestId());
                pointEntity.setPlacement_id(adUnit.getAdslot_id());
                pointEntity.setLoad_id(adUnit.getLoad_id());
                pointEntity.setVid(adUnit.getVid());
                pointEntity.setScene_id(adUnit.getAd_scene_id());
                pointEntity.setScene_desc(adUnit.getAd_scene_desc());
                pointEntity.setPlay_mode(String.valueOf(adUnit.getPlayMode()));
                pointEntity.setCreative_type(String.valueOf(adUnit.getCreativeType()));
                pointEntity.setPrice(adUnit.getAd().settlement_price_enc);
            } catch (Throwable ignored) {

            }
        }
    }

    public static void GtError(String category, AdError adError, BaseAdUnit adUnit) {
        GtError(category, null, adError.getErrorCode(), adError.getMessage(), null, adUnit, null);
    }

    public static void GtError(String category, AdError adError, BaseAdUnit adUnit, LoadAdRequest adRequest) {
        GtError(category, null, adError.getErrorCode(), adError.getMessage(), adRequest, adUnit, null);
    }

    public static void GtError(String category, String sub_category, int error_code, String error_msg, LoadAdRequest loadAdRequest, BaseAdUnit adUnit, OnPointEntityExtraInfo onPointEntityExtraInfo) {

        GtPointEntityAdError pointEntityError = GtPointEntityAdError.GTError(category, "", error_code, error_msg);

        pointEntityError.setSub_category(sub_category);

        updateLoadRequestInfo(loadAdRequest, pointEntityError);

        updateADunitInfo(adUnit, pointEntityError);

        if (onPointEntityExtraInfo != null) {
            onPointEntityExtraInfo.onAddExtra(pointEntityError);
        }

        pointEntityError.commit();
    }

    public static void updateLoadRequestInfo(LoadAdRequest adRequest, GtPointEntityAd pointEntity) {

        if (pointEntity != null && adRequest != null) {

            pointEntity.setPlacement_id(adRequest.getPlacementId());

            if (!TextUtils.isEmpty(adRequest.getLoadId())) {
                pointEntity.setLoad_id(adRequest.getLoadId());
            }

            pointEntity.setAdType(String.valueOf(adRequest.getAdType()));

            if (adRequest.getOptions() != null) {
                JSONObject jsonObject = new JSONObject(adRequest.getOptions());
                pointEntity.setCustom_info_ad(jsonObject.toString());
            }
        }
    }


    public static void eventTracking(AdTracker tracker, String url, BaseAdUnit adUnit, final VolleyError volleyError) {
        NetworkResponse response = null;
        if (volleyError != null) {
            response = volleyError.networkResponse;
        }
        if (response != null) {
            eventTracking(tracker, url, adUnit, response, null);
        } else {
            eventTracking(tracker, url, adUnit, response, new OnPointEntityExtraInfo() {
                @Override
                public void onAddExtra(Object pointEntityBase) {
                }
            });
        }
    }

    public static void updatePointEntity(GtPointEntityAd pointEntity) {
        GtSdkConfig sdkConfig = GtAdSdk.sharedAds().getSdkConfig();
        if (sdkConfig != null && sdkConfig.getGtCustomController() != null) {
            GtCustomController controller = sdkConfig.getGtCustomController();
            pointEntity.setIs_custom_imei(controller.canUsePhoneState() ? "0" : "1");
            pointEntity.setIs_custom_android_id(controller.canUseAndroidId() ? "0" : "1");
            pointEntity.setIs_custom_oaid(TextUtils.isEmpty(controller.getOaid()) ? "0" : "1");
        }
    }

    public static void eventTracking(final AdTracker tracker, final String url, BaseAdUnit adUnit, final NetworkResponse response, final OnPointEntityExtraInfo onPointEntityExtraInfo) {

    }

    public interface OnPointEntityExtraInfo {
        void onAddExtra(Object pointEntityBase);
    }

}
