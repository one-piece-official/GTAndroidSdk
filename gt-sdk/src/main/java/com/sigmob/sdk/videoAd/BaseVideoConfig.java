package com.sigmob.sdk.videoAd;

import android.content.Context;

import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.wire.Wire;
import com.sigmob.sdk.base.SigmobRes;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.common.BaseAdConfig;
import com.sigmob.sdk.base.common.CreativeType;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.config.SigmobDialogSetting;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.base.models.rtb.RvAdSetting;
import com.sigmob.sdk.base.views.CreativeResource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaseVideoConfig extends BaseAdConfig implements Serializable {

    protected static final long serialVersionUID = 2L;
    private final List<FractionalProgressAdTracker> mFractionalTrackers;
    protected VideoCompanionAdConfig mLandscapeVideoCompanionAdConfig;
    protected VideoCompanionAdConfig mPortraitVideoCompanionAdConfig;
    //    protected Map<String, VideoCompanionAdConfig> mSocialActionsCompanionAds;
    protected boolean mIsRewardedVideo;

    protected float videoFinish = 1.f;
    protected int seekPostion = -1000;
    protected int mSkipOffset = 100;
    protected boolean mIsAutoRemoveVideoView = false;
    private int mSkipSeconds = -1;
    private int isMute;
    private boolean mEnableExitOnVideoClose = false;
    private DialogConfig mDialogConfig = null;
    private int end_time;
    /**
     * Flag to indicate if the VAST xml document has explicitly set the orientation as opposed to
     * using the default.
     */
    private boolean mIsForceOrientationSet;


    private BaseVideoConfig() {
        super();

        mFractionalTrackers = new ArrayList<>();
        mIsRewardedVideo = false;

    }

    public static BaseVideoConfig getAdConfig(BaseAdUnit adUnit) {
        BaseVideoConfig baseVideoConfig = new BaseVideoConfig();
//        baseVideoConfig.addTrackers(adUnit, WindSDKConfig.sharedInstance());
        baseVideoConfig.setIsRewardedVideo(adUnit.getAd_type() == AdFormat.REWARD_VIDEO);
        baseVideoConfig.setSeekPostion(adUnit.getMaterial().video_reciprocal_millisecond);
        baseVideoConfig.setAutoRemoveVideoView(adUnit.getMaterial().creative_type != CreativeType.CreativeTypeVideo_transparent_html.getCreativeType());

        RvAdSetting rvAdSetting = adUnit.getRvAdSetting();
        DialogConfig dialogConfig;
        SigmobDialogSetting closeSigmobDialogSetting = WindSDKConfig.getInstance().getCloseDialogSetting();
        if (closeSigmobDialogSetting != null) {

            dialogConfig = new DialogConfig(Wire.get(closeSigmobDialogSetting.title, ""), Wire.get(closeSigmobDialogSetting.body_text, ""),
                    Wire.get(closeSigmobDialogSetting.cancel_button_text, ""), Wire.get(closeSigmobDialogSetting.close_button_text, ""));
        } else {
            dialogConfig = new DialogConfig(SigmobRes.closeAdTitle(), SigmobRes.closeAdMessage(),
                    SigmobRes.closeAdOk(), SigmobRes.closeAdCancel());
        }

        baseVideoConfig.setDialogConfig(dialogConfig);
        if (rvAdSetting != null) {

            baseVideoConfig.enableExitOnVideoClose(rvAdSetting.enable_exit_on_video_close);
//            baseVideoConfig.setSkipOffset(rvAdSetting.skip_percent);
//            baseVideoConfig.setSkipSeconds(rvAdSetting.skip_seconds);
//            baseVideoConfig.setClosePosition(rvAdSetting.endcard_close_position);
//            baseVideoConfig.setSkipPosition(rvAdSetting.video_close_position);
//            baseVideoConfig.setSoundPosition(rvAdSetting.mute_postion);
            baseVideoConfig.setVideoFinish(rvAdSetting.finished);
            baseVideoConfig.setEndTime(rvAdSetting.end_time);


        } else {
//
//            baseVideoConfig.enableExitOnVideoClose(WindSDKConfig.sharedInstance().enableExitOnVideoClose());
//            baseVideoConfig.setSkipOffset(WindSDKConfig.sharedInstance().getShowClose());
//            baseVideoConfig.setSkipSeconds(WindSDKConfig.sharedInstance().getSkipSeconds());
//            baseVideoConfig.setClosePosition(WindSDKConfig.sharedInstance().getClosePosition());
//            baseVideoConfig.setSkipPosition(WindSDKConfig.sharedInstance().getSkipPosition());
//            baseVideoConfig.setSoundPosition(WindSDKConfig.sharedInstance().getSoundPostion());
//            baseVideoConfig.setVideoFinish(WindSDKConfig.sharedInstance().getFinished());
//            baseVideoConfig.setIsMute(WindSDKConfig.sharedInstance().ifMute());

        }

        baseVideoConfig.initAdConfig(adUnit);

        return baseVideoConfig;
    }

    public static VideoCompanionAdConfig getVideoCompanionAd(final BaseAdUnit adUnit) {


        if (adUnit != null) {
            MaterialMeta material = adUnit.getMaterial();
            CreativeResource.CreativeType clickType = material.click_type == 2 ? CreativeResource.CreativeType.IMAGE : CreativeResource.CreativeType.JAVASCRIPT;
            String resourcePath = adUnit.resourcePath();
            CreativeResource.Type resourceType = adUnit.getCreativeResourceType();
            CreativeResource resource = new CreativeResource(resourcePath, resourceType, clickType, 720, 1024);
            return new VideoCompanionAdConfig(768, 1024, adUnit.getInteractionType(), material.landing_page, material.deeplink_url, resource);
        }
        return null;

    }

    public void enableExitOnVideoClose(boolean enableExitOnVideoClose) {
        this.mEnableExitOnVideoClose = enableExitOnVideoClose;
    }

    public int getShowDuration(int duration) {

        if (end_time == 0 || end_time * 1000 > duration) {
            return duration;
        }
        return end_time * 1000;
    }

    public int getEndTime() {
        return end_time;
    }

    public void setEndTime(int end_time) {
        this.end_time = end_time;
    }

    public boolean isEnableExitOnVideoClose() {
        return mEnableExitOnVideoClose;
    }

    public String getDspCreativeId() {
        return mDspCreativeId;
    }

    public void setDspCreativeId(final String dspCreativeId) {
        mDspCreativeId = dspCreativeId;
    }

//    public int getClosePosition() {
//        return closePosition;
//    }
//
//    public void setClosePosition(int closePosition) {
//        if (closePosition > 0 && closePosition < 5)
//            this.closePosition = closePosition;
//    }

//    public int getSkipPosition() {
//        return skipPosition;
//    }
//
//    public void setSkipPosition(int skipPosition) {
//        if (skipPosition > 0 && skipPosition < 5)
//            this.skipPosition = skipPosition;
//    }

//    public int getSoundPosition() {
//        return soundPosition;
//    }
//
//    public void setSoundPosition(int soundPosition) {
//        if (soundPosition > 0 && soundPosition < 5)
//            this.soundPosition = soundPosition;
//    }

    public float getVideoFinish() {
        return videoFinish;
    }

    public void setVideoFinish(float videoFinish) {
        this.videoFinish = videoFinish;
    }


    public int getSeekPostion() {
        return seekPostion;
    }

    public void setSeekPostion(int seekPostion) {
        if (seekPostion != 0) {
            this.seekPostion = seekPostion;
        }
    }

    public boolean isAutoRemoveVideoView() {
        return mIsAutoRemoveVideoView;
    }

    public void setAutoRemoveVideoView(boolean mIsAutoRemoveVideoView) {
        this.mIsAutoRemoveVideoView = mIsAutoRemoveVideoView;
    }

    public DialogConfig getDialogConfig() {
        return mDialogConfig;
    }

    public void setDialogConfig(DialogConfig mDialogConfig) {
        this.mDialogConfig = mDialogConfig;
    }

    public int getRemainingProgressTrackerCount() {

        return 0;
    }

    private void setIsRewardedVideo(final boolean isRewardedVideo) {
        mIsRewardedVideo = isRewardedVideo;
    }


    public String getCustomCtaText() {
        return mCustomCtaText;
    }


//
//    public Map<String, VideoCompanionAdConfig> getSocialActionsCompanionAds() {
//        return mSocialActionsCompanionAds;
//    }
//
//    public void setSocialActionsCompanionAds(
//             final Map<String, VideoCompanionAdConfig> socialActionsCompanionAds) {
//        this.mSocialActionsCompanionAds = socialActionsCompanionAds;
//    }

    public void setCustomCtaText(final String customCtaText) {
        if (customCtaText != null) {
            mCustomCtaText = customCtaText;
        }
    }

    public String getCustomSkipText() {
        return mCustomSkipText;
    }

    public void setCustomSkipText(final String customSkipText) {
        if (customSkipText != null) {
            mCustomSkipText = customSkipText;
        }
    }

    public String getCustomCloseIconUrl() {
        return mCustomCloseIconUrl;
    }

    public void setCustomCloseIconUrl(final String customCloseIconUrl) {
        if (customCloseIconUrl != null) {
            mCustomCloseIconUrl = customCloseIconUrl;
        }
    }


    /**
     * Gets the String specified in the VAST document regarding the skip offset. This should be in
     * the form HH:MM:SS[.mmm] or n%. (e.g. 00:00:12, 00:00:12.345, 42%).
     *
     * @return String representation of the skip offset or {@code null} if not set.
     */

//    public int getSkipOffset() {
//        return mSkipOffset;
//    }
//
//    public void setSkipOffset(final int skipOffset) {
//        if (skipOffset < 0) {
//            mSkipOffset = 0;
//        } else if (skipOffset > 100) {
//            mSkipOffset = 100;
//        } else {
//            mSkipOffset = skipOffset;
//        }
//    }

    /**
     * Returns whether or not this is an unskippable rewarded video.
     *
     * @return True if this is a rewarded video, false otherwise.
     */
    public boolean isRewardedVideo() {
        return mIsRewardedVideo;
    }

    /**
     * Called when the video starts playing.
     *
     * @param context         The context. Can be application or activity context.
     * @param contentPlayHead Current video playback time.
     */
    public void handleImpression(final Context context, int contentPlayHead, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");
    }

    /**
     * Called when the video is not finished and is resumed from the middle of the video.
     *
     * @param context         The context. Can be application or activity context.
     * @param contentPlayHead Current video playback time.
     */
    public void handleResume(final Context context, int contentPlayHead) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

    }

    /**
     * Called when the video is not finished and is paused.
     *
     * @param context         The context. Can be application or activity context.
     * @param contentPlayHead Current video playback time.
     */
    public void handlePause(final Context context, int contentPlayHead) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

    }

    public void handleClose(final Context context, final int contentPlayHead, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

    }


    /**
     * Called when the video is played completely without skipping.
     *
     * @param context        The context. Can be application or activity context.
     * @param currentPostion Current video playback time (should be duration of video).
     */
    public void handleComplete(Context context, int currentPostion, int duration, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");


    }

    public void handleSkip(Context context, int currentPostion, int duration, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");
    }


    public void handleFinish(Context context, int currentPostion, int duration, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

    }


    /**
     * Gets the skip offset in milliseconds. If the skip offset would be past the video duration,
     * this returns the video duration. Returns null when the skip offset is not set or cannot be parsed.
     *
     * @param videoDuration Used to calculate percentage based offsets.
     * @return The skip offset in milliseconds. Can return null.
     */

    public int getSkipOffsetMillis(final int videoDuration) {

        return (int) (videoDuration * (mSkipOffset / 100.0f));
    }


    public int getSkipSeconds() {
        return mSkipSeconds;
    }

    public void setSkipSeconds(int mSkipSeconds) {
        this.mSkipSeconds = mSkipSeconds;
    }

    public void handleShowSkip(boolean isShowForce, int currentPosition, int duration, BaseAdUnit adUnit) {


    }

}

