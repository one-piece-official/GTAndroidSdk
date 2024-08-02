package com.sigmob.sdk.base.common;

public enum CreativeType {
    CreativeTypeVideo_Tar(1),            //由video、endcard资源包构成
    CreativeTypeImage(3),                //单张图片广告，一般由单张image_src构成
    CreativeTypeVideo_Html_Snippet(4),   //奖励视频广告，由video + html_snippet组成
    CreativeTypeVideo_Tar_Companion(5),   //由video+companionAds+endcard资源包构成
    CreativeTypeVideo_transparent_html(6),   //由video+companionAds+endcard资源包构成
    CreativeTypeVideo_EndCardURL(7),   //由video+companionAds+endcard资源包构成
    CreativeTypeSplashVideo(8),   //由video+companionAds+endcard资源包构成
    CreativeTypeMRAID(9),
    CreativeTypeMRAIDTWO(10),

    CreativeTypeNewInterstitial(16);

    private int mCreativeType;

    CreativeType(int type) {
        mCreativeType = type;
    }

    public int getCreativeType() {
        return mCreativeType;
    }
}
