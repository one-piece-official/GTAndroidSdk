// Copyright 2018-2019 Sigmob, Inc.
// Licensed under the Sigmob SDK License Agreement
// http://www.sigmob.com/legal/sdk-license-agreement/

package com.sigmob.sdk.mraid2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.IntentUtil;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.AdSize;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.windad.WindAdError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Mraid2Controller {

    private final FrameLayout mDefaultAdContainer;
    private boolean mIsUserCustomClose = false;
    private MraidListener mMraidListener;
    private Mraid2WebView mMraidWebView;
    private LinearLayout VContentLayout;
    private MraidScroll scrollLayout;
    private OrientationBroadcastReceiver mOrientationBroadcastReceiver;
    private List<Mraid2WebView> mMraidWebViewList = new ArrayList<>();
    private List<BaseAdUnit> adUnitList;
    private AdSize adSize;
    private Mraid2Controller controller;

    public void setMraid2ViewController(Mraid2ControllerListener mraid2ControllerListener) {
        mMraidWebView.getMraidBridge().setControllerListener(mraid2ControllerListener);
    }

    public interface Mraid2ControllerListener {
        void background(int red, int green, int blue, int alpha);

        void blurEffectStart();

        void blurEffectEnd();

        void showDislikeDialog(BaseAdUnit adUnit);
        boolean onConsoleMessage(final ConsoleMessage consoleMessage);

        boolean onJsAlert(String message, JsResult result);

    }

    public Mraid2Controller(Context context, List<BaseAdUnit> adUnitList) {
        Preconditions.checkNotNull(context);

        this.adUnitList = adUnitList;

        mDefaultAdContainer = new FrameLayout(context);
        mDefaultAdContainer.setBackgroundColor(Color.TRANSPARENT);

        mOrientationBroadcastReceiver = new OrientationBroadcastReceiver();
        mOrientationBroadcastReceiver.register(context);

        mMraidWebView = new Mraid2WebView(context, adUnitList, mDefaultAdContainer);

        mMraidWebView.setLoadListener(new Mraid2WebView.LoadWebViewListener() {
            @Override
            public void onPageFinished(Mraid2WebView webView) {
                try {
                    if (mMraidListener != null) {
                        mMraidListener.onLoaded(mMraidWebView);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void handleRenderProcessGone(WindAdError errorCode) {
                try {
                    if (mMraidListener != null) {
                        mMraidListener.onRenderProcessGone(errorCode);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }


        });
        mMraidWebView.setNextWebViewListener(nextWebViewListener);

        mMraidWebViewList.clear();

        mMraidWebViewList.add(mMraidWebView);

        mDefaultAdContainer.addView(mMraidWebView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }


    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
        mMraidWebView.setAdSize(adSize);

        mMraidWebView.loadMain();
    }

    private Mraid2WebView.NextWebViewListener nextWebViewListener = new Mraid2WebView.NextWebViewListener() {

        @Override
        public Mraid2WebView initWebView(Mraid2WebView parentWebView, JSONObject args) {
            Mraid2WebView webView = createWebView(getContext(), parentWebView, args);
            mMraidWebViewList.add(webView);
            return webView;
        }

        /**
         * ScrollView永远在栈底，不管哪个webView创建出来的vipad都在这里面
         *
         * @param mraid2WebView
         * @param orientation
         * @return
         */
        @Override
        public LinearLayout onUseScrollView(Mraid2WebView mraid2WebView, int orientation) {
            if (VContentLayout == null) {
                VContentLayout = new LinearLayout(getContext());//所有Vpaid的父容器
            }

            if (scrollLayout == null) {
                if (orientation == 1) {
                    scrollLayout = new MraidHScrollView(getContext());
                    VContentLayout.setOrientation(LinearLayout.HORIZONTAL);
                } else {
                    scrollLayout = new MraidVScrollView(getContext());
                    VContentLayout.setOrientation(LinearLayout.VERTICAL);
                }

                scrollLayout.setPageChangedListener(new Mraid2Bridge.PageChangedListener() {
                    @Override
                    public void onPageChanged(Mraid2WebView view, int type, int index) {
                        if (view != null && view.getMraidBridge() != null) {
                            view.getMraidBridge().notifyPageChangeEvent(type, index);
                        }
                    }
                });

                scrollLayout.getView().addView(VContentLayout, new ViewGroup.LayoutParams(-1, -1));
                //ScrollView永远在栈底
                mDefaultAdContainer.addView(scrollLayout.getView(), 0, new ViewGroup.LayoutParams(-1, -1));
            }

            if (mraid2WebView != null && mraid2WebView.getScrollTouchListener() == null) {
                mraid2WebView.setScrollTouchListener(new Mraid2WebView.ScrollTouchListener() {
                    @Override
                    public void onTouchStart(JSONObject args) {
                        int x = args.optInt("x");
                        int y = args.optInt("y");
                        if (scrollLayout != null) {
                            scrollLayout.onTouchStart(Dips.asIntPixels(x, getContext()), Dips.asIntPixels(y, getContext()));
                        }
                    }

                    @Override
                    public void onTouchMove(JSONObject args) {
                        int x = args.optInt("x");
                        int y = args.optInt("y");
                        if (scrollLayout != null) {
                            scrollLayout.onTouchMove(Dips.asIntPixels(x, getContext()), Dips.asIntPixels(y, getContext()));
                        }
                    }

                    @Override
                    public void onTouchEnd(Mraid2WebView view, JSONObject args) {
                        int x = args.optInt("x");
                        int y = args.optInt("y");
                        if (scrollLayout != null) {
                            scrollLayout.onTouchEnd(view, Dips.asIntPixels(x, getContext()), Dips.asIntPixels(y, getContext()));
                        }
                    }
                });
            }
            return VContentLayout;
        }

        @Override
        public void onClose(Mraid2WebView webView) {//销毁所有
            if (mMraidListener != null) {
                mMraidListener.onClose();
            }
            if (mMraidWebViewList != null && mMraidWebViewList.size() > 0) {
                for (int i = 0; i < mMraidWebViewList.size(); i++) {
                    Mraid2WebView view = mMraidWebViewList.get(i);
                    view.destroy();
                }
            }
        }

        @Override
        public void onUseCustomClose(Mraid2WebView webView, JSONObject args) {
            mIsUserCustomClose = args.optBoolean("flag");
            if (mMraidListener != null) {
                mMraidListener.useCustomCloseChanged(mIsUserCustomClose);
            }
        }

        @Override
        public void onReward(Mraid2WebView webView) {
            if (mMraidListener != null) {
                mMraidListener.onReward();
            }
        }

        @Override
        public void open(Mraid2WebView webView, BaseAdUnit adUnit, JSONObject args) {
            if (mMraidListener != null) {
                mMraidListener.open(webView, adUnit, args);
            }
        }

        @Override
        public void onReady() {
            if (mMraidListener != null) {
                mMraidListener.onReady();
            }
        }
    };

    private Mraid2WebView createWebView(Context context, Mraid2WebView parentWebView, JSONObject args) {

        Mraid2WebView mraid2WebView = new Mraid2WebView(context, adUnitList, mDefaultAdContainer, parentWebView, args);
        mraid2WebView.setAdSize(adSize);
        mraid2WebView.setNextWebViewListener(nextWebViewListener);

        return mraid2WebView;
    }

    public void onShow(BaseAdUnit adUnit) {

        SigmobLog.e("onShow start");
        int showCloseTime = 5;
//        if (adUnit.getSkipSeconds() != -1) {
//            showCloseTime = adUnit.getSkipSeconds();
//        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                SigmobLog.e("onShow end");

                if (mMraidListener != null) {
                    mMraidListener.useCustomCloseChanged(mIsUserCustomClose);
                }
            }
        }, showCloseTime * 1000L);
    }

    private Context getContext() {
        if (mDefaultAdContainer != null) {
            return mDefaultAdContainer.getContext();
        }
        return null;
    }

    public void onPause() {
        if (mMraidWebViewList != null && mMraidWebViewList.size() > 0) {
            for (int i = 0; i < mMraidWebViewList.size(); i++) {
                Mraid2WebView webView = mMraidWebViewList.get(i);
                if (webView.getMraidBridge() != null) {
                    webView.getMraidBridge().notifyViewAbility(false);
                }
            }
        }
    }

    public void onResume() {
        if (mMraidWebViewList != null && mMraidWebViewList.size() > 0) {
            for (int i = 0; i < mMraidWebViewList.size(); i++) {
                Mraid2WebView webView = mMraidWebViewList.get(i);
                webView.resumeTimers();
                if (webView.getMraidBridge() != null) {
                    webView.getMraidBridge().notifyViewAbility(true);
                }
            }
        }
    }

    public void setMraidListener(MraidListener mraidListener) {
        mMraidListener = mraidListener;
    }

    public Mraid2WebView getCurrentWebView() {
        return mMraidWebView;
    }

    /**
     * 通知屏幕方向和屏幕尺寸改变
     *
     * @param currentRotation
     */
    void handleOrientationChange(int currentRotation) {
        SigmobLog.i("handleOrientationChange " + currentRotation);
        if (mMraidWebViewList != null && mMraidWebViewList.size() > 0) {
            for (int i = 0; i < mMraidWebViewList.size(); i++) {
                Mraid2WebView webView = mMraidWebViewList.get(i);
                if (webView.getMraidBridge() != null) {
                    webView.getMraidBridge().notifyAppOrientation();
                    webView.getMraidBridge().notifyScreenSize(adSize);
                }
            }
        }
    }

    public void destroy() {
        if (mOrientationBroadcastReceiver != null){
            mOrientationBroadcastReceiver.unregister();
            mOrientationBroadcastReceiver = null;
        }
        try {


            for (Mraid2WebView webView : mMraidWebViewList) {
                webView.destroy();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            mMraidWebViewList.clear();
            mMraidWebView = null;
            mMraidListener = null;
        }catch (Throwable th){

        }
    }

    public FrameLayout getAdContainer() {
        return mDefaultAdContainer;
    }

    public interface MraidListener {

        void onLoaded(View view);

        void onFailedToLoad();

        void onRenderProcessGone(final WindAdError error);

        void onClose();

        void useCustomCloseChanged(boolean useCustomClose);

        void onReward();

        void open(Mraid2WebView webView, BaseAdUnit adUnit, JSONObject args);

        void onReady();
    }

    class OrientationBroadcastReceiver extends BroadcastReceiver {
        private Context mContext;

        // -1 until this gets set at least once
        private int mLastRotation = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mContext == null) {
                return;
            }

            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
                int orientation = ClientMetadata.getInstance().getScreenOrientation(getContext());

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
