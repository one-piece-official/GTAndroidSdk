package com.sigmob.sdk.rewardVideoAd;

public enum RequestSceneType {
    AppSWith(1),
    AutoNextPreload(2),
    NormalRequest(3),
    OtherRequest(4),
    SplashCloseRequest(5);


    private Integer mType;

    RequestSceneType(Integer type) {
        mType = type;
    }

    public Integer getValue() {
        return mType;
    }
}
