package com.gt.sdk.base.models.point;


import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.mta.PointEntityBase;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.WindConstants;
import com.gt.sdk.manager.WindSDKConfig;
import com.gt.sdk.manager.PrivacyManager;
import com.gt.sdk.manager.DeviceContextManager;

import java.util.List;

public class GtPointEntityAd extends PointEntityBase {

    private String appId;
    private String code_id;
    private String custom_info;
    private String bid_floor;

    private String bidId;//本次响应标识 ID，用于日志和后续调试
    private String logId;//用于记录日志或行为追踪
    private String adId;//生成的素材校验 id
    private String impId;//对应请求中的 imp 的 id
    private String admId;//序号
    private String price;

    private String final_url;

    public String getBid_floor() {
        return bid_floor;
    }

    public void setBid_floor(String bid_floor) {
        this.bid_floor = bid_floor;
    }

    public void setFinal_url(String downloadUrl) {
        this.final_url = downloadUrl;
    }

    public String getFinal_url() {
        return final_url;
    }

    public String getCustom_info() {
        return custom_info;
    }

    public void setCustom_info(String custom_info) {
        this.custom_info = custom_info;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCode_id() {
        return code_id;
    }

    public void setCode_id(String code_id) {
        this.code_id = code_id;
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
        entityWind.setAd_type(adType);
        entityWind.setCode_id(placement_id);

        return entityWind;
    }

    @Override
    public boolean isAcTypeBlock() {

        if (!PrivacyManager.canCollectPersonalInformation()) {
            return true;
        }
        List<Integer> blackList = WindSDKConfig.getInstance().getLogBlackList();

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
        return WindConstants.SDK_VERSION;
    }

    @Override
    public DeviceContext getDeviceContext() {
        return DeviceContextManager.sharedInstance().getDeviceContext();
    }

}
