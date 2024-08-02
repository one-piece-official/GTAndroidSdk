package com.sigmob.sdk.base.models;

public class AppInfo {

    public final String sdkVersion;
    public final String appVersion;
    public final String os;


    public AppInfo(String sdkVersion, String appVersion, String os) {
        this.sdkVersion = sdkVersion;
        this.appVersion = appVersion;
        this.os = os;
    }

    @Override
    public String toString() {
        return "appInfo={" +
                "sdkVersion='" + sdkVersion + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", os='" + os + '\'' +
                '}';
    }
}
