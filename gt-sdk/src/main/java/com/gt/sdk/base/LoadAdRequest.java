package com.gt.sdk.base;

import com.gt.sdk.AdRequest;

import java.io.Serializable;
import java.util.Map;

public class LoadAdRequest implements Serializable {

    private final String userId;
    private final String codeId;
    private final int adType;
    private String loadId;
    private int bidFloor;
    private int width;
    private int height;
    private Map<String, String> options;

    public LoadAdRequest(AdRequest adRequest, int adType) {
        this.userId = adRequest.getUserID();
        this.adType = adType;
        this.codeId = adRequest.getCodeId();
        this.width = adRequest.getWidth();
        this.height = adRequest.getHeight();
        this.bidFloor = adRequest.getBidFloor();
        this.options = adRequest.getExtOption();
    }

    public String getUserId() {
        return userId;
    }

    public String getCodeId() {
        return codeId;
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

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

}
