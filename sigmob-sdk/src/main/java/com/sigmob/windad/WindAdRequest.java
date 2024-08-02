package com.sigmob.windad;

import android.text.TextUtils;

import com.czhj.sdk.common.Constants;
import com.sigmob.sdk.base.common.AdFormat;

import java.util.Map;

public class WindAdRequest {

    protected int adType;
    /**
     * placement Id
     */
    private String placementId;
    /**
     * user Id
     */
    private String userId;
    protected int adCount = 1;
    private String loadId;
    /**
     * request extend parameters
     */
    private Map<String, Object> options;
    private Map<String, Object> extOptions;

    public boolean isHalfInterstitial() {
        return isHalfInterstitial;
    }

    public void setHalfInterstitial(boolean halfInterstitial) {
        isHalfInterstitial = halfInterstitial;
    }

    private boolean isHalfInterstitial;



    protected WindAdRequest(String placementId, String userId, Map<String, Object> options) {
        this.placementId = placementId;
        this.userId = userId;
        this.options = options;
        this.adType = AdFormat.REWARD_VIDEO;
    }

    public static boolean isPlacementEmpty(WindAdRequest adRequest) {
        return adRequest == null || TextUtils.isEmpty(adRequest.getPlacementId());
    }

    public int getAdCount() {
        return adCount;
    }

    public String getLoadId() {
        if (extOptions != null) {
            Object id = extOptions.get(Constants.LOAD_ID);
            if (id instanceof String) {
                return (String) id;
            }
        }
        return null;
    }

    public String getAdxId(){
        if (extOptions != null) {
            Object id = extOptions.get(WindAds.ADX_ID);
            if (id instanceof String) {
                return (String) id;
            }
        }
        return null;
    }

    public void setExtOptions(Map<String, Object> extOptions) {
        this.extOptions = extOptions;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlacementId() {
        return placementId;
    }

    public boolean hasOptions() {
        return options != null;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public int getAdType() {
        return adType;
    }
}
