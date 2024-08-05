package com.gt.adsdk;


import com.gt.adsdk.api.GtCustomController;
import com.gt.adsdk.api.GtInitCallback;

public class GtSdkConfig {

    public boolean enableDebug;

    public String appId;

    public String appName;

    public boolean showNotification;

    public GtCustomController gtCustomController;

    public GtInitCallback gtInitCallback;

    public String getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }

    public boolean isShowNotification() {
        return showNotification;
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
        this.showNotification = var1.showNotification;
        this.gtCustomController = var1.gtCustomController;
        this.gtInitCallback = var1.gtInitCallback;
    }

    public static class Builder {

        private boolean enableDebug;

        private String appId;

        private String appName;

        private boolean showNotification = true;

        private GtCustomController gtCustomController;

        private GtInitCallback gtInitCallback;

        public Builder() {
        }

        public Builder debug(boolean var1) {
            this.enableDebug = var1;
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

        public Builder showNotification(boolean var1) {
            this.showNotification = var1;
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
