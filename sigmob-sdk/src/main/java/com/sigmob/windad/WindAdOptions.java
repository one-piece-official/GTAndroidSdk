package com.sigmob.windad;


import android.text.TextUtils;

import java.util.HashMap;

public class WindAdOptions {

    private String mAppId;

    private String mAppKey;
    private HashMap<String, String> extData;
    private WindCustomController mCustomController;

    public WindAdOptions(String appId, String appKey) {
        if (!TextUtils.isEmpty(appId)) {
            this.mAppId = appId.trim();
        }
        this.mAppKey = appKey;
    }


    public WindAdOptions setCustomController(WindCustomController customController) {
        this.mCustomController = customController;
        return this;
    }

    public WindAdOptions setExtData(HashMap<String, String> extData) {
        this.extData = extData;
        return this;
    }

    public HashMap<String, String> getExtData() {
        return extData;
    }

    public String getAppId() {

        return mAppId;
    }

    public String getAppKey() {
        return mAppKey;
    }

    public WindCustomController getCustomController() {
        return mCustomController;
    }

}
