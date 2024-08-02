package com.sigmob.sdk.base.models;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.windad.Splash.WindSplashAdRequest;
import com.sigmob.windad.WindAdRequest;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardAdRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class LoadAdRequest implements Serializable {

    private final String mUserId;
    /**
     * placement Id
     */
    private final String mPlacementId;
    private final int mAdtype;
    private String ad_scene_desc;
    private String ad_scene_id;
    private String mLoadId;

    private Map<String, String> mOptions;
    private String last_crid;
    private String last_campid;

    private int request_scene_type;
    private boolean isExpired;
    private boolean enable_screen_lock_displayad;
    private String ad_scene;
    private boolean enable_keep_on;
    private int ad_count = 1;
    private String requestId;
    private String bidToken;
    private boolean disableAutoHideAd;
    private String adx_id;

    public boolean isHalfInterstitial() {
        return isHalfInterstitial;
    }

    public void setHalfInterstitial(boolean halfInterstitial) {
        isHalfInterstitial = halfInterstitial;
    }

    private boolean isHalfInterstitial;


    public LoadAdRequest(WindAdRequest adRequest) {
        this.mUserId = adRequest.getUserId();

        this.mLoadId = adRequest.getLoadId();
        this.adx_id = adRequest.getAdxId();
        SigmobLog.i("adx_id:" + adRequest.getAdxId());

        this.mAdtype = adRequest.getAdType();
        this.isHalfInterstitial = adRequest.isHalfInterstitial();
        this.mPlacementId = adRequest.getPlacementId();
        if (adRequest instanceof WindSplashAdRequest) {
            this.disableAutoHideAd = ((WindSplashAdRequest) adRequest).isDisableAutoHideAd();
        } else if (adRequest instanceof WindRewardAdRequest) {
            enable_keep_on = ((WindRewardAdRequest) adRequest).isEnableKeepOn();
            enable_screen_lock_displayad = ((WindRewardAdRequest) adRequest).isEnableScreenLockDisPlayAd();
        }  else if (adRequest instanceof WindNewInterstitialAdRequest) {
            enable_keep_on = ((WindNewInterstitialAdRequest) adRequest).isEnableKeepOn();
            enable_screen_lock_displayad = ((WindNewInterstitialAdRequest) adRequest).isEnableScreenLockDisPlayAd();
        }

        if (adRequest.hasOptions()) {

            this.mOptions = new HashMap<>();

            for (String key : adRequest.getOptions().keySet()) {

                Object valueObject = adRequest.getOptions().get(key);

                String value;
                if (valueObject instanceof String) {
                    value = (String) valueObject;
                } else {
                    value = valueObject != null ? valueObject.toString() : "";
                }
                mOptions.put(key, value);
            }
        }


    }
    public String getBidToken() {
        return bidToken;
    }

    public LoadAdRequest setBidToken(String bidToken) {
        this.bidToken = bidToken;
        return this;

    }

    public void setAd_count(int ad_count) {
        this.ad_count = ad_count;
    }

    public void setAd_scene_id(String ad_scene_id) {
        this.ad_scene_id = ad_scene_id;
    }

    public void setAd_scene_desc(String ad_scene_desc) {
        this.ad_scene_desc = ad_scene_desc;
    }

    public int getAdCount() {
        return ad_count;
    }


    public String getLastCampid() {
        return last_campid;
    }

    public LoadAdRequest setLastCampid(String last_campid) {
        this.last_campid = last_campid;
        return this;
    }

    public String getLastCrid() {
        return last_crid;
    }

    public LoadAdRequest setLastCrid(String last_crid) {
        this.last_crid = last_crid;
        return this;

    }

    public int getRequest_scene_type() {
        return request_scene_type;
    }

    public LoadAdRequest setRequest_scene_type(int request_scene_type) {
        this.request_scene_type = request_scene_type;
        return this;

    }

    public boolean isExpired() {
        return isExpired;
    }

    public LoadAdRequest setExpired(boolean expired) {
        isExpired = expired;
        return this;

    }

    public String getAdSceneId() {
        return ad_scene_id;
    }

    public String getAdSceneDesc() {
        return ad_scene_desc;
    }

    public boolean isEnable_keep_on() {
        return enable_keep_on;
    }


    public boolean isEnable_screen_lock_displayad() {
        return enable_screen_lock_displayad;
    }


    public String getLoadId() {
        return mLoadId;
    }

    public LoadAdRequest setLoadId(String load_id) {
        mLoadId = load_id;
        return this;
    }

    public String getUserId() {
        return mUserId;
    }

    public Map<String, String> getOptions() {
        return mOptions;
    }

    public void setOptions(Map<String, String> map) {
        if (mOptions == null) {
            mOptions = new HashMap<>();
        }
        mOptions.putAll(map);
    }

    public int getAdType() {
        return mAdtype;
    }

    public String getPlacementId() {
        return mPlacementId;
    }

    public String getRequestId() {
        return requestId;
    }

    public LoadAdRequest setRequestId(String request_id) {
        this.requestId = request_id;
        return this;
    }

    public boolean isDisableAutoHideAd() {
        return disableAutoHideAd;
    }

    public void setDisableAutoHideAd(boolean disableAutoHideAd) {
        this.disableAutoHideAd = disableAutoHideAd;
    }


    private int bidFloor;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private String currency;

    public void setBidFloor(int bidFloor) {
        this.bidFloor = bidFloor;
    }

    public int getBidFloor() {
        return bidFloor;
    }

    public void setAdx_id(String adxId) {
        this.adx_id = adxId;
        SigmobLog.i("new adx_id: " + adx_id);

    }

    public String getAdx_id() {
        return adx_id;
    }
}
