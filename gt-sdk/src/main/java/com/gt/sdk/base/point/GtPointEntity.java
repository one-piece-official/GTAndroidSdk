package com.gt.sdk.base.point;


import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.mta.PointEntityBase;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.GtConstants;
import com.gt.sdk.admanager.GtConfigManager;
import com.gt.sdk.admanager.PrivacyDataManager;
import com.gt.sdk.utils.DeviceContextManager;

import java.util.List;

public class GtPointEntity extends PointEntityBase {

    private String appId;
    private String placement_id;

    private String load_count;
    private String feed_cnt;

    private String offer_id;

    private String e_cpm;

    private String custom_info;

    private String ad_position_custom_info;

    public String getCustom_info() {
        return custom_info;
    }

    public void setCustom_info(String custom_info) {
        this.custom_info = custom_info;
    }

    public String getAd_position_custom_info() {
        return ad_position_custom_info;
    }

    public void setAd_position_custom_info(String position_custom_info) {
        this.ad_position_custom_info = position_custom_info;
    }

    public String getE_cpm() {
        return e_cpm;
    }

    public void setE_cpm(String e_cpm) {
        this.e_cpm = e_cpm;
    }

    public String getOffer_id() {
        return offer_id;
    }

    public void setOffer_id(String offer_id) {
        this.offer_id = offer_id;
    }

    public String getFeed_cnt() {
        return feed_cnt;
    }

    public void setFeed_cnt(String feed_cnt) {
        this.feed_cnt = feed_cnt;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPlacement_id() {
        return placement_id;
    }

    public void setPlacement_id(String placement_id) {
        this.placement_id = placement_id;
    }

    public String getLoad_count() {
        return load_count;
    }

    public void setLoad_count(String load_count) {
        this.load_count = load_count;
    }

    public static GtPointEntity WindTracking(String category, String placement_id, String adType) {
        GtPointEntity entityWind = new GtPointEntity();
        entityWind.setAc_type(PointType.GT_COMMON);
        entityWind.setCategory(category);
        entityWind.setAdType(adType);
        entityWind.setPlacement_id(placement_id);

        return entityWind;
    }

    @Override
    public boolean isAcTypeBlock() {

        if (!PrivacyDataManager.canCollectPersonalInformation()) {
            return true;
        }
        List<Integer> blackList = GtConfigManager.sharedInstance().getLogBlackList();

        for (Integer acType : blackList) {
            if (getAc_type().equals(String.valueOf(acType))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String appId() {
        return GtAdSdk.sharedAds().getAppId();
    }

    @Override
    public String getSdkVersion() {
        return GtConstants.SDK_VERSION;
    }

    @Override
    public DeviceContext getDeviceContext() {
        return DeviceContextManager.sharedInstance().getDeviceContext();
    }

}
