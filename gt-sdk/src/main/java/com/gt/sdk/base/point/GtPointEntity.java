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
    private String gdpr_filters;
    private String loading_filters;
    private String playing_filters;
    private String interval_filters;
    private String init_filters;

    private String load_type;//0:实时播放 1:预加载播放

    private String feed_cnt;

    private String ad_cnt;

    private String offer_id;

    private String e_cpm;

    private String currency;

    private String custom_info;

    private String ad_position_custom_info;

    private String is_custom_imei;
    private String is_custom_android_id;
    private String is_custom_oaid;

    private String event_type;

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getIs_custom_imei() {
        return is_custom_imei;
    }

    public void setIs_custom_imei(String is_custom_imei) {
        this.is_custom_imei = is_custom_imei;
    }

    public String getIs_custom_android_id() {
        return is_custom_android_id;
    }

    public void setIs_custom_android_id(String is_custom_android_id) {
        this.is_custom_android_id = is_custom_android_id;
    }

    public String getIs_custom_oaid() {
        return is_custom_oaid;
    }

    public void setIs_custom_oaid(String is_custom_oaid) {
        this.is_custom_oaid = is_custom_oaid;
    }

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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public String getAd_cnt() {
        return ad_cnt;
    }

    public void setAd_cnt(String ad_cnt) {
        this.ad_cnt = ad_cnt;
    }

    public String getFeed_cnt() {
        return feed_cnt;
    }

    public void setFeed_cnt(String feed_cnt) {
        this.feed_cnt = feed_cnt;
    }

    public String getLoad_type() {
        return load_type;
    }

    public void setLoad_type(String load_type) {
        this.load_type = load_type;
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

    public String getGdpr_filters() {
        return gdpr_filters;
    }

    public void setGdpr_filters(String gdpr_filters) {
        this.gdpr_filters = gdpr_filters;
    }

    public String getLoading_filters() {
        return loading_filters;
    }

    public void setLoading_filters(String loading_filters) {
        this.loading_filters = loading_filters;
    }

    public String getPlaying_filters() {
        return playing_filters;
    }

    public void setPlaying_filters(String playing_filters) {
        this.playing_filters = playing_filters;
    }

    public String getInterval_filters() {
        return interval_filters;
    }

    public void setInterval_filters(String interval_filters) {
        this.interval_filters = interval_filters;
    }

    public String getInit_filters() {
        return init_filters;
    }

    public void setInit_filters(String init_filters) {
        this.init_filters = init_filters;
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
