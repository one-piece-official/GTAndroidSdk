package com.gt.sdk.base.splash;

import static android.view.Gravity.CENTER;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ViewUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.base.common.ADEvent;
import com.gt.sdk.base.common.AdStackManager;
import com.gt.sdk.base.common.BaseBroadcastReceiver;
import com.gt.sdk.base.common.MotionManger;
import com.gt.sdk.base.common.SessionManager;
import com.gt.sdk.base.common.SigMacroCommon;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;
import com.gt.sdk.base.view.BeatheButton;
import com.gt.sdk.base.view.DownloadDialog;
import com.gt.sdk.base.view.MotionView;
import com.gt.sdk.base.view.PrivacyInfoView;
import com.gt.sdk.base.view.ScaleButtonView;
import com.gt.sdk.base.view.ShakeNewView;
import com.gt.sdk.base.view.SkipButtonView;
import com.gt.sdk.base.view.SlideButtonView;
import com.gt.sdk.base.view.SlideUpView;
import com.gt.sdk.base.view.SlopeView;
import com.gt.sdk.base.view.SwingView;
import com.gt.sdk.base.view.TitleDescriptionView;
import com.gt.sdk.base.view.WringView;
import com.gt.sdk.utils.DeviceContextManager;

import java.util.Map;

public final class SplashAdView extends RelativeLayout {
    private static float BUTTON_SIZE_MDPIh = 25;
    private final RelativeLayout sig_splash_template_ad_container;
    private SplashAdContentView mSplashAdContentView;
    private Context mContext;
    private BaseAdUnit mAdUnit;
    private SplashAdConfig mAdConfig;
    private RelativeLayout mClickLY;
    private OnTouchListener mClickTouchListener;
    private volatile boolean isClicked;
    private boolean isShowFourElement;

    private boolean adClose;
    private SkipButtonView mSkipButtonView;
    private MotionManger.Motion motion;
    private MotionView actionView;
    private boolean isSkip;

    public SplashAdView(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        sig_splash_template_ad_container = new RelativeLayout(mContext);
        addView(sig_splash_template_ad_container, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void dismissSplashView(boolean hasClose) {
        if (hasClose) {
            BaseBroadcastReceiver.broadcastAction(mContext, mAdUnit.getUuid(), IntentActions.ACTION_SPLASH_CLOSE);
            mSkipButtonView.setOnClickListener(null);
            AdStackManager.cleanPlayAdUnit(mAdUnit);
            mAdUnit.destroy();
            mAdUnit = null;
        }
        if (motion != null) {
            motion.destroy();
            motion = null;
        }
        if (actionView != null) {
            actionView.stopAnimator();
        }
        if (mSplashAdContentView != null) {
            mSplashAdContentView.setOnTouchListener(null);
        }
    }

    private void hideView() {
        super.setVisibility(GONE);
        if (mSplashAdContentView != null) {
            mSplashAdContentView.setVisibility(GONE);
        }
    }

    public void invisibleView() {
        super.setVisibility(INVISIBLE);
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE) {
            mSkipButtonView.setVisibility(VISIBLE);
            mSplashAdContentView.showAd();
            if (mAdUnit != null) {
                SessionManager sessionManager = mAdUnit.getSessionManager();
                if (sessionManager != null) {
                    sessionManager.recordDisplayEvent(mAdUnit, ADEvent.AD_SHOW);
                }
            }
            super.setVisibility(visibility);
        }
    }

    public boolean loadSplashResource() {
        if (mAdUnit == null || mAdUnit.getSplashFilePath() == null) {
            SigmobLog.e("adUnit or splashFilePath is null");
            return false;
        }
        return mSplashAdContentView.loadResource(mAdUnit);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        if (mSplashAdContentView != null) {
            mSplashAdContentView.setOnTouchListener(l);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAdUnit == null) {
            SigmobLog.e("adUnit is null");
            return;
        }
        if (mAdConfig == null) {
            return;
        }

        mAdConfig.initFourElements(ViewUtil.getActivityFromViewTop(this), mAdUnit, new DownloadDialog.onPrivacyClickListener() {
            @Override
            public void onCloseClick() {
                isShowFourElement = false;
                isClicked = false;
                if (adClose) {
                    dismissSplashView(true);
                } else if (motion != null) {
                    motion.start();
                }
            }

            @Override
            public void onButtonClick(String clickCoordinate) {
                if (mAdUnit != null) {
                    SessionManager sessionManager = mAdUnit.getSessionManager();
                    if (sessionManager != null) {
                        sessionManager.recordDisplayEvent(mAdUnit, ADEvent.AD_CLICK);
                    }
                }
            }

            @Override
            public void onShowSuccess() {
                if (motion != null) {
                    motion.pause();
                }
                isShowFourElement = true;
            }
        });
    }

    private void initSplashAdComponent(final Context context) {
        mSplashAdContentView = SplashAdContentView.getSplashAdContentView(context, mAdUnit);

        LayoutParams imageViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sig_splash_template_ad_container.addView(mSplashAdContentView, imageViewLayoutParams);

        mClickLY = new RelativeLayout(context);

        LayoutParams clickLYParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int padding = Dips.dipsToIntPixels(BUTTON_SIZE_MDPIh, mContext);

        clickLYParams.setMargins(padding, (int) (padding * 2.5), padding, padding);
        mClickLY.setClickable(true);

        mClickLY.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        sig_splash_template_ad_container.addView(mClickLY, clickLYParams);

        mClickTouchListener = new OnTouchListener() {
            private MotionEvent downEvent;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mAdUnit == null) {
                    SigmobLog.e("adUnit is null");
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downEvent = MotionEvent.obtain(event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (downEvent == null) {
                        downEvent = event;
                    }

                    try {
                        SigMacroCommon macroCommon = mAdUnit.getMacroCommon();
                        macroCommon.updateClickMarco(downEvent, event);
                    } catch (Throwable throwable) {
                        SigmobLog.e("splash click macro set " + throwable.getMessage());
                    }
                    handleClick(null);
                }
                return true;
            }
        };

    }


    private void handleClick(String coordinate) {
        SigmobLog.d("handleClick: ");

        if (mAdUnit == null) {
            SigmobLog.e("adUnit is null");
            return;
        }

        isClicked = true;

        Activity topActivity = DeviceContextManager.getTopActivity();

        if (topActivity == null) {
            DeviceContextManager.setTopActivity(ViewUtil.getActivityFromViewTop(this));
        }
        handleUrlAction(mAdUnit, coordinate);
    }

    private void handleUrlAction(final BaseAdUnit adUnit, final String coordinate) {
        SigmobLog.d("handleUrlAction: ");

        if (adUnit == null) {
            SigmobLog.e("adUnit is null");
            return;
        }
//
//        UrlHandler urlHandler = new UrlHandler.Builder().withSupportedUrlActions(UrlAction.IGNORE_ABOUT_SCHEME, UrlAction.DOWNLOAD_APK, UrlAction.MARKET_SCHEME, UrlAction.OPEN_WITH_BROWSER, UrlAction.FOLLOW_PACKAGE_NAME, UrlAction.FOLLOW_DEEP_LINK, UrlAction.MINI_PROGRAM).withResultActions(new UrlHandler.ResultActions() {
//
//            @Override
//            public void urlHandlingSucceeded(final String url, UrlAction urlAction) {
//                SigmobLog.i("urlHandlingSucceeded: " + urlAction.name() + " url: " + url);
//
//                if (mAdUnit == null) {
//                    SigmobLog.e("adUnit is null");
//                    return;
//                }
//                mAdUnit.getClickCommon().is_final_click = true;
//
//                if (!adUnit.getAd().forbiden_parse_landingpage) {
//                    PointEntitySigmobUtils.eventTargetURL(adUnit, urlAction.name(), url);
//                }
//
//                String result = Constants.FAIL;
//                BaseBroadcastReceiver.broadcastAction(mContext, mAdUnit.getUuid(), IntentActions.ACTION_INTERSTITIAL_CLICK);
//
//                switch (urlAction) {
//                    case FOLLOW_PACKAGE_NAME:
//                    case IGNORE_ABOUT_SCHEME:
//                        break;
//                    case MINI_PROGRAM:
//                    case FOLLOW_DEEP_LINK: {
//                        Log.d("lance", "打开小程序成功:" + urlAction);
//                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK);
//                        PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_DEEPLINK, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
//                            @Override
//                            public void onAddExtra(Object pointEntityBase) {
//                                if (pointEntityBase instanceof PointEntitySigmob) {
//                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
//                                    entitySigmob.setFinal_url(url);
//                                    if (mAdUnit.getInteractionType() == InterActionType.FastAppType) {
//                                        Map<String, String> options = entitySigmob.getOptions();
//                                        options.put("fast_pkg", SDKContext.getFastAppPackageName());
//                                    }
//                                }
//                            }
//                        });
//
//                        result = Constants.SUCCESS;
//                    }
//                    break;
//
//                    case MARKET_SCHEME: {
//                        AndroidMarket androidMarket = mAdUnit.getAndroidMarket();
//                        if (androidMarket != null) {
//
//                            String sub = Wire.get(androidMarket.type, 0) == 0 ? "market" : "mimarket";
//                            PointEntitySigmobUtils.SigmobTracking(PointCategory.APK_CLICK, sub, mAdUnit);
//                            if (!TextUtils.isEmpty(androidMarket.app_package_name)) {
//                                try {
//                                    File downloadAPKLogFile = new File(SigmobFileUtil.getDownloadAPKLogPath(), androidMarket.app_package_name + ".log");
//                                    FileUtil.writeToCache(mAdUnit, downloadAPKLogFile.getAbsolutePath());
//                                } catch (Throwable th) {
//                                    SigmobLog.e("write ad info with package error " + th.getMessage());
//                                }
//                            }
//
//                        }
//
//                        PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_MARKET, null, mAdUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
//                            @Override
//                            public void onAddExtra(Object pointEntityBase) {
//                                if (pointEntityBase instanceof PointEntitySigmob) {
//                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
//                                    entitySigmob.setFinal_url(url);
//                                    Map<String, String> options = new HashMap<>();
//
//                                    options.put("app_package_name", mAdUnit.getAndroidMarket().app_package_name);
//                                    options.put("store_package_name", mAdUnit.getAndroidMarket().appstore_package_name);
//                                    entitySigmob.setOptions(options);
//                                }
//                            }
//                        });
//                    }
//                    break;
//                    case DOWNLOAD_APK: {
//
//                        if (mAdConfig.showFourElements()) {
//                            mAdUnit.getClickCommon().is_final_click = false;
//                        } else {
//                            dismissSplashView(true);
//                            mAdConfig.handleFourDownload(url);
//
//                        }
//
//                    }
//                    break;
//                    case OPEN_WITH_BROWSER: {
//                    }
//                    break;
//                    case NOOP: {
//
//                    }
//                    break;
//                }
//
//                SessionManager sessionManager = adUnit.getSessionManager();
//                if (sessionManager != null) {
//                    sessionManager.recordDisplayEvent(ADEvent.AD_CLICK, 0);
//                }
//            }
//
//            @Override
//            public void urlHandlingFailed(final String url, UrlAction urlAction) {
//                SigmobLog.i("urlHandlingFailed: " + urlAction.name() + " url: " + url);
//                if (mAdUnit == null) {
//                    return;
//                }
//
//                mAdUnit.getClickCommon().is_final_click = true;
//
//                switch (urlAction) {
//                    case FOLLOW_PACKAGE_NAME:
//                    case IGNORE_ABOUT_SCHEME:
//                        break;
//                    case MINI_PROGRAM:
//                    case FOLLOW_DEEP_LINK: {
//                        Log.d("lance", "打开小程序失败:" + urlAction);
//                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK_FAIL);
//                        PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_DEEPLINK_FAILED, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
//                            @Override
//                            public void onAddExtra(Object pointEntityBase) {
//                                if (pointEntityBase instanceof PointEntitySigmob) {
//                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
//                                    entitySigmob.setFinal_url(url);
//                                    if (mAdUnit.getInteractionType() == InterActionType.FastAppType) {
//                                        Map<String, String> options = entitySigmob.getOptions();
//                                        options.put("fast_pkg", SDKContext.getFastAppPackageName());
//                                    }
//                                }
//                            }
//                        });
//
//                    }
//                    break;
//                    case MARKET_SCHEME: {
//                        PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_MARKET_FAILED, null, mAdUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
//                            @Override
//                            public void onAddExtra(Object pointEntityBase) {
//                                if (pointEntityBase instanceof PointEntitySigmob) {
//                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
//                                    entitySigmob.setFinal_url(url);
//                                    Map<String, String> options = new HashMap<>();
//
//                                    options.put("app_package_name", mAdUnit.getAndroidMarket().app_package_name);
//                                    options.put("store_package_name", mAdUnit.getAndroidMarket().appstore_package_name);
//                                    entitySigmob.setOptions(options);
//                                }
//                            }
//                        });
//                    }
//                    break;
//                    case DOWNLOAD_APK:
//                        break;
//                    case OPEN_WITH_BROWSER:
//                        break;
//                    case NOOP: {
//                        if (!adUnit.getAd().forbiden_parse_landingpage) {
//
//                            PointEntitySigmobUtils.eventTargetURL(adUnit, urlAction.name(), url);
//                        }
//                        mAdUnit.getClickCommon().is_final_click = true;
//
//                        SessionManager sessionManager = adUnit.getSessionManager();
//                        if (sessionManager != null) {
//                            sessionManager.recordDisplayEvent(ADEvent.AD_CLICK, 0);
//                        }
//
////                                dismissSplashView(mAdUnit.isClickAutoCloseSplash());
//
//                    }
//                    break;
//                }
//
//            }
//        }).withoutSigmobBrowser(adUnit.isSkipSigmobBrowser()).withAdunit(adUnit).withoutResolvedUrl(adUnit.getAd().forbiden_parse_landingpage).build();
//
////        urlHandler.setDelay_millis(0);
//        urlHandler.handleUrl(SDKContext.getApplicationContext(), null);

    }

    private void addTipView() {
        int margin = Dips.dipsToIntPixels(10, getContext());
        int width = Dips.dipsToIntPixels(75, getContext());
        int height = Dips.dipsToIntPixels(25, getContext());

        TextView textView = new TextView(getContext());
        textView.setText("互动广告");
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textView.setTextColor(Color.parseColor("#ccffffff"));
        textView.setGravity(CENTER);
        LayoutParams ll = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height);
        ll.addRule(LEFT_OF, mSkipButtonView.getId());
        ll.setMargins(0, margin * 2, margin, 0);
        addView(textView, ll);
    }

    private void addSkipView() {
        int margin = Dips.dipsToIntPixels(10, getContext());
        int width = Dips.dipsToIntPixels(68, getContext());
        int height = Dips.dipsToIntPixels(25, getContext());

        mSkipButtonView = new SkipButtonView(getContext());
        mSkipButtonView.setId(View.generateViewId());
        LayoutParams ll = new LayoutParams(width, height);
        ll.addRule(ALIGN_PARENT_RIGHT);

        mSkipButtonView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                isSkip = true;
                if (mAdUnit == null) return;

                SessionManager sessionManager = mAdUnit.getSessionManager();
                if (sessionManager != null) {
                    sessionManager.recordDisplayEvent(mAdUnit, ADEvent.AD_SKIP);
                }
                BaseBroadcastReceiver.broadcastAction(mContext, mAdUnit.getUuid(), IntentActions.ACTION_SPLASH_CLOSE);
            }
        });
        ll.setMargins(0, margin * 2, margin, 0);
        addView(mSkipButtonView, ll);
    }

    private void addShakeView() {
        actionView = new ShakeNewView(getContext());
        int size = Dips.dipsToIntPixels(100, getContext());
        int bottomMargin = Dips.dipsToIntPixels(145, getContext());
        LayoutParams ll = new LayoutParams(size, size);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);
        ll.setMargins(0, 0, 0, bottomMargin);

        if (mAdUnit.getClickType() == 1) {
            actionView.setOnTouchListener(mClickTouchListener);
        }
        motion = new MotionManger.ShakeMotion(getContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {

            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null && mAdUnit != null) {

                    Number x_max_acc = info.get("x_max_acc");
                    Number y_max_acc = info.get("y_max_acc");
                    Number z_max_acc = info.get("z_max_acc");

                    SigMacroCommon macroCommon = mAdUnit.getMacroCommon();

                    if (x_max_acc != null && y_max_acc != null && z_max_acc != null) {
                        macroCommon.updateClickMarco(String.valueOf(x_max_acc.intValue()), String.valueOf(y_max_acc.intValue()), String.valueOf(z_max_acc.intValue()));
                    }

                    actionView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isClicked) return;
                            handleClick(null);
                        }
                    }, 400);
                }
            }
        });
        motion.setLevel(mAdUnit.getSensitivity());
        motion.start();
        addView(actionView, ll);
    }

    private void addSlopView() {

        actionView = new SlopeView(getContext());
        int size = Dips.dipsToIntPixels(92, getContext());

        int bottomMargin = Dips.dipsToIntPixels(145, getContext());
        LayoutParams ll = new LayoutParams(size, size);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);
        ll.setMargins(0, 0, 0, bottomMargin);
        if (mAdUnit.getClickType() == 1) {
            actionView.setOnTouchListener(mClickTouchListener);
        }
        motion = new MotionManger.OrientationMotion(getContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {
                if (actionView instanceof SlopeView) {
                    ((SlopeView) actionView).updateScreen(progress);
                }
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null && mAdUnit != null) {
                    Number x_max_acc = info.get("x_max_acc");
                    Number y_max_acc = info.get("y_max_acc");
                    Number z_max_acc = info.get("z_max_acc");

                    SigMacroCommon macroCommon = mAdUnit.getMacroCommon();

                    if (x_max_acc != null && y_max_acc != null && z_max_acc != null) {
                        macroCommon.updateClickMarco(String.valueOf(x_max_acc.intValue()), String.valueOf(y_max_acc.intValue()), String.valueOf(z_max_acc.intValue()));
                    }

                    actionView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isClicked) return;
                            handleClick(null);
                        }
                    }, 400);
                }
            }
        }, MotionManger.OrientationMotionType.SLOPE);
        motion.setLevel(mAdUnit.getSensitivity());
        motion.start();
        addView(actionView, ll);
    }

    private void obSeverLayout() {
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                SplashAdView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mAdUnit != null) {
                    int[] position = new int[2];
                    SplashAdView.this.getLocationOnScreen(position);
                }
            }
        });
    }

    private void addSwingView() {

        actionView = new SwingView(getContext());
        int size = Dips.dipsToIntPixels(100, getContext());

        int bottomMargin = Dips.dipsToIntPixels(145, getContext());
        LayoutParams ll = new LayoutParams(size, size);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);
        ll.setMargins(0, 0, 0, bottomMargin);
        if (mAdUnit.getClickType() == 1) {
            actionView.setOnTouchListener(mClickTouchListener);
        }
        motion = new MotionManger.OrientationMotion(getContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {
                if (actionView instanceof SwingView)
                    ((SwingView) actionView).updateProcess(progress);
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null && mAdUnit != null) {
                    if (isClicked) return;
                    isClicked = true;

                    Number x_max_acc = info.get("x_max_acc");
                    Number y_max_acc = info.get("y_max_acc");
                    Number z_max_acc = info.get("z_max_acc");

                    SigMacroCommon macroCommon = mAdUnit.getMacroCommon();

                    if (x_max_acc != null && y_max_acc != null && z_max_acc != null) {
                        macroCommon.updateClickMarco(String.valueOf(x_max_acc.intValue()), String.valueOf(y_max_acc.intValue()), String.valueOf(z_max_acc.intValue()));
                    }

                    actionView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handleClick(null);
                        }
                    }, 400);
                }
            }
        }, MotionManger.OrientationMotionType.SWING);

        motion.setLevel(mAdUnit.getSensitivity());
        motion.start();
        addView(actionView, ll);
    }

    private void addSideUpView() {

        SlideUpView actionView = new SlideUpView(getContext());
        int size = Dips.dipsToIntPixels(100, getContext());

        if (mAdUnit.getClickType() == 1) {
            actionView.setOnTouchListener(mClickTouchListener);
        }

        int max_distance = 50;
        int sensitivity = mAdUnit.getSensitivity();

        if (sensitivity == 10) {
            max_distance = 0;
        } else if (sensitivity > 0 && sensitivity < 10) {
            max_distance = (10 - sensitivity + 1) * 10;
        }
        int finalMax_distance = Dips.dipsToIntPixels(max_distance, GtAdSdk.sharedAds().getContext());
        mClickLY.setOnTouchListener(new OnTouchListener() {

            private MotionEvent downEvent;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (mAdUnit == null) return false;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downEvent = MotionEvent.obtain(event);
                    isClicked = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP) {
                    if (isClicked) return true;
                    float endX = event.getX();
                    float endY = event.getY();
                    float distanceX = Math.abs(endX - downEvent.getX());
                    float distanceY = Math.abs(endY - downEvent.getY());
                    float distance = (float) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));

                    if (distance >= finalMax_distance) {
                        isClicked = true;
                        SigMacroCommon macroCommon = mAdUnit.getMacroCommon();
                        if (macroCommon != null) {
                            macroCommon.updateClickMarco(downEvent, event);
                        }
                        handleClick(null);
                    }
                }
                return true;
            }
        });
        int bottomMargin = Dips.dipsToIntPixels(145, getContext());
        LayoutParams ll = new LayoutParams(size, size);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);

        ll.setMargins(0, 0, 0, bottomMargin);
        addView(actionView, ll);
    }

    private void addScaleButtonView() {

        ScaleButtonView actionView = new ScaleButtonView(getContext());

        actionView.setButtonColor(mAdUnit.getButtonColor());

        actionView.setTitleAndDesc(mAdUnit.getDesc(), null);
        int size = Dips.dipsToIntPixels(100, getContext());
        actionView.setOnTouchListener(mClickTouchListener);

        int bottomMargin = Dips.dipsToIntPixels(100, getContext());
        LayoutParams ll = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);

        ll.setMargins(0, 0, 0, bottomMargin);
        addView(actionView, ll);
    }

    private void addSlideButtonView() {

        SlideButtonView actionView = new SlideButtonView(getContext());
        String title = mAdUnit.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = "点击查看详情";

        }
        actionView.setTitleAndDesc(title, mAdUnit.getDesc());
        int size = Dips.dipsToIntPixels(100, getContext());
        int rightMargin = Dips.dipsToIntPixels(40, getContext());

        actionView.setOnTouchListener(mClickTouchListener);
        int bottomMargin = Dips.dipsToIntPixels(100, getContext());
        LayoutParams ll = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);
        ll.setMargins(rightMargin, 0, rightMargin, bottomMargin);
        addView(actionView, ll);
    }

    private void addBeatheView() {

        BeatheButton actionView = new BeatheButton(getContext());
        int size = Dips.dipsToIntPixels(183, getContext());
        int bottomMargin = Dips.dipsToIntPixels(40, getContext());

        LayoutParams ll = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);
        addView(actionView, ll);
        actionView.setOnTouchListener(mClickTouchListener);
        String title = mAdUnit.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = "点击前往";
        }
        TitleDescriptionView titleDescriptionView = new TitleDescriptionView(getContext());
        titleDescriptionView.setTitle(title);
        titleDescriptionView.setDescription(mAdUnit.getDesc());

        titleDescriptionView.setOnTouchListener(mClickTouchListener);
        LayoutParams ll2 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll2.addRule(CENTER_HORIZONTAL);
        ll2.addRule(ALIGN_PARENT_BOTTOM);
        ll2.setMargins(0, 0, 0, bottomMargin);
        addView(titleDescriptionView, ll2);

    }

    private void addPrivacyView() {

        PrivacyInfoView actionView = new PrivacyInfoView(getContext());
        int size = Dips.dipsToIntPixels(20, getContext());
        Map<String, String> adPrivacy = mAdUnit.getPrivacyMap();
        boolean isShow = adPrivacy != null;
        if (isShow) {
            actionView.setOnTouchListener(new OnTouchListener() {

                private MotionEvent downEvent;

                @Override
                public boolean onTouch(View view, MotionEvent event) {

                    if (mAdUnit == null) return false;

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        downEvent = MotionEvent.obtain(event);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (downEvent == null) {
                            downEvent = event;
                        }
                        try {
                            SigMacroCommon macroCommon = mAdUnit.getMacroCommon();
                            macroCommon.updateClickMarco(downEvent, event);
                        } catch (Throwable throwable) {
                            SigmobLog.e("splash click macro set " + throwable.getMessage());
                        }
                        mAdConfig.showDownloadDialog();
                        return true;
                    }
                    return true;
                }
            });
        }
        actionView.setupView(mAdUnit.getAd_source_logo(), isShow);
        actionView.setAlpha(0.5f);
        int bottomMargin = Dips.dipsToIntPixels(5, getContext());
        LayoutParams ll = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);
        ll.setMargins(bottomMargin, 0, bottomMargin, bottomMargin);
        addView(actionView, ll);
    }

    private void addWringView() {

        WringView actionView = new WringView(getContext());
        int size = Dips.dipsToIntPixels(100, getContext());

        int bottomMargin = Dips.dipsToIntPixels(145, getContext());
        LayoutParams ll = new LayoutParams(size, size);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);
        ll.setMargins(0, 0, 0, bottomMargin);
        if (mAdUnit.getClickType() == 1) {
            actionView.setOnTouchListener(mClickTouchListener);
        }
        motion = new MotionManger.OrientationMotion(getContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null && mAdUnit != null) {
                    if (isClicked) return;

                    isClicked = true;
                    Number turn_x = info.get("turn_x");
                    Number turn_y = info.get("turn_y");
                    Number turn_z = info.get("turn_z");
                    Number turn_time = info.get("turn_time");


                    SigMacroCommon macroCommon = mAdUnit.getMacroCommon();

                    if (turn_x != null && turn_y != null && turn_z != null && turn_time != null) {
                        macroCommon.updateClickMarco(turn_x.intValue(), turn_y.intValue(), turn_z.intValue(), turn_time.intValue());
                    }

                    actionView.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            handleClick(null);
                        }
                    }, 400);
                }
            }
        }, MotionManger.OrientationMotionType.WRING);
        motion.setLevel(mAdUnit.getSensitivity());
        motion.start();
        addView(actionView, ll);
    }

    private void addTitleDescriptionView(String title, String desc) {
        TitleDescriptionView titleDescriptionView = new TitleDescriptionView(getContext());
        titleDescriptionView.setTitle(title);
        titleDescriptionView.setDescription(desc);
        int bottomMargin = Dips.dipsToIntPixels(82, getContext());

        if (mAdUnit.getClickType() == 1) {
            titleDescriptionView.setOnTouchListener(mClickTouchListener);
        }
        LayoutParams ll = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addRule(CENTER_HORIZONTAL);
        ll.addRule(ALIGN_PARENT_BOTTOM);
        ll.setMargins(0, 0, 0, bottomMargin);

        addView(titleDescriptionView, ll);

    }


    public boolean loadUI(BaseAdUnit adUnit) {
        try {
            mAdUnit = adUnit;
            mAdConfig = (SplashAdConfig) adUnit.getAdConfig();

            initSplashAdComponent(mContext);
            addSkipView();

//            int templateId = mAdUnit.getTemplateId();

            int templateId = 2101;

            obSeverLayout();

            switch (templateId) {
                case 2101: {
                    addTipView();
                    addShakeView();
                    String title = adUnit.getTitle();
                    if (TextUtils.isEmpty(title)) {
                        title = "摇动手机";
                    }

                    addTitleDescriptionView(title, adUnit.getDesc());
                }
                break;
                case 2102: {
                    addTipView();
                    addSlopView();
                    String title = adUnit.getTitle();
                    if (TextUtils.isEmpty(title)) {
                        title = "前倾手机";
                    }
                    addTitleDescriptionView(title, adUnit.getDesc());
                }
                break;
                case 2103: {
                    addTipView();
                    addSwingView();
                    String title = adUnit.getTitle();
                    if (TextUtils.isEmpty(title)) {
                        title = "晃动手机";
                    }
                    addTitleDescriptionView(title, adUnit.getDesc());
                }
                break;
                case 2104: {
                    addTipView();
                    addWringView();
                    String title = adUnit.getTitle();
                    if (TextUtils.isEmpty(title)) {
                        title = "转动手机";
                    }
                    addTitleDescriptionView(title, adUnit.getDesc());
                }
                break;
                case 2105: {
                    addTipView();
                    addSideUpView();
                    String title = adUnit.getTitle();
                    if (TextUtils.isEmpty(title)) {
                        title = "向上滑动";
                    }
                    addTitleDescriptionView(title, adUnit.getDesc());
                }
                break;
                case 2106: {
                    addScaleButtonView();
                }
                break;
                case 2107: {
                    addSlideButtonView();
                }
                break;
                case 2108: {
                    addBeatheView();
                }
                break;
                default: {
                    SigmobLog.e("splash error 无效的模版id");
                    return false;
                }
            }
            if (actionView != null) {
                actionView.startAnimator();
            }
            addPrivacyView();
            return true;
        } catch (Throwable throwable) {
            SigmobLog.e("setupAd error", throwable);
        }

        return false;
    }

    public int getDuration() {
        return mSplashAdContentView.getDuration();
    }

    public void setDuration(int duration) {
        if (mAdUnit == null) return;
        if (duration > 0 && mSkipButtonView != null) {
            mSkipButtonView.updateTimer(duration);
        } else {
            adClose = true;
            if (!isShowFourElement || isSkip) {
                dismissSplashView(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (motion != null) {
            motion.destroy();
        }
        if (actionView != null) {
            actionView.stopAnimator();
        }
        removeAllViews();

    }

    public void onPause() {
        isClicked = false;
        if (motion != null) {
            motion.pause();
        }
//        if(actionView != null ) {
//            actionView.stopAnimator();
//        }
        mSplashAdContentView.onPause();
    }

    public void onResume() {
        if (motion != null) {
            motion.start();
        }
        if (actionView != null) {
            actionView.startAnimator();
        }
        mSplashAdContentView.onResume();

    }
}
