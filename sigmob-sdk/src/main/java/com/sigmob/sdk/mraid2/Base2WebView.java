package com.sigmob.sdk.mraid2;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.CustomDownloadAPK;
import com.sigmob.sdk.base.common.CustomEventInterstitial;
import com.sigmob.sdk.base.common.DownloadAPK;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.views.BaseWebView;
import com.sigmob.sdk.base.views.WebViewPools;
import com.sigmob.sdk.mraid.MraidObject;
import com.sigmob.sdk.nativead.APKStatusBroadcastReceiver;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Base2WebView extends FrameLayout {
    private static String TAG = "BaseWebView2";
    private static boolean sDeadlockCleared = false;
    private Map<String, APKStatusBroadcastReceiver> mAPKStatusBroadcastReceiverList = new HashMap<>();
    public static HashMap<String, MraidObject> mMraidObjects = new LinkedHashMap<>();


    private BaseWebView mBaseWebView = null;

    public Base2WebView(Context context) {

        super(context);

        mBaseWebView = WebViewPools.getInstance().acquireWebView(context);
        if (mBaseWebView == null) {
            mBaseWebView = new BaseWebView(context);
        }


        mBaseWebView.addJavascriptInterface(new SigAndroidAPKJS(), "sigandroidapk");
        addView(mBaseWebView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            mBaseWebView.setBackground(background);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        mBaseWebView.setBackgroundColor(color);
    }

    WebSettings getSettings() {
        return mBaseWebView.getSettings();
    }

    @SuppressLint("JavascriptInterface")
    void addJavascriptInterface(Object object, String name) {

        mBaseWebView.addJavascriptInterface(object, name);
    }

    abstract List<BaseAdUnit> getAdUnitList();

    public void registerDownloadAPK(BaseAdUnit adUnit) {

        if (adUnit != null) {

            APKStatusBroadcastReceiver apkStatusBroadcastReceiver = mAPKStatusBroadcastReceiverList.get(adUnit.getUuid());

            if (apkStatusBroadcastReceiver == null) {

                apkStatusBroadcastReceiver = new APKStatusBroadcastReceiver(new CustomEventInterstitial.APKStatusEventInterstitialListener() {

                    @Override
                    public void onInterstitialDownloadAPKStart(boolean result, long downloadId) {

                        if (result) {
                            notifyApkDownloadStart();
                        } else {
                            notifyApkDownloadFail();
                        }
                    }

                    @Override
                    public void onInterstitialDownloadAPKEnd(boolean result, long downloadId) {
                        if (result) {
                            notifyApkDownloadEnd();
                        } else {
                            notifyApkDownloadFail();
                        }
                    }

                    @Override
                    public void onInterstitialDownloadAPKPause(boolean result, long downloadId) {
                        notifyApkDownloadPause();
                    }

                    @Override
                    public void onInterstitialInstallAPKStart(boolean result) {
                        if (result) {
                            notifyApkDownloadInstallStart();
                        } else {
                            notifyApkDownloadFail();
                        }
                    }

                    @Override
                    public void onInterstitialInstallAPKEnd(boolean result) {
                        if (result) {
                            notifyApkDownloadInstalled();
                        } else {
                            notifyApkDownloadFail();
                        }
                    }
                }, adUnit.getUuid());

                apkStatusBroadcastReceiver.register(apkStatusBroadcastReceiver);

                mAPKStatusBroadcastReceiverList.put(adUnit.getUuid(), apkStatusBroadcastReceiver);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void loadUrl(String url) {
        mBaseWebView.loadUrl(url);
    }


    public void destroy() {

        if (mAPKStatusBroadcastReceiverList != null && mAPKStatusBroadcastReceiverList.size() > 0) {
            for (APKStatusBroadcastReceiver receiver : mAPKStatusBroadcastReceiverList.values()) {
                receiver.unregister(receiver);
            }
            mAPKStatusBroadcastReceiverList.clear();
        }
        if (mMraidObjects != null && mMraidObjects.size() > 0) {
            for (MraidObject object : mMraidObjects.values()) {
                if (object != null) {
                    object.destroy();
                }
            }
            mMraidObjects.clear();
        }
        if (mBaseWebView != null) {
            WebViewPools.getInstance().recycle(mBaseWebView);
            mBaseWebView = null;
        }

        removeAllViews();
    }


    void injectJavaScript(String javascript, ValueCallback callback) {

        if (mBaseWebView == null) return;
        SigmobLog.d("Injecting Javascript into MRAID WebView:\n\t" + javascript);
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            mBaseWebView.evaluateJavascript(javascript, callback);
        } else {
            loadUrl("javascript:" + javascript);
        }
    }

    /**
     * var DOWNLOAD_STATES = mraid.DOWNLOAD_STATES = {
     * DOWNLOAD_STRAT: 'download_strat',
     * DOWNLOAD_FAIL: 'download_fail',
     * DOWNLOAD_END: 'download_end',
     * DOWNLOAD_INSTALLED: 'download_installed',
     * };
     **/

    void notifyApkDownloadStart() {
        notifyApkDownloadState("download_start");
    }

    void notifyApkDownloadFail() {
        notifyApkDownloadState("download_fail");
    }

    void notifyApkDownloadPause() {
        notifyApkDownloadState("download_pause");
    }

    void notifyApkDownloadEnd() {
        notifyApkDownloadState("download_end");
    }

    void notifyApkDownloadInstallStart() {
        notifyApkDownloadState("install_start");
    }

    void notifyApkDownloadInstalled() {
        notifyApkDownloadState("install_end");
    }

    void notifyApkDownloadState(final String state) {
        this.post(new Runnable() {
            @Override
            public void run() {
                injectJavaScript("mraidbridge.notifyApkDownloadStateEvent(\"" + state + "\");", null);
            }
        });
    }

    void loadDataWithBaseURL(String s, String htmlData, String s1, String s2, String o) {
        if (mBaseWebView != null)
            mBaseWebView.loadDataWithBaseURL(s, htmlData, s1, s2, o);
    }

    void setWebViewClient(WebViewClient webViewClient) {
        if (mBaseWebView != null)
            mBaseWebView.setWebViewClient(webViewClient);
    }

    void enablePlugins(boolean b) {
        if (mBaseWebView != null)
            mBaseWebView.enablePlugins(b);
    }

    public void reload() {
        if (mBaseWebView != null)
            mBaseWebView.reload();
    }

    public void stopLoading() {
        if (mBaseWebView != null)
            mBaseWebView.stopLoading();
    }

    void setWebChromeClient(WebChromeClient webChromeClient) {
        if (mBaseWebView != null)
            mBaseWebView.setWebChromeClient(webChromeClient);
    }

    public void evaluateJavascript(String javascript, ValueCallback callback) {
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            if (mBaseWebView != null)
                mBaseWebView.evaluateJavascript(javascript, callback);
        }
    }

    public void resumeTimers() {
        mBaseWebView.resumeTimers();
    }


    private class SigAndroidAPKJS extends Object {

        public SigAndroidAPKJS() {

        }

        @JavascriptInterface
        public void registerDownloadEvent(String vid) {
            BaseAdUnit adUnit = null;

            if (getAdUnitList() != null && getAdUnitList().size() > 0) {
                for (int i = 0; i < getAdUnitList().size(); i++) {
                    BaseAdUnit baseAdUnit = getAdUnitList().get(i);
                    if (baseAdUnit.getAd().vid.equals(vid)) {
                        adUnit = baseAdUnit;
                        break;
                    }
                }
            }

            Log.d(TAG, adUnit + "-----------registerDownloadEvent---------" + vid);

            if (adUnit == null) {
                adUnit = getAdUnitList().get(0);
            }

            registerDownloadAPK(adUnit);
        }

        @JavascriptInterface
        public int pauseDownloadByVid(String vid) {

            BaseAdUnit adUnit = null;

            if (getAdUnitList() != null && getAdUnitList().size() > 0) {
                for (int i = 0; i < getAdUnitList().size(); i++) {
                    BaseAdUnit baseAdUnit = getAdUnitList().get(i);
                    if (baseAdUnit.getAd().vid.equals(vid)) {
                        adUnit = baseAdUnit;
                        break;
                    }
                }
            }

            Log.d(TAG, adUnit + "-----------PauseDownloadByVid---------" + vid);

           if(adUnit != null) {
               if (adUnit.getApkDownloadType() != 0) {
                    return CustomDownloadAPK.PauseDownload(getContext(), adUnit);
                }
           }
           return -1;

        }

        @JavascriptInterface
        public int cancelDownloadTaskByVid(String vid){

            BaseAdUnit adUnit = null;

            if (getAdUnitList() != null && getAdUnitList().size() > 0) {
                for (int i = 0; i < getAdUnitList().size(); i++) {
                    BaseAdUnit baseAdUnit = getAdUnitList().get(i);
                    if (baseAdUnit.getAd().vid.equals(vid)) {
                        adUnit = baseAdUnit;
                        break;
                    }
                }
            }

            Log.d(TAG, adUnit + "-----------resumeDownloadByVid---------" + vid);

            if(adUnit != null) {
                if (adUnit.getApkDownloadType() != 0) {

                    return CustomDownloadAPK.CancelDownloadFile(getContext(), adUnit);
                }
            }
            return -1;

        }

        @JavascriptInterface
        public int resumeDownloadByVid(String vid) {

            BaseAdUnit adUnit = null;

            if (getAdUnitList() != null && getAdUnitList().size() > 0) {
                for (int i = 0; i < getAdUnitList().size(); i++) {
                    BaseAdUnit baseAdUnit = getAdUnitList().get(i);
                    if (baseAdUnit.getAd().vid.equals(vid)) {
                        adUnit = baseAdUnit;
                        break;
                    }
                }
            }

            Log.d(TAG, adUnit + "-----------resumeDownloadByVid---------" + vid);

            if(adUnit != null) {
                if (adUnit.getApkDownloadType() != 0) {
                    return CustomDownloadAPK.resumeDownload(getContext(), adUnit);
                }
            }
            return -1;

        }

        @JavascriptInterface
        public int getApKDownloadProcessId(String vid) {
            BaseAdUnit adUnit = null;

            if (getAdUnitList() != null && getAdUnitList().size() > 0) {
                for (int i = 0; i < getAdUnitList().size(); i++) {
                    BaseAdUnit baseAdUnit = getAdUnitList().get(i);
                    if (baseAdUnit.getAd().vid.equals(vid)) {
                        adUnit = baseAdUnit;
                        break;
                    }
                }
            }

            Log.d(TAG, adUnit + "-----------getApKDownloadProcessId---------" + vid);

            if (adUnit == null) {
                adUnit = getAdUnitList().get(0);
            }

            if (getContext() != null && adUnit != null) {
                long[] downloadIdBytesAndStatus;
                if (adUnit.getApkDownloadType() != 0){
                    downloadIdBytesAndStatus = CustomDownloadAPK.getDownloadIdBytesAndStatus(getContext(), adUnit);
                }else {
                    downloadIdBytesAndStatus = DownloadAPK.getDownloadIdBytesAndStatus(getContext(), adUnit.getDownloadId());
                }

                switch ((int) downloadIdBytesAndStatus[2]) {
                    case DownloadManager.STATUS_RUNNING: {
                        long current = downloadIdBytesAndStatus[0];
                        long total = downloadIdBytesAndStatus[1];
                        if (total == 0 || current == 0) return 0;

                        return (int) (current * 100 / total);
                    }
                    case DownloadManager.STATUS_PENDING: {
                        return 0;
                    }
                    case DownloadManager.STATUS_PAUSED: {
                        return -2;
                    }
                    case DownloadManager.STATUS_SUCCESSFUL: {
                        return 100;
                    }
                    case DownloadManager.STATUS_FAILED: {
                        return -1;
                    }
                }

            }
            return -1;
        }
    }

}