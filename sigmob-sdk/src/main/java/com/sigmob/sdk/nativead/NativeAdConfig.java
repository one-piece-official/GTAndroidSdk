package com.sigmob.sdk.nativead;

import android.content.Context;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.TouchLocation;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.BaseAdConfig;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.models.rtb.NativeAdSetting;
import com.sigmob.sdk.base.models.rtb.SingleNativeAdSetting;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;
import com.sigmob.sdk.videoAd.FractionalProgressAdTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        NativeAdConfig adConfig = new NativeAdConfig();
        adConfig.initAdConfig(adUnit);

        return adConfig;
    }

    @Override
    public void initAdConfig(BaseAdUnit adUnit) {
        super.initAdConfig(adUnit);

        NativeAdSetting nativeAdSetting = adUnit.getNativeAdSetting();
        SingleNativeAdSetting nativeSetting = adUnit.getSingleNativeSetting();

        if (nativeSetting != null) {
            use_na_video_component = nativeSetting.use_na_video_component;

        }
        if (nativeAdSetting != null) {
            preview_page_video_mute = nativeAdSetting.preview_page_video_mute;
            detail_page_video_mute = nativeAdSetting.detail_page_video_mute;
            impression_percent = nativeAdSetting.impression_percent;
            impression_time = nativeAdSetting.impression_time;
            video_auto_play = nativeAdSetting.video_auto_play;
            end_impression_time = nativeAdSetting.end_impression_time;
        }


        final List<FractionalProgressAdTracker> trackers =
                new ArrayList<>();
        trackers.add(new FractionalProgressAdTracker(
                ADEvent.AD_PLAY_QUARTER, 0.25f));
        trackers.add(new FractionalProgressAdTracker(
                ADEvent.AD_PLAY_TWO_QUARTERS, 0.5f));
        trackers.add(new FractionalProgressAdTracker(
                ADEvent.AD_PLAY_THREE_QUARTERS, 0.75f));
        addFractionalTrackers(trackers);

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

        PointEntitySigmobUtils.SigmobTracking(PointCategory.TEMPLATE_SHOW, null, adUnit);
    }

    public void handleDetailAdClose(Context context, BaseAdUnit adUnit) {

        PointEntitySigmobUtils.SigmobTracking(PointCategory.TEMPLATE_CLOSE, null, adUnit);
    }


    public void handleAdHide(Context context, BaseAdUnit adUnit) {


        PointEntitySigmobUtils.eventRecord(PointCategory.AD_SHOW, null, adUnit);

        SigmobTrackingRequest.sendTrackings(
                adUnit,
                ADEvent.AD_NATIVE_SHOW);
    }

    public void handleFeedClick(Context context, BaseAdUnit adUnit) {


        PointEntitySigmobUtils.eventRecord(PointCategory.FEED_CLICK, null, adUnit);

        SigmobTrackingRequest.sendTrackings(
                adUnit,
                ADEvent.AD_FEED_CLICK);
    }


    @Override
    public void handleClick(Context context, TouchLocation down, TouchLocation up, ClickUIType clickUIType, BaseAdUnit adUnit) {

        String coordinate = "";
        SigMacroCommon common = adUnit.getMacroCommon();
        if (common != null) {
            common.updateClickMarco(down, up);
            coordinate = common.getCoordinate();
        }

        handleUrlAction(clickUIType, coordinate, true);
    }

    public boolean isAutoPlay() {
        return getVideo_auto_play() == 0 ||
                (getVideo_auto_play() == 1 &&
                        ClientMetadata.getInstance().getActiveNetworkType() == 100);
    }

    public void handleVideoStart(BaseAdUnit adUnit, final int currentPosition, final int duration) {
        PointEntitySigmobUtils.SigmobTracking(PointCategory.VIDEO_START, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {
                    HashMap<String, String> option = new HashMap<>();

                    option.put("video_time", String.valueOf(currentPosition));
                    option.put("begin_time", String.valueOf(duration));
                    ((PointEntitySigmob) pointEntityBase).setOptions(option);
                }
            }
        });

        SigMacroCommon macroCommon = adUnit.getMacroCommon();

        macroCommon.addMarcoKey(SigMacroCommon._VIDEOTIME_, String.valueOf(duration));
        macroCommon.addMarcoKey(SigMacroCommon._BEGINTIME_, String.valueOf(currentPosition));

        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_VIDEO_START);

    }

    public void handleVideoPause(BaseAdUnit adUnit, final int endTime, final int currentPosition, final int duration) {
        PointEntitySigmobUtils.SigmobTracking(PointCategory.VIDEO_PAUSE, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {
                    HashMap<String, String> option = new HashMap<>();

                    option.put("video_time", String.valueOf(duration));
                    option.put("begin_time", String.valueOf(currentPosition));
                    option.put("end_time", String.valueOf(endTime));
                    option.put("is_first", String.valueOf(currentPosition == 0 ? 1 : 0));
                    option.put("is_last", String.valueOf((endTime >= duration - 1000) ? 1 : 0));

                    ((PointEntitySigmob) pointEntityBase).setOptions(option);
                }
            }
        });

        SigMacroCommon macroCommon = adUnit.getMacroCommon();

        macroCommon.addMarcoKey(SigMacroCommon._VIDEOTIME_, String.valueOf(duration));
        macroCommon.addMarcoKey(SigMacroCommon._BEGINTIME_, String.valueOf(currentPosition));
        macroCommon.addMarcoKey(SigMacroCommon._ENDTIME_, String.valueOf(endTime));
        macroCommon.addMarcoKey(SigMacroCommon._PLAYFIRSTFRAME_, String.valueOf(currentPosition == 0 ? 1 : 0));
        macroCommon.addMarcoKey(SigMacroCommon._ENDTIME_, String.valueOf(endTime));
        macroCommon.addMarcoKey(SigMacroCommon._ENDTIME_, String.valueOf(endTime));

        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_VIDEO_PAUSE);

    }
}
