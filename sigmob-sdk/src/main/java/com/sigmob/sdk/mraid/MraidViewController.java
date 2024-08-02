// Copyright 2018-2019 Sigmob, Inc.
// Licensed under the Sigmob SDK License Agreement
// http://www.sigmob.com/legal/sdk-license-agreement/

package com.sigmob.sdk.mraid;

import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_CLICK;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_FAIL;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_SHOW;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_VOPEN;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_REWARDED_VIDEO_CLOSE;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_REWARDED_VIDEO_COMPLETE;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_REWARDED_VIDEO_PLAY;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_REWARDED_VIDEO_SKIP;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.common.BaseAdViewControllerListener;
import com.sigmob.sdk.base.common.ExternalViewabilitySessionManager;
import com.sigmob.sdk.base.common.OnSigAdClickListener;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.ClickCommon;
import com.sigmob.sdk.base.models.IntentActions;
import com.sigmob.sdk.base.models.PlacementType;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.views.DownloadDialog;
import com.sigmob.sdk.base.views.OvalButton;
import com.sigmob.sdk.base.views.VideoButtonWidget;
import com.sigmob.sdk.mraid2.Mraid2Motion;
import com.sigmob.sdk.nativead.DisLikeDialog;
import com.sigmob.sdk.videoAd.BaseVideoConfig;
import com.sigmob.sdk.videoAd.BaseVideoViewController;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.WindNativeAdData;

import org.json.JSONObject;

import java.net.URI;
import java.util.Map;

public class MraidViewController extends BaseVideoViewController implements OnSigAdClickListener {
    protected ExternalViewabilitySessionManager mExternalViewabilitySessionManager;
    private int mRequestedOrientation;
    private BaseVideoConfig mVideoConfig;
    private int mInsetBottom;
    private boolean isReward;
    private BaseAdUnit mAdUnit;
    private MraidController mMraidController;
    private MraidWebViewDebugListener mDebugListener;
    private VideoButtonWidget mCloseButtonWidget;
    private int mDuration;
    private boolean mIsSkip;
    /**
     * For when the video is closing.
     */
    private boolean mIsClosing = false;
    private boolean mIsDismiss = false;

    private DownloadDialog downloadDialog;
    private boolean isDialogShow = false;
    private Mraid2Motion mraid2Motion;
    private MraidMotionView motionViewMap;
    private DisLikeDialog mDislikeDialog;
    private OvalButton endcardFeedBack;
    private JSONObject log_data;

    public MraidViewController(final Activity activity,
                               BaseAdUnit baseAdUnit,
                               final Bundle intentExtras,
                               final Bundle savedInstanceState,
                               final String mBroadcastIdentifier,
                               final BaseAdViewControllerListener baseAdViewControllerListener) {
        // No broadcast identifiers are used by MraidVideoViews.
        super(activity, mBroadcastIdentifier, baseAdViewControllerListener);

        mAdUnit = baseAdUnit;
        mVideoConfig = (BaseVideoConfig) mAdUnit.getAdConfig();
        mVideoConfig.initFourElements(getActivity(), mAdUnit, new DownloadDialog.onPrivacyClickListener() {
            @Override
            public void onCloseClick() {

//                if (mExternalViewabilitySessionManager != null) {
//                    mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_CLOSE, 0);
//                }
                if (mMraidController != null) {
                    MraidWebView currentWebView = mMraidController.getCurrentWebView();
                    if (currentWebView != null) {
                        MraidBridge mraidBridge = mMraidController.getMraidBridge();
                        if (mraidBridge != null) {
                            mraidBridge.notifyEvent("fourElementsDidDisappear", null);
                        }
                    }
                }
            }

            @Override
            public void onButtonClick(String url, String clickCoordinate) {//执行点击逻辑
                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
                mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_APPINFO;
                mAdUnit.getClickCommon().is_final_click = true;
            }

            @Override
            public void onShowSuccess() {
//                if (mExternalViewabilitySessionManager != null) {
//
//                    mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_SHOW, 0);
//                }
                if (mMraidController != null) {
                    MraidWebView currentWebView = mMraidController.getCurrentWebView();
                    if (currentWebView != null) {
                        MraidBridge mraidBridge = mMraidController.getMraidBridge();
                        if (mraidBridge != null) {
                            mraidBridge.notifyEvent("fourElementsDidAppear", null);
                        }
                    }
                }
            }
        });
        mVideoConfig.setOnAdClickListener(this);

        mInsetBottom = ClientMetadata.getInstance().getInsetBottom();

//        int display_orientation = mAdUnit.getAd().display_orientation;
//
//        switch (display_orientation) {
//            case 1: {
//                mRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
//            }
//            break;
//            case 2: {
//                mRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
//            }
//            break;
//            default: {
//                mRequestedOrientation = intentExtras.getInt(WindConstants.REQUESTED_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_BEHIND);
//            }
//        }
//
////        getActivity().setRequestedOrientation(mRequestedOrientation);
//        getBaseAdViewControllerListener().onSetRequestedOrientation(mRequestedOrientation);
        getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        optionSetting(activity, mRequestedOrientation, intentExtras);

        getLayout().setBackgroundColor(Color.TRANSPARENT);
    }

    public void HandleVpaidEvent(final String event) {
        final Integer duration = mMraidController.getAdDuration();
        mMraidController.getAdPlayTime(new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                try {
                    if(mExternalViewabilitySessionManager == null){
                        return;
                    }
                    if (MraidBridge.convertJsUndefined(value) == null) {
                        if (event.equals(ADEvent.AD_SHOW_SKIP)) {
                            mExternalViewabilitySessionManager.onVideoShowSkip(false, 0);
                        }
                        mExternalViewabilitySessionManager.recordDisplayEvent(event, (int) 0);
                    } else {
                        Float progress = Float.valueOf(value);
                        float playTime = -1;
                        if (progress > 0.0000001 && duration != null && duration > 0) {
                            playTime = progress * duration;
                        }
                        if (event.equals(ADEvent.AD_SHOW_SKIP)) {
                            mExternalViewabilitySessionManager.onVideoShowSkip(false, (int) playTime);
                        }
                        mExternalViewabilitySessionManager.recordDisplayEvent(event, (int) playTime);
                    }

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }


    public View getAdView() {

        final String broadcastIdentifier = mBroadcastIdentifier;
        WebViewCacheService.Config config = null;
        if (broadcastIdentifier != null) {
            config = WebViewCacheService.popWebViewConfig(broadcastIdentifier);
        }

        if (config != null && config.getController() != null) {
            mMraidController = config.getController();
        } else {
            mMraidController = new MraidController(mContext, mAdUnit, PlacementType.INTERSTITIAL);
        }

        mMraidController.setAdSize(adSize);

        mMraidController.setVpaidEventListener(new MraidController.VPaidEventListener() {

            private boolean isPlaycomplete;

            @Override
            public void adClickThru(Map<String, String> params) {
                //'url', url, 'id', id, 'playerHandles', playerHandles
                try {
                    String id = params.get("id");
                    String url = params.get("url");
                    String playerHandles = params.get("playerHandles");

                    if (!playerHandles.equalsIgnoreCase("false")) {
                        if (!TextUtils.isEmpty(url)) {
                            if (url.startsWith("http")) {
                                mAdUnit.setCustomLandPageUrl(url);
                            } else {
                                mAdUnit.setCustomDeeplink(url);
                            }
                        }

                        mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, mMraidController.getClickCoordinate(), true);
                    }


                    SigmobLog.d("adClickThru() called" + params.toString());
                } catch (Throwable throwable) {
                    SigmobLog.e("adClickThru", throwable);
                }
            }

            @Override
            public void adError(Map<String, String> params) {
//                broadcastAction(ACTION_MRAID_PLAYFAIL);
//                HandleVpaidEvent(ADEvent.AD_ERROR);
                SigmobLog.d("adError() called " + params);
            }

            @Override
            public void adImpression() {
                SigmobLog.d("adImpression() called");
            }

            @Override
            public void adPaused() {
                if (!isPlaycomplete) {
                    HandleVpaidEvent(ADEvent.AD_PAUSE);
                }
            }

            @Override
            public void adPlaying() {
//                HandleVpaidEvent(ADEvent.AD_PLAYING);
                SigmobLog.d("adPlaying() called");
            }

            @Override
            public void adVideoComplete() {
                isPlaycomplete = true;
//                if (!isReward && mAdUnit.getAd_type() != AdFormat.FULLSCREEN_VIDEO) {
//                    isReward = true;
//                    broadcastAction(ACTION_REWARDED_VIDEO_COMPLETE);
//                    mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_REWARD, mMraidController.getAdDuration());
//                }
                HandleVpaidEvent(ADEvent.AD_COMPLETE);

                SigmobLog.d("adVideoComplete() called");
            }

            @Override
            public void adVideoFirstQuartile() {
                HandleVpaidEvent(ADEvent.AD_PLAY_QUARTER);
                SigmobLog.d("adVideoFirstQuartile() called");
            }

            @Override
            public void adVideoThirdQuartile() {
                HandleVpaidEvent(ADEvent.AD_PLAY_THREE_QUARTERS);

                SigmobLog.d("adVideoThirdQuartile() called");
            }

            @Override
            public void adVideoMidpoint() {
                HandleVpaidEvent(ADEvent.AD_PLAY_TWO_QUARTERS);
                SigmobLog.d("adVideoMidpoint() called");
            }

            @Override
            public void adVideoStart() {
                HandleVpaidEvent(ADEvent.AD_AD_VIDEO_START);

                broadcastAction(ACTION_REWARDED_VIDEO_PLAY);

                SigmobLog.d("adVideoStart() called");
            }

            @Override
            public void onVideoPrepared(Integer duration) {
                mDuration = duration == null ? -1 : duration;
                mExternalViewabilitySessionManager.onVideoPrepared(mDuration, mVideoConfig.getEndTime());
            }
        });

        mMraidController.setMraidListener(new MraidController.MraidListener() {

            @Override
            public void onLoaded(View view) {
                // This is only done for the interstitial. Banners have a different mechanism
                // for tracking third party impressions.

                HandleVpaidEvent(ADEvent.AD_START);

                broadcastAction(ACTION_INTERSTITIAL_SHOW);

                SigmobLog.d("onLoaded() called");
            }

            @Override
            public void onFailedToLoad() {
                SigmobLog.d("MraidActivity failed to load. Finishing the activity");
                if (mBroadcastIdentifier != null) {
                    broadcastAction(ACTION_INTERSTITIAL_FAIL);
                }
                mBaseAdViewControllerListener.onFinish();
            }

            @Override
            public void onRenderProcessGone(final WindAdError error) {
                SigmobLog.d("Finishing the activity due to a problem: " + error);
                HandleVpaidEvent(ADEvent.AD_ERROR);

                if (mBroadcastIdentifier != null) {
                    broadcastAction(ACTION_INTERSTITIAL_FAIL);
                }
                mBaseAdViewControllerListener.onFinish();
            }

            @Override
            public void onClose() {
                mMraidController.getAdPlayTime(new ValueCallback<String>() {

                    @Override
                    public void onReceiveValue(String value) {
                        if (MraidBridge.convertJsUndefined(value) != null  && mExternalViewabilitySessionManager != null) {
                            Float progress = Float.valueOf(value);
                            if (progress < 0.999f && !mIsSkip) {
                                mIsSkip = true;
                                HandleVpaidEvent(ADEvent.AD_SKIP);
                            }

                            if (!isReward && mAdUnit.getAd_type() != AdFormat.FULLSCREEN_VIDEO && progress > mAdUnit.getFinishedTime()) {
                                isReward = true;
                                mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_REWARD, mMraidController.getAdDuration());
                                broadcastAction(ACTION_REWARDED_VIDEO_COMPLETE);
                            }
                        }
                    }
                });

                broadcastAction(ACTION_REWARDED_VIDEO_CLOSE);

                mIsClosing = true;

                mBaseAdViewControllerListener.onFinish();
            }

            @Override
            public void onReward(float cvtime) {
                isReward = true;
                HandleVpaidEvent(ADEvent.AD_REWARD);
                broadcastAction(ACTION_REWARDED_VIDEO_COMPLETE);
            }

            @Override
            public void onSkip(float progress) {
                if (!mIsSkip) {
                    mIsSkip = true;
                    HandleVpaidEvent(ADEvent.AD_SKIP);
                    broadcastAction(ACTION_REWARDED_VIDEO_SKIP);
                }
            }

            @Override
            public void onMute(boolean isMute) {
                if (isMute) {
                    HandleVpaidEvent(ADEvent.AD_MUTE);
                } else {
                    HandleVpaidEvent(ADEvent.AD_UNMUTE);
                }
            }

            @Override
            public void onEndCardShow() {
                HandleVpaidEvent(ADEvent.AD_SHOW);
            }

            @Override
            public void onCompanionClick(String ext) {

                boolean isRecord = true;//默认原逻辑走

                if (!TextUtils.isEmpty(ext)) {
                    try {
                        JSONObject object = new JSONObject(ext);
                        int tp = object.optInt("type");
                        if (object.has("x") && object.has("y")) {
                            int x = object.optInt("x");
                            int y = object.optInt("y");
                            mMraidController.updateClickCoordinate(String.valueOf(x), String.valueOf(y));
                        }
                        if (tp != 1) {
                            isRecord = true;
                            log_data = object.optJSONObject("log_data");
                        } else {
                            isRecord = false;
                        }
                    } catch (Exception e) {
                        isRecord = true;
                        mMraidController.updateClickCoordinate("0", "0");
                    }
                }

                mVideoConfig.handleUrlAction(ClickUIType.COMPANION, mMraidController.getClickCoordinate(), isRecord);

            }

            @Override
            public void onShowSkipTime() {
                HandleVpaidEvent(ADEvent.AD_SHOW_SKIP);
            }

            @Override
            public void onFeedBack() {


                Activity activity = getActivity();
                if (activity != null && mAdUnit != null) {
                    mDislikeDialog = new DisLikeDialog(activity, mAdUnit);

                    if (mDislikeDialog != null) {
                        mDislikeDialog.showDislikeDialog();
                    }
                    mDislikeDialog.setDislikeInteractionCallback(new WindNativeAdData.DislikeInteractionCallback() {
                        @Override
                        public void onShow() {
                            if (mMraidController != null) {
                                MraidWebView currentWebView = mMraidController.getCurrentWebView();
                                if (currentWebView != null) {
                                    MraidBridge mraidBridge = mMraidController.getMraidBridge();
                                    if (mraidBridge != null) {
                                        mraidBridge.notifyEvent("feedbackDidAppear", null);
                                    }
                                }
                            }

                        }

                        @Override
                        public void onSelected(int position, String value, boolean enforce) {

                            if (mMraidController != null) {
                                MraidWebView currentWebView = mMraidController.getCurrentWebView();
                                if (currentWebView != null) {
                                    MraidBridge mraidBridge = mMraidController.getMraidBridge();
                                    if (mraidBridge != null) {
                                        mraidBridge.notifyEvent("feedbackDidDisappear", null);
                                    }
                                }
                            }
                            if (mDislikeDialog != null) {
                                mDislikeDialog.dismiss();
                                mDislikeDialog.destroy();
                                mDislikeDialog = null;
                            }
                        }

                        @Override
                        public void onCancel() {
                            if (mMraidController != null) {
                                MraidWebView currentWebView = mMraidController.getCurrentWebView();
                                if (currentWebView != null) {
                                    MraidBridge mraidBridge = mMraidController.getMraidBridge();
                                    if (mraidBridge != null) {
                                        mraidBridge.notifyEvent("feedbackDidDisappear", null);
                                    }
                                }
                            }
                            if (mDislikeDialog != null) {
                                mDislikeDialog.dismiss();
                                mDislikeDialog.destroy();
                                mDislikeDialog = null;
                            }
                        }
                    });
                }
            }


            @Override
            public void onExpand() {

            }

            @Override
            public void onResize(int width, int height, int offsetX, int offsetY, CloseableLayout.ClosePosition closePosition, boolean allowOffscreen) {

            }

            @Override
            public void onUnload() {

            }

            @Override
            public void onOpenFourElements() {//打开四要素
                if (downloadDialog == null) {
                    downloadDialog = new DownloadDialog(getActivity(), mAdUnit);
                    downloadDialog.setOnPrivacyClickListener(new DownloadDialog.onPrivacyClickListener() {
                        @Override
                        public void onCloseClick() {
                            if (downloadDialog != null) {
                                downloadDialog.dismiss();
                                downloadDialog.destroy();
                                downloadDialog = null;
                                isDialogShow = false;
                            }
                            if (mExternalViewabilitySessionManager != null) {
                                mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_CLOSE, 0);
                            }
                            if (mMraidController != null) {
                                MraidWebView currentWebView = mMraidController.getCurrentWebView();
                                if (currentWebView != null) {
                                    MraidBridge mraidBridge = mMraidController.getMraidBridge();
                                    if (mraidBridge != null) {
                                        mraidBridge.notifyEvent("fourElementsDidDisappear", null);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onButtonClick(String url, String clickCoordinate) {//执行点击逻辑

                            if (mVideoConfig != null) {
                                mVideoConfig.handleUrlFourAction(ClickUIType.ENDCARD, url, clickCoordinate, true);
                            }
                        }

                        @Override
                        public void onShowSuccess() {
                            if (mExternalViewabilitySessionManager != null) {
                                mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_SHOW, 0);
                            }
                            if (mMraidController != null) {
                                MraidWebView currentWebView = mMraidController.getCurrentWebView();
                                if (currentWebView != null) {
                                    MraidBridge mraidBridge = mMraidController.getMraidBridge();
                                    if (mraidBridge != null) {
                                        mraidBridge.notifyEvent("fourElementsDidAppear", null);
                                    }
                                }
                            }
                        }
                    });
                }

                if (downloadDialog != null && downloadDialog.isRenderSuccess() && !isDialogShow) {
                    downloadDialog.show();
                    isDialogShow = true;
                }
            }

            @Override
            public void onOpen(URI uri, int InterActionType, String ext) {
                boolean isRecord = true;
                boolean disable_landing = false;
                boolean showAppElement = true;

                if (!TextUtils.isEmpty(ext)) {
                    try {
                        JSONObject object = new JSONObject(ext);
                        int tp = object.optInt("type");

                        if (object.has("x") && object.has("y")) {
                            int x = object.optInt("x");
                            int y = object.optInt("y");
                            mMraidController.updateClickCoordinate(String.valueOf(x), String.valueOf(y));
                        }

                        disable_landing = object.optBoolean("disable_landing");
                        showAppElement = !object.optBoolean("feDisable");

                        if (tp != 1) {
                            log_data = object.optJSONObject("log_data");
                            isRecord = true;
                        } else {
                            isRecord = false;
                        }
                    } catch (Exception e) {

                    }
                }
                if (disable_landing || TextUtils.isEmpty(mAdUnit.getLanding_page())) {
                    mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, uri.toString(), mMraidController.getClickCoordinate(), isRecord, showAppElement);
                } else {
                    mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, null, mMraidController.getClickCoordinate(), isRecord, showAppElement);
                }

            }
        });

        // Needed because the Activity provides the close button, not the controller. This
        // gets called if the creative calls mraid.useCustomClose.
        mMraidController.setUseCustomCloseListener(new MraidController.UseCustomCloseListener() {
            public void useCustomCloseChanged(boolean useCustomClose) {
                if (useCustomClose) {
                    hideInterstitialCloseButton();
                } else {
                    showInterstitialCloseButton();
                }
            }
        });

        if (config != null) {
            mExternalViewabilitySessionManager = config.getViewabilityManager();
        } else if (!TextUtils.isEmpty(mAdUnit.getHtmlData())) {
            mMraidController.fillContentWithHtmlData(mAdUnit.getHtmlData(),
                    new MraidController.MraidWebViewCacheListener() {
                        @Override
                        public void onReady(final MraidWebView webView,
                                            final ExternalViewabilitySessionManager viewabilityManager) {
                            if (viewabilityManager != null) {
                                mExternalViewabilitySessionManager = viewabilityManager;
                            } else {
                                mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager();
                                mExternalViewabilitySessionManager.createDisplaySession(mAdUnit);
                            }
                        }
                    });
        } else if (!TextUtils.isEmpty(mAdUnit.getHtmlUrl())) {
            mMraidController.fillContentWithUrl(mAdUnit.getHtmlUrl(),
                    new MraidController.MraidWebViewCacheListener() {
                        @Override
                        public void onReady(final MraidWebView webView,
                                            final ExternalViewabilitySessionManager viewabilityManager) {
                            if (viewabilityManager != null) {
                                mExternalViewabilitySessionManager = viewabilityManager;
                            } else {
                                mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager();
                                mExternalViewabilitySessionManager.createDisplaySession(mAdUnit);
                            }
                        }
                    });
        }

        return mMraidController.getAdContainer();
    }


//    private void addRuleLayoutWithPosition(int position, RelativeLayout.LayoutParams layoutParams) {
//
//        int padding = Dips.dipsToIntPixels(10, mContext);
//
//        switch (position) {
//            case 1:
//            case 2:
//                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                layoutParams.setMargins(padding, padding * 2, padding, padding);
//                break;
//            case 3:
//            case 4:
//                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                layoutParams.setMargins(padding, padding * 2, padding, padding);
//                break;
//        }
//    }


    private void addCloseButtonWidget(final Context context, int initialVisibility) {

        if (mCloseButtonWidget != null) return;
        mCloseButtonWidget = new VideoButtonWidget(context);
        mCloseButtonWidget.setVisibility(initialVisibility);
        mCloseButtonWidget.setId(ClientMetadata.generateViewId());
        int padding = Dips.dipsToIntPixels(10, mContext);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(22, context), Dips.dipsToIntPixels(22, context));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.setMargins(padding, padding * 2, padding, padding);
        getLayout().addView(mCloseButtonWidget, layoutParams);

        final View.OnTouchListener closeOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mIsClosing = true;
                    mVideoConfig.handleClose(getContext(), mDuration, mAdUnit);

                    getBaseAdViewControllerListener().onFinish();
                }
                return true;
            }
        };

        mCloseButtonWidget.setOnTouchListener(closeOnTouchListener);

        mCloseButtonWidget.updateCloseButtonIcon(mAdUnit);

        // Update custom close icon if specified in icon extensions
        final String customCloseIconUrl = mVideoConfig.getCustomCloseIconUrl();
        if (customCloseIconUrl != null) {
            mCloseButtonWidget.updateButtonIcon(customCloseIconUrl);
        }
    }


    private void hideInterstitialCloseButton() {
        if (mCloseButtonWidget != null) {
            mCloseButtonWidget.setVisibility(View.INVISIBLE);
        }
        if (endcardFeedBack != null) {
            endcardFeedBack.setVisibility(View.INVISIBLE);
        }
    }

    private void showInterstitialCloseButton() {
        if (mCloseButtonWidget == null) {
            addCloseButtonWidget(mContext, View.VISIBLE);
        }

        mCloseButtonWidget.setVisibility(View.VISIBLE);
        addEndCardFeedBack(getContext(), View.VISIBLE);
    }

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

                    }

                    @Override
                    public void onSelected(int position, String value, boolean enforce) {

                        if (mDislikeDialog != null) {
                            mDislikeDialog.dismiss();
                            mDislikeDialog.destroy();
                            mDislikeDialog = null;
                        }
                    }

                    @Override
                    public void onCancel() {
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


    private void addEndCardFeedBack(Context context, int visibility) {

//        if (videoAdFeedBack == null && !TextUtils.isEmpty(WindSDKConfig.getInstance().getFeedbackUrl())) {
        if (endcardFeedBack == null) {

            endcardFeedBack = new OvalButton(context);
            endcardFeedBack.setText("反馈");
            endcardFeedBack.setOnClickListener(onFeedBackListener);
            endcardFeedBack.setId(ClientMetadata.generateViewId());
            int padding = Dips.dipsToIntPixels(3, mContext);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(45, context), Dips.dipsToIntPixels(30, context));
            layoutParams.addRule(RelativeLayout.LEFT_OF, mCloseButtonWidget.getId());
            layoutParams.addRule(RelativeLayout.ALIGN_TOP,mCloseButtonWidget.getId());
            layoutParams.setMargins(0, -padding, 0, 0);
            getLayout().addView(endcardFeedBack, layoutParams);

        }
        if (endcardFeedBack != null) {

            endcardFeedBack.setVisibility(visibility);
        }
    }

    public void onCreate() {

//        if (mAdUnit != null && mAdUnit.getAd_type() == AdFormat.FULLSCREEN_VIDEO) {
//            int style = SigmobRes.getSig_transparent_style();
//            if (style > 0) {
//                getActivity().getTheme().applyStyle(style, true);
//            }
//        }

        super.onCreate();
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_SHOW, 0);
        }

        View adView = getAdView();

        getLayout().addView(adView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (mMraidController != null) {
            mMraidController.onShow(getActivity());
        }
        broadcastAction(ACTION_INTERSTITIAL_VOPEN);
        setDebugListener(new MraidWebViewDebugListener() {
            @Override
            public boolean onJsAlert(String message, JsResult result) {
                return false;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();
                switch (level){
                    case ERROR:{
                        SigmobLog.e("onConsoleMessage "+ consoleMessage.message());
                        PointEntitySigmobUtils.SigmobError("h5_error","mraid1",0,consoleMessage.message(),null,null,mAdUnit,null);
                    }break;
                }
                return false;
            }
        });
    }


    @Override
    public void onDestroy() {

        if (mExternalViewabilitySessionManager != null) {
//            mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_VCLOSE, 0);
            mExternalViewabilitySessionManager.endDisplaySession();
            mExternalViewabilitySessionManager = null;
        }
        if (mMraidController != null) {
            mMraidController.destroy();
        }
        if (mDislikeDialog != null) {
            mDislikeDialog.dismiss();
            mDislikeDialog.destroy();
            mDislikeDialog = null;
        }

        if (downloadDialog != null) {
            downloadDialog.dismiss();
            downloadDialog.destroy();
            downloadDialog = null;
            isDialogShow = false;
        }

        if (!mIsClosing || !mIsDismiss) {
            mIsDismiss = true;
            broadcastAction(IntentActions.ACTION_INTERSTITIAL_DISMISS);
        }
        if (mAdUnit != null) {
            mAdUnit.destroy();
        }
        super.onDestroy();

    }

    public void setDebugListener(MraidWebViewDebugListener debugListener) {
        mDebugListener = debugListener;
        if (mMraidController != null) {
            mMraidController.setDebugListener(debugListener);
        }
    }

    @Override
    public void onPause() {
        if (mIsClosing) {
            mIsDismiss = true;
            broadcastAction(IntentActions.ACTION_INTERSTITIAL_DISMISS);
            return;
        }
        mMraidController.onPause();
    }

    @Override
    public void onResume() {
        mMraidController.onResume();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

        mMraidController.handleOrientationChange(configuration.orientation);
    }

    @Override
    public boolean backButtonEnabled() {
        return false;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onAdClick(boolean isRecord, ClickUIType type) {
        broadcastAction(ACTION_INTERSTITIAL_CLICK);

        if (isRecord) {
            switch (type) {
                case COMPANION: {

                    ClickCommon clickCommon = mAdUnit.getClickCommon();
                    PointEntitySigmobUtils.eventRecord(ClickUIType.COMPANION,
                            PointCategory.CLICK,
                            mAdUnit, clickCommon.isDeeplink,
                            clickCommon.clickUrl, clickCommon.clickCoordinate, mDuration, log_data);

                    HandleVpaidEvent(ADEvent.AD_COMPANION_CLICK);

                }
                break;
                case VIDEO_CLICK: {
                    ClickCommon clickCommon = mAdUnit.getClickCommon();
                    PointEntitySigmobUtils.eventRecord(ClickUIType.VIDEO_CLICK,
                            PointCategory.CLICK,
                            mAdUnit, clickCommon.isDeeplink,
                            clickCommon.clickUrl, clickCommon.clickCoordinate, mDuration, log_data);

                    HandleVpaidEvent(ADEvent.AD_VIDEO_CLICK);

                }
                break;
                default: {
                    ClickCommon clickCommon = mAdUnit.getClickCommon();
                    PointEntitySigmobUtils.eventRecord(ClickUIType.ENDCARD,
                            PointCategory.CLICK,
                            mAdUnit, clickCommon.isDeeplink,
                            clickCommon.clickUrl, clickCommon.clickCoordinate, mDuration, log_data);
                    HandleVpaidEvent(ADEvent.AD_CLICK);

                }
                break;
            }
        }
        log_data = null;

    }
}
