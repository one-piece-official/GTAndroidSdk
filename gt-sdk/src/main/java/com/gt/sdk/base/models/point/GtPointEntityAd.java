package com.gt.sdk.base.models.point;


import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.mta.PointEntityBase;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.GtConstants;
import com.gt.sdk.admanager.GtConfigManager;
import com.gt.sdk.admanager.PrivacyDataManager;
import com.gt.sdk.utils.DeviceContextManager;

import java.util.List;

public class GtPointEntityAd extends PointEntityBase {

    private String appId;
    private String placement_id;
    private String load_count;
    private String feed_cnt;
    private String custom_info;
    private String custom_info_ad;

    private String bidId;//本次响应标识 ID，用于日志和后续调试
    private String logId;//用于记录日志或行为追踪
    private String adId;//生成的素材校验 id
    private String impId;//对应请求中的 imp 的 id
    private String admId;//序号
    private String price;

    public String getCustom_info() {
        return custom_info;
    }

    public void setCustom_info(String custom_info) {
        this.custom_info = custom_info;
    }

    public String getCustom_info_ad() {
        return custom_info_ad;
    }

    public void setCustom_info_ad(String position_custom_info) {
        this.custom_info_ad = position_custom_info;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
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

    public String getBidId() {
        return bidId;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getImpId() {
        return impId;
    }

    public void setImpId(String impId) {
        this.impId = impId;
    }

    public String getAdmId() {
        return admId;
    }

    public void setAdmId(String admId) {
        this.admId = admId;
    }

    public static GtPointEntityAd AdTracking(String category, String placement_id, String adType) {
        GtPointEntityAd entityWind = new GtPointEntityAd();
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
