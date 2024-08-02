// Copyright 2018-2019 Sigmob, Inc.
// Licensed under the Sigmob SDK License Agreement
// http://www.sigmob.com/legal/sdk-license-agreement/

package com.sigmob.sdk.mraid;

import static com.sigmob.sdk.mraid.MraidBridge.convertJsUndefined;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.utils.IntentUtil;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.common.AdSize;
import com.sigmob.sdk.base.common.ExternalViewabilitySessionManager;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.CurrentAppOrientation;
import com.sigmob.sdk.base.models.ExposureChange;
import com.sigmob.sdk.base.models.ExtensionEvent;
import com.sigmob.sdk.base.models.MraidEnv;
import com.sigmob.sdk.base.models.PlacementType;
import com.sigmob.sdk.base.models.VideoItem;
import com.sigmob.sdk.base.models.ViewState;
import com.sigmob.sdk.base.models.rtb.Ad;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.base.models.rtb.Tracking;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.mraid2.Mraid2Motion;
import com.sigmob.sdk.mraid2.MraidBridgeMotionListener;
import com.sigmob.sdk.nativead.APKStatusBroadcastReceiver;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;

import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

//Si06u84AoigVXO7qS8
public class MraidController {

    private final PlacementType mPlacementType;
    private final BaseAdUnit mAdUnit;
    // An ad container, which contains the ad web view in default state, but is empty when expanded.
    private final FrameLayout mDefaultAdContainer;
    // Helper classes for updating screen values
    private final ScreenMetricsWaiter mScreenMetricsWaiter;
    private final MraidScreenMetrics mScreenMetrics;
    // A bridge to handle all interactions with the WebView HTML and Javascript.
    private final MraidBridge mMraidBridge;
    private final MraidNativeCommandHandler mMraidNativeCommandHandler;
    private boolean mIsUserCustomClose = false;
    /**
     * Holds a weak reference to the activity if the context that is passed in is an activity.
     * While this field is never null, the reference could become null. This reference starts out
     * null if the passed-in context is not an activity.
     */
    // Ad ad container which contains the ad view in expanded state.
    private Integer mDuration;
    private String clickCoordinate;
    private ViewGroup mRootView;
    // Current view state
    private ViewState mViewState = ViewState.LOADING;
    // Listeners
    private MraidListener mMraidListener;
    private UseCustomCloseListener mOnCloseButtonListener;
    private MraidWebViewDebugListener mDebugListener;
    private VPaidEventListener mVpaidEventListener;
    // The WebView which will display the ad. "Two part" creatives, loaded via handleExpand(URL)
    // are shown in a separate web view
    private MraidWebView mMraidWebView;

    private OrientationBroadcastReceiver mOrientationBroadcastReceiver =
            new OrientationBroadcastReceiver();
    // Stores the requested orientation for the Activity to which this controller's view belongs.
    // This is needed to restore the Activity's requested orientation in the event that the view
    // itself requires an orientation lock.
    private Integer mOriginalActivityOrientation;
    private boolean mAllowOrientationChange = true;
    private MraidOrientation mForceOrientation = MraidOrientation.NONE;
    private boolean mIsPaused = false;
    private HashMap<String, MraidObject> mMraidObjects = new HashMap<>();


    private Mraid2Motion mraid2Motion;
    private final MraidBridge.MraidBridgeV2Listener mMraidBridgeV2Listener = new MraidBridge.MraidBridgeV2Listener() {
        @Override
        public void onVpaidEvent(String event, JSONObject args) {
            SigmobLog.d(" handleVpaidEvent event:" + event);
            SigmobLog.d(" postMessage args:" + args);
            String uniqueId = args.optString("uniqueId");

            if (TextUtils.isEmpty(uniqueId) || mMraidWebView == null) {
                SigmobLog.e(" handleVpaidEvent uniqueId is null:" + args);
                return;
            }
            MraidVpaid mraidVpaid = null;
            MraidObject mraidObject = mMraidObjects.get(uniqueId);
            if (mraidObject != null && mraidObject instanceof MraidVpaid) {
                mraidVpaid = (MraidVpaid) mraidObject;
            }

            switch (event) {
                case "init": {
                    mraidVpaid = new MraidVpaid(uniqueId);

                    mraidVpaid.setMraidVpaidListener(new MraidVpaid.MraidVpaidListener() {
                        @Override
                        public void OnReady(String uniqueId, long duration, int width, int height) {
                            if (mMraidBridge != null) {
                                mMraidBridge.notifyVPaidReady(uniqueId, (int) duration, width, height);
                            }
                        }

                        @Override
                        public void OnPlayStateChange(String uniqueId, int state) {
                            if (mMraidBridge != null) {
                                mMraidBridge.notifyVPaidPlayStateChange(uniqueId, state);
                            }
                        }

                        @Override
                        public void OnLoadStateChange(String uniqueId, int state) {
                            if (mMraidBridge != null) {
                                mMraidBridge.notifyVPaidLoadStateChange(uniqueId, state);
                            }
                        }

                        @Override
                        public void OnProgressUpdate(String uniqueId, long position, long duration) {
                            if (mMraidBridge != null) {
                                mMraidBridge.notifyVPaidPlayCurrentTime(uniqueId, (int) position, (int) duration);
                            }
                        }

                        @Override
                        public void OnPlayEnd(String uniqueId, long position) {
                            if (mMraidBridge != null) {
                                mMraidBridge.notifyVPaidPlayEnd(uniqueId, (int) position);
                            }
                        }

                        @Override
                        public void OnError(String uniqueId, int code, String message) {
                            if (mMraidBridge != null) {
                                mMraidBridge.notifyVPaidPlayError(uniqueId, "code:" + code + ", msg:" + message);
                            }
                            PointEntitySigmobUtils.SigmobError(PointCategory.VIDEO,code,message,mAdUnit);

                        }
                    });
                    mMraidObjects.put(uniqueId, mraidVpaid);
                    mraidVpaid.OnVpaidInit(mMraidWebView.getContext(), args);
                    View view = mraidVpaid.getView();
                    if (view != null) {
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(1, 1);
                        layoutParams.topMargin = -1;
                        layoutParams.rightMargin = -1;
                        mDefaultAdContainer.addView(view, layoutParams);
                    }
                }
                break;
                case "assetURL": {
                    if (mraidVpaid != null) {
                        mraidVpaid.OnVpaidAssetURL(args);
                    }
                }
                break;
                case "play": {
                    if (mraidVpaid != null) {
                        mraidVpaid.OnVpaidPlay(args);
                    }

                }
                break;
                case "replay": {
                    if (mraidVpaid != null) {
                        mraidVpaid.OnVpaidReplay(args);
                    }
                }
                break;
                case "pause": {
                    if (mraidVpaid != null) {
                        mraidVpaid.OnVpaidPause(args);
                    }

                }
                break;
                case "stop": {
                    if (mraidVpaid != null) {
                        mraidVpaid.OnVpaidStop(args);
                        mraidVpaid.destroy();
                        mMraidObjects.remove(uniqueId);
                    }

                }
                break;
                case "muted": {
                    if (mraidVpaid != null) {

                        mraidVpaid.OnVpaidMuted(args);
                    }
                }
                break;
                case "seek": {
                    if (mraidVpaid != null) {

                        mraidVpaid.OnVpaidSeek(args);
                    }

                }
                break;
                case "frame": {
                    if (mraidVpaid != null) {
                        mraidVpaid.OnVpaidFrame(args);
                    }
                }
                break;
            }

        }


        @Override
        public void onBelowSubview(String subEvent, JSONObject args) {
            String uniqueId = args.optString("uniqueId");
            if (!TextUtils.isEmpty(uniqueId)) {

                MraidObject mraidObject = mMraidObjects.get(uniqueId);

                if (mraidObject != null) {
                    View view = mraidObject.getView();
                    if (view != null) {
                        mDefaultAdContainer.bringChildToFront(mMraidWebView);
                    }
                }
            }
        }

        @Override
        public void onAddSubview(String subEvent, JSONObject args) {
            String uniqueId = args.optString("uniqueId");
            if (!TextUtils.isEmpty(uniqueId)) {

                MraidObject mraidObject = mMraidObjects.get(uniqueId);

                if (mraidObject != null) {
                    View view = mraidObject.getView();
                    if (view != null) {
                        ViewUtil.removeFromParent(view);
                        mDefaultAdContainer.addView(view);
                    }
                }
            }
        }

        @Override
        public void onMotionEvent(String subEvent, JSONObject args) {

            SigmobLog.d(" postMessage subEvent data:" + args);

            Mraid2Motion motion = null;
            String uniqueId = args.optString("uniqueId");

            if (TextUtils.isEmpty(uniqueId)) {
                SigmobLog.e(" onMotionViewEvent uniqueId is null:" + args);
                return;
            }
            MraidObject mraidObject = mMraidObjects.get(uniqueId);
            if (mraidObject instanceof Mraid2Motion) {
                motion = (Mraid2Motion) mraidObject;
            }
            switch (subEvent) {
                case "init": {
                    String type = args.optString("type", "");
                    int level = args.optInt("sensitivity", 0);

                    motion = new Mraid2Motion(uniqueId, type);
                    motion.setMraidBridgeMotionListener(new MraidBridgeMotionListener() {
                        @Override
                        public void notifyMotionEvent(String uniqueId, String type, String event, HashMap<String, Object> args) {
                            if (mMraidBridge != null) {
                                mMraidBridge.notifyMotionEvent(uniqueId, type, event, args);
                            }
                        }
                    });
                    if (level == 0) {
                        level = 3;
                    } else if (level == 2) {
                        level = 1;
                    } else if (level == 1) {
                        level = 2;
                    }
                    motion.setLevel(level);
                    motion.start();

                    mMraidObjects.put(uniqueId, motion);

                }
                break;
                case "init_sensitivity_raw": {
                    String type = args.optString("type", "");
                    int sensitivity_raw = args.optInt("sensitivity_raw", 0);

                    motion = new Mraid2Motion(uniqueId, type);
                    motion.setMraidBridgeMotionListener(new MraidBridgeMotionListener() {
                        @Override
                        public void notifyMotionEvent(String uniqueId, String type, String event, HashMap<String, Object> args) {
                            if (mMraidBridge != null) {
                                mMraidBridge.notifyMotionEvent(uniqueId, type, event, args);
                            }
                        }
                    });

                    motion.setRawSensitivity(sensitivity_raw);
                    motion.start();
                    mMraidObjects.put(uniqueId, motion);

                }
                break;
                case "destroy": {
                    if (motion != null) {
                        motion.destroy();
                    }
                    mMraidObjects.remove(uniqueId);

                }
                break;
                default: {

                }
                break;
            }


        }

        @Override
        public void onMotionViewEvent(String subEvent, JSONObject args) {
            MraidMotionView motionView = null;
            SigmobLog.d(" onMotionViewEvent event:" + subEvent);
            SigmobLog.d(" postMessage args:" + args);
            String uniqueId = args.optString("uniqueId");

            if (TextUtils.isEmpty(uniqueId)) {
                SigmobLog.e(" onMotionViewEvent uniqueId is null:" + args);
                return;
            }
            MraidObject mraidObject = mMraidObjects.get(uniqueId);
            if (mraidObject != null && mraidObject instanceof MraidMotionView) {
                motionView = (MraidMotionView) mraidObject;
            }

            switch (subEvent) {
                case "init": {
                    int type = args.optInt("type");

                    motionView = new MraidMotionView(SDKContext.getApplication(), uniqueId, type);

                    motionView.setMotionListener(new MraidBridgeMotionListener() {
                        @Override
                        public void notifyMotionEvent(String uniqueId, String type, String event, HashMap<String, Object> args) {
                            if (mMraidBridge != null) {
                                mMraidBridge.notifyMotionEvent(uniqueId, type, event, args);
                            }
                        }
                    });
                    mMraidObjects.put(uniqueId, motionView);
                }
                break;
                case "sensitivity": {
                    if (motionView != null) {
                        motionView.sensitivity(args.optInt("sensitivity"));
                    }
                }
                break;
                case "sensitivity_raw": {
                    if (motionView != null) {
                        motionView.setRawSensitivity(args.optInt("sensitivity_raw"));
                    }
                }
                break;
                case "start": {
                    if (motionView != null) {
                        motionView.start();
                    }
                }
                break;
                case "hidden": {
                    if (motionView != null) {
                        motionView.setHidden(args.optBoolean("hidden"));
                    }
                }
                break;
                case "destroy": {
                    if (motionView != null) {
                        motionView.destroy();
                    }
                    mMraidObjects.remove(uniqueId);
                }
                break;
                case "frame": {
                    if (motionView != null) {
                        motionView.OnFrame(args);
                    }
                }
                break;
            }
        }
    };
    @SuppressWarnings("FieldCanBeLocal")
    private final MraidBridge.MraidBridgeListener mMraidBridgeListener = new MraidBridge.MraidBridgeListener() {
        @Override
        public void onPageLoaded() {
            handlePageLoad();
            if (mMraidListener != null) {
                mMraidListener.onLoaded(mDefaultAdContainer);
            }
        }

        @Override
        public void onMraidJsLoaded() {
            handleMraidLoad();
        }

        @Override
        public void onPageFailedToLoad() {
            if (mMraidListener != null) {
                mMraidListener.onFailedToLoad();
            }
        }

        @Override
        public void onRenderProcessGone(final WindAdError errorCode) {
            handleRenderProcessGone(errorCode);
        }

        @Override
        public void onVisibilityChanged(final boolean isVisible) {
            // The bridge only receives visibility events if there is no 2 part covering it
            mMraidBridge.notifyViewability(isVisible);

        }

        @Override
        public boolean onJsAlert(final String message, final JsResult result) {
            return handleJsAlert(message, result);
        }

        @Override
        public boolean onConsoleMessage(final ConsoleMessage consoleMessage) {
            return handleConsoleMessage(consoleMessage);
        }

        @Override
        public void onClose() {
            handleClose();
            mMraidListener.onClose();
        }

        @Override
        public void onExpand(final URI uri, final boolean shouldUseCustomClose)
                throws MraidCommandException {
            handleExpand(uri, shouldUseCustomClose);
        }

        @Override
        public void onUseCustomClose(final boolean shouldUseCustomClose) {
            handleCustomClose(shouldUseCustomClose);
        }

        @Override
        public void onSetOrientationProperties(final boolean allowOrientationChange,
                                               final MraidOrientation forceOrientation) throws MraidCommandException {
            handleSetOrientationProperties(allowOrientationChange, forceOrientation);
        }

        @Override
        public void onOpen(final URI uri, int type, String ext) {
            handleOpen(uri, type, ext);
        }

        @Override
        public void onPlayVideo(final URI uri) {
            handleShowVideo(uri.toString());
        }

        @Override
        public void onVPaidEvent(String event, Map<String, String> params) {
            handleVpaidEvent(event, params);
        }

        @Override
        public void onExtensionEvent(String event, Map<String, String> params) {
            handleExtensionEvent(event, params);
        }

        @Override
        public void onUnload() {
            if (mMraidListener != null) {
                mMraidListener.onUnload();
            }
        }

        @Override
        public void onOpenFourElements() {
            if (mMraidListener != null) {
                mMraidListener.onOpenFourElements();
            }
        }


        @Override
        public void onResize(int width, int height, int offsetX, int offsetY, CloseableLayout.ClosePosition closePosition, boolean allowOffscreen) {
            if (mMraidListener != null) {
                mMraidListener.onResize(width, height, offsetX, offsetY, closePosition, allowOffscreen);
            }
        }

        @Override
        public void onFeedBack() {
            if (mMraidListener != null) {
                mMraidListener.onFeedBack();
            }
        }

    };
    private APKStatusBroadcastReceiver mAPKStatusBroadcastReceiver;
    private boolean isLoaded;

    private AdSize adSize;

    public MraidController(Context context, BaseAdUnit adUnit, PlacementType placementType) {
        this(context, adUnit, placementType,
                new MraidBridge(adUnit, placementType),
                null,
                new ScreenMetricsWaiter());
    }

    MraidController(Context context, BaseAdUnit adUnit,
                    PlacementType placementType,
                    MraidBridge bridge, MraidBridge twoPartBridge,
                    ScreenMetricsWaiter screenMetricsWaiter) {
        Preconditions.checkNotNull(context);


        mDefaultAdContainer = new FrameLayout(context);

        mAdUnit = adUnit;
        mPlacementType = placementType;
        mMraidBridge = bridge;
        mScreenMetricsWaiter = screenMetricsWaiter;

        mViewState = ViewState.LOADING;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mScreenMetrics = new MraidScreenMetrics(context, displayMetrics.density);

        /**
         * Mraid广告和推荐关闭页都设置透明背景
         */
        mDefaultAdContainer.setBackgroundColor(Color.TRANSPARENT);

        View dimmingView = new View(context);
        dimmingView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mOrientationBroadcastReceiver.register(context);

        mMraidBridge.setMraidBridgeListener(mMraidBridgeListener);
        mMraidBridge.setMraidBridgeV2Listener(mMraidBridgeV2Listener);
        mMraidNativeCommandHandler = new MraidNativeCommandHandler();
    }


    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
    }

    private Context getContext() {

        if (mDefaultAdContainer != null) {
            return mDefaultAdContainer.getContext();
        }
        return null;
    }

    static void callMraidListenerCallbacks(final MraidListener mraidListener,
                                           final ViewState previousViewState, final ViewState currentViewState) {
        Preconditions.checkNotNull(mraidListener);
        Preconditions.checkNotNull(previousViewState);
        Preconditions.checkNotNull(currentViewState);

//        if (currentViewState == ViewState.EXPANDED) {
//            mraidListener.onExpand();
//        } else if (previousViewState == ViewState.EXPANDED && currentViewState == ViewState.DEFAULT) {
//            mraidListener.onClose();
//        } else if (currentViewState == ViewState.HIDDEN) {
//            mraidListener.onClose();
//        } else if (previousViewState == ViewState.RESIZED && currentViewState == ViewState.DEFAULT) {
//            mraidListener.onResize(true);
//        } else if (currentViewState == ViewState.RESIZED) {
//            mraidListener.onResize(false);
//        }
    }


    public String getClickCoordinate() {
        return mMraidBridge.getClickCoordinate();
    }

    public void onPause() {
        mIsPaused = true;
        mMraidBridge.notifyViewability(false);
    }

    public void onResume() {
        if (mMraidWebView != null) {
            mMraidWebView.resumeTimers();
        }
        if (mIsPaused){
            mMraidBridge.notifyViewability(true);
        }
        mIsPaused = false;
    }

    public void onDownloadStart(boolean result) {
        if (result) {
            mMraidBridge.notifyApkDownloadStart();
        } else {
            mMraidBridge.notifyApkDownloadFail();
        }
    }

    public void onDownloadEnd(boolean result) {
        if (result) {
            mMraidBridge.notifyApkDownloadEnd();
        } else {
            mMraidBridge.notifyApkDownloadFail();
        }
    }

    public void onInstallAPK(boolean result) {
        if (result) {
            mMraidBridge.notifyApkDownloadInstalled();
        }
    }

    public void onExposeChange() {
        ExposureChange exposureChange = new ExposureChange(100, new Rect(0, 0, getRootView().getWidth(), getRootView().getHeight()), null);
        mMraidBridge.notifyExposureChange(exposureChange);
    }

    public void updateClickCoordinate(String x, String y) {
        mMraidBridge.setClickCoordinate(x, y);
    }

    private void handleExtensionEvent(String event, Map<String, String> params) {
        if (mMraidListener == null) return;

        try {
            String ext = convertJsUndefined(params.get("ext"));
//            String x = convertJsUndefined(params.get("x"));
//            String y = convertJsUndefined(params.get("y"));
            String ctime = convertJsUndefined(params.get("ctime"));
            String state = convertJsUndefined(params.get("state"));

            switch (event) {
                case ExtensionEvent.AD_SKIP: {

                    Float cvtime = 0.f;
                    try {
                        cvtime = Float.valueOf(ctime);
                    } catch (Throwable throwable) {

                    }
                    mMraidListener.onSkip(cvtime);
                }
                break;
                case ExtensionEvent.AD_REWARD: {

                    Float cvtime = 0.f;
                    try {
                        cvtime = Float.valueOf(ctime);
                    } catch (Throwable throwable) {

                    }
                    mMraidListener.onReward(cvtime);
                }
                break;
                case ExtensionEvent.AD_MUTE: {
                    boolean ismute = state.equalsIgnoreCase("true") || state.equalsIgnoreCase("1");
                    mMraidListener.onMute(ismute);
                }
                break;
                case ExtensionEvent.AD_ENDCARD_SHOW: {

                    mMraidListener.onEndCardShow();

                }
                break;
                case ExtensionEvent.AD_COMPANION_CLICK: {

                    mMraidListener.onCompanionClick(ext);

                }
                break;
                case ExtensionEvent.AD_SHOE_SKIP_TIME: {
                    mMraidListener.onShowSkipTime();
                }
                break;
                default: {
                }
                break;
            }
        } catch (Throwable throwable) {

        }
    }

    private void handleVpaidEvent(String event, Map<String, String> params) {

        if (mVpaidEventListener == null) return;

        switch (event) {
            case VPAID_EVENT.AD_CLICK_THRU: {
                mVpaidEventListener.adClickThru(params);
            }
            break;
            case VPAID_EVENT.AD_ERROR: {
                mVpaidEventListener.adError(params);

            }
            break;
            case VPAID_EVENT.AD_IMPRESSION: {
                mVpaidEventListener.adImpression();
            }
            break;
            case VPAID_EVENT.AD_PAUSED: {
                mVpaidEventListener.adPaused();
            }
            break;
            case VPAID_EVENT.AD_PLAYING: {
                mVpaidEventListener.adPlaying();
            }
            break;
            case VPAID_EVENT.AD_VIDEO_COMPLETE: {
                mVpaidEventListener.adVideoComplete();
            }
            break;
            case VPAID_EVENT.AD_VIDEO_FIRST_QUARTILE: {
                mVpaidEventListener.adVideoFirstQuartile();
            }
            break;
            case VPAID_EVENT.AD_VIDEO_THIRD_QUARTILE: {
                mVpaidEventListener.adVideoThirdQuartile();
            }
            break;
            case VPAID_EVENT.AD_VIDEO_MIDPOINT: {
                mVpaidEventListener.adVideoMidpoint();
            }
            break;
            case VPAID_EVENT.AD_VIDEO_START: {
                mVpaidEventListener.adVideoStart();
            }
            break;
            default: {
            }
        }
    }

    public void setMraidListener(MraidListener mraidListener) {
        mMraidListener = mraidListener;
    }

    public void setVpaidEventListener(VPaidEventListener vpaidEventListener) {
        mVpaidEventListener = vpaidEventListener;
    }

    public void setUseCustomCloseListener(UseCustomCloseListener listener) {
        mOnCloseButtonListener = listener;
    }

    public void setDebugListener(MraidWebViewDebugListener debugListener) {
        mDebugListener = debugListener;
    }

    /**
     * Creates an MraidWebView and fills it with data.
     *
     * @param htmlData The HTML of the ad. This will only be loaded if a cached WebView
     *                 is not found.
     * @param listener Optional listener that (if non-null) is notified when an
     *                 MraidWebView is loaded from the cache or created.
     */
    public void fillContentWithHtmlData(final String htmlData,
                                        final MraidWebViewCacheListener listener) {

        mMraidWebView = new MraidWebView(getContext());

        mMraidWebView.enablePlugins(true);
        /**
         * Mraid广告和推荐关闭页都设置透明背景
         */
        mMraidWebView.setBackgroundColor(Color.TRANSPARENT);
        if (listener != null) {
            listener.onReady(mMraidWebView, null);
        }


        mMraidBridge.attachView(mMraidWebView);
        mDefaultAdContainer.addView(mMraidWebView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));


        mMraidBridge.setContentHtml(getContext(), htmlData);
    }

    /**
     * Creates an MraidWebView and fills it with data.
     *
     * @param url      The HTML of the ad. This will only be loaded if a cached WebView
     *                 is not found.
     * @param listener Optional listener that (if non-null) is notified when an
     *                 MraidWebView is loaded from the cache or created.
     */
    public void fillContentWithUrl(final String url, final MraidWebViewCacheListener listener) {

        mMraidWebView = new MraidWebView(getContext());

        mMraidWebView.enablePlugins(true);
        /**
         * Mraid广告和推荐关闭页都设置透明背景
         */
        mMraidWebView.setBackgroundColor(Color.TRANSPARENT);
        if (listener != null) {
            listener.onReady(mMraidWebView, null);
        }
        mMraidBridge.attachView(mMraidWebView);
        mDefaultAdContainer.addView(mMraidWebView,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mMraidBridge.setContentUrl(url);
    }

    /**
     * Updates the activity and calls any onShow() callbacks when an ad is showing.
     *
     * @param activity The new activity associated with this mraid controller
     */
    public void onShow(final Activity activity) {

        int showCloseTime = 5;
//        if (mAdUnit.getRvAdSetting() != null && mAdUnit.getSkipSeconds() != -1) {
//            showCloseTime = mAdUnit.getSkipSeconds();
//        }
//
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mOnCloseButtonListener != null) {
                    mOnCloseButtonListener.useCustomCloseChanged(mIsUserCustomClose);
                }
            }
        }, showCloseTime * 1000);
    }

    public void getAdPlayTime(ValueCallback callback) {
        mMraidBridge.notifyVpaidGetPlayProgress(callback);
    }


    public MraidBridge getMraidBridge() {
        return mMraidBridge;
    }

    public Integer getAdDuration() {
        if (mDuration != null) {
            return mDuration;
        }
        mMraidBridge.notifyVpaidGetAdDuration(new ValueCallback<String>() {

            @Override
            public void onReceiveValue(String value) {
                try {
                    if (!value.equalsIgnoreCase("null") && !value.equalsIgnoreCase("undefined")) {
                        Float duration = Float.valueOf(value);
                        if (duration != null && duration > 0.00001) {
                            mDuration = (int) (duration * 1000);

                            if (mVpaidEventListener != null) {
                                mVpaidEventListener.onVideoPrepared(mDuration);
                            }
                        }
                    }
                } catch (Throwable throwable) {

                }
            }
        });
        return 0;
    }

    // onPageLoaded gets fired once the html is loaded into the webView.
    private int getDisplayRotation() {
        return ClientMetadata.getInstance().getScreenOrientation(getContext());
    }

    boolean handleConsoleMessage(final ConsoleMessage consoleMessage) {
        //noinspection SimplifiableIfStatement
        if (mDebugListener != null) {
            return mDebugListener.onConsoleMessage(consoleMessage);
        }
        return true;
    }

    boolean handleJsAlert(final String message, final JsResult result) {
        if (mDebugListener != null) {
            return mDebugListener.onJsAlert(message, result);
        }
        result.confirm();
        return true;
    }

    public MraidWebView getCurrentWebView() {
        return mMraidWebView;
    }

    /**
     * Checks that the hardware acceleration is enabled.
     * <p>
     * Will always return true for PlacementType.INTERSTITIAL since those activities will always
     * force hardware acceleration when created.
     */

    boolean isInlineVideoAvailable() {
        final Activity activity = ViewUtil.getActivityFromViewTop(mDefaultAdContainer);
        //noinspection SimplifiableIfStatement
        if (activity == null || getCurrentWebView() == null) {
            return false;
        } else if (mPlacementType != PlacementType.INLINE) {
            return true;
        }

        return mMraidNativeCommandHandler.isInlineVideoAvailable(activity, getCurrentWebView());
    }

    void loadDefualtScreenMetrics() {
        String cuurrentOrientation = ClientMetadata.getInstance().getOrientationInt() == 1 ? "portrait" : "landscape";

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        if (adSize != null) {
            mScreenMetrics.setScreenSize(adSize.getWidth(), adSize.getHeight());
        } else {
            mScreenMetrics.setScreenSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }

        View rootView = getRootView();
        mScreenMetrics.setRootViewPosition(0, 0, rootView.getMeasuredWidth(), rootView.getMeasuredHeight());

        mScreenMetrics.setDefaultAdPosition(0, 0, rootView.getMeasuredWidth(), rootView.getMeasuredHeight());

        mScreenMetrics.setCurrentAdPosition(0, 0, rootView.getMeasuredWidth(), rootView.getMeasuredHeight());

        // Always notify both bridges of the new metrics
        CurrentAppOrientation orientation = new CurrentAppOrientation(cuurrentOrientation, true);
        mMraidBridge.notifyAppOrientation(orientation);
        mMraidBridge.notifyScreenMetrics(mScreenMetrics);

    }


    @SuppressLint("JavascriptInterface")
    void handleMraidLoad() {
        WindAds.sharedAds().getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mMraidBridge.notifyENV(new MraidEnv());
                    mMraidBridge.notifySupports(
                            mMraidNativeCommandHandler.isSmsAvailable(getContext()),
                            mMraidNativeCommandHandler.isTelAvailable(getContext()),
                            MraidNativeCommandHandler.isCalendarAvailable(getContext()),
                            MraidNativeCommandHandler.isStorePictureSupported(getContext()),
                            isInlineVideoAvailable(), true, true);
                    mMraidBridge.notifyPlacementType(mPlacementType);
                    mMraidBridge.notifyRvseting(mAdUnit.getRvAdSetting());
//            AppInfo appInfo =  new AppInfo(Constants.SDK_VERSION,ClientMetadata.getInstance().getAppVersion(),"2");
//            mMraidBridge.notifyAppInfo(appInfo);

                    if (!TextUtils.isEmpty(mAdUnit.getVideo_url())) {
                        String videoUrl;
                        videoUrl = mAdUnit.getProxyVideoUrl();
                        VideoItem videoItem;
                        if (mAdUnit.getMaterial().video_size != null) {
                            videoItem = new VideoItem(videoUrl, mAdUnit.getMaterial().video_size.width, mAdUnit.getMaterial().video_size.height);
                        } else {
                            videoItem = new VideoItem(videoUrl, 0, 0);
                        }

                        mMraidBridge.notifyVideo(videoItem);
                        getAdDuration();
                    }

                    MaterialMeta.Builder materialMetaBuilder = mAdUnit.getMaterial().newBuilder();
                    if (!mAdUnit.getMaterial().has_companion_endcard) {
                        materialMetaBuilder = materialMetaBuilder.companion(null);
                    }
                    MaterialMeta materialMeta = materialMetaBuilder.html_snippet(null).html_url(null).
                            deeplink_url(null).landing_page(null).web_event_handle(null).endcard_url(null).build();

                    mMraidBridge.notifyMaterial(materialMeta);

                    //Ad
                    Ad.Builder builder = mAdUnit.getAd().newBuilder();
                    Ad ad = builder.materials(new LinkedList<MaterialMeta>()).ad_tracking(new LinkedList<Tracking>()).build();
                    mMraidBridge.notifyAd(ad,mAdUnit.getSlotAdSetting());
                } catch (Throwable th) {
                    SigmobLog.e("handleMraidLoad", th);
                }
                if (isLoaded) {
                    handlePageLoad();
                }

            }
        });

    }

    @SuppressLint("JavascriptInterface")
    void handlePageLoad() {

        try {

            isLoaded = true;
            loadDefualtScreenMetrics();
            setViewState(ViewState.DEFAULT);
            onExposeChange();
            mMraidBridge.notifyReady();
            mMraidBridge.notifyViewability(true);

            try {
                mMraidBridge.notifyVPaidStartAd();
                applyOrientation();
                DeviceContext deviceContext = SDKContext.getDeviceContext();
                Location location = deviceContext != null ? deviceContext.getLocation() : ClientMetadata.getInstance().getLocation();

                mMraidBridge.notifyLocation(location);

            } catch (Throwable e) {
                SigmobLog.e("Failed to apply orientation.");
            }
        } catch (Throwable throwable) {

            SigmobLog.e("handlePageLoad error", throwable);
        }
    }


    /**
     * Updates screen metrics, calling the successRunnable once they are available. The
     * successRunnable will always be called asynchronously, ie on the next main thread loop.
     */
    private void updateScreenMetricsAsync(final Runnable successRunnable) {
        // Don't allow multiple metrics wait requests at once
        mScreenMetricsWaiter.cancelLastRequest();

        // Determine which web view should be used for the current ad position
        final View currentWebView = getCurrentWebView();
        if (currentWebView == null || mIsPaused) {
            return;
        }

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        if (adSize != null) {
            mScreenMetrics.setScreenSize(adSize.getWidth(), adSize.getHeight());
        } else {
            mScreenMetrics.setScreenSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }

        int[] location = new int[2];
        View rootView = getRootView();
        rootView.getLocationOnScreen(location);
        mScreenMetrics.setRootViewPosition(location[0], location[1],
                rootView.getWidth(),
                rootView.getHeight());

        mScreenMetrics.setDefaultAdPosition(location[0], location[1],
                rootView.getWidth(),
                rootView.getHeight());

        currentWebView.getLocationOnScreen(location);
        mScreenMetrics.setCurrentAdPosition(location[0], location[1],
                currentWebView.getWidth(),
                currentWebView.getHeight());

        // Always notify both bridges of the new metrics
        String cuurrentOrientation = ClientMetadata.getInstance().getOrientationInt() == 1 ? "portrait" : "landscape";
        CurrentAppOrientation orientation = new CurrentAppOrientation(cuurrentOrientation, true);
        mMraidBridge.notifyAppOrientation(orientation);
        mMraidBridge.notifyScreenMetrics(mScreenMetrics);

        if (successRunnable != null) {
            successRunnable.run();
        }

    }

    void handleOrientationChange(int currentRotation) {
        SigmobLog.i("handleOrientationChange " + currentRotation);
        updateScreenMetricsAsync(null);
    }

    public void pause(boolean isFinishing) {
        mIsPaused = true;

        // This causes an inline video to pause if there is one playing
        if (mMraidWebView != null) {
            mMraidWebView.onPause(isFinishing);
        }

    }

    public void resume() {
        mIsPaused = false;

        // This causes an inline video to resume if it was playing previously
        if (mMraidWebView != null) {
            mMraidWebView.onResume();
        }

    }

    public void destroy() {
        mScreenMetricsWaiter.cancelLastRequest();

        mOnCloseButtonListener = null;
        mMraidListener = null;
//        mVpaidEventListener = null;

        for (MraidObject object : mMraidObjects.values()) {
            object.destroy();
        }

        mMraidObjects.clear();
        try {
            mOrientationBroadcastReceiver.unregister();
        } catch (Throwable e) {

        }

        try {
            if (mAPKStatusBroadcastReceiver != null) {
                mAPKStatusBroadcastReceiver.unregister(mAPKStatusBroadcastReceiver);
            }
        } catch (Throwable th) {

        }
        // Pause the controller to make sure the video gets stopped.
        if (!mIsPaused) {
            pause(true);
        }

        // Calling destroy eliminates a memory leak on Gingerbread devices
        detachMraidWebView();
        unApplyOrientation();
    }

    private void detachMraidWebView() {
        mMraidBridge.detach();
        mMraidWebView = null;
    }


    int clampInt(int min, int target, int max) {
        return Math.max(min, Math.min(target, max));
    }

    void handleResize(final int widthDips, final int heightDips, final int offsetXDips,
                      final int offsetYDips, final CloseableLayout.ClosePosition closePosition,
                      final boolean allowOffscreen)
            throws MraidCommandException {

//        if (mMraidWebView == null) {
//            throw new MraidCommandException("Unable to resize after the WebView is destroyed");
//        }
//
//        // The spec says that there is no effect calling resize from loaded or hidden, but that
//        // calling it from expanded should raise an error.
//        if (mViewState == ViewState.LOADING
//                || mViewState == ViewState.HIDDEN) {
//            return;
//        } else if (mViewState == ViewState.EXPANDED) {
//            throw new MraidCommandException("Not allowed to resize from an already expanded ad");
//        }
//
//        if (mPlacementType == PlacementType.INTERSTITIAL) {
//            throw new MraidCommandException("Not allowed to resize from an interstitial ad");
//        }
//
//        // Translate coordinates to px and get the resize rect
//        int width = Dips.dipsToIntPixels(widthDips, mContext);
//        int height = Dips.dipsToIntPixels(heightDips, mContext);
//        int offsetX = Dips.dipsToIntPixels(offsetXDips, mContext);
//        int offsetY = Dips.dipsToIntPixels(offsetYDips, mContext);
//        int left = mScreenMetrics.getDefaultAdRect().left + offsetX;
//        int top = mScreenMetrics.getDefaultAdRect().top + offsetY;
//        Rect resizeRect = new Rect(left, top, left + width, top + height);
//
//        if (!allowOffscreen) {
//            // Require the entire ad to be on-screen.
//            Rect bounds = mScreenMetrics.getRootViewRect();
//            if (resizeRect.width() > bounds.width() || resizeRect.height() > bounds.height()) {
//                throw new MraidCommandException("resizeProperties specified a size ("
//                        + widthDips + ", " + heightDips + ") and offset ("
//                        + offsetXDips + ", " + offsetYDips + ") that doesn't allow the ad to"
//                        + " appear within the max allowed size ("
//                        + mScreenMetrics.getRootViewRectDips().width() + ", "
//                        + mScreenMetrics.getRootViewRectDips().height() + ")");
//            }
//
//            // Offset the resize rect so that it displays on the screen
//            int newLeft = clampInt(bounds.left, resizeRect.left, bounds.right - resizeRect.width());
//            int newTop = clampInt(bounds.top, resizeRect.top, bounds.bottom - resizeRect.height());
//            resizeRect.offsetTo(newLeft, newTop);
//        }
//
//        // The entire close region must always be visible.
//        Rect closeRect = new Rect();
//        mCloseableAdContainer.applyCloseRegionBounds(closePosition, resizeRect, closeRect);
//        if (!mScreenMetrics.getRootViewRect().contains(closeRect)) {
//            throw new MraidCommandException("resizeProperties specified a size ("
//                    + widthDips + ", " + heightDips + ") and offset ("
//                    + offsetXDips + ", " + offsetYDips + ") that doesn't allow the close"
//                    + " region to appear within the max allowed size ("
//                    + mScreenMetrics.getRootViewRectDips().width() + ", "
//                    + mScreenMetrics.getRootViewRectDips().height() + ")");
//        }
//
//        if (!resizeRect.contains(closeRect)) {
//            throw new MraidCommandException("resizeProperties specified a size ("
//                    + widthDips + ", " + height + ") and offset ("
//                    + offsetXDips + ", " + offsetYDips + ") that don't allow the close region to appear "
//                    + "within the resized ad.");
//        }
//
//        // Resized ads always rely on the creative's close button (as if useCustomClose were true)
//
//        // Put the ad in the closeable container and resize it
//        LayoutParams layoutParams = new LayoutParams(resizeRect.width(), resizeRect.height());
//        layoutParams.leftMargin = resizeRect.left - mScreenMetrics.getRootViewRect().left;
//        layoutParams.topMargin = resizeRect.top - mScreenMetrics.getRootViewRect().top;
//        if (mViewState == ViewState.DEFAULT) {
//            mDefaultAdContainer.removeView(mMraidWebView);
//            mDefaultAdContainer.setVisibility(View.INVISIBLE);
//                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//        } else if (mViewState == ViewState.RESIZED) {
//        }
//
//        setViewState(ViewState.RESIZED);
    }

    void handleExpand(URI uri, boolean shouldUseCustomClose)
            throws MraidCommandException {

        return;
//        if (mMraidWebView == null) {
//            throw new MraidCommandException("Unable to expand after the WebView is destroyed");
//        }
//
//        if (mPlacementType == PlacementType.INTERSTITIAL) {
//            return;
//        }
//
//        if (mViewState != ViewState.DEFAULT && mViewState != ViewState.RESIZED) {
//            return;
//        }
//
//        applyOrientation();
//
//        // For two part expands, create a new web view
//        boolean isTwoPart = (uri != null);
//        if (isTwoPart) {
//            // Of note: the two part ad will start off with its view state as LOADING, and will
//            // transition to EXPANDED once the page is fully loaded
//            mTwoPartWebView = new MraidWebView(mContext);
//            mTwoPartBridge.attachView(mTwoPartWebView);
//
//            // onPageLoaded gets fired once the html is loaded into the two part webView
//            mTwoPartBridge.setContentUrl(uri.toString());
//        }
//
//        // Make sure the correct webView is in the closeable  container and make it full screen
//        LayoutParams layoutParams = new LayoutParams(
//                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        if (mViewState == ViewState.DEFAULT) {
//            if (isTwoPart) {
//            } else {
//                mDefaultAdContainer.removeView(mMraidWebView);
//                mDefaultAdContainer.setVisibility(View.INVISIBLE);
//            }
//            getAndMemoizeRootView().addView(mCloseableAdContainer,
//                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//        } else if (mViewState == ViewState.RESIZED) {
//            if (isTwoPart) {
//                // Move the ad back to the original container so that when we close the
//                // resized ad, it will be in the correct place
//                mCloseableAdContainer.removeView(mMraidWebView);
//                mDefaultAdContainer.addView(mMraidWebView, layoutParams);
//                mDefaultAdContainer.setVisibility(View.INVISIBLE);
//                mCloseableAdContainer.addView(mTwoPartWebView, layoutParams);
//            }
//            // If we were resized and not 2 part, nothing to do.
//        }
//        mCloseableAdContainer.setLayoutParams(layoutParams);
//        handleCustomClose(shouldUseCustomClose);
//
//        // Update to expanded once we have new screen metrics. This won't update the two-part ad,
//        // because it is not yet loaded.
//        setViewState(ViewState.EXPANDED);
    }

    protected void handleClose() {
        if (mMraidWebView == null) {
            // Doesn't throw an exception because the ad has been destroyed
            return;
        }

        if (mViewState == ViewState.LOADING || mViewState == ViewState.HIDDEN) {
            return;
        }

        // Unlock the orientation before changing the view hierarchy.
        if (mViewState == ViewState.EXPANDED || mPlacementType == PlacementType.INTERSTITIAL) {
            unApplyOrientation();
        }

        if (mViewState == ViewState.RESIZED || mViewState == ViewState.EXPANDED) {
            // Move the web view from the closeable container back to the default container
            mDefaultAdContainer.addView(mMraidWebView, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mDefaultAdContainer.setVisibility(View.VISIBLE);

            // Set the view state to default
            setViewState(ViewState.DEFAULT);
        } else if (mViewState == ViewState.DEFAULT) {
            mDefaultAdContainer.setVisibility(View.INVISIBLE);
            setViewState(ViewState.HIDDEN);
        }

    }

    void handleRenderProcessGone(final WindAdError error) {
        if (mMraidListener != null) {
            mMraidListener.onRenderProcessGone(error);
        }
    }

    private ViewGroup getRootView() {
        if (mRootView != null) {
            return mRootView;
        }

        final View bestRootView = ViewUtil.getTopmostView(getContext(),
                mDefaultAdContainer);
        return mRootView = bestRootView instanceof ViewGroup
                ? (ViewGroup) bestRootView
                : mDefaultAdContainer;
    }

    void handleShowVideo(String videoUrl) {
//        MraidVideoPlayerActivity.startMraid(mContext, videoUrl);
    }

    void lockOrientation(final int screenOrientation) throws MraidCommandException {
        final Activity activity = ViewUtil.getActivityFromViewTop(mDefaultAdContainer);
        if (activity == null || !shouldAllowForceOrientation(mForceOrientation)) {
            throw new MraidCommandException("Attempted to lock orientation to unsupported value: " + mForceOrientation.name());
        }

        if (mOriginalActivityOrientation == null) {
            mOriginalActivityOrientation = activity.getRequestedOrientation();
        }

        try {
            activity.setRequestedOrientation(screenOrientation);
        } catch (Exception e) {
            SigmobLog.e("lockOrientation: " + e.getMessage());
        }
    }

    void applyOrientation() throws MraidCommandException {
        if (mForceOrientation == MraidOrientation.NONE) {
            if (mAllowOrientationChange) {
                // If screen orientation can be changed, an orientation of NONE means that any
                // orientation lock should be removed
                unApplyOrientation();
            } else {
                final Activity activity = ViewUtil.getActivityFromViewTop(mDefaultAdContainer);
                if (activity == null) {
                    throw new MraidCommandException("Unable to set MRAID expand orientation to " +
                            "'none'; expected passed in Activity Context.");
                }
                // If screen orientation cannot be changed and we can obtain the current
                // screen orientation, locking it to the current orientation is a best effort
                lockOrientation(ClientMetadata.getInstance().getScreenOrientation(activity));
            }
        } else {
            // Otherwise, we have a valid, non-NONE orientation. Lock the screen based on this value
            lockOrientation(mForceOrientation.getActivityInfoOrientation());
        }
    }

    /*
     * Prefer this method over getAndMemoizeRootView() when the rootView is only being used for
     * screen size calculations (and not for adding/removing anything from the view hierarchy).
     * Having consistent return values is less important in the former case.
     */

    void unApplyOrientation() {
        try {
            final Activity activity = ViewUtil.getActivityFromViewTop(mDefaultAdContainer);
            if (activity != null && mOriginalActivityOrientation != null) {
                activity.setRequestedOrientation(mOriginalActivityOrientation);
            }
            mOriginalActivityOrientation = null;
        } catch (Exception e) {
            SigmobLog.e("unApplyOrientation: " + e.getMessage());
        }
    }

    boolean shouldAllowForceOrientation(final MraidOrientation newOrientation) {
        // NONE is the default and always allowed
        if (newOrientation == MraidOrientation.NONE) {
            return true;
        }

        final Activity activity = ViewUtil.getActivityFromViewTop(mDefaultAdContainer);
        // If we can't obtain an Activity, return false
        if (activity == null) {
            return false;
        }

        final ActivityInfo activityInfo;
        try {
            activityInfo = activity.getPackageManager().getActivityInfo(
                    new ComponentName(activity, activity.getClass()), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        // If an orientation is explicitly declared in the manifest, allow forcing this orientation
        final int activityOrientation = activityInfo.screenOrientation;
        if (activityOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            return activityOrientation == newOrientation.getActivityInfoOrientation();
        }

        return true;
//        // Make sure the config changes won't tear down the activity when moving to this orientation
//        // The necessary configChanges must always include "orientation"
//        boolean containsNecessaryConfigChanges =
//                bitMaskContainsFlag(activityInfo.configChanges, CONFIG_ORIENTATION);
//
//        // configChanges must also include "screenSize"
//        containsNecessaryConfigChanges = containsNecessaryConfigChanges
//                && bitMaskContainsFlag(activityInfo.configChanges, CONFIG_SCREEN_SIZE);
//
//        return containsNecessaryConfigChanges;
    }

    protected void handleCustomClose(boolean useCustomClose) {
        mIsUserCustomClose = useCustomClose;
        if (mOnCloseButtonListener != null) {
            mOnCloseButtonListener.useCustomCloseChanged(useCustomClose);
        }
    }

    public FrameLayout getAdContainer() {
        return mDefaultAdContainer;
    }

    /**
     * Loads a javascript URL. Useful for running callbacks, such as javascript:webviewDidClose()
     */
    public void loadJavascript(String javascript) {
        mMraidBridge.injectJavaScript(javascript);
    }


    void handleSetOrientationProperties(final boolean allowOrientationChange,
                                        final MraidOrientation forceOrientation) throws MraidCommandException {
        if (!shouldAllowForceOrientation(forceOrientation)) {
            throw new MraidCommandException(
                    "Unable to force orientation to " + forceOrientation);
        }

        mAllowOrientationChange = allowOrientationChange;
        mForceOrientation = forceOrientation;

        if (mViewState == ViewState.EXPANDED || (mPlacementType == PlacementType.INTERSTITIAL && !mIsPaused)) {
            applyOrientation();
        }
    }

    void handleOpen(final URI uri, int interActionType, String ext) {
        if (mMraidListener != null) {

            mMraidListener.onOpen(uri, interActionType, ext);
        }
    }

    @Deprecated
    ViewState getViewState() {
        return mViewState;
    }

    private void setViewState(ViewState viewState) {
        // Make sure this is a valid transition.
        SigmobLog.d("MRAID state set to " + viewState);
        final ViewState previousViewState = mViewState;
        mViewState = viewState;
        mMraidBridge.notifyViewState(viewState);

        if (mMraidListener != null) {
            callMraidListenerCallbacks(mMraidListener, previousViewState, viewState);
        }
        SigmobLog.e("setViewState state set to " + viewState);
        updateScreenMetricsAsync(null);
    }

    @Deprecated
    void setViewStateForTesting(ViewState viewState) {
        mViewState = viewState;
    }

    @Deprecated
    void setRootViewSize(int width, int height) {
        mScreenMetrics.setRootViewPosition(0, 0, width, height);
    }

    @Deprecated
    Integer getOriginalActivityOrientation() {
        return mOriginalActivityOrientation;
    }

    @Deprecated
    boolean getAllowOrientationChange() {
        return mAllowOrientationChange;
    }

    @Deprecated
    MraidOrientation getForceOrientation() {
        return mForceOrientation;
    }

    @Deprecated
    void setOrientationBroadcastReceiver(OrientationBroadcastReceiver receiver) {
        mOrientationBroadcastReceiver = receiver;
    }

    @Deprecated
    MraidWebView getMraidWebView() {
        return mMraidWebView;
    }

    public void notifyMotionEvent(String uniqueId, String type, String began, HashMap<String, Object> args) {
        mMraidBridge.notifyMotionEvent(uniqueId, type, began, args);
    }


    public interface MraidListener {
        void onLoaded(View view);

        void onFailedToLoad();

        void onRenderProcessGone(final WindAdError error);

        void onExpand();

        void onResize(int width, int height, int offsetX, int offsetY, CloseableLayout.ClosePosition closePosition, boolean allowOffscreen);

        void onUnload();

        void onOpenFourElements();

        void onOpen(URI uri, int type, String ext);

        void onClose();

        void onReward(float progress);

        void onSkip(float progress);

        void onMute(boolean isMute);

        void onEndCardShow();

        void onCompanionClick(String ext);

        void onShowSkipTime();

        void onFeedBack();
    }


    public interface VPaidEventListener {

        void adClickThru(Map<String, String> params);

        void adError(Map<String, String> params);

        void adImpression();

        void adPaused();

        void adPlaying();

        void adVideoComplete();

        void adVideoFirstQuartile();

        void adVideoThirdQuartile();

        void adVideoMidpoint();

        void adVideoStart();

        void onVideoPrepared(Integer duration);

    }


    public interface UseCustomCloseListener {
        void useCustomCloseChanged(boolean useCustomClose);
    }


    public interface MraidWebViewCacheListener {
        void onReady(final MraidWebView webView, final ExternalViewabilitySessionManager viewabilityManager);
    }

    static class ScreenMetricsWaiter {
        private final Handler mHandler = new Handler();
        private WaitRequest mLastWaitRequest;

        WaitRequest waitFor(View... views) {
            mLastWaitRequest = new WaitRequest(mHandler, views);
            return mLastWaitRequest;
        }

        void cancelLastRequest() {
            if (mLastWaitRequest != null) {
                mLastWaitRequest.cancel();
                mLastWaitRequest = null;
            }
        }

        static class WaitRequest {
            private final View[] mViews;
            private final Handler mHandler;
            int mWaitCount;
            private Runnable mSuccessRunnable;
            private final Runnable mWaitingRunnable = new Runnable() {
                @Override
                public void run() {
                    for (final View view : mViews) {
                        // Immediately count down for any views that already have a size
                        if (view.getHeight() > 0 || view.getWidth() > 0) {
                            countDown();
                            continue;
                        }

                        // For views that didn't have a size, listen (once) for a preDraw. Note
                        // that this doesn't leak because the ViewTreeObserver gets detached when
                        // the view is no longer part of the view hierarchy.
                        view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                view.getViewTreeObserver().removeOnPreDrawListener(this);
                                countDown();
                                return true;
                            }
                        });
                    }
                }
            };

            private WaitRequest(Handler handler, final View[] views) {
                mHandler = handler;
                mViews = views;
            }

            private void countDown() {
                mWaitCount--;
                if (mWaitCount == 0 && mSuccessRunnable != null) {
                    mSuccessRunnable.run();
                    mSuccessRunnable = null;
                }
            }

            void start(Runnable successRunnable) {
                mSuccessRunnable = successRunnable;
                mWaitCount = mViews.length;
                mHandler.post(mWaitingRunnable);
            }

            void cancel() {
                mHandler.removeCallbacks(mWaitingRunnable);
                mSuccessRunnable = null;
            }
        }
    }

    class OrientationBroadcastReceiver extends BroadcastReceiver {
        private Context mContext;

        // -1 until this gets set at least once
        private int mLastRotation = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mContext == null || mIsPaused) {
                return;
            }

            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
                int orientation = getDisplayRotation();

                if (orientation != mLastRotation) {
                    mLastRotation = orientation;
                    handleOrientationChange(mLastRotation);
                }
            }
        }

        public void register(final Context context) {
            Preconditions.checkNotNull(context);
            mContext = context.getApplicationContext();
            if (mContext != null) {
                IntentUtil.registerReceiver(mContext,this,
                        new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
            }
        }

        public void unregister() {
            if (mContext != null) {
                mContext.unregisterReceiver(this);
                mContext = null;
            }
        }
    }
}
