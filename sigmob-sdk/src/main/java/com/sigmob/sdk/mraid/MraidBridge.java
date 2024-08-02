package com.sigmob.sdk.mraid;

import static com.sigmob.sdk.base.WindConstants.ENABLEFILE;
import static com.sigmob.sdk.base.WindConstants.SIGMOBHTML;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.json.JSONSerializer;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.Md5Util;
import com.czhj.sdk.common.utils.ReflectionUtil;
import com.czhj.sdk.common.utils.TouchLocation;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.toolbox.StringUtil;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.IntentUtil;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.SigAdTracker;
import com.sigmob.sdk.base.common.ViewGestureDetector;
import com.sigmob.sdk.base.models.AppInfo;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.CurrentAppOrientation;
import com.sigmob.sdk.base.models.ExposureChange;
import com.sigmob.sdk.base.models.MraidEnv;
import com.sigmob.sdk.base.models.PlacementType;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.models.VideoItem;
import com.sigmob.sdk.base.models.ViewState;
import com.sigmob.sdk.base.models.rtb.Ad;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.base.models.rtb.RvAdSetting;
import com.sigmob.sdk.base.models.rtb.SlotAdSetting;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.sdk.videoAd.InterActionType;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MraidBridge {
    static final String MRAID_OPEN = "mraid://open?url=";
    private final BaseAdUnit mAdUnit;
    private final PlacementType mPlacementType;
    private final MraidNativeCommandHandler mMraidNativeCommandHandler;
    private String clickCoordinate;
    private MraidBridgeListener mMraidBridgeListener;
    private MraidWebView mMraidWebView;
    private ViewGestureDetector mGestureDetector;
    private boolean mHasLoaded;

    private final MraidWebViewClient mMraidWebViewClient = new MraidWebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleShouldOverrideUrl(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

            if (!TextUtils.isEmpty(mAdUnit.getVideoPath()) && url.endsWith(mAdUnit.getVideoPath()) && mAdUnit.isVideoExist()) {
                InputStream data = null;
                try {
                    data = new FileInputStream(mAdUnit.getVideoPath());
                    return new WebResourceResponse("video/mp4", "UTF-8", data);

                } catch (FileNotFoundException e) {
                    SigmobLog.e(e.getMessage());
                }
            } else {

            }

            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            handlePageFinished();
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PointEntitySigmobUtils.SigmobError("h5_error","mraid1",errorResponse.getStatusCode(),request.getUrl().toString(),null,null,mAdUnit,null);
            }
        }


        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            PointEntitySigmobUtils.SigmobError("h5_error","mraid1",errorCode,failingUrl+" error:" +description,null,null,mAdUnit,null);

        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            SigmobLog.i("onReceivedError:" + error.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PointEntitySigmobUtils.SigmobError("h5_error","mraid1",0,request.getUrl()+" error:" +error.getDescription(),null,null,mAdUnit,null);
                }
            }
        }
        @Override
        public boolean onRenderProcessGone(final WebView view, final RenderProcessGoneDetail detail) {
            handleRenderProcessGone(detail);
            return true;
        }
    };
    private MraidBridgeV2Listener mMraidBridgeV2Listener;

    MraidBridge(BaseAdUnit adUnit, PlacementType placementType) {
        this(adUnit, placementType, new MraidNativeCommandHandler());
    }

    MraidBridge(BaseAdUnit adUnit, PlacementType placementType,
                MraidNativeCommandHandler mraidNativeCommandHandler) {
        mAdUnit = adUnit;
        mPlacementType = placementType;
        mMraidNativeCommandHandler = mraidNativeCommandHandler;
    }

    public static String responseJson(int code, String message, Object data) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", code);
            if (!TextUtils.isEmpty(message)) {
                jsonObject.put("message", message);
            }
            if (data != null) {
                jsonObject.put("data", data);
            }
            return jsonObject.toString();
        } catch (Throwable throwable) {
            return throwable.getMessage();
        }

    }

    public static String convertJsUndefined(String value) {
        if (value == null) return null;

        if (value.equalsIgnoreCase("null") || value.equalsIgnoreCase("undefined")) {

            return null;
        }
        return value;
    }

    public String getClickCoordinate() {
        return clickCoordinate;
    }

    void setMraidBridgeListener(MraidBridgeListener listener) {
        mMraidBridgeListener = listener;
    }

    void setMraidBridgeV2Listener(MraidBridgeV2Listener listener) {
        mMraidBridgeV2Listener = listener;
    }

    @SuppressLint("AddJavascriptInterface")
    public void attachView(MraidWebView mraidWebView) {
        mMraidWebView = mraidWebView;
        try {
            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(mraidWebView.getSettings(), new String(Base64.decode(WindConstants.ENABLEJS, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class, true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(mraidWebView.getSettings(), new String(Base64.decode(ENABLEFILE, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class, true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (mPlacementType == PlacementType.INTERSTITIAL) {
                mraidWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }
        }

        mMraidWebViewClient.disable_requested_with(mAdUnit.isDisablexRequestWith());
        mMraidWebViewClient.enableUseCache(WindSDKConfig.getInstance().enableWebSourceCache());
        mMraidWebView.setScrollContainer(false);
        mMraidWebView.setVerticalScrollBarEnabled(false);
        mMraidWebView.setHorizontalScrollBarEnabled(false);
        mMraidWebView.setBackgroundColor(Color.TRANSPARENT);
        mMraidWebView.setAdUnit(mAdUnit);
        mMraidWebView.addSigAndroidAPK(null);
        mMraidWebView.setWebViewClient(mMraidWebViewClient);

        mMraidWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsAlert(final WebView view, final String url, final String message,
                                     final JsResult result) {
                if (mMraidBridgeListener != null) {
                    return mMraidBridgeListener.onJsAlert(message, result);
                }
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(final ConsoleMessage consoleMessage) {
                if (mMraidBridgeListener != null) {
                    return mMraidBridgeListener.onConsoleMessage(consoleMessage);
                }
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onShowCustomView(final View view, final CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
        });

        mGestureDetector = new ViewGestureDetector(
                mMraidWebView.getContext(), mMraidWebView, mAdUnit);

        mMraidWebView.setOnTouchListener(new OnTouchListener() {

            private MotionEvent downEvent;

            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                mGestureDetector.onTouchEvent(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    clickCoordinate = String.format("%d,%d,%d,%d", (int) downEvent.getRawX(), (int) downEvent.getRawY(), (int) event.getRawX(), (int) event.getRawY());

                    if (downEvent == null) {
                        downEvent = event;
                    }
                    SigMacroCommon baseMacroCommon = mAdUnit.getMacroCommon();
                    baseMacroCommon.updateClickMarco(downEvent, event, true);


                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downEvent = MotionEvent.obtain(event);
                }


                return false;
            }
        });

        mMraidWebView.setVisibilityChangedListener(new MraidWebView.OnVisibilityChangedListener() {
            @Override
            public void onVisibilityChanged(final boolean isVisible) {
                if (mMraidBridgeListener != null) {
                    mMraidBridgeListener.onVisibilityChanged(isVisible);
                }
            }
        });
    }

    void setClickCoordinate(String x, String y) {


        clickCoordinate = String.format("%s,%s,%s,%s", x, y, x, y);


        SigMacroCommon baseMacroCommon = mAdUnit.getMacroCommon();
        baseMacroCommon.updateClickMarco(x, y, x, y);


        mAdUnit.getClickCommon().down = new TouchLocation(Integer.parseInt(x), Integer.parseInt(y));
        mAdUnit.getClickCommon().up = new TouchLocation(Integer.parseInt(x), Integer.parseInt(y));

    }

    void detach() {
        if (mMraidWebView != null) {
            mMraidWebView.destroy();
            mMraidWebView = null;
        }
    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    public void setContentHtml(Context context, String htmlData) {
        if (mMraidWebView == null) {
            SigmobLog.e("MRAID bridge called setContentHtml before WebView was attached");
            return;
        }
        mMraidWebView.addJavascriptInterface(new AppInfoJS(this), "sigandroid");

        mHasLoaded = false;
        String fileName = Md5Util.md5(htmlData);
        File file = SigmobFileUtil.dataToFile(htmlData, fileName + ".html");
        if (file != null && !TextUtils.isEmpty(file.getAbsolutePath())) {
            mMraidWebView.loadUrl(SIGMOBHTML + "://" + file.getAbsolutePath());
        } else {
            mMraidWebView.loadDataWithBaseURL(Networking.getBaseUrlScheme() + "://localhost/",
                    htmlData, "text/html", "UTF-8", null);
        }
    }

    @SuppressLint("JavascriptInterface")
    public void setContentUrl(String url) {
        if (mMraidWebView == null) {
            SigmobLog.e("MRAID bridge called setContentHtml while WebView was not attached");
            return;
        }
        mMraidWebView.addJavascriptInterface(new AppInfoJS(this), "sigandroid");
        mHasLoaded = false;
        mMraidWebView.loadUrl(url);
    }

    void injectJavaScript(String javascript) {
        if (mMraidWebView == null) {
            SigmobLog.e("Attempted to inject Javascript into MRAID WebView while was not "
                    + "attached:\n\t" + javascript);
            return;
        }
        if (javascript.startsWith("bridge")) {
            javascript = javascript.replaceFirst("bridge", StringUtil.decode(StringUtil.s));
        }
        SigmobLog.d("Injecting Javascript into MRAID WebView:\n\t" + javascript);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mMraidWebView.evaluateJavascript(javascript, null);
        } else {
            mMraidWebView.loadUrl("javascript:" + javascript);
        }

    }

    void injectJavaScript(String javascript, ValueCallback callback) {
        if (mMraidWebView == null) {
            SigmobLog.e("Attempted to inject Javascript into MRAID WebView while was not "
                    + "attached:\n\t" + javascript);
            return;
        }
        if (javascript.startsWith("bridge")) {
            javascript = javascript.replaceFirst("bridge", StringUtil.decode(StringUtil.s));
        }
        SigmobLog.d("Injecting Javascript into MRAID WebView:\n\t" + javascript);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mMraidWebView.evaluateJavascript(javascript, callback);
        } else {
            SigmobLog.e("Injecting Javascript into MRAID WebView:\n\t can't support less KITKAT" + javascript);
        }
    }

    private void fireErrorEvent(MraidJavascriptCommand command, String message) {
        injectJavaScript("window.mraidbridge.notifyErrorEvent("
                + JSONObject.quote(command.toJavascriptString()) + ", "
                + JSONObject.quote(message) + ")");
    }

    private void fireNativeCommandCompleteEvent(MraidJavascriptCommand command) {
        injectJavaScript("window.mraidbridge.nativeCallComplete("
                + JSONObject.quote(command.toJavascriptString()) + ")");
    }

    boolean handleShouldOverrideUrl(WebView view, final String url) {
        try {
            // This is purely for validating the URI before proceeding
            final URI uri = new URI(url);
        } catch (URISyntaxException e) {
            SigmobLog.e("Invalid MRAID URL: " + url);
            fireErrorEvent(MraidJavascriptCommand.UNSPECIFIED, "Mraid command sent an invalid URL");
            return true;
        }

        Uri uri = Uri.parse(url);

        // Note that scheme will be null when we are passed a relative Uri
        String scheme = uri.getScheme();
        String host = uri.getHost();

        if (StringUtil.decode(StringUtil.s).equals(scheme)) {
            if ("failLoad".equals(host)) {
                if (mPlacementType == PlacementType.INLINE && mMraidBridgeListener != null) {
                    mMraidBridgeListener.onPageFailedToLoad();
                }
            }
            return true;
        }

        if ("mraid".equals(scheme)) {
            MraidJavascriptCommand command = MraidJavascriptCommand.fromJavascriptString(host);
            try {
                runCommand(command, ClientMetadata.getQueryParamMap(uri));
            } catch (Throwable exception) {
                fireErrorEvent(command, exception.getMessage());
            }
            fireNativeCommandCompleteEvent(command);
            return true;
        }

        //若设置WebViewClient且在方法中调用loadUrl的话则不会走retrun,所以返回true和false都是无效的，会重新加载url。
        if (!TextUtils.isEmpty(url)) {
            if (url.startsWith("http")) {
                view.loadUrl(url);
            } else {
                try {
                    List<String> scheme_white_list = mAdUnit.getAdSetting() != null ? mAdUnit.getAdSetting().scheme_white_list : null;
                    if (scheme_white_list != null && scheme_white_list.size() > 0) {
                        for (int i = 0; i < scheme_white_list.size(); i++) {
                            String sc = scheme_white_list.get(i);
                            if (url.startsWith(sc) || sc.equals("*")) {//通配符
                                IntentUtil.launchApplicationUrl(mMraidWebView.getContext(), Uri.parse(url));
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    private void handlePageFinished() {
        // This can happen a second time if the ad does something that changes the window location,
        // such as a redirect, changing window.location in Javascript, or programmatically clicking
        // a hyperlink. Note that the handleShouldOverrideUrl method skips doing its own
        // processing if the user hasn't clicked the ad.
        if (mHasLoaded) {
            return;
        }

        mHasLoaded = true;
        if (mMraidBridgeListener != null) {
            mMraidBridgeListener.onPageLoaded();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    void handleRenderProcessGone(final RenderProcessGoneDetail detail) {
        final WindAdError errorCode = (detail != null && detail.didCrash())
                ? WindAdError.RENDER_PROCESS_GONE_WITH_CRASH
                : WindAdError.RENDER_PROCESS_GONE_UNSPECIFIED;

        SigmobLog.e("handleRenderProcessGone " + errorCode.toString());
        detach();

        if (mMraidBridgeListener != null) {
            mMraidBridgeListener.onRenderProcessGone(errorCode);
        }
    }


    void notifyEvent(String event, HashMap<String, Object> args) {
        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("event", event);

            if (args != null) {
                JSONObject argsjson = new JSONObject(args);
                object.put("args", argsjson);
            }

            json.put("onChangeEvent", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    void notifyMotionEvent(String uniqueId, String type, String event, HashMap<String, Object> args) {
        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("event", event);
            object.put("uniqueId", uniqueId);

            if (args != null) {
                JSONObject argsjson = new JSONObject(args);
                object.put("args", argsjson);
            }

            json.put("onChangeFired", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    void runCommand(final MraidJavascriptCommand command,
                    Map<String, String> params)
            throws MraidCommandException {

        if (mMraidBridgeListener == null) {
            throw new MraidCommandException("Invalid state to execute this command");
        }

        if (mMraidWebView == null) {
            throw new MraidCommandException("The current WebView is being destroyed");
        }

        switch (command) {
            case CLOSE:
                mMraidBridgeListener.onClose();
                break;
            case UNLOAD:
                mMraidBridgeListener.onUnload();
                break;
            case OPENFOURELEMENTS:
                mMraidBridgeListener.onOpenFourElements();
                break;
            case RESIZE:
                // All these params are required
                int width = checkRange(parseSize(params.get("width")), 0, 100000);
                int height = checkRange(parseSize(params.get("height")), 0, 100000);
                int offsetX = checkRange(parseSize(params.get("offsetX")), -100000, 100000);
                int offsetY = checkRange(parseSize(params.get("offsetY")), -100000, 100000);
                CloseableLayout.ClosePosition closePosition = parseClosePosition(params.get("customClosePosition"), CloseableLayout.ClosePosition.TOP_RIGHT);
                boolean allowOffscreen = parseBoolean(params.get("allowOffscreen"), true);
                mMraidBridgeListener.onResize(width, height, offsetX, offsetY, closePosition, allowOffscreen);
                break;
            case EXPAND:
                URI uri = parseURI(params.get("url"), null);
                boolean shouldUseCustomClose = parseBoolean(params.get("shouldUseCustomClose"),
                        false);
                mMraidBridgeListener.onExpand(uri, shouldUseCustomClose);
                break;
            case USE_CUSTOM_CLOSE:
                shouldUseCustomClose = parseBoolean(params.get("shouldUseCustomClose"), false);
                mMraidBridgeListener.onUseCustomClose(shouldUseCustomClose);
                break;
            case OPEN:
                uri = parseURIWithDefaultValue(params.get("url"), "");
//                String x = convertJsUndefined(params.get("x")) ;
//                String y = convertJsUndefined(params.get("y"));
                String ext = convertJsUndefined(params.get("ext"));

                mMraidBridgeListener.onOpen(uri, InterActionType.BrowserType, ext);
                break;
            case feedBack:
                mMraidBridgeListener.onFeedBack();
                break;

            case SET_ORIENTATION_PROPERTIES:
                boolean allowOrientationChange = parseBoolean(params.get("allowOrientationChange"));
                MraidOrientation forceOrientation = parseOrientation(params.get("forceOrientation"));

                mMraidBridgeListener.onSetOrientationProperties(allowOrientationChange,
                        forceOrientation);
                break;
            case PLAY_VIDEO:
                uri = parseURI(params.get("uri"));
                mMraidBridgeListener.onPlayVideo(uri);
                break;
            case STORE_PICTURE:
                uri = parseURI(params.get("uri"));
                mMraidNativeCommandHandler.storePicture(mMraidWebView.getContext(), uri.toString(),
                        new MraidNativeCommandHandler.MraidCommandFailureListener() {
                            @Override
                            public void onFailure(final MraidCommandException exception) {
                                fireErrorEvent(command, exception.getMessage());
                            }
                        });
                break;

            case CREATE_CALENDAR_EVENT:
                mMraidNativeCommandHandler.createCalendarEvent(mMraidWebView.getContext(), params);
                break;
            case VPAID: {
                String event = params.get("event");
                mMraidBridgeListener.onVPaidEvent(event, params);
            }
            break;
            case EXTENSION: {
                String event = params.get("event");
                mMraidBridgeListener.onExtensionEvent(event, params);
            }
            break;

            case UNSPECIFIED:
                throw new MraidCommandException("Unspecified MRAID Javascript command");
        }
    }

    private CloseableLayout.ClosePosition parseClosePosition(String text,
                                                             CloseableLayout.ClosePosition defaultValue)
            throws MraidCommandException {
        if (TextUtils.isEmpty(text)) {
            return defaultValue;
        }

        if (text.equals("top-left")) {
            return CloseableLayout.ClosePosition.TOP_LEFT;
        } else if (text.equals("top-right")) {
            return CloseableLayout.ClosePosition.TOP_RIGHT;
        } else if (text.equals("center")) {
            return CloseableLayout.ClosePosition.CENTER;
        } else if (text.equals("bottom-left")) {
            return CloseableLayout.ClosePosition.BOTTOM_LEFT;
        } else if (text.equals("bottom-right")) {
            return CloseableLayout.ClosePosition.BOTTOM_RIGHT;
        } else if (text.equals("top-center")) {
            return CloseableLayout.ClosePosition.TOP_CENTER;
        } else if (text.equals("bottom-center")) {
            return CloseableLayout.ClosePosition.BOTTOM_CENTER;
        } else {
            throw new MraidCommandException("Invalid close position: " + text);
        }
    }

    private int parseSize(String text) throws MraidCommandException {
        int result;
        try {
            result = Integer.parseInt(text, 10);
        } catch (NumberFormatException e) {
            throw new MraidCommandException("Invalid numeric parameter: " + text);
        }
        return result;
    }

    private MraidOrientation parseOrientation(String text) throws MraidCommandException {
        if ("portrait".equals(text)) {
            return MraidOrientation.PORTRAIT;
        } else if ("landscape".equals(text)) {
            return MraidOrientation.LANDSCAPE;
        } else if ("none".equals(text)) {
            return MraidOrientation.NONE;
        } else {
            throw new MraidCommandException("Invalid orientation: " + text);
        }
    }

    private int checkRange(int value, int min, int max) throws MraidCommandException {
        if (value < min || value > max) {
            throw new MraidCommandException("Integer parameter out of range: " + value);
        }
        return value;
    }

    private boolean parseBoolean(
            String text, boolean defaultValue) throws MraidCommandException {
        if (text == null) {
            return defaultValue;
        }
        return parseBoolean(text);
    }

    private boolean parseBoolean(final String text) throws MraidCommandException {
        if ("true".equals(text)) {
            return true;
        } else if ("false".equals(text)) {
            return false;
        }
        throw new MraidCommandException("Invalid boolean parameter: " + text);
    }

    private URI parseURI(String encodedText, URI defaultValue)
            throws MraidCommandException {
        if (encodedText == null) {
            return defaultValue;
        }
        return parseURI(encodedText);
    }

    private URI parseURI(String encodedText) throws MraidCommandException {
        if (encodedText == null) {
            throw new MraidCommandException("Parameter cannot be null");
        }
        try {
            return new URI(encodedText);
        } catch (URISyntaxException e) {
            throw new MraidCommandException("Invalid URL parameter: " + encodedText);
        }
    }

    private URI parseURIWithDefaultValue(String encodedText, String defaultValue) throws MraidCommandException {

        try {
            if (encodedText == null) {
                return new URI(defaultValue);
            }
            return new URI(encodedText);
        } catch (URISyntaxException e) {
            try {
                return new URI(defaultValue);
            } catch (URISyntaxException uriSyntaxException) {
                throw new MraidCommandException("Invalid URL parameter: " + encodedText);
            }
        }
    }

    void notifyViewability(boolean isViewable) {
        injectJavaScript("mraidbridge.setIsViewable("
                + isViewable
                + ")");
    }

    void notifyPlacementType(PlacementType placementType) {
        injectJavaScript("mraidbridge.setPlacementType("
                + JSONObject.quote(placementType.toJavascriptString())
                + ")");
    }

    void notifyViewState(ViewState state) {
        injectJavaScript("mraidbridge.setState("
                + JSONObject.quote(state.toJavascriptString())
                + ")");
    }

    void notifySupports(boolean sms, boolean telephone, boolean calendar,
                        boolean storePicture, boolean inlineVideo, boolean vpaid, boolean location) {
        injectJavaScript("mraidbridge.setSupports("
                + sms + "," + telephone + "," + calendar + "," + storePicture + "," + inlineVideo
                + "," + vpaid + "," + location
                + ")");
    }

    private String stringifyRect(Rect rect) {
        return rect.left + "," + rect.top + "," + rect.width() + "," + rect.height();
    }

    private String stringifySize(Rect rect) {
        return rect.width() + "," + rect.height();
    }

    public void notifyScreenMetrics(final MraidScreenMetrics screenMetrics) {
        injectJavaScript("mraidbridge.setScreenSize("
                + stringifySize(screenMetrics.getScreenRectDips())
                + ");mraidbridge.setMaxSize("
                + stringifySize(screenMetrics.getRootViewRectDips())
                + ");mraidbridge.setCurrentPosition("
                + stringifyRect(screenMetrics.getCurrentAdRectDips())
                + ");mraidbridge.setDefaultPosition("
                + stringifyRect(screenMetrics.getDefaultAdRectDips())
                + ")");
        injectJavaScript("mraidbridge.notifySizeChangeEvent("
                + stringifySize(screenMetrics.getCurrentAdRectDips())
                + ")");
    }

    void notifyRvseting(RvAdSetting rvAdSetting) {
        String content = JSONSerializer.Serialize(rvAdSetting, "rvSetting", false);
        injectJavaScript("bridge.fireChangeEvent(" + content + ");");
    }

    void notifyVideo(VideoItem videoItem) {
        String content = JSONSerializer.Serialize(videoItem, "video", false);

        injectJavaScript("bridge.fireChangeEvent(" + content + ");");
    }

    void notifyMaterial(MaterialMeta materialMeta) {
        String content = JSONSerializer.Serialize(materialMeta, "material", true);
        injectJavaScript("bridge.fireChangeEvent(" + content + ");");
    }

    void notifyAd(Ad ad, SlotAdSetting adSetting) {
        String content = JSONSerializer.Serialize(ad, "ad", true);
        try {

            String serialize = JSONSerializer.Serialize(adSetting, null, true);

            JSONObject object = new JSONObject(content);
            JSONObject adObject = object.getJSONObject("ad");
            adObject.put("slotAdSetting", new JSONObject(serialize) );
            content = object.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        injectJavaScript("bridge.fireChangeEvent(" + content + ");");
    }

    void notifyENV(MraidEnv env) {
        String content = JSONSerializer.Serialize(env, "env", true);
        injectJavaScript("mraidbridge.fireChangeEvent(" + content + ");");
    }

    void notifyOSType() {

        injectJavaScript("bridge.fireChangeEvent({\"osType\":2});");

    }

    public void notifyLocation(Location location) {
        injectJavaScript("mraidbridge.setLocation(" + location.getLatitude() + "," + location.getLongitude() + ",\"" + location.getProvider() + "\");");
    }

    void notifyAppInfo(AppInfo appInfo) {

        injectJavaScript("bridge.fireChangeEvent({" + appInfo.toString().replace("=", ":") + "});");
    }

    void notifyAppOrientation(CurrentAppOrientation appOrientation) {

        injectJavaScript("mraidbridge.fireChangeEvent({" + appOrientation.toString().replace("=", ":") + "});");
    }

    void notifyHostSDKVersion() {

        injectJavaScript("mraidbridge.fireChangeEvent({\"hostSDKVersion\":" + WindConstants.SDK_VERSION + "});");
    }

    void notifyReady() {
        injectJavaScript("mraidbridge.notifyReadyEvent();");
    }

    void notifyApkDownloadStart() {
        injectJavaScript("bridge.notifyApkDownloadStartEvent();");
    }

    void notifyApkDownloadFail() {
        injectJavaScript("bridge.notifyApkDownloadFailEvent();");
    }

    void notifyApkDownloadProcess(int process) {
        injectJavaScript("bridge.notifyApkDownloadProcessEvent(" + process + ");");
    }

    void notifyApkDownloadEnd() {
        injectJavaScript("bridge.notifyApkDownloadEndEvent();");
    }

    void notifyApkDownloadInstalled() {
        injectJavaScript("bridge.notifyApkDownloadInstalledEvent();");
    }

    void notifyExposureChange(ExposureChange exposedProperty) {
        injectJavaScript("mraidbridge.fireChangeEvent({" + exposedProperty.toString().replace("=", ":") + "});");
    }

    void notifyVpaidGetPlayProgress(ValueCallback callback) {
        injectJavaScript("mraidbridge.getPlayProgress();", callback);
    }

    void notifyVpaidGetAdDuration(ValueCallback callback) {
        injectJavaScript("mraidbridge.getAdDuration();", callback);
    }

    void notifyVPaidStartAd() {
        injectJavaScript("mraidbridge.startAd();");
    }

    void fireVideoSrc(String url) {
        injectJavaScript("mraidbridge.fireVideoSrc(\"" + url + "\")");
    }

    void nativeCallComplete(String cmd) {
        injectJavaScript("mraidbridge.nativeCallCompleteV2(" + JSONObject.quote(cmd) + ")");
    }

    void notifyVPaidReady(String uniqueId, int duration, int width, int height) {
        JSONObject json = new JSONObject();
        try {
            json.put("uniqueId", uniqueId);
            json.put("duration", duration / 1000.0f);
            json.put("width", width);
            json.put("height", height);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        injectJavaScript("mraidbridge.setvdReadyToPlay(" + json + ")");
    }

    boolean isClicked() {
        final ViewGestureDetector gDetector = mGestureDetector;
        return gDetector != null && gDetector.isClicked();
    }

    boolean isViewable() {
        final MraidWebView mraidWebView = mMraidWebView;
        return mraidWebView != null && mraidWebView.isMraidViewable();
    }

    boolean isAttached() {
        return mMraidWebView != null;
    }

    boolean isLoaded() {
        return mHasLoaded;
    }


    public void notifyVPaidPlayCurrentTime(String uniqueId, int position, int duration) {
        JSONObject json = new JSONObject();
        try {
            json.put("uniqueId", uniqueId);
            json.put("currentTime", position / 1000.f);
            json.put("duration", duration / 1000.f);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.setvdPlayCurrentTime(" + json + ")");
    }

    public void notifyVPaidPlayError(String uniqueId, String error) {
        JSONObject json = new JSONObject();
        try {
            json.put("uniqueId", uniqueId);
            json.put("error", error);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.setvdPlayError(" + json + ")");
    }

    public void notifyVPaidPlayEnd(String uniqueId, int position) {
        JSONObject json = new JSONObject();
        try {
            json.put("uniqueId", uniqueId);
            json.put("currentTime", position / 1000.0f);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.setvdPlayToEnd(" + json + ")");
    }

    public void notifyVPaidLoadStateChange(String uniqueId, int state) {
        JSONObject json = new JSONObject();
        try {
            json.put("uniqueId", uniqueId);
            json.put("state", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.setvdLoadStateChanged(" + json + ")");
    }

    public void notifyVPaidPlayStateChange(String uniqueId, int state) {
        JSONObject json = new JSONObject();
        try {
            json.put("uniqueId", uniqueId);
            json.put("state", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        injectJavaScript("mraidbridge.setvdPlayStateChanged(" + json + ")");
    }

    public interface MraidBridgeV2Listener {
        void onVpaidEvent(String subEvent, JSONObject args);

        void onBelowSubview(String subEvent, JSONObject args);

        void onAddSubview(String subEvent, JSONObject args);

        void onMotionEvent(String subEvent, JSONObject args);

        void onMotionViewEvent(String subEvent, JSONObject args);

    }

    private interface MraidBridgeV3Listener {
        void onMotionViewEvent(String event, Map<String, String> params);
    }

    public interface MraidBridgeListener {
        void onPageLoaded();

        void onMraidJsLoaded();

        void onPageFailedToLoad();

        void onRenderProcessGone(final WindAdError error);

        void onVisibilityChanged(boolean isVisible);

        boolean onJsAlert(String message, JsResult result);

        boolean onConsoleMessage(ConsoleMessage consoleMessage);


        void onExpand(URI uri, boolean shouldUseCustomClose) throws MraidCommandException;

        void onClose();

        void onUseCustomClose(boolean shouldUseCustomClose);

        void onSetOrientationProperties(boolean allowOrientationChange, MraidOrientation
                forceOrientation) throws MraidCommandException;

        void onOpen(URI uri, int interActionType, String ext);

        void onPlayVideo(URI uri);

        void onVPaidEvent(String event, Map<String, String> params);

        void onExtensionEvent(String event, Map<String, String> params);

        void onUnload();

        void onOpenFourElements();

        void onResize(int width, int height, int offsetX, int offsetY, CloseableLayout.ClosePosition closePosition, boolean allowOffscreen);

        void onFeedBack();
    }


    //      var payload = {"arguments": ['os', 'idfa','os_version']};
    static class AppInfoJS extends Object {

        private final WeakReference<MraidBridge> mraidBridgeWeakReference;

        public AppInfoJS(MraidBridge mraidBridge) {
            mraidBridgeWeakReference = new WeakReference<>(mraidBridge);
        }

        public MraidBridge getMraidBridge() {
            return mraidBridgeWeakReference.get();
        }

        public BaseAdUnit getAdUnit() {
            MraidBridge mraidBridge = getMraidBridge();
            if (mraidBridge != null) {
                return mraidBridge.mAdUnit;
            }
            return null;
        }

        @JavascriptInterface
        public String javascriptAddDcLog(final JSONObject jsonData) {

            try {
                if (jsonData == null) return responseJson(400, "not params", null);

                if (jsonData.has("_ac_type")) {

                    String ac_type = jsonData.getString("_ac_type");

                    BaseAdUnit adUnit = getAdUnit();
                    PointEntitySigmobUtils.SigmobMRIADJS(ac_type, adUnit, null, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {


                            Map<String, String> option = new HashMap<>();
                            for (Iterator<String> it = jsonData.keys(); it.hasNext(); ) {
                                String key = it.next();

                                if (key.equalsIgnoreCase(WindConstants.EXT)) {
                                    try {
                                        String ext = jsonData.optString(key);
                                        JSONObject object = new JSONObject(ext);
                                        for (Iterator<String> obj = object.keys(); obj.hasNext(); ) {
                                            String k = obj.next();
                                            option.put(k, object.optString(k));
                                        }
                                    } catch (Exception e) {
                                        try {
                                            option.put(WindConstants.EXT, Base64.encodeToString((jsonData.getString(WindConstants.EXT)).getBytes(), Base64.NO_WRAP));
                                        } catch (JSONException jsonException) {
                                            jsonException.printStackTrace();
                                        }
                                    }
                                } else if (!key.equalsIgnoreCase("_ac_type")) {
                                    try {
                                        option.put(key, jsonData.getString(key));
                                    } catch (Throwable e) {

                                    }
                                }
                            }
                            option.put(WindConstants.SOURCE, "js");

                            if (pointEntityBase instanceof PointEntitySigmob) {

                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                entitySigmob.setOptions(option);
                            }


                        }
                    });

                } else {
                    return responseJson(300, "_ac_type is empty", null);
                }

            } catch (Throwable throwable) {
                return responseJson(500, throwable.toString(), null);
            }

            return responseJson(200, "add dc log success", null);
        }

        @JavascriptInterface
        public String func(String jsonData) {
            try {
                JSONObject data = new JSONObject(jsonData);
                String func = null;
                if (data.has("func")) {
                    func = data.getString("func");
                }

                if (!TextUtils.isEmpty(func)) {
                    String funcation = func.replace(":", "");
                    ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(this, funcation);
                    methodBuilder.addParam(JSONObject.class, data);
                    Object result = methodBuilder.execute();
                    return (String) result;
                } else {
                    return responseJson(400, "func is empty", null);
                }

            } catch (Throwable throwable) {
                return responseJson(500, throwable.getMessage(), null);
            }

        }

        @JavascriptInterface
        public String hello(JSONObject jsonData) {

            try {

                return jsonData.toString();
            } catch (Throwable throwable) {

            }

            return null;

        }


        @JavascriptInterface
        public String addMacro(JSONObject jsonData) {

            try {
                BaseAdUnit adUnit = getAdUnit();

                String key = null;
                if (jsonData.has("key")) {
                    key = jsonData.getString("key");
                }
                String value = null;
                if (jsonData.has("value")) {
                    value = jsonData.getString("value");
                }

                if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                    return responseJson(300, "key or value is empty", null);
                }

                adUnit.getMacroCommon().addMarcoKey(key, value);

                return responseJson(200, "addMacro success", null);

            } catch (Throwable throwable) {
                return responseJson(500, "addMacro add fail " + throwable.getMessage(), null);
            }


        }

        @JavascriptInterface
        public String excuteRewardAdTrack(JSONObject jsonData) {
            try {
                BaseAdUnit adUnit = getAdUnit();

                String event = jsonData.getString("event");
                int result = SigmobTrackingRequest.sendJsTrackings(adUnit, event, true);
                if (result == 0) {
                    return responseJson(200, "excuteRewardAdTrack success", null);

                } else if (result == -1) {
                    return responseJson(300, "event is empty", null);

                } else if (result == -2) {
                    return responseJson(300, event + " can't find in trackers", null);
                } else {
                    return responseJson(400, "unknown error: " + result, null);
                }

            } catch (Throwable throwable) {
                return responseJson(500, "unknown error: " + throwable.getMessage(), null);

            }
        }

        @JavascriptInterface
        public String tracking(JSONObject jsonData) {
            try {
                BaseAdUnit adUnit = getAdUnit();

                String event = jsonData.optString("event");
                JSONArray urls = jsonData.optJSONArray("urls");
                if (urls != null && urls.length() > 0) {
                    for (int i = 0; i < urls.length(); i++) {
                        SigAdTracker sigAdTracker = new SigAdTracker(urls.optString(i), event, adUnit.getRequestId());
                        sigAdTracker.setRetryNum(adUnit.getTrackingRetryNum());
                        sigAdTracker.setSource("js");
                        SigmobTrackingRequest.sendTracking(sigAdTracker, adUnit, false);
                    }
                    return responseJson(200, "tracking success", null);
                }
                return responseJson(300, "urls is empty", null);
            } catch (Throwable throwable) {
                return responseJson(500, "unknown error: " + throwable.getMessage(), null);
            }
        }

        @JavascriptInterface
        public String mraidJsLoaded() {

            MraidBridge mraidBridge = getMraidBridge();
            if (mraidBridge != null && mraidBridge.mMraidBridgeListener != null) {
                mraidBridge.mMraidBridgeListener.onMraidJsLoaded();
            }
            return null;
        }

        /*
       {"event":"vpaid","subEvent":"OnInit","args":{"uniqueId":"vd_1_1655111854345"}}
        */
        @JavascriptInterface
        public void postMessage(final String data) {

            final MraidBridge mraidBridge = getMraidBridge();


            SigmobLog.d("postMessage: raw " + data);


            if (mraidBridge != null) {
                mraidBridge.mMraidWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = new JSONObject(data);

                            String event = json.optString("event");

                            String subEvent = json.optString("subEvent");
                            JSONObject args = json.optJSONObject("args");

                            mraidBridge.handlePostMessage(event, subEvent, args);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mraidBridge.nativeCallComplete(data);

                    }
                });
            }


        }

        @JavascriptInterface
        public String getAppInfo(JSONObject jsonData) {
            try {

                JSONObject result = new JSONObject();
                BaseAdUnit mAdUnit = getAdUnit();

                if (jsonData != null && mAdUnit != null && jsonData.has("arguments")) {

                    JSONArray arguments = jsonData.getJSONArray("arguments");

                    for (int i = 0; i < arguments.length(); i++) {
                        String key = arguments.getString(i);
                        try {
                            switch (key) {
                                case "os": {
                                    result.put(key, 2);
                                }
                                break;
                                case "imei": {
                                    result.put(key, ClientMetadata.getInstance().getDeviceId());
                                }
                                break;
                                case "android_id": {
                                    result.put(key, ClientMetadata.getInstance().getAndroidId());
                                }
                                break;
                                case "google_aid": {
                                    result.put(key, ClientMetadata.getInstance().getAdvertisingId());
                                }
                                break;
                                case "clienttype": {
                                    result.put(key, ClientMetadata.getDeviceModel());
                                }
                                break;
                                case "app_version": {
                                    result.put(key, ClientMetadata.getInstance().getAppVersion());
                                }
                                break;
                                case "sdk_version": {
                                    result.put(key, WindConstants.SDK_VERSION);
                                }
                                break;
                                case "os_version": {
                                    result.put(key, ClientMetadata.getDeviceOsVersion());
                                }
                                break;
                                case "client_pixel": {
                                    String pixel = String.format("%sx%s", ClientMetadata.getInstance().getDisplayMetrics().widthPixels, ClientMetadata.getInstance().getDisplayMetrics().heightPixels);
                                    result.put(key, pixel);
                                }
                                break;
                                case "device_width": {
                                    result.put(key, ClientMetadata.getInstance().getDeviceScreenWidthDip());
                                }
                                break;
                                case "device_height": {
                                    result.put(key, ClientMetadata.getInstance().getDeviceScreenHeightDip());
                                }
                                break;
                                case "screen_density": {
                                    result.put(key, ClientMetadata.getInstance().getDensityDpi());
                                }
                                break;
                                case "network_type": {
                                    result.put(key, ClientMetadata.getInstance().getActiveNetworkType());
                                }
                                break;
                                case "pkgname": {
                                    result.put(key, ClientMetadata.getInstance().getAppPackageName());
                                }
                                break;
                                case "screenangle": {
                                    result.put(key, Math.abs(ClientMetadata.getInstance().getOrientationInt() - 1) * 90);
                                }
                                break;
                                case "creative_type": {
                                    result.put(key, mAdUnit.getCreativeType());
                                }
                                break;
                                case "ad_type": {
                                    result.put(key, mAdUnit.getAd_type());
                                }
                                break;
                                case "request_id": {
                                    result.put(key, mAdUnit.getRequestId());
                                }
                                break;
                                case "placement_id": {
                                    result.put(key, mAdUnit.getAdslot_id());
                                }
                                break;
                                case "appid": {
                                    result.put(key, WindAds.sharedAds().getAppId());
                                }
                                break;
                                case "ad_source_logo": {
                                    result.put(key, mAdUnit.getAd_source_logo());
                                }
                                break;
                                case "ad_source_channel": {
                                    result.put(key, mAdUnit.getAd_source_channel());
                                }
                                break;
                                case "adslot_id": {
                                    result.put(key, mAdUnit.getAdslot_id());
                                }
                                break;
                                case "vid": {
                                    result.put(key, mAdUnit.getAd().vid);
                                }
                                break;
                                case "crid": {
                                    result.put(key, mAdUnit.getCrid());
                                }
                                break;
                                case "camp_id": {
                                    result.put(key, mAdUnit.getCamp_id());
                                }
                                break;
                                case "cust_id": {
                                    result.put(key, mAdUnit.getAd().cust_id);
                                }
                                break;
                                case "bid_price": {
                                    result.put(key, mAdUnit.getAd().bid_price);
                                }
                                break;
                                case "product_id": {
                                    result.put(key, mAdUnit.getAd().product_id);
                                }
                                break;
                                case "settlement_price_enc": {
                                    result.put(key, mAdUnit.getAd().settlement_price_enc);
                                }
                                break;
                                case "is_override": {
                                    result.put(key, mAdUnit.getAd().is_override);
                                }
                                break;
                                case "forbiden_parse_landingpage": {
                                    result.put(key, mAdUnit.getAd().forbiden_parse_landingpage);
                                }
                                break;
                                case "display_orientation": {
                                    result.put(key, mAdUnit.getDisplay_orientation());
                                }
                                break;
                                case "expired_time": {
                                    result.put(key, mAdUnit.getAd().expired_time);
                                }
                                break;
                                default: {

                                }
                                break;
                            }
                        } catch (Throwable throwable) {

                        }

                    }

                }
                return responseJson(200, "getAppInfo success ", result);
            } catch (Throwable ex) {
                return responseJson(500, "getAppInfo error: " + ex.getMessage(), null);
            }
        }

    }

    private void handlePostMessage(String event, String subEvent, JSONObject args) {


        switch (event) {
            case "vpaid": {
                mMraidBridgeV2Listener.onVpaidEvent(subEvent, args);
            }
            break;
            case "belowSubview": {
                mMraidBridgeV2Listener.onBelowSubview(subEvent, args);

            }
            break;
            case "addSubview": {
                mMraidBridgeV2Listener.onAddSubview(subEvent, args);
            }
            break;
            case "motion": {
                mMraidBridgeV2Listener.onMotionEvent(subEvent, args);

            }
            break;
            case "motionView": {
                mMraidBridgeV2Listener.onMotionViewEvent(subEvent, args);
            }
            break;
            default: {

            }
            break;
        }
    }


}
