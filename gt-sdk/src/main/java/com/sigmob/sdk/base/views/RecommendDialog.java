package com.sigmob.sdk.base.views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.SigmobRes;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.ExternalViewabilitySessionManager;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.PlacementType;
import com.sigmob.sdk.mraid.CloseableLayout;
import com.sigmob.sdk.mraid.MraidController;
import com.sigmob.sdk.mraid.MraidWebView;
import com.sigmob.sdk.videoAd.BaseVideoConfig;
import com.sigmob.windad.WindAdError;

import org.json.JSONObject;

import java.net.URI;
import java.util.Map;


/**
 * Create by lance on 2019/8/14/0014
 */
public class RecommendDialog extends Dialog implements DialogInterface.OnShowListener {
    protected ExternalViewabilitySessionManager mExternalViewAbilitySessionManager;
    private View mAdView;
    private Window window = null;
    private onCloseClickListener closeClickListener;
    private RelativeLayout mLayout;
    private BaseVideoConfig mVideoConfig;
    private BaseAdUnit mAdUnit;
    private MraidController mraidController;
    private Context mContext;
    private int mHeight = 0;
    private int mWidth = 0;
    private boolean isRenderFail = false;

    public RecommendDialog(Context context, BaseAdUnit adUnit, BaseVideoConfig videoConfig) {
        super(context, SigmobRes.getSig_custom_dialog());
        this.mContext = context.getApplicationContext();
        this.mAdUnit = adUnit;
        mVideoConfig = videoConfig;
        mAdView = getAdView();
    }

    public boolean isRenderFail() {
        if (mWidth <= 0 || mHeight <= 0) {
            isRenderFail = true;
        }
        return isRenderFail;
    }

    /**
     * 去除状态栏
     */
    @Override
    protected void onStart() {
        super.onStart();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//isShowNavigationBar
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;//isShowStatusBar
        this.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = new RelativeLayout(this.getContext());
        mLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mLayout.setBackgroundColor(Color.TRANSPARENT);
        setContentView(mLayout);
        this.setOnShowListener(this);

        SigmobLog.i("RecommendDialog onCreate:" + mWidth + ":" + mHeight);

        if (mAdView != null) {
            mLayout.addView(mAdView);
        }
        //点击dialog以外的空白处是否隐藏
        setCanceledOnTouchOutside(false);
        //点击返回键取消
        setCancelable(true);
        //设置窗口显示
        windowDeploy();
    }

//    private int getStatusBarHeight() {
//        Resources resources = mContext.getResources();
//        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
//        int height = resources.getDimensionPixelSize(resourceId);
//        return height;
//    }
//
//    private int getNavigationBarHeight(Context context) {
//        Resources resources = context.getResources();
//        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
//        int height = resources.getDimensionPixelSize(resourceId);
//        return height;
//    }
//
//    //获取是否存在NavigationBar
//    public static boolean checkDeviceHasNavigationBar(Context context) {
//        boolean hasNavigationBar = false;
//        Resources rs = context.getResources();
//        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
//        if (id > 0) {
//            hasNavigationBar = rs.getBoolean(id);
//        }
//        try {
//            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
//            Method m = systemPropertiesClass.getMethod("get", String.class);
//            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
//            if ("1".equals(navBarOverride)) {
//                hasNavigationBar = false;
//            } else if ("0".equals(navBarOverride)) {
//                hasNavigationBar = true;
//            }
//        } catch (Exception e) {
//
//        }
//        return hasNavigationBar;
//    }

    public View getAdView() {

        if (mraidController == null) {
            mraidController = new MraidController(mContext, mAdUnit, PlacementType.INTERSTITIAL);
        }

        mraidController.setVpaidEventListener(new MraidController.VPaidEventListener() {

            @Override
            public void adClickThru(Map<String, String> params) {
            }

            @Override
            public void adError(Map<String, String> params) {
            }

            @Override
            public void adImpression() {
            }

            @Override
            public void adPaused() {
            }

            @Override
            public void adPlaying() {
            }

            @Override
            public void adVideoComplete() {
            }

            @Override
            public void adVideoFirstQuartile() {
            }

            @Override
            public void adVideoThirdQuartile() {
            }

            @Override
            public void adVideoMidpoint() {
            }

            @Override
            public void adVideoStart() {
            }

            @Override
            public void onVideoPrepared(Integer duration) {
            }
        });

        mraidController.setMraidListener(new MraidController.MraidListener() {

            @Override
            public void onLoaded(View view) {
                SigmobLog.d("RecommendDialog onLoaded()");
            }

            @Override
            public void onReward(float cvTime) {
                SigmobLog.d("RecommendDialog onReward()");
            }

            @Override
            public void onSkip(float progress) {
                SigmobLog.d("RecommendDialog onSkip()");
            }

            @Override
            public void onMute(boolean isMute) {
                SigmobLog.d("RecommendDialog onMute()");
            }

            @Override
            public void onEndCardShow() {
                SigmobLog.d("RecommendDialog onEndCardShow()");
            }

            @Override
            public void onShowSkipTime() {
                SigmobLog.d("RecommendDialog onShowSkipTime()");
            }

            @Override
            public void onFeedBack() {
                
            }


            @Override
            public void onExpand() {
                SigmobLog.d("RecommendDialog onExpand()");
            }

            @Override
            public void onFailedToLoad() {
                SigmobLog.i("RecommendDialog onFailedToLoad()");
                isRenderFail = true;
            }

            @Override
            public void onRenderProcessGone(final WindAdError error) {
                SigmobLog.i("RecommendDialog onRenderProcessGone:" + error.toString());
                isRenderFail = true;
            }

            @Override
            public void onUnload() {
                SigmobLog.i("RecommendDialog onUnload()");
                dismiss();
                HandleVapidEvent(ADEvent.AD_CLOSE_CARD_CLOSE);
                destroy();
            }

            @Override
            public void onOpenFourElements() {

            }

            @Override
            public void onClose() {
                SigmobLog.i("RecommendDialog onClose()");
//                HandleVapidEvent(ADEvent.AD_FINISH);
                if (closeClickListener != null) {
                    closeClickListener.onCloseClick();
                }
            }

            @Override
            public void onCompanionClick(String ext) {
                SigmobLog.i("RecommendDialog onCompanionClick:" + ext);
                boolean isRecord = true;//默认原逻辑走

                if (!TextUtils.isEmpty(ext)) {
                    try {
                        JSONObject object = new JSONObject(ext);
                        int tp = object.optInt("type");
                        int x = object.optInt("x");
                        int y = object.optInt("y");
                        mraidController.updateClickCoordinate(String.valueOf(x), String.valueOf(y));
                        if (tp != 1) {
                            isRecord = true;
                            HandleVapidEvent(ADEvent.AD_COMPANION_CLICK);
                        } else {
                            isRecord = false;
                        }
                    } catch (Exception e) {
                        isRecord = true;
                        mraidController.updateClickCoordinate("0", "0");
                        HandleVapidEvent(ADEvent.AD_COMPANION_CLICK);
                    }
                }

                mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, mraidController.getClickCoordinate(), isRecord);
            }

            @Override
            public void onResize(int width, int height, int offsetX, int offsetY, CloseableLayout.ClosePosition closePosition, boolean allowOffscreen) {

                int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                int screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;

                SigmobLog.i("RecommendDialog Origin onResize :" + width + "==" + height + "==" + offsetX + "==" + offsetY + "==" + allowOffscreen);

                mWidth = Dips.dipsToIntPixels(width, mContext);
                mHeight = Dips.dipsToIntPixels(height, mContext);
                offsetX = Dips.dipsToIntPixels(offsetX, mContext);
                offsetY = Dips.dipsToIntPixels(offsetY, mContext);

                if (mAdView != null) {

                    if (!allowOffscreen) {//不允许超出屏幕

                        offsetX = offsetX < 0 ? 0 : offsetX;
                        offsetX = offsetX > screenWidth ? screenWidth : offsetX;
                        offsetY = offsetY < 0 ? 0 : offsetY;
                        offsetY = offsetY > screenHeight ? screenHeight : offsetY;

                        if ((offsetX + mWidth) > screenWidth) {
                            mWidth = screenWidth - offsetX;
                        }

                        if ((offsetY + mHeight) > screenHeight) {
                            mHeight = screenHeight - offsetY;
                        }
                    }

                    SigmobLog.i("RecommendDialog onResize: " + mWidth + "==" + mHeight + "==" + offsetX + "==" + offsetY);

                    if (mWidth <= 0 || mHeight <= 0) {
                        isRenderFail = true;
                    }

                    mAdView.setX(offsetX);
                    mAdView.setY(offsetY);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mWidth, mHeight);
//                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    mAdView.setLayoutParams(params);
                    mAdView.requestLayout();
                }
            }

            @Override
            public void onOpen(URI uri, int type, String ext) {
                SigmobLog.i("RecommendDialog  onOpen:" + uri + "======" + type + "=====" + ext);
                boolean isRecord;
                boolean disable_landing = false;
                boolean showAppElement = true;

                if (!TextUtils.isEmpty(ext)) {
                    try {
                        JSONObject object = new JSONObject(ext);
                        int tp = object.optInt("type");
                        int x = object.optInt("x");
                        int y = object.optInt("y");
                        disable_landing = object.optBoolean("disable_landing");
                        showAppElement = !object.optBoolean("feDisable");

                        mraidController.updateClickCoordinate(String.valueOf(x), String.valueOf(y));
                        if (tp != 1) {
                            isRecord = true;
                            HandleVapidEvent(ADEvent.AD_CLICK);
                        } else {
                            isRecord = false;
                        }
                    } catch (Exception e) {
                        isRecord = true;
                        mraidController.updateClickCoordinate("0", "0");
                        HandleVapidEvent(ADEvent.AD_CLICK);
                    }
                } else {
                    isRecord = true;
                    mraidController.updateClickCoordinate("0", "0");
                    HandleVapidEvent(ADEvent.AD_CLICK);
                }

                if (disable_landing || TextUtils.isEmpty(mAdUnit.getLanding_page())) {
                    mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, uri.toString(), mraidController.getClickCoordinate(), isRecord, showAppElement);
                } else {
                    mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, null, mraidController.getClickCoordinate(), isRecord, showAppElement);
                }

            }
        });

        if (!TextUtils.isEmpty(mAdUnit.getCloseCardHtmlData())) {
            mraidController.fillContentWithHtmlData(mAdUnit.getCloseCardHtmlData(),
                    new MraidController.MraidWebViewCacheListener() {
                        @Override
                        public void onReady(final MraidWebView webView, final ExternalViewabilitySessionManager viewAbilityManager) {
                            if (viewAbilityManager != null) {
                                mExternalViewAbilitySessionManager = viewAbilityManager;
                            } else {
                                mExternalViewAbilitySessionManager = new ExternalViewabilitySessionManager();
                                mExternalViewAbilitySessionManager.createDisplaySession(mAdUnit);
                            }
                        }
                    });
        }

        return mraidController.getAdContainer();
    }

    public void HandleVapidEvent(String event) {
        if (mExternalViewAbilitySessionManager != null) {
            mExternalViewAbilitySessionManager.recordDisplayEvent(event, 0);
        }
    }

    private void windowDeploy() {
        window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM); //设置窗口显示位置

            int SigMobDialogWindowAnim = SigmobRes.getSig_dialog_window_anim();
            if (SigMobDialogWindowAnim != 0) {
                window.setWindowAnimations(SigMobDialogWindowAnim); //设置窗口弹出动画
            }
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            int sw = mContext.getResources().getDisplayMetrics().widthPixels;
            int sh = mContext.getResources().getDisplayMetrics().heightPixels;
            if (mWidth - 1 <= sw) {
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            } else {
                lp.width = mWidth;
            }
            if (mHeight - 1 <= sh) {
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            } else {
                lp.height = mHeight;
            }
            SigmobLog.i("RecommendDialog windowDeploy: " + sw + "====" + sh + "====" + +lp.width + "====" + lp.height);
            window.setAttributes(lp);
        }
    }

    public void setOnCloseClickListener(onCloseClickListener closeClickListener) {
        this.closeClickListener = closeClickListener;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        SigmobLog.i("RecommendDialog  onShow");
        HandleVapidEvent(ADEvent.AD_CLOSE_CARD_SHOW);
    }

    public void destroy() {
        if (mraidController != null) {
            closeClickListener = null;
            mraidController.destroy();
            mraidController = null;
        }
    }

    public interface onCloseClickListener {
        void onCloseClick();
    }

}
