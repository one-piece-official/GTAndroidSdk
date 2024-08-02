// Copyright 2018-2019 Sigmob, Inc.
// Licensed under the Sigmob SDK License Agreement
// http://www.sigmob.com/legal/sdk-license-agreement/

package com.sigmob.sdk.mraid2;

import static com.sigmob.sdk.base.common.UrlAction.OPEN_WITH_BROWSER;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_CLICK;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_FAIL;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_SHOW;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_VOPEN;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_REWARDED_VIDEO_CLOSE;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_REWARDED_VIDEO_COMPLETE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.utils.AppPackageUtil;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.Sigmob;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.AdActivity;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.BaseAdConfig;
import com.sigmob.sdk.base.common.BaseAdViewControllerListener;
import com.sigmob.sdk.base.common.IntentUtil;
import com.sigmob.sdk.base.common.MiMarketManager;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.UrlResolutionTask;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.ClickCommon;
import com.sigmob.sdk.base.models.IntentActions;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.sdk.base.views.OvalButton;
import com.sigmob.sdk.base.views.VideoButtonWidget;
import com.sigmob.sdk.nativead.DisLikeDialog;
import com.sigmob.sdk.videoAd.BaseVideoViewController;
import com.sigmob.sdk.videoAd.InterActionType;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.WindNativeAdData;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MraidView2Controller extends BaseVideoViewController {

    private int mRequestedOrientation;
    private boolean isReward;

    private Mraid2Controller mMraidController;
    private VideoButtonWidget mCloseButtonWidget;
    private DisLikeDialog mDislikeDialog;
    private boolean ad_closeTracking;

    List<BaseAdUnit> adUnitList;
    /**
     * For when the video is closing.
     */
    private boolean mIsClosing = false;

    private boolean isReady = false;

    private boolean mIsDismiss = true;
    private OvalButton endcardFeedBack;

    public MraidView2Controller(final Activity activity,
                                BaseAdUnit baseAdUnit,
                                final Bundle intentExtras,
                                final Bundle savedInstanceState,
                                final String mBroadcastIdentifier,
                                final BaseAdViewControllerListener baseAdViewControllerListener) {
        super(activity, mBroadcastIdentifier, baseAdViewControllerListener);

        this.adUnitList = AdStackManager.getAdUnitList(baseAdUnit.getUuid());

        mMraidController = MRAID2ControllerCache.getInstance().pop(baseAdUnit.getUuid());
        if (mMraidController == null) {
            mMraidController = new Mraid2Controller(SDKContext.getApplicationContext(), adUnitList);
        }
//
//        int display_orientation = adUnitList.get(0).getAd().display_orientation;
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
//        getBaseAdViewControllerListener().onSetRequestedOrientation(mRequestedOrientation);
        getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        optionSetting(activity, mRequestedOrientation, intentExtras);

    }


    public void onCreate() {

        super.onCreate();

        BaseAdUnit baseAdUnit = adUnitList.get(0);
        if (baseAdUnit != null) {
            MaterialMeta material = baseAdUnit.getMaterial();
            if (material.theme_data == 1 || baseAdUnit.getTemplateType() == 1) {
                setBackgroundDimHide();
                getLayout().setBackgroundColor(Color.argb((int) (255 * 0.5f), 0, 0, 0));
            }
        }


        View adView = getAdView();

        getLayout().addView(adView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }


    private void h5_state() {
        if (!isReady) {
            PointEntitySigmobUtils.eventRecord("h5_state", Constants.FAIL, adUnitList.get(0));
        }
    }


    public View getAdView() {

        if (mMraidController == null) {
            mMraidController = new Mraid2Controller(mContext, adUnitList);
        }

        mMraidController.setAdSize(adSize);
        mMraidController.setMraid2ViewController(new Mraid2Controller.Mraid2ControllerListener() {


            @Override
            public void background(int red, int green, int blue, int alpha) {
                try {

                    FrameLayout adContainer = mMraidController.getAdContainer();
                    if (alpha == 0) {
                        getLayout().setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        int argb = Color.argb((int) ((alpha / 100.0f) * 255.0f), red, green, blue);
                        getLayout().setBackgroundColor(argb);
                    }
//                    adContainer.invalidate();

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void blurEffectStart() {


                blurEffectViewEnable();

            }

            @Override
            public void blurEffectEnd() {
                blurEffectViewDisable();

            }

            @Override
            public void showDislikeDialog(BaseAdUnit adUnit) {
                BaseAdUnit baseAdUnit = adUnit;

                if (baseAdUnit == null && adUnitList != null) {
                    baseAdUnit = adUnitList.get(0);
                }
                Activity activity = getActivity();
                if (activity != null && baseAdUnit != null) {
                    mDislikeDialog = new DisLikeDialog(activity, baseAdUnit);

                    if (mDislikeDialog != null) {
                        mDislikeDialog.showDislikeDialog();
                    }
                    mDislikeDialog.setDislikeInteractionCallback(new WindNativeAdData.DislikeInteractionCallback() {
                        @Override
                        public void onShow() {
                            if (mMraidController != null) {
                                Mraid2WebView currentWebView = mMraidController.getCurrentWebView();
                                if (currentWebView != null) {
                                    Mraid2Bridge mraidBridge = currentWebView.getMraidBridge();
                                    if (mraidBridge != null) {
                                        mraidBridge.notifyEvent("feedbackDidAppear", null);
                                    }
                                }
                            }

                        }

                        @Override
                        public void onSelected(int position, String value, boolean enforce) {

                            if (mMraidController != null) {
                                Mraid2WebView currentWebView = mMraidController.getCurrentWebView();
                                if (currentWebView != null) {
                                    Mraid2Bridge mraidBridge = currentWebView.getMraidBridge();
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
                                Mraid2WebView currentWebView = mMraidController.getCurrentWebView();
                                if (currentWebView != null) {
                                    Mraid2Bridge mraidBridge = currentWebView.getMraidBridge();
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
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();
                switch (level){
                    case ERROR:{
                        SigmobLog.e("onConsoleMessage "+ consoleMessage.message());
                        PointEntitySigmobUtils.SigmobError("h5_error","mraid2",0,consoleMessage.message(),null,null,adUnitList.get(0),null);
                    }break;
                }
                return false;
            }

            @Override
            public boolean onJsAlert(String message, JsResult result) {
                return false;
            }

        });
        mMraidController.setMraidListener(new Mraid2Controller.MraidListener() {

            @Override
            public void onLoaded(View view) {
                // This is only done for the interstitial. Banners have a different mechanism
                // for tracking third party impressions.
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
                if (mBroadcastIdentifier != null) {
                    broadcastAction(ACTION_INTERSTITIAL_FAIL);
                }
                mBaseAdViewControllerListener.onFinish();
            }

            @Override
            public void onClose() {

                broadcastAction(ACTION_REWARDED_VIDEO_CLOSE);
                mIsClosing = true;
                mBaseAdViewControllerListener.onFinish();
            }

            @Override
            public void onReward() {
                if (!isReward) {
                    isReward = true;
                    broadcastAction(ACTION_REWARDED_VIDEO_COMPLETE);
                }
            }

            /**
             * mraidBridge.notifyOpenState(key + "_success", "");
             * mraidBridge.notifyOpenState(key + "_failed", "lance fail");
             * @param webView
             * @param adUnit
             * @param args
             */
            @Override
            public void open(final Mraid2WebView webView, final BaseAdUnit adUnit, JSONObject args) {
                final String url = args.optString("url");
                final String key = args.optString("event");
                JSONObject ext = args.optJSONObject("data");
                if (ext != null) {
                    final JSONObject log_data = ext.optJSONObject("log_data");
                    final String default_url = ext.optString("default_url");
                    final boolean in_app = ext.optBoolean("in_app");
                    boolean parse_302 = ext.optBoolean("parse_302");
                    int interaction_type = ext.optInt("interaction_type");
                    String errorMessage = "";
                    switch (interaction_type) {
                        case InterActionType.BrowserType://浏览器打开
                            if (parse_302) {
                                UrlResolutionTask.getResolvedUrl(url, new UrlResolutionTask.UrlResolutionListener() {
                                    @Override
                                    public void onSuccess(String resolvedUrl) {
                                        openBrowser(webView, adUnit, key, resolvedUrl, log_data, default_url, in_app);
                                    }

                                    @Override
                                    public void onFailure(String message, Throwable throwable) {
                                        openBrowser(webView, adUnit, key, url, log_data, default_url, in_app);
                                    }
                                });
                            } else {
                                openBrowser(webView, adUnit, key, url, log_data, default_url, in_app);
                            }
                            break;
                        case InterActionType.DownloadType://应用下载

                            if (adUnit == null) {
                                BaseAdUnit baseAdUnit = webView.getAdUnitList().get(0);
                                baseAdUnit.setRecord(false);
                                BaseAdConfig.handleMRAID2Download(adUnit, url, parse_302);
                            } else {
                                adUnit.setRecord(true);
                                BaseAdConfig.handleMRAID2Download(adUnit, url, parse_302);
                            }
                            doOpenResult(webView, adUnit, key, log_data, errorMessage);
                            break;
                        case 3://android market
                            JSONObject market = ext.optJSONObject("market");
                            if (market == null) return;
                            final String market_url = market.optString("market_url");
                            final String app_package_name = market.optString("app_package_name");
                            final String appstore_package_name = market.optString("appstore_package_name");
                            final String market_type = market.optString("type");
                            int type = 0;


                            if (!TextUtils.isEmpty(market_url)) {
                                if (!TextUtils.isEmpty(market_type)) {
                                    try {
                                        type = Integer.parseInt(market_type);
                                    } catch (Throwable t) {
                                    }
                                }
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                if (!TextUtils.isEmpty(appstore_package_name)) {
                                    try {
                                        PackageInfo packageInfo = AppPackageUtil.getPackageManager(getContext()).getPackageInfo(appstore_package_name, 0);
                                        if (packageInfo != null) {
                                            intent.setPackage(appstore_package_name);
                                        }
                                    } catch (Throwable th) {
                                        SigmobLog.e("get store package error " + th.getMessage());
                                    }
                                }
                                Uri parse = Uri.parse(market_url);
                                intent.setData(parse);

                                if (type == 1) {
                                    MiMarketManager.DirectMailStatusReceiver directMailStatusReceiver = new MiMarketManager.DirectMailStatusReceiver();
                                    if (directMailStatusReceiver != null) {
                                        directMailStatusReceiver.register(SDKContext.getApplicationContext(), adUnit);
                                    }

                                    Activity topActivity = SDKContext.getTopActivity();

                                    if (topActivity == null) {
                                        topActivity = SDKContext.getLastActivity();
                                    }


                                    try {
                                        if (topActivity != null) {
                                            topActivity.startActivity(intent);
                                        } else {
                                            getContext().startActivity(intent);
                                        }
                                    } catch (Throwable th) {
                                        errorMessage = th.getMessage();
                                    }
                                } else {
                                    try {
                                        IntentUtil.launchApplicationIntent(getContext(), intent);
                                    } catch (Throwable th) {
                                        errorMessage = th.getMessage();
                                    }
                                }
                            } else {
                                errorMessage = "market_url is null";

                            }

                            /**
                             * 打点和安装完成打点
                             */
                            try {
                                if (adUnit != null) {
                                    String category;
                                    if (TextUtils.isEmpty(errorMessage)) {//成功
                                        category = PointCategory.OPEN_MARKET;
                                        if (!TextUtils.isEmpty(app_package_name)) {
                                            File downloadAPKLogFile = new File(SigmobFileUtil.getDownloadAPKLogPath(), app_package_name + ".log");
                                            FileUtil.writeToCache(adUnit, downloadAPKLogFile.getAbsolutePath());
                                        }
                                        PointEntitySigmobUtils.SigmobTracking(PointCategory.APK_CLICK, type == 1 ? "mimarket" : "market", adUnit);

                                    } else {//失败
                                        category = PointCategory.OPEN_MARKET_FAILED;
                                    }
                                    PointEntitySigmobUtils.SigmobTracking(category, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                        @Override
                                        public void onAddExtra(Object pointEntityBase) {
                                            if (pointEntityBase instanceof PointEntitySigmob) {
                                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                                entitySigmob.setFinal_url(market_url);
                                                Map<String, String> options = new HashMap<>();
                                                if (adUnit.getAndroidMarket() != null) {
                                                    options.put("app_package_name", app_package_name);
                                                    options.put("store_package_name", appstore_package_name);
                                                }
                                                entitySigmob.setOptions(options);
                                            }
                                        }
                                    });
                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }

                            doOpenResult(webView, adUnit, key, log_data, errorMessage);
                            break;
                        case InterActionType.MiniProgramType://mimi_program
                        {
                            JSONObject program = ext.optJSONObject("program");
                            if (program == null) return;
                            String wx_app_id = program.optString("wx_app_id");
                            final String wx_app_username = program.optString("wx_app_username");
                            final String wx_app_path = program.optString("wx_app_path");
                            if (!TextUtils.isEmpty(wx_app_id) && !TextUtils.isEmpty(wx_app_username) && !TextUtils.isEmpty(wx_app_path)) {
                                try {
                                    Class factory = Class.forName("com.tencent.mm.opensdk.openapi.WXAPIFactory");
                                    Method createWXAPI = factory.getMethod("createWXAPI", Context.class, String.class);
                                    createWXAPI.setAccessible(true);
                                    Object api = createWXAPI.invoke(factory, getContext(), wx_app_id);

                                    Class req = Class.forName("com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram$Req");
                                    Object obj = req.newInstance();

                                    Field userName = req.getDeclaredField("userName");
                                    userName.setAccessible(true);
                                    userName.set(obj, wx_app_username);

                                    Field path = req.getDeclaredField("path");
                                    path.setAccessible(true);
                                    path.set(obj, wx_app_path);

                                    Field miniprogramType = req.getDeclaredField("miniprogramType");
                                    miniprogramType.setAccessible(true);
                                    miniprogramType.set(obj, 0);

                                    Method sendReq = api.getClass().getMethod("sendReq", obj.getClass().getSuperclass());
                                    sendReq.setAccessible(true);
                                    boolean isSendReq = (boolean) sendReq.invoke(api, obj);
                                    Log.d("lance", "openB:isSendReq " + isSendReq);
                                    if (!isSendReq) {
                                        errorMessage = "sendReq mini_program return false";
                                    }
                                } catch (Throwable th) {
                                    errorMessage = th.getMessage();
                                }
                            } else {
                                errorMessage = "wx_app_id or wx_app_username or wx_app_path is null";
                            }
                            /**
                             * deeplink打点
                             */
                            try {
                                if (adUnit != null) {
                                    String category;
                                    if (TextUtils.isEmpty(errorMessage)) {//成功
                                        category = PointCategory.OPEN_DEEPLINK;
                                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK);
                                        adUnit.getClickCommon().isDeeplink = Constants.SUCCESS;
                                    } else {//失败
                                        category = PointCategory.OPEN_DEEPLINK_FAILED;
                                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK_FAIL);
                                        adUnit.getClickCommon().isDeeplink = Constants.FAIL;
                                    }

                                    PointEntitySigmobUtils.SigmobTracking(category, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                        @Override
                                        public void onAddExtra(Object pointEntityBase) {
                                            if (pointEntityBase instanceof PointEntitySigmob) {
                                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                                entitySigmob.setFinal_url(url);
                                            }
                                        }
                                    });
                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                            doOpenResult(webView, adUnit, key, log_data, errorMessage);

                        }
                        break;
                        case InterActionType.FastAppType: {

                            if (!TextUtils.isEmpty(url)) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    intent.setPackage(SDKContext.getFastAppPackageName());
                                    IntentUtil.launchApplicationIntent(getContext(), intent);
                                } catch (Throwable th) {
                                    errorMessage = th.getMessage();
                                }
                            } else {
                                errorMessage = "market_url is null";
                            }
                            /**
                             * 打点和安装完成打点
                             */
                            try {
                                if (adUnit != null) {
                                    String category;
                                    if (TextUtils.isEmpty(errorMessage)) {//成功
                                        category = PointCategory.OPEN_DEEPLINK;
                                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK);
                                        adUnit.getClickCommon().isDeeplink = Constants.SUCCESS;

                                    } else {//失败
                                        category = PointCategory.OPEN_DEEPLINK_FAILED;
                                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK_FAIL);
                                        adUnit.getClickCommon().isDeeplink = Constants.FAIL;
                                    }
                                    PointEntitySigmobUtils.SigmobTracking(category, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                        @Override
                                        public void onAddExtra(Object pointEntityBase) {
                                            if (pointEntityBase instanceof PointEntitySigmob) {
                                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                                entitySigmob.setFinal_url(url);
                                                Map<String, String> options = new HashMap<>();
                                                if (adUnit.getAndroidMarket() != null) {
                                                    options.put("fast_pkg", SDKContext.getFastAppPackageName());
                                                }

                                                entitySigmob.setOptions(options);
                                            }
                                        }
                                    });
                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }

                            doOpenResult(webView, adUnit, key, log_data, errorMessage);
                        }
                        break;
                    }

                }
            }

            @Override
            public void onReady() {
                isReady = true;
            }

            @Override
            public void useCustomCloseChanged(boolean useCustomClose) {
                if (useCustomClose) {
                    hideInterstitialCloseButton();
                } else {
                    showInterstitialCloseButton();
                }
            }
        });

        return mMraidController.getAdContainer();
    }

    /**
     * 执行open结果
     */
    private void doOpenResult(final Mraid2WebView webView, final BaseAdUnit adUnit, final String key, final JSONObject log_data, final String errorMessage) {
        try {
            if (TextUtils.isEmpty(errorMessage)) {
                broadcastAction(ACTION_INTERSTITIAL_CLICK);
                if (webView.getMraidBridge() != null) {
                    webView.getMraidBridge().notifyOpenState(key + "_success", "");
                }
                if (adUnit != null) {
                    if (adUnit.isRecord()) {
                        final ClickCommon clickCommon = adUnit.getClickCommon();
                        PointEntitySigmobUtils.SigmobTracking(ClickUIType.ENDCARD.name().toLowerCase(), PointCategory.CLICK, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                            @Override
                            public void onAddExtra(Object pointEntityBase) {
                                if (pointEntityBase instanceof PointEntitySigmob) {
                                    ((PointEntitySigmob) pointEntityBase).setScene_id(adUnit.getAd_scene_id());
                                    ((PointEntitySigmob) pointEntityBase).setScene_desc(adUnit.getAd_scene_desc());
                                    ((PointEntitySigmob) pointEntityBase).setIs_deeplink(clickCommon.isDeeplink);
                                    ((PointEntitySigmob) pointEntityBase).setFinal_url(clickCommon.clickUrl);
                                    ((PointEntitySigmob) pointEntityBase).setCoordinate(clickCommon.clickCoordinate);

                                    ((PointEntitySigmob) pointEntityBase).setVtime(String.format("%.2f", 0 / 1000.0f));
                                    if (log_data != null) {
                                        Map<String, String> options = new HashMap<>();
                                        for (Iterator<String> it = log_data.keys(); it.hasNext(); ) {
                                            String key = it.next();
                                            options.put(key, log_data.optString(key));
                                        }
                                        ((PointEntitySigmob) pointEntityBase).setOptions(options);
                                    }

                                    Map<String, String> options = ((PointEntitySigmob) pointEntityBase).getOptions();
                                    options.put("cwidth", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                                    options.put("cheight", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));
                                }
                            }
                        });

                        SigmobTrackingRequest.sendTrackings(
                                adUnit,
                                ADEvent.AD_CLICK);
                    }

                }
            } else {
                if (webView.getMraidBridge() != null) {
                    webView.getMraidBridge().notifyOpenState(key + "_failed", errorMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBrowser(final Mraid2WebView webView, final BaseAdUnit adUnit, String key, final String url, final JSONObject log_data, final String default_url, final boolean in_app) {
        String errorMessage = "";
        boolean handlingUri = OPEN_WITH_BROWSER.shouldTryHandlingUri(Uri.parse(url), 0);

        if (handlingUri) {//浏览器打开
            try {
                if (in_app) {
                    BaseAdUnit baseAdUnit;
                    if (adUnit != null) {
                        baseAdUnit = adUnit;
                        baseAdUnit.setRecord(true);
                    } else {
                        baseAdUnit = webView.getAdUnitList().get(0);
                        baseAdUnit.setRecord(false);
                    }
                    baseAdUnit.setUrl(url);
                    AdStackManager.addAdUnit(baseAdUnit);
                    AdActivity.startActivity(getContext(), AdActivity.class, baseAdUnit);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    IntentUtil.launchApplicationIntent(getContext(), intent);
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                if (!TextUtils.isEmpty(default_url)) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(default_url));
                        IntentUtil.launchApplicationIntent(getContext(), intent);
                        errorMessage = null;

                    } catch (Exception exception) {
                        exception.printStackTrace();
                        errorMessage = e.getMessage();

                    }
                }
            }
        } else {//deeplink
            try {
                IntentUtil.launchApplicationUrl(getContext(), Uri.parse(url));
            } catch (Exception e) {
                errorMessage = e.getMessage();
            }
            /**
             * deeplink打点
             */
            try {
                if (adUnit != null) {
                    String category;
                    if (TextUtils.isEmpty(errorMessage)) {//成功
                        category = PointCategory.OPEN_DEEPLINK;
                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK);
                        adUnit.getClickCommon().isDeeplink = Constants.SUCCESS;
                    } else {//失败
                        category = PointCategory.OPEN_DEEPLINK_FAILED;
                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK_FAIL);
                        adUnit.getClickCommon().isDeeplink = Constants.FAIL;
                    }

                    PointEntitySigmobUtils.SigmobTracking(category, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {
                            if (pointEntityBase instanceof PointEntitySigmob) {
                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                entitySigmob.setFinal_url(url);
                            }
                        }
                    });
                }
                if (!TextUtils.isEmpty(errorMessage)) {
                    if (!TextUtils.isEmpty(default_url)) {
                        if (in_app) {
                            BaseAdUnit baseAdUnit;
                            if (adUnit != null) {
                                baseAdUnit = adUnit;
                                baseAdUnit.setRecord(true);
                            } else {
                                baseAdUnit = webView.getAdUnitList().get(0);
                                baseAdUnit.setRecord(false);
                            }
                            baseAdUnit.setUrl(default_url);
                            AdStackManager.addAdUnit(baseAdUnit);
                            AdActivity.startActivity(getContext(), AdActivity.class, baseAdUnit);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(default_url));
                            IntentUtil.launchApplicationIntent(getContext(), intent);
                        }
                        errorMessage = null;
                    }
                }
            } catch (Exception exception) {
                errorMessage = exception.getMessage();
                exception.printStackTrace();
            }
        }

        doOpenResult(webView, adUnit, key, log_data, errorMessage);
    }

    private View.OnClickListener onFeedBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Activity activity = getActivity();

            if (activity != null && adUnitList != null && adUnitList.get(0) != null) {
                mDislikeDialog = new DisLikeDialog(activity, adUnitList.get(0));

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

    private void addRuleLayoutWithPosition(int position, RelativeLayout.LayoutParams layoutParams) {

        int padding = Dips.dipsToIntPixels(10, mContext);

        switch (position) {
            case 1:
            case 2:
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layoutParams.setMargins(padding, padding * 2, padding, padding);
                break;
            case 3:
            case 4:
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams.setMargins(padding, padding * 2, padding, padding);
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addCloseButtonWidget(final Context context, int initialVisibility) {

        if (mCloseButtonWidget != null) return;

        int close_position = 3;
//        if (adUnitList.get(0) != null && adUnitList.get(0).slotAdSetting.rv_setting != null) {
//            close_position = adUnitList.get(0).slotAdSetting.rv_setting.endcard_close_position;
//        }

        mCloseButtonWidget = new VideoButtonWidget(context);
        mCloseButtonWidget.setVisibility(initialVisibility);
        mCloseButtonWidget.setId(ClientMetadata.generateViewId());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(22, context), Dips.dipsToIntPixels(22, context));
        addRuleLayoutWithPosition(close_position, layoutParams);
        getLayout().addView(mCloseButtonWidget, layoutParams);

        mCloseButtonWidget.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mIsClosing = true;
                    ad_closeTracking = true;
                    getBaseAdViewControllerListener().onFinish();
                }
                return true;
            }
        });

        mCloseButtonWidget.updateCloseButtonIcon(adUnitList.get(0));
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
        if (mMraidController != null) {
            mMraidController.handleOrientationChange(configuration.orientation);
        }
    }

    @Override
    public boolean backButtonEnabled() {
        return false;
    }

    @Override
    public void onStart() {
        if (mMraidController != null) {
            mMraidController.onShow(adUnitList.get(0));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                h5_state();
            }
        }, 5 * 1000);
        broadcastAction(ACTION_INTERSTITIAL_VOPEN);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onDestroy() {


        if (mMraidController != null) {
            mMraidController.destroy();
        }
        if (mDislikeDialog != null) {
            mDislikeDialog.dismiss();
            mDislikeDialog.destroy();
            mDislikeDialog = null;
        }

        if (adUnitList != null) {
            if (ad_closeTracking){
                BaseAdUnit baseAdUnit = adUnitList.get(0);
                if (baseAdUnit != null) {
                    SigmobTrackingRequest.sendTrackings(
                            baseAdUnit, ADEvent.AD_CLOSE);
                    PointEntitySigmobUtils.SigmobTracking(PointCategory.AD_CLOSE, null, baseAdUnit);
                }
            }
            for (BaseAdUnit baseAdUnit : adUnitList) {
                if (baseAdUnit != null) {
                    baseAdUnit.destroy();
                }
            }

        }

        if (!mIsClosing || !mIsDismiss) {
            mIsDismiss = true;
            broadcastAction(IntentActions.ACTION_INTERSTITIAL_DISMISS);
        }
        if (adUnitList != null) {
           for (BaseAdUnit baseAdUnit : adUnitList) {
               if (baseAdUnit != null) {
                   baseAdUnit.destroy();
               }
           }
           adUnitList.clear();
        }
        Sigmob.getInstance().getMacroCommon().clearMacro();
        super.onDestroy();

    }
}
