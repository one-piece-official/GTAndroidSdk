package com.sigmob.sdk.nativead;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.SigmobRes;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.BaseAdViewController;
import com.sigmob.sdk.base.common.BaseAdViewControllerListener;
import com.sigmob.sdk.base.common.ExternalViewabilitySessionManager;
import com.sigmob.sdk.base.common.SessionManager;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.ClickCommon;
import com.sigmob.sdk.base.models.IntentActions;
import com.sigmob.sdk.base.models.PlacementType;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.base.views.DownloadDialog;
import com.sigmob.sdk.base.views.Drawables;
import com.sigmob.sdk.mraid.CloseableLayout;
import com.sigmob.sdk.mraid.MraidController;
import com.sigmob.sdk.mraid.MraidWebView;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.WindNativeAdData;

import org.json.JSONObject;

import java.net.URI;


/**
 * 列表平滑进入详情页
 * <p>1.获取当前播放的View添加到详情页中
 * <p>2.获取列表中View的坐标，宽高，获取详情页View坐标，宽高，借助ViewMoveHelper实现平移{@link ViewMoveHelper}
 **/
public class SigmobNativeAdLandViewController extends BaseAdViewController {

    private static SigNativeAdVideo mSigAdView;
    public final long DURATION = 300;
    private ViewAttr attr;
    private ViewAttr currentAttr;
    private RelativeLayout llContent;//装下面落地页的容器
    private FrameLayout container;//装视频的容器
    private BaseAdUnit mAdUnit;
    private NativeAdConfig mNativeAdConfig;
    private MraidController mraidController;

    private View mAdView;
    private WindNativeAdData nativeAdUnit;
    private APKStatusBroadcastReceiver mAPKStatusBroadcastReceiver;
    private ImageView closeView;
    /**
     * For when the video is closing.
     */
    private DownloadDialog downloadDialog;
    private boolean isDialogShow = false;
    private boolean isExitDetail;


    public SigmobNativeAdLandViewController(final Activity activity,
                                            BaseAdUnit baseAdUnit,
                                            final Bundle intentExtras,
                                            final Bundle savedInstanceState,
                                            final String broadcastIdentifier,
                                            final BaseAdViewControllerListener baseAdViewControllerListener) {
        super(activity, broadcastIdentifier, baseAdViewControllerListener);

        mAdUnit = baseAdUnit;

        mNativeAdConfig = (NativeAdConfig) mAdUnit.getAdConfig();
        mNativeAdConfig.initFourElements(getActivity(), mAdUnit, null);
        attr = intentExtras.getParcelable("attr");

        getBaseAdViewControllerListener().onSetRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        int SigMobTheme = SigmobRes.getSig_transparent_lang();
        if (SigMobTheme != 0) {
            getActivity().getTheme().applyStyle(SigMobTheme, true);
        }

    }

    public static void setSigAdView(SigAdView sigAdView) {
        if (sigAdView instanceof SigNativeAdVideo) {
            mSigAdView = (SigNativeAdVideo) sigAdView;
        }
    }


    private int getVideoHeight() {
        DisplayMetrics displayMetrics = ClientMetadata.getInstance().getDisplayMetrics();

        int halfScreenHeight = Math.max(displayMetrics.heightPixels, displayMetrics.widthPixels) / 2;

        int height = (int) (Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / mAdUnit.getAdPercent());

        return Math.min(height, halfScreenHeight);
    }

    public void onCreate() {
        getLayout().removeAllViews();
        getBaseAdViewControllerListener().onSetContentView(getLayout());

        mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_TEMPLATE;

        llContent = new RelativeLayout(getContext());

        llContent.setBackgroundColor(Color.WHITE);

        mAdView = getAdView();

        if (mraidController != null) {
            mraidController.onShow(getActivity());
        }


        llContent.addView(mAdView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (mSigAdView != null) {
            nativeAdUnit = mSigAdView.getNativeAdUnit();
            if (nativeAdUnit != null) {
                params.setMargins(0, getVideoHeight(), 0, 0);
            }
        }

        getLayout().addView(llContent, params);

        if (mSigAdView != null) {

            container = new FrameLayout(getActivity());

            mSigAdView.setBackClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getBaseAdViewControllerListener().onBackPressed();
                }
            });

            getLayout().addView(container, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getVideoHeight()));

            SigVideoAdController adController = mSigAdView.getSigVideoAdController();
            adController.start();
            container.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {

                    if (mSigAdView == null || container == null) {
                        return false;
                    }

                    container.getViewTreeObserver().removeOnPreDrawListener(this);

                    mSigAdView.setUIStyle(SigAdStyle.DETAIL_PAGE);

                    View adContainer = mSigAdView.getVideoContainer();
                    if (adContainer != null) {
                        ViewUtil.removeFromParent(adContainer);
                        container.addView(adContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    }
                    currentAttr = new ViewAttr();
                    int[] location = new int[2];
                    container.getLocationOnScreen(location);
                    currentAttr.setX(location[0]);
                    currentAttr.setY(0);
                    currentAttr.setWidth(container.getMeasuredWidth());
                    currentAttr.setHeight(container.getMeasuredHeight());
                    new ViewMoveHelper(container, attr, currentAttr, DURATION).startAnim();

                    attr.setY(attr.getY() - location[1]);

                    if (llContent != null) {
                        AlphaAnimation animation = new AlphaAnimation(0, 1);
                        animation.setDuration(DURATION);
                        llContent.setAnimation(animation);
                        animation.start();
                    }
                    return true;
                }
            });
        }

        broadcastAction(IntentActions.ACTION_NATIVE_TEMPLIE_SHOW, 100);

    }

    private void addCloseButtonWidget(Context context) {

        if (closeView != null) return;
        closeView = new ImageView(context);
        closeView.setImageBitmap(Drawables.CLOSE.getBitmap());
        closeView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        closeView.setImageAlpha((int) (0.5f * 255.0f));
        closeView.setClickable(true);
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                HandleVapidEvent(ADEvent.AD_FINISH);
                getBaseAdViewControllerListener().onBackPressed();
            }
        });

        int width = Dips.dipsToIntPixels(20, mContext);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, width);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.setMargins(width / 2, width / 2, 0, 0);
        if (llContent != null) {
            llContent.addView(closeView, layoutParams);
        }
    }

    public SessionManager getSessionManager() {
        SessionManager sessionManager = mAdUnit.getSessionManager();
        if (sessionManager == null) {
            sessionManager = new NativeAdViewAbilitySessionManager();
            sessionManager.createDisplaySession(mAdUnit);
        }
        return sessionManager;
    }

    public View getAdView() {

        if (mraidController == null) {
            mraidController = new MraidController(mContext, mAdUnit, PlacementType.INTERSTITIAL);
        }

        mraidController.setMraidListener(new MraidController.MraidListener() {

            @Override
            public void onLoaded(View view) {
                SigmobLog.d("SigNativeAdLandViewController onLoaded()");
            }

            @Override
            public void onReward(float cvTime) {
                SigmobLog.d("SigNativeAdLandViewController onReward()");
            }

            @Override
            public void onSkip(float progress) {
                SigmobLog.d("SigNativeAdLandViewController onSkip()");
            }

            @Override
            public void onMute(boolean isMute) {
                SigmobLog.d("SigNativeAdLandViewController onMute()");
            }

            @Override
            public void onEndCardShow() {
                SigmobLog.d("SigNativeAdLandViewController onEndCardShow()");
            }

            @Override
            public void onShowSkipTime() {
                SigmobLog.d("SigNativeAdLandViewController onShowSkipTime()");
            }

            @Override
            public void onFeedBack() {

            }

            @Override
            public void onExpand() {
                SigmobLog.d("SigNativeAdLandViewController onExpand()");
            }

            @Override
            public void onFailedToLoad() {
                SigmobLog.i("SigNativeAdLandViewController onFailedToLoad()");
            }

            @Override
            public void onRenderProcessGone(final WindAdError error) {
                SigmobLog.i("SigNativeAdLandViewController onRenderProcessGone:" + error.toString());
            }

            @Override
            public void onUnload() {
                SigmobLog.i("SigNativeAdLandViewController onUnload()");
            }

            @Override
            public void onOpenFourElements() {
                SigmobLog.i("SigNativeAdLandViewController onOpenFourElements()");
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
                            mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_TEMPLATE;

                            getSessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_CLOSE, 0);
                        }

                        @Override
                        public void onButtonClick(String url, String clickCoordinate) {//执行点击逻辑
                            mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
                            mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_AREA_APPINFO;

                            mAdUnit.getClickCommon().is_final_click = true;
                            if (mNativeAdConfig != null) {
                                mNativeAdConfig.handleUrlFourAction(ClickUIType.COMPANION, url, clickCoordinate, true);
                                broadcastAction(IntentActions.ACTION_NATIVE_TEMPLE_CLICK);
                            }
                        }

                        @Override
                        public void onShowSuccess() {
                            getSessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_SHOW, 0);

                        }
                    });
                }

                if (downloadDialog != null && downloadDialog.isRenderSuccess() && !isDialogShow) {
                    mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_APPINFO;
                    mAdUnit.getClickCommon().is_final_click = false;
                    getSessionManager().recordDisplayEvent(ADEvent.AD_CLICK, 0);
                    downloadDialog.show();
                    isDialogShow = true;
                }
            }

            @Override
            public void onClose() {
                SigmobLog.i("SigNativeAdLandViewController onClose()");
                getBaseAdViewControllerListener().onBackPressed();
            }

            @Override
            public void onCompanionClick(String ext) {
                SigmobLog.i("SigNativeAdLandViewController onCompanionClick:" + ext);

                boolean isRecord = true;//默认原逻辑走

                if (!TextUtils.isEmpty(ext)) {
                    try {
                        mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
                        mAdUnit.getClickCommon().is_final_click = true;
                        JSONObject object = new JSONObject(ext);
                        int tp = object.optInt("type");
                        int x = object.optInt("x");
                        int y = object.optInt("y");
                        mraidController.updateClickCoordinate(String.valueOf(x), String.valueOf(y));
                        if (tp != 1) {
                            isRecord = true;
                            getSessionManager().recordDisplayEvent(ADEvent.AD_CLICK, 0);

                        } else {
                            isRecord = false;
                        }
                    } catch (Exception e) {
                        isRecord = true;
                        mraidController.updateClickCoordinate("0", "0");
                        getSessionManager().recordDisplayEvent(ADEvent.AD_CLICK, 0);
                    }
                }
                mNativeAdConfig.handleUrlAction(ClickUIType.COMPANION, mraidController.getClickCoordinate(), isRecord);
                broadcastAction(IntentActions.ACTION_NATIVE_TEMPLE_CLICK);
            }

            @Override
            public void onResize(int width, int height, int offsetX, int offsetY, CloseableLayout.ClosePosition closePosition, boolean allowOffscreen) {
                SigmobLog.i("SigNativeAdLandViewController onResize()");
            }

            @Override
            public void onOpen(URI uri, int type, String ext) {
                SigmobLog.i("SigNativeAdLandViewController  onOpen:" + uri + "======" + type + "=====" + ext);
                boolean isRecord = true;
                boolean disable_landing = false;
                boolean showAppElement = true;

                if (!TextUtils.isEmpty(ext)) {
                    try {
                        mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;

                        mAdUnit.getClickCommon().is_final_click = true;
                        JSONObject object = new JSONObject(ext);
                        int tp = object.optInt("type");
                        int x = object.optInt("x");
                        int y = object.optInt("y");
                        disable_landing = object.optBoolean("disable_landing");
                        showAppElement = !object.optBoolean("feDisable");

                        mraidController.updateClickCoordinate(String.valueOf(x), String.valueOf(y));
                        if (tp == 1) {
                            isRecord = false;
                        }
                    } catch (Exception e) {
                    }
                }

                if (disable_landing || TextUtils.isEmpty(mAdUnit.getLanding_page())) {
                    mNativeAdConfig.handleUrlAction(ClickUIType.ENDCARD, uri.toString(), mraidController.getClickCoordinate(), isRecord, showAppElement);
                } else {
                    mNativeAdConfig.handleUrlAction(ClickUIType.ENDCARD, null, mraidController.getClickCoordinate(), isRecord, showAppElement);
                }


                broadcastAction(IntentActions.ACTION_NATIVE_TEMPLE_CLICK);
            }
        });

        if (!TextUtils.isEmpty(mAdUnit.getHtmlUrl())) {
            mraidController.fillContentWithUrl(mAdUnit.getHtmlUrl(),
                    new MraidController.MraidWebViewCacheListener() {
                        @Override
                        public void onReady(final MraidWebView webView, final ExternalViewabilitySessionManager viewAbilityManager) {

                        }
                    });
        } else if (!TextUtils.isEmpty(mAdUnit.getHtmlData())) {
            mraidController.fillContentWithHtmlData(mAdUnit.getHtmlData(),
                    new MraidController.MraidWebViewCacheListener() {
                        @Override
                        public void onReady(final MraidWebView webView, final ExternalViewabilitySessionManager viewAbilityManager) {

                        }
                    });
        }
        // Needed because the Activity provides the close button, not the controller. This
        // gets called if the creative calls mraid.useCustomClose.
        mraidController.setUseCustomCloseListener(new MraidController.UseCustomCloseListener() {
            public void useCustomCloseChanged(boolean useCustomClose) {
                if (useCustomClose) {
                    hideInterstitialCloseButton();
                } else {
                    if (mSigAdView == null) {
                        showInterstitialCloseButton();
                    }
                }
            }
        });

        return mraidController.getAdContainer();
    }

    private void hideInterstitialCloseButton() {
        if (closeView != null) {
            closeView.setVisibility(View.INVISIBLE);
        }
    }

    private void showInterstitialCloseButton() {
        if (closeView == null) {
            addCloseButtonWidget(getActivity());
        }
        closeView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {

        if (mSigAdView != null && !isExitDetail) {
            SigVideoAdController sigVideoAdController = mSigAdView.getSigVideoAdController();
            if (sigVideoAdController != null) {
                sigVideoAdController.pause();
            }
        }
    }

    @Override
    public void onResume() {

        if (mSigAdView != null) {
            SigVideoAdController sigVideoAdController = mSigAdView.getSigVideoAdController();
            if (sigVideoAdController != null) {
                sigVideoAdController.start();
            }
        }

        if (mraidController != null) {
            mraidController.onResume();
        }
    }

    @Override
    public void onDestroy() {

        if (mAPKStatusBroadcastReceiver != null) {
            mAPKStatusBroadcastReceiver.unregister(mAPKStatusBroadcastReceiver);
            mAPKStatusBroadcastReceiver = null;
        }

        if (downloadDialog != null) {
            downloadDialog.dismiss();
            downloadDialog.destroy();
            downloadDialog = null;
        }

        if (mraidController != null) {
            mraidController.destroy();
            mraidController = null;
        }
        if (mSigAdView != null) {
            mSigAdView.setBackClickListener(null);
            mSigAdView = null;
        }

        broadcastAction(IntentActions.ACTION_NATIVE_TEMPLE_DISMISS);
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

    }

    @Override
    public boolean backButtonEnabled() {
        if (mSigAdView != null) {

            if (mSigAdView.onBackPressed()) {//退出全屏
                return false;
            }
        }

        backAnimation();

        return false;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onBackPressed() {

    }

    private void backAnimation() {
        isExitDetail = true;
        if (mSigAdView != null) {
            new ViewMoveHelper(container, currentAttr, attr, DURATION).startAnim();

            llContent.setVisibility(View.GONE);

            container.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mSigAdView != null) {
                        mSigAdView.setUIStyle(SigAdStyle.PREVIEW);
                    }
                    getBaseAdViewControllerListener().onFinish();
                }
            }, DURATION);
        } else {
            getBaseAdViewControllerListener().onFinish();
        }
    }
}
