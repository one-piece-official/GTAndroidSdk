package com.sigmob.sdk.newInterstitial;


import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_CLICK;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_VOPEN;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ResourceUtil;
import com.czhj.sdk.common.utils.ViewUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.BaseAdConfig;
import com.sigmob.sdk.base.common.BaseAdViewControllerListener;
import com.sigmob.sdk.base.common.OnSigAdClickListener;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.SessionManager;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.ClickCommon;
import com.sigmob.sdk.base.models.IntentActions;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.models.rtb.InterstitialSetting;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.views.DownloadDialog;
import com.sigmob.sdk.nativead.DisLikeDialog;
import com.sigmob.sdk.videoAd.BaseVideoViewController;
import com.sigmob.sdk.videoAd.FractionalProgressAdTracker;
import com.sigmob.sdk.videoplayer.BaseVideoAdView;
import com.sigmob.sdk.videoplayer.VIDEO_PLAYER_STATE;
import com.sigmob.sdk.videoplayer.VideoPlayerStatusListener;
import com.sigmob.windad.natives.WindNativeAdData;

import java.util.ArrayList;
import java.util.List;

public class NewInterstitialViewController extends BaseVideoViewController {

    private final BaseAdUnit mAdUnit;
    private BaseAdConfig mVideoConfig;

    private NewInterstitialAdView mAdView;
    private RelativeLayout waitRelativeLayout;
    private List<String> duration_seq = new ArrayList<>();
    private List<String> video_time_seq = new ArrayList<>();
    private BaseVideoAdView mVideoAdView;
    private ImageView mMainImageView;
    private NewInterstitialEndCardView mEndCardView;

    private InterstitialSetting setting;
    private boolean isFullInterstitial = false;

    private ViewGroup mMainAdContainer;
    private View actionButton;
    private NewInterstitialHeaderView mHeaderView;
    private SigAdInfoView mSigAdInfoView;
    private int mDuration;
    private String skip_state;
    private long mShowWaitTimeStamp;
    private int mWaitShowCount;
    private Handler mHandle;
    private boolean mIsMute;
    private boolean isShowEndCardView;
    private DownloadDialog downloadDialog;
    private boolean isElementDialogShow;
    private boolean isElementDialogClick;

    private boolean isCharging;


    public NewInterstitialViewController(final Activity activity,
                                         BaseAdUnit baseAdUnit, final Bundle intentExtras,
                                         final Bundle savedInstanceState,
                                         final String broadcastIdentifier,
                                         final BaseAdViewControllerListener baseVideoViewControllerListener)
            throws IllegalStateException {
        super(activity, broadcastIdentifier, baseVideoViewControllerListener);

        mAdUnit = baseAdUnit;

        mHandle = new Handler(Looper.getMainLooper());
        mVideoConfig = mAdUnit.getAdConfig();

        mVideoConfig.initFourElements(getActivity(), mAdUnit, new DownloadDialog.onPrivacyClickListener() {
            @Override
            public void onCloseClick() {
                isElementDialogShow = false;
                if (mVideoAdView != null) {
                    mVideoAdView.goOnPlayOnResume();
                }
            }

            @Override
            public void onButtonClick(String url, String clickCoordinate) {
                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_APPINFO;
                mAdUnit.getClickCommon().is_final_click = true;
                broadcastAction(ACTION_INTERSTITIAL_CLICK);
                if (getSessionManager() != null)
                    getSessionManager().recordDisplayEvent(ADEvent.AD_CLICK, 0);
            }

            @Override
            public void onShowSuccess() {
                isElementDialogShow = true;
                if (mVideoAdView != null) {
                    mVideoAdView.goOnPlayOnPause();
                }
            }
        });

        getBaseAdViewControllerListener().onSetRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        isFullInterstitial = mAdUnit.getTemplateType() == 0;
        optionSetting(activity, ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, intentExtras);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setBackgroundDimHide();
        setupView();
        if (getSessionManager() != null)
            getSessionManager().recordDisplayEvent(ADEvent.AD_START, 0);
        broadcastAction(IntentActions.ACTION_INTERSTITIAL_SHOW);

    }

    private void setupView() {
        mAdView = new NewInterstitialAdView(mContext);
        int resId = 0;

        int screenHeightAsIntDips = Dips.screenHeightAsIntDips(mContext);
        int screenWidthAsIntDips = Dips.screenWidthAsIntDips(mContext);
        if (isFullInterstitial) {
            resId = ResourceUtil.getLayoutId(mContext, "sig_new_interstitial_full_layout");
        } else if (screenHeightAsIntDips * 0.8f > 553 && screenWidthAsIntDips * 0.8f > 287) {
            resId = ResourceUtil.getLayoutId(mContext, "sig_new_interstitial_layout");
        } else {
            resId = ResourceUtil.getLayoutId(mContext, "sig_new_interstitial_small_layout");
        }

        mAdView.init(resId);

        broadcastAction(ACTION_INTERSTITIAL_VOPEN);
        mMainAdContainer = mAdView.getMainAdContainer();
        setting = mAdUnit.getNewInterstitialSetting();

        getLayout().addView(mAdView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initMainAdView(mMainAdContainer);
        initAdInfView();
        initHeaderView();
        initCTAButton(mAdView.getCTAButton(), ClickUIType.AD);
        if (setting != null) {
            getLayout().postDelayed(new Runnable() {

                @Override
                public void run() {
                    onCharge();
                }
            }, setting.charge_time * 1000);

        }
    }


    private void initCTAButton(Button button, final ClickUIType clickUIType) {

        button.setText(mAdUnit.getCTAText());
        button.setOnTouchListener(new View.OnTouchListener() {
            private MotionEvent down;

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    down = event;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    SigMacroCommon macroCommon = mAdUnit.getMacroCommon();
                    String coordinate = "";
                    if (macroCommon != null) {
                        macroCommon.updateClickMarco(down, event, false);
                        coordinate = macroCommon.getCoordinate();
                    }
                    mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
                    mAdUnit.getClickCommon().click_scene = clickUIType == ClickUIType.AD ? ClickCommon.CLICK_SCENE_AD : ClickCommon.CLICK_SCENE_ENDCARD;
                    mAdUnit.getClickCommon().is_final_click = mAdUnit.noHasDownloadDialog();
                    mAdUnit.getAdConfig().handleUrlAction(clickUIType, coordinate, true);
                }
                return false;
            }
        });
        mAdUnit.getAdConfig().setOnAdClickListener(new OnSigAdClickListener() {
            @Override
            public void onAdClick(boolean isRecord, ClickUIType type) {
                if (mAdUnit.getClickCommon().is_final_click) {
                    broadcastAction(ACTION_INTERSTITIAL_CLICK);
                }
                if (getSessionManager() != null)
                    getSessionManager().recordDisplayEvent(ADEvent.AD_CLICK, 0);
            }
        });
    }


    private void onClose() {


        getBaseAdViewControllerListener().onFinish();
    }


    @Override
    public void onDestroy() {
        broadcastAction(IntentActions.ACTION_INTERSTITIAL_DISMISS);

        if (mVideoAdView != null) {
            mVideoAdView.destroy();
            mVideoAdView = null;
        }
        if (downloadDialog != null) {
            downloadDialog.dismiss();
            downloadDialog.destroy();
            downloadDialog = null;
        }
        if (mDislikeDialog != null) {
            mDislikeDialog.dismiss();
            mDislikeDialog.destroy();
            mDislikeDialog = null;
        }
        mAdUnit.destroy();
        super.onDestroy();


    }

    private void initHeaderView() {
        mHeaderView = mAdView.getHeaderView();

        mHeaderView.showFeedback(mVideoAdView != null, onFeedBackListener);
        mHeaderView.setAdHeaderViewStateListener(new NewInterstitialHeaderView.AdHeaderViewStateListener() {

            @Override
            public void onShowClose() {
                onCharge();
                if (getSessionManager() != null)
                    getSessionManager().recordDisplayEvent(ADEvent.AD_SHOW_CLOSE, 0);
            }

            @Override
            public void onShowSkip() {
                onCharge();
                if (getSessionManager() != null)
                    getSessionManager().recordDisplayEvent(ADEvent.AD_SHOW_SKIP, 0);
            }
        });


        mHeaderView.setSoundClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsMute = !mIsMute;
                mHeaderView.setSoundStatus(mIsMute);
                if (mVideoAdView != null) {
                    mVideoAdView.setMute(mIsMute);
                }
            }
        });
        mHeaderView.setCloseClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onClose();
            }
        });

        mHeaderView.setSkipClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSkip();
            }
        });
        if (setting != null) {
            boolean closeAd = setting.skip_close_ad || !mAdUnit.hasEndCard();
            mHeaderView.startAdTimer(setting.show_skip_seconds, closeAd);
        }

    }

    private void onSkip() {
        if (getSessionManager() != null)
            getSessionManager().recordDisplayEvent(ADEvent.AD_SKIP, 0);
        showEndCardView();
    }

    private void onCharge() {
        if (isCharging) return;
        isCharging = true;
        if (getSessionManager() != null)
            getSessionManager().recordDisplayEvent(ADEvent.AD_CHARGE, 0);
    }


    private void initPrivacyInfoView(SigAdPrivacyInfoView adPrivacyInfo, final ClickUIType type) {
        if (adPrivacyInfo != null) {
            boolean isShow = mAdUnit.getadPrivacy() != null;

            adPrivacyInfo.setupView(mAdUnit.getAd_source_logo(), isShow);

            adPrivacyInfo.setOnTouchListener(new View.OnTouchListener() {
                private MotionEvent down;

                @Override
                public boolean onTouch(View view, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        down = event;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {

                        SigMacroCommon macroCommon = mAdUnit.getMacroCommon();
                        if (macroCommon != null) {
                            macroCommon.updateClickMarco(down, event, false);
                        }
                        showFourElement(type);
                    }
                    return true;
                }
            });
        }
    }

    private void initAdInfView() {
        SigAdInfoView adInfView = mAdView.getAdInfView();
        if (adInfView != null) {
            adInfView.setAppInfoView(mAdUnit.getIconUrl(), mAdUnit.getAppName(), mAdUnit.getCreativeTitle());
            SigAdPrivacyInfoView adPrivacyInfo = adInfView.getAdPrivacyInfo();
            if (adPrivacyInfo != null) {
                TextView adText = adPrivacyInfo.getPrivacyAdText();
                adText.setTextColor(Color.WHITE);
                initPrivacyInfoView(adInfView.getAdPrivacyInfo(), ClickUIType.AD);
            }
        }
    }

    private boolean showFourElement(final ClickUIType type) {
        try {
            Activity activity = getActivity();
            if (downloadDialog == null && activity != null) {
                downloadDialog = new DownloadDialog(getActivity(), mAdUnit);
                downloadDialog.setOnPrivacyClickListener(new DownloadDialog.onPrivacyClickListener() {

                    @Override
                    public void onCloseClick() {

                        if (mVideoAdView != null) {
                            mVideoAdView.goOnPlayOnResume();
                        }
                        if (downloadDialog != null) {
                            downloadDialog.dismiss();
                            downloadDialog.destroy();
                            downloadDialog = null;
                        }
                        /**
                         * 四要素消失的时候恢复视频，如果非endCard页面
                         */
                        isElementDialogShow = false;
                        if (getSessionManager() != null)
                            getSessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_CLOSE, 0);

                    }

                    @Override
                    public void onButtonClick(String url, String clickCoordinate) {//执行点击逻辑
                        if (mAdUnit != null) {
                            mAdUnit.getClickCommon().is_final_click = true;
                            mAdUnit.getAdConfig().handleUrlFourAction(type, url, clickCoordinate, true);

                        }
                    }

                    @Override
                    public void onShowSuccess() {
                        /**
                         * 四要素出来的时候暂停视频，如果非endCard页面
                         */
                        if (mVideoAdView != null) {
                            mVideoAdView.goOnPlayOnPause();
                        }
                        if (getSessionManager() != null)
                            getSessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_SHOW, 0);
                    }
                });
            }

            if (downloadDialog != null && downloadDialog.isRenderSuccess() && !isElementDialogShow) {
                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_SCENE_APPINFO;
                mAdUnit.getClickCommon().click_scene = type == ClickUIType.AD ? ClickCommon.CLICK_SCENE_AD : ClickCommon.CLICK_SCENE_ENDCARD;
                mAdUnit.getClickCommon().is_final_click = false;
                if (getSessionManager() != null)
                    getSessionManager().recordDisplayEvent(ADEvent.AD_CLICK, 0);
                downloadDialog.show();
                isElementDialogShow = true;
                return true;
            }
            return false;
        } catch (Exception e) {
            SigmobLog.e("openFourElements fail:" + e.getMessage());
            return false;
        }


    }

    private BaseVideoAdView getVideoAdView() {

        if (mVideoAdView == null) {
            mVideoAdView = new BaseVideoAdView(getContext());
            mVideoAdView.setVideoPlayerStatusListener(new VideoPlayerStatusListener() {

                private boolean isPause;

                @Override
                public void OnStateChange(final VIDEO_PLAYER_STATE state) {
                    if (mVideoAdView == null) {
                        return;
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            SigmobLog.i("video player state change " + state);
                            switch (state) {
                                case STATE_PREPARED: {
                                    if (mVideoAdView != null) {
                                        mDuration = (int) mVideoAdView.getDuration();
                                        if (mDuration == 0) {
                                            mDuration = mAdUnit.getDuration();
                                        }
                                        mAdUnit.getVideoCommon().video_time = (int) (mDuration / 1000.0f);
                                    }
                                    mAdUnit.getVideoCommon().is_first = 1;
                                    mAdUnit.getVideoCommon().type = 1;
                                    mAdUnit.getVideoCommon().scene = 1;
                                    mAdUnit.getVideoCommon().is_auto_play = 1;
                                    mAdUnit.getVideoCommon().is_last = 0;
                                    mAdUnit.getVideoCommon().end_time = 0;
                                    mIsMute = mAdUnit.getIsMute() != 0;
                                    if (mVideoAdView != null) {
                                        mVideoAdView.setMute(mIsMute);
                                        mHeaderView.setSoundStatus(mIsMute);
                                        mHeaderView.showSoundIcon();
                                    }


                                }
                                break;
                                case STATE_PLAYING: {
                                    if (isPause) {
                                        mAdUnit.getVideoCommon().type = 2;
                                    }
                                    isPause = false;
                                    if (getSessionManager() != null)
                                        getSessionManager().recordDisplayEvent(ADEvent.AD_VIDEO_START, 0);
                                    if (mVideoAdView != null) {
                                        mAdUnit.getVideoCommon().begin_time = (int) (mVideoAdView.getCurrentPositionWhenPlaying() / 1000.0f);
                                    }

                                }
                                break;
                                case STATE_ERROR: {
                                    if (mVideoAdView != null){
                                        int code = mVideoAdView.getErrorCode();
                                        String msg = mVideoAdView.getErrorMessage();
                                        PointEntitySigmobUtils.SigmobError(PointCategory.VIDEO,code,msg,mAdUnit);
                                    }
                                    stopVideoPlay();
                                }break;
                                case STATE_AUTO_COMPLETE: {
                                    if (getSessionManager() != null)
                                        getSessionManager().recordDisplayEvent(ADEvent.AD_COMPLETE, 0);

                                    if (!mAdUnit.hasEndCard()) {
                                        mAdUnit.getVideoCommon().type = 3;
                                        if (mVideoAdView != null) {
                                            mVideoAdView.goOnPlayOnResume();
                                        }
                                        break;
                                    }

                                }

                                case STATE_STOP: {
                                    stopVideoPlay();
                                }
                                break;
                                case STATE_BUFFERING_START: {
                                    showProgressDialog();
                                    if (mVideoAdView != null) {
                                        mVideoAdView.goOnPlayOnPause();
                                    }
                                }
                                break;
                                case STATE_BUFFERING_END: {
                                    dismissProgressDialog();
                                    if (mVideoAdView != null) {
                                        mVideoAdView.goOnPlayOnResume();
                                    }
                                }
                                break;
                                case STATE_PAUSE: {
                                    if (getSessionManager() != null)
                                        getSessionManager().recordDisplayEvent(ADEvent.AD_VIDEO_PAUSE, 0);
                                    isPause = true;
                                }
                                break;
                                default: {

                                }
                                break;
                            }
                        }
                    });


                }

                @Override
                public void OnProgressUpdate(long position, long duration) {


                    SigmobLog.d("position " + position + " duration " + duration);
                    final List<FractionalProgressAdTracker> trackersToTrack =
                            mVideoConfig.getUntriggeredTrackersBefore(position, mDuration);

                    for (FractionalProgressAdTracker tracker : trackersToTrack) {
                        handleViewabilityQuartileEvent(tracker.getEvent());
                        tracker.setTracked();
                    }
                    if (position > mDuration) {
                        stopVideoPlay();
                    }


                }
            });

            initVideoTracks();
        }

        mVideoAdView.setUp(mAdUnit.getProxyVideoUrl());

        return mVideoAdView;

    }

    private void initVideoTracks() {

        final List<FractionalProgressAdTracker> trackers =
                new ArrayList<>();
        trackers.add(new FractionalProgressAdTracker(
                ADEvent.AD_PLAY_QUARTER, 0.25f));
        trackers.add(new FractionalProgressAdTracker(
                ADEvent.AD_PLAY_TWO_QUARTERS, 0.5f));
        trackers.add(new FractionalProgressAdTracker(
                ADEvent.AD_PLAY_THREE_QUARTERS, 0.75f));
//        trackers.add(new FractionalProgressAdTracker(MessageType.QUARTILE_EVENT,
//                ADEvent.AD_PLAY_COMPLETE, 0.85f));
        mVideoConfig.addFractionalTrackers(trackers);

    }

    void handleViewabilityQuartileEvent(final String adEvent) {

        if (getSessionManager() != null) {
            getSessionManager().recordDisplayEvent(adEvent, (int) mVideoAdView.getCurrentPositionWhenPlaying());
        }
    }

    private void showProgressDialog() {

        if (waitRelativeLayout == null || waitRelativeLayout.getVisibility() == View.VISIBLE)
            return;

        mShowWaitTimeStamp = System.currentTimeMillis();
        waitRelativeLayout.setVisibility(View.VISIBLE);
        skip_state = "loading";

        if (++mWaitShowCount > 2) {
//            showSkip((int) mVideoAdView.getCurrentPositionWhenPlaying(), true);

        } else {

            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mHandle.removeCallbacksAndMessages(null);
//                    showSkip((int) mVideoAdView.getCurrentPositionWhenPlaying(), true);

                }
            }, 5000);
        }
    }

    @SuppressLint("DefaultLocale")
    private void dismissProgressDialog() {

        if (waitRelativeLayout != null && waitRelativeLayout.getVisibility() == View.VISIBLE) {

            duration_seq.add(String.format("%d", System.currentTimeMillis() - mShowWaitTimeStamp));
            video_time_seq.add(String.format("%.2f", (int) mVideoAdView.getCurrentPositionWhenPlaying() / 1000.0f));
            skip_state = "play";

            mHandle.removeCallbacksAndMessages(null);
            waitRelativeLayout.setVisibility(View.INVISIBLE);
        }

    }

    public SessionManager getSessionManager() {
        SessionManager sessionManager = null;
        if (mAdUnit != null) {
            sessionManager = mAdUnit.getSessionManager();
            if (sessionManager == null) {
                sessionManager = new InterstitialAdViewAbilitySessionManager();
                sessionManager.createDisplaySession(mAdUnit);
            }
        }

        return sessionManager;
    }

    public void stopVideoPlay() {
        dismissProgressDialog();
        showEndCardView();
    }

    private void initMainAdView(ViewGroup adContainer) {
        int templateId = mAdUnit.getTemplateId();

        switch (templateId) {
            case 6001: {
                mVideoAdView = getVideoAdView();
                adContainer.addView(mVideoAdView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mVideoAdView.startVideo();
            }
            break;
            case 6002: {
                mMainImageView = new ImageView(mContext);

                adContainer.addView(mMainImageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                AdStackManager.getImageManger().load(mAdUnit.getMainImage()).into(mMainImageView);
            }
        }
    }

    private DisLikeDialog mDislikeDialog;
    private View.OnClickListener onFeedBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Activity activity = getActivity();
            if (activity != null && mAdUnit != null) {
                mDislikeDialog = new DisLikeDialog(activity, mAdUnit);

                if (mDislikeDialog != null) {
                    mDislikeDialog.showDislikeDialog();
                }
                mDislikeDialog.setDislikeInteractionCallback(new WindNativeAdData.DislikeInteractionCallback() {
                    @Override
                    public void onShow() {
                        onPause();

                    }

                    @Override
                    public void onSelected(int position, String value, boolean enforce) {

                        onResume();
                        if (mDislikeDialog != null) {
                            mDislikeDialog.dismiss();
                            mDislikeDialog.destroy();
                            mDislikeDialog = null;
                        }
                    }

                    @Override
                    public void onCancel() {
                        onResume();

                        if (mDislikeDialog != null) {
                            mDislikeDialog.dismiss();
                            mDislikeDialog.destroy();
                            mDislikeDialog = null;
                        }
                    }
                });
            }
        }
    };


    private void showEndCardView() {

        if (isShowEndCardView) return;

        if (mVideoAdView != null) {
            mVideoAdView.destroy();
            mVideoAdView = null;
        }
        isShowEndCardView = true;

        if (mEndCardView == null) {
            mEndCardView = new NewInterstitialEndCardView(mContext);
            mEndCardView.showFeedback(onFeedBackListener);
            initCTAButton(mEndCardView.getCTAButton(), ClickUIType.ENDCARD);
            mEndCardView.setupEndCardView(mAdUnit.getIconUrl(),
                    mAdUnit.getEndCardImageUrl(),
                    mAdUnit.getAppName(),
                    mAdUnit.getCreativeTitle(),
                    mAdUnit.getCTAText());

            SigAdPrivacyInfoView adPrivacyInfo = mEndCardView.getAdPrivacyInfo();
            if (adPrivacyInfo != null) {
                View ll = adPrivacyInfo.getPrivacyLl();
                if (ll != null) {
                    ll.setBackgroundColor(Color.TRANSPARENT);
                    initPrivacyInfoView(adPrivacyInfo, ClickUIType.ENDCARD);
                }


            }


            View closeButton = mEndCardView.getCloseButton();
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClose();
                }
            });
        }
        mHeaderView.hideSoundIcon();
        ViewUtil.removeFromParent(mAdView);
        getLayout().addView(mEndCardView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onPause() {

        if (mVideoAdView != null) {
            if (!isElementDialogShow)
                mVideoAdView.goOnPlayOnPause();
        }

    }

    @Override
    public void onResume() {
        if (mVideoAdView != null) {
            if (!isElementDialogShow)
                mVideoAdView.goOnPlayOnResume();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onStart() {

    }

}
