package com.gt.sdk.base;

import com.gt.sdk.AdRequest;

import java.io.Serializable;
import java.util.Map;

public class LoadAdRequest implements Serializable {

    private final String userId;
    private final String placementId;
    private final int adType;
    private String loadId;
    private Map<String, String> options;
    private int bidFloor;
    private int width;
    private int height;

    private String bidId;//本次响应标识 ID，用于日志和后续调试
    private String logId;//用于记录日志或行为追踪
    private String adId;//生成的素材校验 id
    private String impId;//对应请求中的 imp 的 id
    private String admId;//序号


    public LoadAdRequest(AdRequest adRequest, int adType) {
        this.userId = adRequest.getUserID();
        this.adType = adType;
        this.placementId = adRequest.getCodeId();
        this.width = adRequest.getWidth();
        this.height = adRequest.getHeight();
        this.bidFloor = adRequest.getBidFloor();
        this.options = adRequest.getExtOption();
    }

    public String getUserId() {
        return userId;
    }

    public String getPlacementId() {
        return placementId;
    }

    public int getAdType() {
        return adType;
    }

    public String getLoadId() {
        return loadId;
    }

    public void setLoadId(String loadId) {
        this.loadId = loadId;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public int getBidFloor() {
        return bidFloor;
    }

    public void setBidFloor(int bidFloor) {
        this.bidFloor = bidFloor;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setBidId(String bidId) {

    }

    public String getBidId() {
        return bidId;
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

}
