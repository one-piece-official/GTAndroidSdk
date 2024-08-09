package com.gt.sdk.natives;

import android.content.Context;

import com.gt.sdk.base.common.BaseAdConfig;
import com.gt.sdk.base.models.BaseAdUnit;


public class NativeAdConfig extends BaseAdConfig {

    private int video_auto_play; // 播放器自动控制：0-总是；1-wifi；2-不自动
    private int preview_page_video_mute; // 预览页播放器静音控制：0-静音；1-不静音
    private int detail_page_video_mute; // 详情页播放器静音控制：0-不静音；1-静音
    private int impression_percent; // 广告有效曝光定义-曝光像素百分比
    private int impression_time; // 广告有效曝光定义-曝光持续时间
    private int end_impression_time; //针对view-through-ad
    private boolean use_na_video_component;
    private Boolean isVideoMute;

    public static NativeAdConfig getAdConfig(BaseAdUnit adUnit) {
        return new NativeAdConfig(adUnit);
    }

    protected NativeAdConfig(BaseAdUnit adUnit) {
        super(adUnit);
    }

    public boolean isUse_na_video_component() {
        return use_na_video_component;
    }

    public int getVideo_auto_play() {
        return video_auto_play;
    }

    public boolean getPreview_page_video_mute() {
        return isVideoMute != null ? isVideoMute : preview_page_video_mute == 0;
    }

    public boolean getDetail_page_video_mute() {
        return detail_page_video_mute != 0;
    }

    public int getImpression_percent() {
        return impression_percent;
    }

    public int getImpression_time() {
        return impression_time;
    }

    public int getEnd_impression_time() {
        return end_impression_time;
    }


    public int getImpPercent() {
        return impression_percent >= 0 ? impression_percent : 50;
    }

    public int getImpTime() {
        return impression_time >= 0 ? impression_time : 1;
    }

    public void setVideoMute(boolean isMute) {
        isVideoMute = isMute;
    }

    public void handleDetailAdShow(Context context, BaseAdUnit adUnit) {

    }

    public void handleDetailAdClose(Context context, BaseAdUnit adUnit) {

    }
}
