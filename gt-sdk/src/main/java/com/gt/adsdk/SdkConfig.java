package com.gt.adsdk;


import com.gt.adsdk.api.GtCustomController;
import com.gt.adsdk.api.GtInitCallback;

public class SdkConfig {

    public boolean enableDebug;

    public String appId;

    public String appName;

    public boolean showNotification;

    public GtCustomController gtCustomController;

    public GtInitCallback gtInitCallback;

    private SdkConfig(Builder var1) {
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

        public SdkConfig build() {
            return new SdkConfig(this);
        }
    }
}
