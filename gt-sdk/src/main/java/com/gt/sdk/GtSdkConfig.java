package com.gt.sdk;


import com.gt.sdk.api.GtCustomController;
import com.gt.sdk.api.GtInitCallback;

import java.util.Map;

public class GtSdkConfig {

    public boolean enableDebug;

    public String appId;

    public String appName;

    public String userId;

    public Map<String, String> customData;

    public GtCustomController gtCustomController;

    public GtInitCallback gtInitCallback;

    public String getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, String> getCustomData() {
        return customData;
    }

    public GtCustomController getGtCustomController() {
        return gtCustomController;
    }

    public GtInitCallback getGtInitCallback() {
        return gtInitCallback;
    }

    public boolean isEnableDebug() {
        return enableDebug;
    }

    private GtSdkConfig(Builder var1) {
        this.enableDebug = var1.enableDebug;
        this.appId = var1.appId;
        this.appName = var1.appName;
        this.userId = var1.userId;
        this.customData = var1.customData;
        this.gtCustomController = var1.gtCustomController;
        this.gtInitCallback = var1.gtInitCallback;
    }

    public static class Builder {

        private boolean enableDebug;

        private String appId;

        private String appName;

        private String userId;

        private Map<String, String> customData;

        private GtCustomController gtCustomController;

        private GtInitCallback gtInitCallback;

        public Builder() {
        }

        public Builder debug(boolean var1) {
            this.enableDebug = var1;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder appId(String var1) {
            this.appId = var1;
            return this;
        }

        public Builder appName(String var1) {
            this.appName = var1;
            return this;
        }

        public Builder addCustomData(Map<String, String> customData) {
            this.customData = customData;
            return this;
        }

        public Builder customController(GtCustomController var1) {
            this.gtCustomController = var1;
            return this;
        }

        public Builder setInitCallback(GtInitCallback var1) {
            this.gtInitCallback = var1;
            return this;
        }

        public GtSdkConfig build() {
            return new GtSdkConfig(this);
        }
    }
}
