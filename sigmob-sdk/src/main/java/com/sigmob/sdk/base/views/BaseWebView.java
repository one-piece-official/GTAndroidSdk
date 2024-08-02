package com.sigmob.sdk.base.views;

import static com.sigmob.sdk.base.WindConstants.ENABLEFILE;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.czhj.sdk.common.utils.ReflectionUtil;
import com.czhj.sdk.common.utils.RomUtils;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.toolbox.StringUtil;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.CustomDownloadAPK;
import com.sigmob.sdk.base.common.CustomEventInterstitial;
import com.sigmob.sdk.base.common.DownloadAPK;
import com.sigmob.sdk.base.common.IntentUtil;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.services.AppInstallService;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.nativead.APKStatusBroadcastReceiver;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BaseWebView extends WebView {
    private static boolean sDeadlockCleared = false;
    private APKStatusBroadcastReceiver mAPKStatusBroadcastReceiver;
    private WeakReference<CustomEventInterstitial.APKStatusEventInterstitialListener> mListener;
    private BaseAdUnit mAdUnit;
    private WebViewClient mClient;
    private Set<String> keys = new HashSet<>();

    public BaseWebView(Context context) {
        /*
         * Important: don't allow any WebView subclass to be instantiated using
         * an Activity context, as it will leak on Froyo devices and earlier.
         */
        super(context);

        removeJavascriptInterface("searchBoxJavaBridge_");
        removeJavascriptInterface("accessibility");
        removeJavascriptInterface("accessibilityTraversal");

        webSetting(getSettings());
        if (!sDeadlockCleared) {
            clearWebViewDeadlock(getContext());
            sDeadlockCleared = true;
        }

        resumeTimers();
        handleWebviewDir(context);


    }

    private static void handleWebviewDir(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        try {
            String suffix = "";
            String webViewDir = "/app_webview";
            String huaweiWebViewDir = "/app_hws_webview";
            String lockFile = "/webview_data.lock";

            String dataPath = context.getDataDir().getAbsolutePath();
            Set<String> pathSet = new HashSet<>();

            String processName = Application.getProcessName();
            if (!TextUtils.equals(context.getPackageName(), processName)) {//判断不等于默认进程名称
                suffix = TextUtils.isEmpty(processName) ? context.getPackageName() : processName;
                WebView.setDataDirectorySuffix(suffix);
                suffix = "_" + suffix;
                pathSet.add(dataPath + webViewDir + suffix + lockFile);
                if (RomUtils.isHuawei()) {
                    pathSet.add(dataPath + huaweiWebViewDir + suffix + lockFile);
                }

            } else {
                //主进程
                suffix = "_" + processName;
                pathSet.add(dataPath + webViewDir + lockFile);//默认未添加进程名后缀
                pathSet.add(dataPath + webViewDir + suffix + lockFile);//系统自动添加了进程名后缀
                if (RomUtils.isHuawei()) {
                    pathSet.add(dataPath + huaweiWebViewDir + lockFile);
                    pathSet.add(dataPath + huaweiWebViewDir + suffix + lockFile);
                }
            }

            for (String path : pathSet) {
                File file = new File(path);
                if (file.exists()) {
                    tryLockOrRecreateFile(file);
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @TargetApi(Build.VERSION_CODES.P)
    private static void tryLockOrRecreateFile(File file) {
        try {
            FileLock tryLock = new RandomAccessFile(file, "rw").getChannel().tryLock();
            if (tryLock != null) {
                tryLock.close();
            } else {
                createFile(file, file.delete());
            }
        } catch (Exception e) {
            e.printStackTrace();
            boolean deleted = false;
            if (file.exists()) {
                deleted = file.delete();
            }
            createFile(file, deleted);
        }
    }

    private static void createFile(File file, boolean deleted) {
        try {
            if (deleted && !file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CustomEventInterstitial.APKStatusEventInterstitialListener getListener() {
        if (mListener != null) {
            return mListener.get();
        }
        return null;
    }

    public void setAdUnit(BaseAdUnit mAdUnit) {
        this.mAdUnit = mAdUnit;
        if (mAdUnit != null) {
            addJavascriptInterface(new SigH5Js(mAdUnit), "sigandroidh5");
        }
    }

    @SuppressLint("JavascriptInterface")
    public void addJavascriptInterface(Object object, String key) {
        super.addJavascriptInterface(object, key);

        keys.add(key);

    }

    @SuppressLint("AddJavascriptInterface")
    public void addSigAndroidAPK(final CustomEventInterstitial.APKStatusEventInterstitialListener listener) {
        mListener = new WeakReference<>(listener);
    }

    public void registerDownloadAPK() {

        if (mAdUnit != null && mAPKStatusBroadcastReceiver == null) {
            mAPKStatusBroadcastReceiver = new APKStatusBroadcastReceiver(new CustomEventInterstitial.APKStatusEventInterstitialListener() {

                @Override
                public void onInterstitialDownloadAPKStart(boolean result, long downloadId) {

                    CustomEventInterstitial.APKStatusEventInterstitialListener listener = getListener();
                    if (listener != null) {
                        listener.onInterstitialDownloadAPKEnd(result, downloadId);
                    } else {
                        if (result) {
                            notifyApkDownloadStart();
                        } else {
                            notifyApkDownloadFail();
                        }
                    }
                }


                @Override
                public void onInterstitialDownloadAPKEnd(boolean result, long downloadId) {
                    CustomEventInterstitial.APKStatusEventInterstitialListener listener = getListener();

                    if (listener != null) {
                        listener.onInterstitialDownloadAPKEnd(result, downloadId);
                    } else {
                        if (result) {
                            notifyApkDownloadEnd();
                        } else {
                            notifyApkDownloadFail();
                        }
                    }
                }

                @Override
                public void onInterstitialDownloadAPKPause(boolean result, long downloadId) {
                    CustomEventInterstitial.APKStatusEventInterstitialListener listener = getListener();

                    if (listener != null){
                        listener.onInterstitialDownloadAPKPause(result, downloadId);
                    }
                    notifyApkDownloadPause();
                }


                @Override
                public void onInterstitialInstallAPKStart(boolean result) {
                    CustomEventInterstitial.APKStatusEventInterstitialListener listener = getListener();

                    if (listener != null) {
                        listener.onInterstitialInstallAPKStart(result);
                    }
                }

                @Override
                public void onInterstitialInstallAPKEnd(boolean result) {
                    CustomEventInterstitial.APKStatusEventInterstitialListener listener = getListener();

                    if (listener != null) {
                        listener.onInterstitialInstallAPKEnd(result);
                    } else {
                        if (result) {
                            notifyApkDownloadInstalled();
                        } else {
                            notifyApkDownloadFail();
                        }
                    }
                }
            }, mAdUnit.getUuid());

            mAPKStatusBroadcastReceiver.register(mAPKStatusBroadcastReceiver);
        }

    }


    private void webSetting(WebSettings webSettings) {

        try {
            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(webSettings, new String(Base64.decode(WindConstants.ENABLEJS, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class, true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {


            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(webSettings, new String(Base64.decode(ENABLEFILE, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class, true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setBlockNetworkImage(false);//解决图片不显示
        webSettings.setBlockNetworkLoads(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        webSettings.setLoadsImagesAutomatically(true);
//        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowUniversalAccessFromFileURLs(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.setSafeBrowsingEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        addJavascriptInterface(new SigAndroidAPKJS(), "sigandroidapk");

    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        mClient = client;
        super.setWebViewClient(client);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    private Map<String, String> httpHeaders = new HashMap();

    public void loadUrl(String url) {

        url = StringUtil.getUrl(url);
        httpHeaders.put("Referer", "");

        if (mAdUnit != null) {
            if (mAdUnit.isDisablexRequestWith()) {
                httpHeaders.put("X-Requested-With", "");
            }


        }


        loadUrl(url, httpHeaders);
    }


    @Override
    public void destroy() {

        reset();
        SigmobLog.d(" BaseWebView destroy called ");
        super.destroy();
    }

    public void reset() {
        // Needed to prevent receiving the following error on SigmobAndroid versions using WebViewClassic
        // https://code.google.com/p/android/issues/detail?id=65833.
        ViewUtil.removeFromParent(this);

        if (mAPKStatusBroadcastReceiver != null) {
            mAPKStatusBroadcastReceiver.unregister(mAPKStatusBroadcastReceiver);
            mAPKStatusBroadcastReceiver = null;
        }
        setWebViewClient(null);

        // Even after removing from the parent, WebViewClassic can leak because of a static
        // reference from HTML5VideoViewProcessor. Removing children fixes this problem.
        removeAllViews();
        for (String key : keys) {
            removeJavascriptInterface(key);
        }
        keys.clear();
        setWebChromeClient(null);
        loadUrl("");
        mAdUnit = null;

    }

    public void enablePlugins(final boolean enabled) {
        // SigmobAndroid 4.3 and above has no concept of plugin states
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }

        if (enabled) {
            getSettings().setPluginState(WebSettings.PluginState.ON);
        } else {
            getSettings().setPluginState(WebSettings.PluginState.OFF);
        }
    }

    /**
     * This fixes https://code.google.com/p/android/issues/detail?id=63754,
     * which occurs on KitKat devices. When a WebView containing an HTML5 video is
     * is destroyed it can deadlock the WebView thread until another hardware accelerated WebView
     * is added to the view hierarchy and restores the GL context. Since we need to use WebView
     * before adding it to the view hierarchy, this method clears the deadlock by adding a
     * separate invisible WebView.
     * <p>
     * This potential deadlock must be cleared anytime you attempt to access a WebView that
     * is not added to the view hierarchy.
     */
    private void clearWebViewDeadlock(final Context context) {
        if (VERSION.SDK_INT == VERSION_CODES.KITKAT) {
            // Create an invisible WebView
            final WebView webView = new WebView(context.getApplicationContext());
            webView.setBackgroundColor(Color.TRANSPARENT);

            // For the deadlock to be cleared, we must load content and add to the view hierarchy. Since
            // we don't have an activity context, we'll use a system window.
            webView.loadDataWithBaseURL(null, "", "text/html", "UTF-8", null);
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.width = 1;
            params.height = 1;
            // Unlike other system window types TYPE_TOAST doesn't require extra permissions
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            params.format = PixelFormat.TRANSPARENT;
            params.gravity = Gravity.START | Gravity.TOP;
            final WindowManager windowManager =
                    (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            if (windowManager != null) {
                windowManager.addView(webView, params);
            }
        }
    }


    public void onPause(boolean isFinishing) {
        // XXX
        // We need to call WebView#stopLoading and WebView#loadUrl here due to an SigmobAndroid
        // bug where the audio of an HTML5 video will continue to play after the activity has been
        // destroyed. The web view must stop then load an invalid url during the onPause lifecycle
        // event in order to stop the audio.
        if (isFinishing) {
            stopLoading();
            loadUrl("");
        }

        onPause();
    }

    public void setDisableJSChromeClient() {
        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(final WebView view, final String url,
                                     final String message, final JsResult result) {
                SigmobLog.i(message);
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(final WebView view, final String url,
                                       final String message, final JsResult result) {
                SigmobLog.i(message);
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsPrompt(final WebView view, final String url,
                                      final String message, final String defaultValue,
                                      final JsPromptResult result) {
                SigmobLog.i(message);
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsBeforeUnload(final WebView view, final String url,
                                            final String message, final JsResult result) {
                SigmobLog.i(message);
                result.confirm();
                return true;
            }
        });
    }

    public static void manageWebCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (PrivacyManager.getInstance().canCollectPersonalInformation()) {
            cookieManager.setAcceptCookie(true);
            CookieManager.setAcceptFileSchemeCookies(true);
            return;
        }

        // remove all cookies
        cookieManager.setAcceptCookie(false);
        CookieManager.setAcceptFileSchemeCookies(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookies(null);
            cookieManager.flush();
        } else {
            cookieManager.removeSessionCookie();
            cookieManager.removeAllCookie();
        }
    }

    public static void manageThirdPartyCookies(final WebView webView) {

        CookieManager cookieManager = CookieManager.getInstance();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, PrivacyManager.getInstance().canCollectPersonalInformation());
        }
    }

//    public void enableJavascriptCaching() {
//
//        try {
//            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(getSettings(), String.valueOf(Base64.decode("c2V0SmF2YVNjcmlwUmVhYmxlZA==", Base64.NO_WRAP)));
//            methodBuilder.addParam(Boolean.class,true);
//            methodBuilder.execute();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        getSettings().setDomStorageEnabled(true);
//        getSettings().setAppCacheEnabled(true);
//        // Required for the Application Caches API to be enabled
//        // See: http://developer.android.com/reference/android/webkit/WebSettings.html#setAppCachePath(java.lang.String)
//        getSettings().setAppCachePath(getContext().getCacheDir().getAbsolutePath());
//    }


    void injectJavaScript(String javascript, ValueCallback callback) {

        SigmobLog.d("Injecting Javascript into MRAID WebView:\n\t" + javascript);

        if (javascript.startsWith("bridge")) {
            javascript = javascript.replaceFirst("bridge", StringUtil.decode(StringUtil.s));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(javascript, callback);
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
     */

    void notifyApkDownloadStart() {
        injectJavaScript("bridge.notifyApkDownloadStartEvent();", null);
    }

    void notifyApkDownloadFail() {
        injectJavaScript("bridge.notifyApkDownloadFailEvent();", null);
    }

    void notifyApkDownloadPause() {
        injectJavaScript("bridge.notifyApkDownloadPauseEvent();", null);
    }
    void notifyApkDownloadEnd() {
        injectJavaScript("bridge.notifyApkDownloadEndEvent();", null);
    }

    void notifyApkDownloadInstalled() {
        injectJavaScript("bridge.notifyApkDownloadInstalledEvent();", null);
    }

    private class SigAndroidAPKJS extends Object {

        public SigAndroidAPKJS() {

        }

        @JavascriptInterface
        public int pauseDownload() {

            if(mAdUnit != null) {
                if (mAdUnit.getApkDownloadType() != 0) {
                    return CustomDownloadAPK.PauseDownload(getContext(), mAdUnit);
                }
            }
            return -1;

        }

        @JavascriptInterface
        public int resumeDownload() {

            if(mAdUnit != null) {
                if (mAdUnit.getApkDownloadType() != 0) {
                    return CustomDownloadAPK.resumeDownload(getContext(), mAdUnit);
                }
            }
            return -1;

        }

        @JavascriptInterface
        public int cancelDownloadTask(){

            if(mAdUnit != null) {
                if (mAdUnit.getApkDownloadType() != 0) {
                    return CustomDownloadAPK.CancelDownloadFile(getContext(), mAdUnit);
                }
            }
            return -1;

        }

        @JavascriptInterface
        public int getApKDownloadType() {

            if (mAdUnit != null){
                return mAdUnit.getApkDownloadType();
            }
            return -1;
        }
        @JavascriptInterface
        public int getApKDownloadProcess() {

            registerDownloadAPK();
            if (getContext() != null && mAdUnit != null) {
                long[] downloadIdBytesAndStatus;
                if (mAdUnit.getApkDownloadType() != 0){

                    downloadIdBytesAndStatus = CustomDownloadAPK.getDownloadIdBytesAndStatus(getContext(), mAdUnit);
                }else {
                    downloadIdBytesAndStatus = DownloadAPK.getDownloadIdBytesAndStatus(getContext(), mAdUnit.getDownloadId());
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
                    case DownloadManager.STATUS_SUCCESSFUL: {
                        return 100;
                    }
                    case DownloadManager.STATUS_PAUSED: {
                        return -2;
                    }
                    case DownloadManager.STATUS_FAILED: {
                        return -1;
                    }
                }
            }
            return -1;
        }
    }

    private class SigH5Js {

        private BaseAdUnit adUnit;

        SigH5Js(BaseAdUnit adUnit) {
            this.adUnit = adUnit;
        }

        @JavascriptInterface
        public boolean canOpen(String packageName) {

            if (!TextUtils.isEmpty(packageName)) {
                return IntentUtil.deviceCanHandlePackageName(SDKContext.getApplicationContext(),packageName);
            } else {
                if(adUnit == null) return false;
                return adUnit.canOpen();
            }
        }

        @JavascriptInterface
        public boolean isOpenListReport(){
          return !WindSDKConfig.getInstance().getCanOpenList().isEmpty() && !AppInstallService.isCanOpenTodaySend();
        }

        @JavascriptInterface
        public void onOpenListReport(){
            AppInstallService.onCanOpenListSend();
        }
        @JavascriptInterface
        public boolean canInstall(String apkName) {

            if(adUnit == null) return false;
            if (TextUtils.isEmpty(apkName)){
                apkName = TextUtils.isEmpty(adUnit.getApkName())? TextUtils.isEmpty(adUnit.getApkMd5())? "":adUnit.getApkMd5()+".apk":adUnit.getApkName();
            }
            return adUnit.canInstall(apkName);
        }

        @JavascriptInterface
        public int getInteractionType(){
            if (mAdUnit != null){
               return mAdUnit.getInteractionType();
            }
            return 0;
        }

        @JavascriptInterface
        public int getSubInteractionType(){
            if (mAdUnit != null){
                return mAdUnit.getsubInteractionType();
            }
            return 0;
        }

    }
}