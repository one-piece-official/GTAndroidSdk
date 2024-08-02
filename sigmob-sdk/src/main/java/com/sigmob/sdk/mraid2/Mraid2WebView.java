package com.sigmob.sdk.mraid2;

import static com.sigmob.sdk.base.WindConstants.ENABLEFILE;
import static com.sigmob.sdk.base.WindConstants.SIGMOBHTML;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.Md5Util;
import com.czhj.sdk.common.utils.ReflectionUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.toolbox.DownloadItem;
import com.czhj.volley.toolbox.FileDownloadRequest;
import com.czhj.volley.toolbox.FileDownloader;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.AdSize;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.DownloaderFactory;
import com.sigmob.sdk.base.common.IntentUtil;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.Ad;
import com.sigmob.sdk.base.models.rtb.Template;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.services.AppInstallService;
import com.sigmob.sdk.base.utils.GZipUtil;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.mraid.MraidMotionView;
import com.sigmob.sdk.mraid.MraidObject;
import com.sigmob.sdk.mraid.MraidVideoAdView;
import com.sigmob.sdk.mraid.MraidVpaid;
import com.sigmob.windad.WindAdError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * created by lance on   2022/7/6 : 2:33 下午
 */
public class Mraid2WebView extends Base2WebView {

    private final Mraid2WebView parentWebView;
    private Mraid2Bridge mMraidBridge;
    private Mraid2WebView selfWebView;
    private String mUniqueId;
    private ScrollTouchListener scrollTouchListener;
    private NextWebViewListener nextWebViewListener;
    private LoadWebViewListener loadWebViewListener;
    private boolean mHasLoaded;
    private FrameLayout baseLayout;
    private LinearLayout scrollLayout;
    private boolean isUseScrollView = false;
    private JSONObject arguments;
    private BaseAdUnit curAdUnit = null;

    private static String TAG = "Mraid2Bridge";
    private final Mraid2WebViewClient webViewClient = new Mraid2WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            Log.d(TAG, "-----------shouldOverrideUrlLoading---------" + url);
            if (url == null) return false;

            try {
                if (url.startsWith("http") || url.startsWith("https")) {
                    view.loadUrl(url);
                } else {
                    if (curAdUnit != null) {
                        List<String> scheme_white_list = curAdUnit.getAdSetting() != null ? curAdUnit.getAdSetting().scheme_white_list : null;
                        if (scheme_white_list != null && scheme_white_list.size() > 0) {
                            for (int j = 0; j < scheme_white_list.size(); j++) {
                                String scheme = scheme_white_list.get(j);
                                if (url.startsWith(scheme) || scheme.equals("*")) {//通配符
                                    IntentUtil.launchApplicationUrl(getContext(), Uri.parse(url));
                                    return true;
                                }
                            }
                        }
                    } else {
                        for (int i = 0; i < getAdUnitList().size(); i++) {
                            BaseAdUnit baseAdUnit = getAdUnitList().get(i);
                            List<String> scheme_white_list = baseAdUnit.getAdSetting() != null ? baseAdUnit.getAdSetting().scheme_white_list : null;
                            if (scheme_white_list != null && scheme_white_list.size() > 0) {
                                for (int j = 0; j < scheme_white_list.size(); j++) {
                                    String scheme = scheme_white_list.get(j);
                                    if (url.startsWith(scheme) || scheme.equals("*")) {//通配符
                                        IntentUtil.launchApplicationUrl(getContext(), Uri.parse(url));
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) { //uri 匹配不到的情况
                e.printStackTrace();
            }
            return true;//都在系统内部打开
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
//            if (!mMraidBridge.isJsLoaded()){
//                mMraidBridge.injectJavaScript("javascript:"+Mraid2Javascript.JAVASCRIPT_SOURCE,null);
//            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, arguments + "-----------onPageFinished---------" + url);
            if (!mMraidBridge.isJsLoaded()){
                mMraidBridge.injectJavaScript("javascript:" + Mraid2Javascript.JAVASCRIPT_SOURCE, new ValueCallback() {
                    @Override
                    public void onReceiveValue(Object o) {
                        handleOnPageFinished();
                    }
                });
            }else {
                handleOnPageFinished();
            }

        }
        private void handleOnPageFinished(){
            if (mMraidBridge != null) {
                if (arguments != null) {
                    mMraidBridge.notifyBindDate(arguments);
                }
                mMraidBridge.notifyReady();
            }
            /**
             * 父view要通知自己创建的view加载完成
             */
            if (parentWebView != null && parentWebView.getMraidBridge() != null && !TextUtils.isEmpty(mUniqueId)) {
                parentWebView.getMraidBridge().notifyWvFinished(mUniqueId);
            }

            if (mHasLoaded) {
                return;
            }
            mHasLoaded = true;

            if (loadWebViewListener != null) {
                loadWebViewListener.onPageFinished(selfWebView);
            }
        }


        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PointEntitySigmobUtils.SigmobError("h5_error","mraid2",errorResponse.getStatusCode(),request.getUrl().toString(),null,null,getAdUnitList().get(0),null);
            }

        }


        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            PointEntitySigmobUtils.SigmobError("h5_error","mraid2",errorCode,failingUrl+" error:" +description,null,null,getAdUnitList().get(0),null);
            /**
             * 父view要通知自己创建的view加载完成
             */
            if (parentWebView != null && parentWebView.getMraidBridge() != null && !TextUtils.isEmpty(mUniqueId)) {
                parentWebView.getMraidBridge().notifyWvError(mUniqueId, errorCode, description);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            SigmobLog.i("onReceivedError:" + error.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PointEntitySigmobUtils.SigmobError("h5_error","mraid2",0,request.getUrl()+" error:" +error.getDescription(),null,null,getAdUnitList().get(0),null);
                    /**
                     * 父view要通知自己创建的view加载完成
                     */
                    if (parentWebView != null && parentWebView.getMraidBridge() != null && !TextUtils.isEmpty(mUniqueId)) {
                        parentWebView.getMraidBridge().notifyWvError(mUniqueId, error.getErrorCode(), ""+error.getDescription());
                    }
                }
            }
        }
        @Override
        public boolean onRenderProcessGone(final WebView view, final RenderProcessGoneDetail detail) {
            Log.d(TAG, "-----------onRenderProcessGone---------" + detail);
            WindAdError errorCode = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                errorCode = (detail != null && detail.didCrash())
                        ? WindAdError.RENDER_PROCESS_GONE_WITH_CRASH
                        : WindAdError.RENDER_PROCESS_GONE_UNSPECIFIED;
            }

            SigmobLog.e("handleRenderProcessGone " + errorCode);

            if (loadWebViewListener != null) {
                loadWebViewListener.handleRenderProcessGone(errorCode);
            }

            destroy();

            return true;
        }
    };


    public Mraid2WebView(Context context, List<BaseAdUnit> adUnitList, FrameLayout parentLayout) {

        this(context, adUnitList, parentLayout, null, null);


        addJavascriptInterface(new SigH5Js(), "sigandroidh5");

    }


    public void loadMain() {
        Template template = null;
        BaseAdUnit baseAdUnit = getAdUnitList().get(0);
        if (baseAdUnit.scene != null) {
            template = baseAdUnit.scene;
        } else if (baseAdUnit.getMaterial() != null && baseAdUnit.getMaterial().main_template != null) {
            template = baseAdUnit.getMaterial().main_template;
        } else if (baseAdUnit.getMaterial() != null && baseAdUnit.getMaterial().sub_template != null) {
            template = baseAdUnit.getMaterial().sub_template;
        }

        if (template != null) {
            switch (template.type) {
                case 1:
                    loadContentUrl(template.context.utf8());
                    break;
                case 2:
                    loadContentHtml(template.context.utf8());
                    break;
                case 3:
                    loadURLByPackage(template.context.utf8());
                    break;
            }
        }
    }


    private class SigH5Js {

        @JavascriptInterface
        public boolean isOpenListReport(){
            return !WindSDKConfig.getInstance().getCanOpenList().isEmpty() && !AppInstallService.isCanOpenTodaySend();
        }

        @JavascriptInterface
        public void onOpenListReport(){
            AppInstallService.onCanOpenListSend();
        }
        @JavascriptInterface
        public boolean canOpenByVid(String vid,String packageName) {

            if (!TextUtils.isEmpty(packageName)) {
                Intent launchIntentForPackage = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
                return launchIntentForPackage != null;
            } else {
                if(getAdUnitList() == null) return false;

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
                if(adUnit == null) return false;
                return adUnit.canOpen();
            }
        }
        @JavascriptInterface
        public boolean canOpen(String packageName) {

            if (!TextUtils.isEmpty(packageName)) {
                return IntentUtil.deviceCanHandlePackageName(SDKContext.getApplicationContext(),packageName);
            } else {
                return false;
            }
        }
        @JavascriptInterface
        public boolean canInstallByVid(String vid,String apkName) {

            if(getAdUnitList() == null) return false;

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
            if(adUnit == null) return false;
            if (TextUtils.isEmpty(apkName)){
                apkName = TextUtils.isEmpty(adUnit.getApkName())? TextUtils.isEmpty(adUnit.getApkMd5())? "":adUnit.getApkMd5()+".apk":adUnit.getApkName();
            }
            return adUnit.canInstall(apkName);
        }

    }


    public static HashMap<String, Mraid2WebView> getMraidWebViews() {
        return mMraidWebViews;
    }

    public String getUniqueId() {
        return mUniqueId;
    }

    public Mraid2WebView(Context context, List<BaseAdUnit> adUnitList, FrameLayout parentLayout, Mraid2WebView parentWebView, JSONObject args) {
        super(context);
        this.selfWebView = this;
        this.baseLayout = parentLayout;
        this.parentWebView = parentWebView;
        this.mHasLoaded = false;
        if (args != null) {
            try {
                this.arguments = new JSONObject(args.optString("args"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.mUniqueId = args.optString("uniqueId");
        } else {
            this.mUniqueId = "wv_" + UUID.randomUUID().toString();
            mMraidWebViews.put(this.mUniqueId, this);

        }

        this.enablePlugins(true);
        this.setBackgroundColor(Color.TRANSPARENT);

        try {
            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(getSettings(), new String(Base64.decode(WindConstants.ENABLEJS, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class, true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(getSettings(), new String(Base64.decode(ENABLEFILE, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class, true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        webViewClient.enableUseCache(WindSDKConfig.getInstance().enableWebSourceCache());

        BaseAdUnit baseAdUnit = adUnitList.get(0);
        if (baseAdUnit != null){
            webViewClient.disable_requested_with(baseAdUnit.isDisablexRequestWith());
        }

        this.setWebViewClient(webViewClient);

        mMraidBridge = new Mraid2Bridge(adUnitList);
        mMraidBridge.setBridgeV2Listener(mMraidBridgeV2Listener);
        mMraidBridge.attachView(this);
    }

    @SuppressLint("AddJavascriptInterface")
    public void loadContentHtml(String htmlData) {
//        Log.d(TAG, "---------loadContentHtml----------" + htmlData);
        addJavascriptInterface(new Mraid2Bridge.AppInfoJS(mMraidBridge), "sigandroid");
        String fileName = Md5Util.md5(htmlData);
        File file = SigmobFileUtil.dataToFile(htmlData, fileName + ".html");
        if (file != null && !TextUtils.isEmpty(file.getAbsolutePath())) {
            this.loadUrl(SIGMOBHTML + "://" + file.getAbsolutePath());
        } else {
            this.loadDataWithBaseURL(Networking.getBaseUrlScheme() + "://localhost/", htmlData, "text/html", "UTF-8", null);
        }

    }

    @SuppressLint("AddJavascriptInterface")
    public void loadContentUrl(String url) {
        Log.d(TAG, "---------loadContentUrl----------" + url);
        addJavascriptInterface(new Mraid2Bridge.AppInfoJS(mMraidBridge), "sigandroid");
        loadUrl(url);
    }

    @SuppressLint("AddJavascriptInterface")
    private void loadURLByPackage(String url) {//压缩包的地址
        if (TextUtils.isEmpty(url)) return;
        this.addJavascriptInterface(new Mraid2Bridge.AppInfoJS(mMraidBridge), "sigandroid");

        final String fileName = Md5Util.md5(url);
        final File sigZipDir = SigmobFileUtil.getSigZipDir(SigmobFileUtil.SigZipResourceDir);
        final File file = new File(sigZipDir.getAbsolutePath() + File.separator + fileName, "endcard.html");
        Log.d(TAG, file.exists() + "---------loadURLByPackage----------" + file.getAbsolutePath());
        if (file.exists()) {
            this.loadUrl(SIGMOBHTML + "://" + file.getAbsolutePath());
        } else {//不存在就要下载
            File destFile = new File(sigZipDir, fileName + ".tgz");
            //然后再去下载新的文件
            DownloadItem sceneItem = new DownloadItem();
            sceneItem.url = url;
            sceneItem.filePath = destFile.getAbsolutePath();
            sceneItem.type = DownloadItem.FileType.FILE;
            FileDownloader downloader = DownloaderFactory.getDownloader();
            downloader.add(sceneItem, new FileDownloadRequest.FileDownloadListener() {
                @Override
                public void onSuccess(DownloadItem item) {
                    Log.d(TAG, item.url + "-----------onSuccess----------" + item.filePath);
                    try {
                        GZipUtil.uncompressTarGzipSync(new File(item.filePath), new File(item.filePath.replace(".tgz", "/")));
                        if (file.exists()) {
                            selfWebView.loadUrl(SIGMOBHTML + "://" + file.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancel(DownloadItem item) {
                    Log.d(TAG, "-----------onCancel----------" + item.url);
                }

                @Override
                public void onErrorResponse(DownloadItem item) {
                    Log.d(TAG, "-----------onErrorResponse----------" + item.url);
                }

                @Override
                public void downloadProgress(DownloadItem item, long totalSize, long readSize) {

                }
            });
        }
    }

    private void loadContentId(String templateId) {
        Log.d(TAG, "---------loadContentId----------" + templateId);
        String htmlData = null;

        for (int i = 0; i < getAdUnitList().size(); i++) {
            BaseAdUnit baseAdUnit = getAdUnitList().get(i);
            if (baseAdUnit != null) {
                if (baseAdUnit.scene != null && templateId.equals(baseAdUnit.scene.templateId)) {
                    htmlData = baseAdUnit.scene.context.utf8();
                    break;
                }

                Ad ad = baseAdUnit.getAd();
                if (ad != null) {
                    if (ad.materials != null && ad.materials.get(0) != null) {
                        if (ad.materials.get(0).main_template != null && templateId.equals(ad.materials.get(0).main_template.templateId)) {
                            htmlData = ad.materials.get(0).main_template.context.utf8();
                            break;
                        }

                        if (ad.materials.get(0).sub_template != null && templateId.equals(ad.materials.get(0).sub_template.templateId)) {
                            htmlData = ad.materials.get(0).sub_template.context.utf8();
                            break;
                        }
                    }
                }
            }

        }

        if (!TextUtils.isEmpty(htmlData)) {
            loadContentHtml(htmlData);
        }
    }

    public ScrollTouchListener getScrollTouchListener() {
        return scrollTouchListener;
    }

    public void setScrollTouchListener(ScrollTouchListener scrollTouchListener) {
        this.scrollTouchListener = scrollTouchListener;
    }

    public void setNextWebViewListener(NextWebViewListener nextWebViewListener) {
        this.nextWebViewListener = nextWebViewListener;
    }

    public void setLoadListener(LoadWebViewListener loadWebViewListener) {
        this.loadWebViewListener = loadWebViewListener;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mMraidBridge != null) {
            if (visibility == View.VISIBLE) {
                mMraidBridge.notifyViewAbility(true);
            } else {
                mMraidBridge.notifyViewAbility(false);
            }
        }

    }

    public Mraid2Bridge getMraidBridge() {
        return mMraidBridge;
    }

    private AdSize adSize;

    public void setAdSize(AdSize adSize) {
        this.adSize = adSize;
    }

    public AdSize getAdSize() {
        return adSize;
    }

    public interface LoadWebViewListener {

        void onPageFinished(Mraid2WebView webView);

        void handleRenderProcessGone(WindAdError errorCode);

    }


    public interface NextWebViewListener {

        Mraid2WebView initWebView(Mraid2WebView parentWebView, JSONObject args);

        LinearLayout onUseScrollView(Mraid2WebView mraid2WebView, int orientation);

        void onClose(Mraid2WebView webView);

        void onUseCustomClose(Mraid2WebView webView, JSONObject args);

        void onReward(Mraid2WebView webView);

        void open(Mraid2WebView webView, BaseAdUnit uri, JSONObject ext);

        void onReady();
    }

    public interface ScrollTouchListener {

        void onTouchStart(JSONObject args);

        void onTouchMove(JSONObject args);

        void onTouchEnd(Mraid2WebView view, JSONObject args);
    }


    private static HashMap<String, Mraid2WebView> mMraidWebViews = new LinkedHashMap<>();

    private HashMap<String, MraidTimer> mMraidTimers = new LinkedHashMap<>();

    private Mraid2Motion mraid2Motion;
    private final Mraid2Bridge.MraidBridgeV2Listener mMraidBridgeV2Listener = new Mraid2Bridge.MraidBridgeV2Listener() {
        @Override
        public void onVpaidEvent(String subEvent, JSONObject args) {
            SigmobLog.d(" onVpaidEvent :" + subEvent + ":" + args);

            String uniqueId = args.optString("uniqueId");
            if (TextUtils.isEmpty(uniqueId)) {
                return;
            }
            MraidVpaid mraidVpaid = null;
            MraidObject mraidObject = mMraidObjects.get(uniqueId);
            if (mraidObject instanceof MraidVpaid) {
                mraidVpaid = (MraidVpaid) mraidObject;
            }

            switch (subEvent) {
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
                            PointEntitySigmobUtils.SigmobError(PointCategory.VIDEO,code,message,curAdUnit);

                        }
                    });
                    mraidVpaid.OnVpaidInit(getContext(), args);

                    View view = mraidVpaid.getView();
                    if (view != null) {
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(1, 1);
                        layoutParams.topMargin = 0;
                        layoutParams.rightMargin = 0;
                        view.setLayoutParams(layoutParams);
                    }
                    mMraidObjects.put(uniqueId, mraidVpaid);
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
                        mMraidObjects.remove(mraidVpaid);
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
        public void onTimerEvent(String subEvent, JSONObject args) {
            SigmobLog.d(" onTimerEvent :" + subEvent + ":" + args);

            String uniqueId = args.optString("uniqueId");
            if (TextUtils.isEmpty(uniqueId)) {
                return;
            }

            MraidTimer timer = mMraidTimers.get(uniqueId);

            switch (subEvent) {
                case "init":
                    timer = new MraidTimer(mMraidBridge, args);
                    mMraidTimers.put(uniqueId, timer);
                    break;
//                case "resume":
                case "fire":
                    if (timer != null) {
                        timer.fire();
                    }
                    break;
                case "pause":
                    if (timer != null) {
                        timer.pause();
                    }
                    break;
                case "invalidate":
                    if (timer != null) {
                        timer.invalidate();
                    }
                    mMraidTimers.remove(uniqueId);
                    break;
            }
        }

        @Override
        public void onWebViewEvent(String subEvent, JSONObject args) {
            SigmobLog.d(" onWebViewEvent :" + subEvent + ":" + args);

            String uniqueId = args.optString("uniqueId");
            if (TextUtils.isEmpty(uniqueId)) {
                return;
            }

            Mraid2WebView mraid2WebView = mMraidWebViews.get(uniqueId);

            switch (subEvent) {
                case "init":
                    if (nextWebViewListener != null) {
                        mraid2WebView = nextWebViewListener.initWebView(selfWebView, args);
                        mMraidWebViews.put(uniqueId, mraid2WebView);
                    }
                    break;
                case "loadURL":
                    String url = args.optString("url");
                    if (mraid2WebView != null) {
                        mraid2WebView.loadContentUrl(url);
                    }
                    break;
                case "frame":
                    if (mraid2WebView != null) {
                        JSONObject frame = args.optJSONObject("frame");

                        int top = frame.optInt("x", 0);
                        int left = frame.optInt("y", 0);
                        int width = frame.optInt("w", -1);
                        int height = frame.optInt("h", -1);
                        int realWidth = width > 0 ? Dips.dipsToIntPixels(width, SDKContext.getApplicationContext()) : width;
                        int realHeight = height > 0 ? Dips.dipsToIntPixels(height, SDKContext.getApplicationContext()) : height;

                        LayoutParams layoutParams = new LayoutParams(realWidth, realHeight);
                        mraid2WebView.setLayoutParams(layoutParams);
                        mraid2WebView.setX(Dips.dipsToIntPixels(top, SDKContext.getApplicationContext()));
                        mraid2WebView.setY(Dips.dipsToIntPixels(left, SDKContext.getApplicationContext()));
                        mraid2WebView.requestLayout();
                    }
                    break;
                case "loadHTMLString":
                    String html = args.optString("html");
                    if (mraid2WebView != null && !TextUtils.isEmpty(html)) {
                        mraid2WebView.loadContentHtml(html);
                    }
                    break;
                case "loadId":
                    String templateId = args.optString("id");
                    if (mraid2WebView != null && !TextUtils.isEmpty(templateId)) {
                        mraid2WebView.loadContentId(templateId);
                    }
                    break;
                case "loadURLByPackage":
                    String URL = args.optString("URL");
                    if (mraid2WebView != null && !TextUtils.isEmpty(URL)) {
                        mraid2WebView.loadURLByPackage(URL);
                    }
                    break;
                case "reload":
                    if (mraid2WebView != null) {
                        mraid2WebView.reload();
                    }
                    break;
                case "stopLoading":
                    if (mraid2WebView != null) {
                        mraid2WebView.stopLoading();
                    }
                    break;
            }
        }

        @Override
        public void onAnimationEvent(JSONObject args) {
            Log.d(TAG, "----------onAnimationEvent-------------" + args.toString());
            final String eventName = args.optString("event");
            String uniqueId = args.optString("uniqueId");
            double duration = args.optDouble("duration");
            JSONObject from = args.optJSONObject("from");
            JSONObject to = args.optJSONObject("to");

            int fromX = 0, fromY = 0, fromW = 0, fromH = 0;
            int toX = 0, toY = 0, toW = 0, toH = 0;

            if (from != null) {
                fromX = Dips.dipsToIntPixels(from.optInt("x"), getContext());
                fromY = Dips.dipsToIntPixels(from.optInt("y"), getContext());
                fromW = Dips.dipsToIntPixels(from.optInt("w"), getContext());
                fromH = Dips.dipsToIntPixels(from.optInt("h"), getContext());
            }

            if (to != null) {
                toX = Dips.dipsToIntPixels(to.optInt("x"), getContext());
                toY = Dips.dipsToIntPixels(to.optInt("y"), getContext());
                toW = Dips.dipsToIntPixels(to.optInt("w"), getContext());
                toH = Dips.dipsToIntPixels(to.optInt("h"), getContext());
            }

//            Log.d(TAG, fromX + ":" + fromY + ":" + fromW + ":" + fromH + "----------onAnimationEvent-------------" + toX + ":" + toY + ":" + toW + ":" + toH);

            Animator.AnimatorListener listener = new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
//                    Log.d(TAG, "----------onAnimationStart-------------");
                }

                @Override
                public void onAnimationEnd(Animator animation) {
//                    Log.d(TAG, "----------onAnimationEnd-------------");
                    mMraidBridge.notifyAnimationEvent(eventName);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
//                    Log.d(TAG, "----------onAnimationCancel-------------");
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
//                    Log.d(TAG, "----------onAnimationRepeat-------------");
                }
            };

            if (!TextUtils.isEmpty(uniqueId)) {
                MraidObject mraidObject = mMraidObjects.get(uniqueId);
                if (mraidObject != null) {
                    mraidObject.setParentId(mUniqueId);
                    View view = mraidObject.getView();
                    if (view != null) {//执行动画
                        AnimatorSet set = new AnimatorSet();
                        ViewWrapper wrapper = new ViewWrapper(view);
                        set.playTogether(
                                ObjectAnimator.ofFloat(view, "translationX", fromX, toX),
                                ObjectAnimator.ofFloat(view, "translationY", fromY, toY),
                                ObjectAnimator.ofInt(wrapper, "width", fromW, toW),
                                ObjectAnimator.ofInt(wrapper, "height", fromH, toH)
                        );
                        set.addListener(listener);
                        set.setDuration((long) (duration * 1000)).start();
                    }
                }

                Mraid2WebView webView = mMraidWebViews.get(uniqueId);
                if (webView != null) {//执行动画
                    AnimatorSet set = new AnimatorSet();
                    ViewWrapper wrapper = new ViewWrapper(webView);
                    set.playTogether(
                            ObjectAnimator.ofFloat(webView, "translationX", fromX, toX),
                            ObjectAnimator.ofFloat(webView, "translationY", fromY, toY),
                            ObjectAnimator.ofInt(wrapper, "width", fromW, toW),
                            ObjectAnimator.ofInt(wrapper, "height", fromH, toH)
                    );
                    set.addListener(listener);
                    set.setDuration((long) (duration * 1000)).start();
                }
            }
        }

        @Override
        public void onAddSubview(JSONObject args) {
            String uniqueId = args.optString("uniqueId");
            if (!TextUtils.isEmpty(uniqueId)) {

                MraidObject mraidObject = mMraidObjects.get(uniqueId);
                if (mraidObject != null) {
                    mraidObject.setParentId(mUniqueId);
                    View view = mraidObject.getView();
                    if (view != null) {

                        ViewUtil.removeFromParent(view);

                        if (isUseScrollView && scrollLayout != null) {
                            view.setX(0);
                            view.setY(0);
                            scrollLayout.addView(view);
                        } else {
                            baseLayout.addView(view);
                        }
                    }
                }

                Mraid2WebView webView = mMraidWebViews.get(uniqueId);
                if (webView != null) {
                    ViewUtil.removeFromParent(webView);
                    baseLayout.addView(webView);
                }
            }
        }

        @Override
        public void onBelowSubview(JSONObject args) {
            String uniqueId = args.optString("uniqueId");
            if (!TextUtils.isEmpty(uniqueId)) {
                MraidObject mraidObject = mMraidObjects.get(uniqueId);
                if (mraidObject != null) {
                    mraidObject.setParentId(mUniqueId);
                    View view = mraidObject.getView();
                    if (view != null) {

                        ViewUtil.removeFromParent(view);

                        if (isUseScrollView && scrollLayout != null) {
                            view.setX(0);
                            view.setY(0);
                            scrollLayout.addView(view);
                        } else {
                            baseLayout.addView(view);
                            baseLayout.bringChildToFront(selfWebView);
                        }
                    }
                }

                Mraid2WebView webView = mMraidWebViews.get(uniqueId);
                if (webView != null) {
                    ViewUtil.removeFromParent(webView);
                    baseLayout.addView(webView);
                    baseLayout.bringChildToFront(selfWebView);
                }
            }
        }

        @Override
        public void onUseScrollView(JSONObject args) {
            isUseScrollView = true;
            int orientation = args.optInt("flag");
            if (nextWebViewListener != null) {
                if (scrollLayout == null) {
                    scrollLayout = nextWebViewListener.onUseScrollView(selfWebView, orientation);
                    /**
                     * 按照VPaid原容器的顺序添加
                     */
                    List<MraidVideoAdView> viewList = new ArrayList<>();
                    for (int i = 0; i < baseLayout.getChildCount(); i++) {
                        View childAt = baseLayout.getChildAt(i);
                        if (childAt instanceof MraidVideoAdView) {
                            viewList.add((MraidVideoAdView) childAt);
                        }
                    }
                    Log.d(TAG, "-------------onUseScrollView----------" + viewList.size());
                    for (int i = 0; i < viewList.size(); i++) {
                        MraidVideoAdView view = viewList.get(i);
                        view.setX(0);
                        view.setY(0);
                        ViewUtil.removeFromParent(view);
                        scrollLayout.addView(view);
                    }

//                    for (MraidObject object : mMraidObjects.values()) {
//                        View view = object.getView();
//                        if (view != null) {
//                            boolean hasParent = false;
//                            if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
//                                hasParent = true;
//                                ((ViewGroup) view.getParent()).removeView(view);
//                            }
//
//
//                            if (hasParent) {
//                                view.setX(0);
//                                view.setY(0);
//                                scrollLayout.addView(view);
//                            }
//                        }
//                    }
                }
            }
        }

        @Override
        public void onClose() {
            if (nextWebViewListener != null) {
                nextWebViewListener.onClose(selfWebView);
            }
        }

        @Override
        public void onUseCustomClose(JSONObject args) {
            if (nextWebViewListener != null) {
                nextWebViewListener.onUseCustomClose(selfWebView, args);
            }
        }

        @Override
        public void onUnload() {
            //再将自己创建出来或者使用的Vapid移除
            for (MraidObject object : mMraidObjects.values()) {
                if (!TextUtils.isEmpty(object.getParentId()) && object.getParentId().equals(mUniqueId)) {
                    View view = object.getView();
                    ViewUtil.removeFromParent(view);
                    object.destroy();
                }
            }
            //把自己从父容器移除
            ViewUtil.removeFromParent(selfWebView);

            //onUnload销毁
            if (mMraidTimers != null && mMraidTimers.size() > 0) {
                for (MraidTimer mraidTimer : mMraidTimers.values()) {
                    mraidTimer.invalidate();
                }
                mMraidTimers.clear();
            }

            if (selfWebView != null) {
                selfWebView = null;
            }
        }

        @Override
        public void open(BaseAdUnit adUnit, JSONObject args) {
            if (nextWebViewListener != null) {
                nextWebViewListener.open(selfWebView, adUnit, args);
            }
        }

        @Override
        public void onReward() {
            if (nextWebViewListener != null) {
                nextWebViewListener.onReward(selfWebView);
            }
        }

        @Override
        public void setCurPlayAd(String vid) {
            if (!TextUtils.isEmpty(vid)) {
                for (int i = 0; i < getAdUnitList().size(); i++) {
                    curAdUnit = getAdUnitList().get(i);
                    if (vid.equals(curAdUnit.getVid())) {
                        break;
                    }

                }
            }

            if (curAdUnit != null) {
                AdStackManager.shareInstance().setLast_campid(curAdUnit.getCamp_id());
                AdStackManager.shareInstance().setLast_crid(curAdUnit.getCrid());
            }
        }

        @Override
        public void onReady() {
            if (nextWebViewListener != null) {
                nextWebViewListener.onReady();
            }
        }

        @Override
        public void onMotionEvent(JSONObject json) {
            SigmobLog.d(" postMessage data:" + json);
            JSONObject args = json.optJSONObject("args");
            String uniqueId = null;
            if (args != null) {
                uniqueId = args.optString("uniqueId");
            }
            String subEvent = json.optString("subEvent");

            if (TextUtils.isEmpty(uniqueId)) {
                SigmobLog.e(" onMotionViewEvent uniqueId is null:" + args);
                return;
            }
            Mraid2Motion motion = null;

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
//                    if (level == 0) {
//                        level = 3;
//                    } else if (level == 2) {
//                        level = 1;
//                    } else if (level == 1) {
//                        level = 2;
//                    }
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
        public void onMotionViewEvent(JSONObject json) {
            MraidMotionView motionView = null;
            SigmobLog.d(" postMessage data:" + json);

            JSONObject args = json.optJSONObject("args");
            String uniqueId = null;
            if (args != null) {
                uniqueId = args.optString("uniqueId");
            }
            String subEvent = json.optString("subEvent");

            if (TextUtils.isEmpty(uniqueId)) {
                SigmobLog.e(" onMotionViewEvent uniqueId is null:" + args);
                return;
            }
            MraidObject mraidObject = mMraidObjects.get(uniqueId);
            if (mraidObject instanceof MraidMotionView) {
                motionView = (MraidMotionView) mraidObject;
            }

            switch (subEvent) {
                case "init": {
                    int type = args.optInt("type");
                    motionView = new MraidMotionView(SDKContext.getApplicationContext(), uniqueId, type);
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
                }break;
                case "hidden": {
                    if (motionView != null) {
                        motionView.setHidden(args.optBoolean("hidden"));
                    }
                }
                break;
                case "start": {
                    if (motionView != null) {
                        motionView.start();
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


    @Override
    List<BaseAdUnit> getAdUnitList() {
        if (mMraidBridge != null) {
            return mMraidBridge.getRequestAdUnitList();
        }
        return null;
    }

    private static class ViewWrapper {

        private View target;

        public ViewWrapper(View target) {
            this.target = target;
        }

        public int getHeight() {
            return target.getLayoutParams().height;
        }

        public void setHeight(int height) {
            target.getLayoutParams().height = height;
            target.requestLayout();
        }

        public int getWidth() {
            return target.getLayoutParams().width;
        }

        public void setWidth(int width) {
            target.getLayoutParams().width = width;
            target.requestLayout();
        }
    }

    @Override
    public void destroy() {

        try {
            setLoadListener(null);
            setNextWebViewListener(null);
            setScrollTouchListener(null);
            if (selfWebView != null) {
                selfWebView = null;
            }
//        if (getAdUnitList() != null && getAdUnitList().size() > 0) {
//            for (int i = 0; i < getAdUnitList().size(); i++) {
//                BaseAdUnit baseAdUnit = getAdUnitList().get(i);
//                SessionManager sessionManager = baseAdUnit.getSessionManager();
//                if (sessionManager != null) {
//                    sessionManager.endDisplaySession();
//                }
//            }
//        }
            if (mMraidBridge != null) {
                mMraidBridge.destroy();
                mMraidBridge = null;
            }
            super.destroy();
        }catch (Throwable th){

        }


    }
}
