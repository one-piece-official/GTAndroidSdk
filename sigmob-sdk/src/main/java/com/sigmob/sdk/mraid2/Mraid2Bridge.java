package com.sigmob.sdk.mraid2;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.json.JSONSerializer;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ReflectionUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.wire.Wire;
import com.sigmob.sdk.Sigmob;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.common.AdSize;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.SigAdTracker;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.models.rtb.Ad;
import com.sigmob.sdk.base.models.rtb.AndroidMarket;
import com.sigmob.sdk.base.models.rtb.BidResponse;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.base.models.rtb.SlotAdSetting;
import com.sigmob.sdk.base.models.rtb.Template;
import com.sigmob.sdk.base.models.rtb.WXProgramRes;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.RequestFactory;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;
import com.sigmob.sdk.videoAd.InterActionType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public class Mraid2Bridge implements MraidBridgeMotionListener {
    private static String TAG = "Mraid2Bridge";
    private List<BaseAdUnit> adUnitList;
    private MraidBridgeV2Listener mBridgeV2Listener;
    private Mraid2WebView mWebView;
    private static MraidStorageManager mraidStorageManager;

    private Mraid2Controller.Mraid2ControllerListener mControllerListener;

    private List<BaseAdUnit> requestAdUnitList = new LinkedList<>();
    private static HashMap<String, List<String>> eventMap = new HashMap<>();
    private volatile boolean isJsLoaded;
    private boolean injectMraid = false;

    Mraid2Bridge(List<BaseAdUnit> adUnitList) {
        this.adUnitList = adUnitList;
        this.requestAdUnitList.addAll(adUnitList);
    }

    void setBridgeV2Listener(MraidBridgeV2Listener listener) {
        mBridgeV2Listener = listener;
    }

    public void setControllerListener(Mraid2Controller.Mraid2ControllerListener controllerListener) {
        this.mControllerListener = controllerListener;
    }

    public Mraid2WebView getWebView() {
        return mWebView;
    }


    @SuppressLint("AddJavascriptInterface")
    public void attachView(Mraid2WebView mraidWebView) {
        mWebView = mraidWebView;
        mWebView.setScrollContainer(false);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (!isJsLoaded && !injectMraid && newProgress>70){
                    injectMraid = true;
                    injectJavaScript("javascript:"+Mraid2Javascript.JAVASCRIPT_SOURCE,null);
                }
            }

            @Override
            public boolean onJsAlert(final WebView view, final String url, final String message, final JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(final ConsoleMessage consoleMessage) {
                if (mControllerListener != null) {
                   return mControllerListener.onConsoleMessage(consoleMessage);
                }
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onShowCustomView(final View view, final CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
        });
    }

    void injectJavaScript(String javascript) {

        if (!isJsLoaded) {
            SigmobLog.e("MRAID JS Not Load " + "attached:\n\t" + javascript);
            return;
        }

        if (mWebView == null) {
            SigmobLog.e("Attempted to inject Javascript into MRAID WebView while was not " + "attached:\n\t" + javascript);
            return;
        }
        SigmobLog.d("Injecting Javascript into MRAID WebView:\n\t" + javascript);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(javascript, null);
        } else {
            mWebView.loadUrl("javascript:" + javascript);
        }
    }

    void injectJavaScript(String javascript, ValueCallback callback) {
        if (mWebView == null) {
            SigmobLog.e("Attempted to inject Javascript into MRAID WebView while was not " + "attached:\n\t" + javascript);
            return;
        }
        SigmobLog.d("Injecting Javascript into MRAID WebView:\n\t" + javascript);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(javascript, callback);
        } else {
            SigmobLog.e("Injecting Javascript into MRAID WebView:\n\t can't support less KITKAT" + javascript);
        }
    }

    private void handleJsLoad() {
        isJsLoaded = true;
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyViewAbility(true);
                    notifyAppOrientation();
                    notifyScreenSize(mWebView.getAdSize());
                    notifySDKVersion();
                    notifyExposureChange(100);

                    BidResponse bidResponse = doBidResponseToJson(adUnitList);

                    notifyBidResponse(bidResponse);
                } catch (Throwable th) {
                    SigmobLog.e("handleMraidLoad", th);
                }
            }
        });
    }

    private BidResponse doBidResponseToJson(List<BaseAdUnit> adUnitList) {

        List<Ad> adList = new ArrayList<>();

        for (int i = 0; i < adUnitList.size(); i++) {
            Ad ad = adUnitList.get(i).getAd();
            List<MaterialMeta> metaList = new ArrayList<>();

            if (ad != null && ad.materials != null && ad.materials.size() > 0) {

                MaterialMeta.Builder builder = ad.materials.get(0).newBuilder();

                if (builder.main_template != null && builder.main_template.type == 2) {
                    //这里一定要随机一个数值,不然对象一样转为Json会丢失
                    Template build = builder.main_template.newBuilder().context(null).build();
                    build.templateId = builder.main_template.templateId;
                    builder.main_template(build);
                }
                if (builder.sub_template != null && builder.sub_template.type == 2) {
                    //这里一定要随机一个数值,不然对象一样转为Json会丢失
                    Template build = builder.sub_template.newBuilder().context(null).build();
                    build.templateId = builder.sub_template.templateId;
                    builder.sub_template(build);
                }

                MaterialMeta build = builder.html_snippet(null).build();
                metaList.add(build);
                adList.add(ad.newBuilder().materials(metaList).build());

            }

        }

        BidResponse.Builder builder = new BidResponse.Builder();

        if (adUnitList.get(0) != null) {
            if (adUnitList.get(0).scene != null) {
                //这里一定要随机一个数值,不然对象一样转为Json会丢失
                Template build;
                if (adUnitList.get(0).scene.type == 2) {
                    build = adUnitList.get(0).scene.newBuilder().context(null).build();
                    build.templateId = adUnitList.get(0).scene.templateId;
                } else {
                    build = adUnitList.get(0).scene.newBuilder().build();
                }
                builder.scene(build);
            }

            if (adUnitList.get(0).slotAdSetting != null) {
                SlotAdSetting build = adUnitList.get(0).slotAdSetting.newBuilder().build();
                builder.slot_ad_setting(build);
            }

            if (adUnitList.get(0).bidding_response != null) {
                BiddingResponse build = adUnitList.get(0).bidding_response.newBuilder().build();
                builder.bidding_response(build);
            }

            builder.request_id(adUnitList.get(0).getRequestId());
            builder.uid(adUnitList.get(0).uid);
            builder.expiration_time(adUnitList.get(0).expiration_time);
        }

        return builder.ads(adList).build();
    }

    public boolean isJsLoaded() {
        return isJsLoaded;
    }

    public interface MraidBridgeV2Listener {

        void onVpaidEvent(String subEvent, JSONObject args);

        void onTimerEvent(String subEvent, JSONObject args);

        void onWebViewEvent(String subEvent, JSONObject args);

        void onAnimationEvent(JSONObject args);

        void onAddSubview(JSONObject args);

        void onBelowSubview(JSONObject args);

        void onUseScrollView(JSONObject args);

        void onClose();

        void onUseCustomClose(JSONObject args);

        void onUnload();

        void open(BaseAdUnit adUnit, JSONObject args);

        void onReward();

        void setCurPlayAd(String vid);

        void onReady();

        void onMotionEvent(JSONObject args);

        void onMotionViewEvent(JSONObject json);
    }

    private LoadAdRequest getLoadAdRequest() {
        if (adUnitList != null && adUnitList.size() > 0) {
            return adUnitList.get(0).getAdRequest();
        }
        return null;
    }

    public BaseAdUnit getAdUnitByVid(String vid) {
        if (TextUtils.isEmpty(vid)) return null;
        if (requestAdUnitList != null && requestAdUnitList.size() > 0) {
            for (int i = 0; i < requestAdUnitList.size(); i++) {
                BaseAdUnit baseAdUnit = adUnitList.get(i);
                if (vid.equals(baseAdUnit.getAd().vid)) {
                    return baseAdUnit;
                }
            }
        }
        return null;
    }

    static class AppInfoJS extends Object {

        private final WeakReference<Mraid2Bridge> mraidBridgeWeakReference;

        public AppInfoJS(Mraid2Bridge mraidBridge) {
            mraidBridgeWeakReference = new WeakReference<>(mraidBridge);
        }

        public Mraid2Bridge getMraidBridge() {
            return mraidBridgeWeakReference.get();
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
        public String getUniqueId() {

            Mraid2Bridge mraidBridge = getMraidBridge();
            if (mraidBridge != null) {
                return mraidBridge.getWebView().getUniqueId();
            }
            return null;
        }

        @JavascriptInterface
        public String addDclog(final JSONObject jsonData) {
            try {
                if (jsonData == null) return responseJson(400, "not params", null);

                Log.d(TAG, "----------addDclog----------" + jsonData);

                final JSONObject data = jsonData.optJSONObject("data");
                if (data != null && data.has("_ac_type")) {
                    String ac_type = data.optString("_ac_type");
                    String vid = jsonData.optString("vid");

                    BaseAdUnit adUnit = getMraidBridge() != null ? getMraidBridge().getAdUnitByVid(vid) : null;

                    LoadAdRequest adRequest = null;
                    if (adUnit == null) {
                        adRequest = getMraidBridge().getLoadAdRequest();
                    }

                    PointEntitySigmobUtils.SigmobMRIADJS(ac_type, adUnit, adRequest, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {

                            Map<String, String> option = new HashMap<>();
                            for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                                String key = it.next();
                                if (!key.equalsIgnoreCase("_ac_type")) {
                                    option.put(key, data.optString(key));
                                }
                            }

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
        public String tracking(JSONObject jsonData) {
            try {
                if (jsonData == null) return responseJson(400, "not params", null);

                Log.d(TAG, "---------tracking----------" + jsonData);

                String event = jsonData.optString("event");
                JSONArray urls = jsonData.optJSONArray("urls");
                JSONObject data = jsonData.optJSONObject("data");
                boolean inQueue = false;//失败是否进入队列
                boolean statistic = false;//是否统计请求结果
                boolean repeat = false;//是否统计请求结果

                int retry = 0;//重试次数
                String vid = "";//广告唯一曝光ID，当vid有值时，红替换优先使用vid区域的宏，同时在inQueue或者statistic为true时也会携带上对应的广告信息

                if (data != null) {
                    inQueue = data.optBoolean("inQueue");
                    statistic = data.optBoolean("statistic");
                    retry = data.optInt("retry");
                    vid = data.optString("vid");
                    repeat = data.optBoolean("repeat");

                }

                BaseAdUnit adUnit = getMraidBridge() != null ? getMraidBridge().getAdUnitByVid(vid) : null;

                if (urls != null && urls.length() > 0) {
                    for (int i = 0; i < urls.length(); i++) {
                        SigAdTracker sigAdTracker = new SigAdTracker(urls.optString(i), event, adUnit != null ? adUnit.getRequestId() : "");
                        sigAdTracker.setRetryNum(retry);
                        sigAdTracker.setSource("js");
                        SigmobTrackingRequest.sendTracking(sigAdTracker, adUnit, repeat, inQueue, statistic, null);
                    }
                    return responseJson(200, "tracking success", null);
                } else {
                    List<SigAdTracker> sigAdTrackers = null;
                    if (adUnit != null) {
                        sigAdTrackers = adUnit.getAdTracker(event);
                    }
                    if (sigAdTrackers != null) {
                        for (SigAdTracker tracker : sigAdTrackers) {
                            tracker.setRetryNum(retry);
                            tracker.setSource("js");
                            SigmobTrackingRequest.sendTracking(tracker, adUnit, repeat, inQueue, statistic, null);

                        }
                    }
                }
                return responseJson(300, "urls is empty", null);
            } catch (Throwable throwable) {
                return responseJson(500, "unknown error: " + throwable.getMessage(), null);
            }
        }

        @JavascriptInterface
        public String getDeviceInfo() {
            try {
                Log.d(TAG, "---------getDeviceInfo----------");

                JSONObject result = new JSONObject();
                result.put("clientType", ClientMetadata.getDeviceModel());
                result.put("osVersion", ClientMetadata.getDeviceOsVersion());
                result.put("appVersion", ClientMetadata.getInstance().getAppVersion());
                result.put("width", ClientMetadata.getInstance().getDisplayMetrics().widthPixels);
                result.put("height", ClientMetadata.getInstance().getDisplayMetrics().heightPixels);
                result.put("screenDensity", ClientMetadata.getInstance().getDensityDpi());
                result.put("networkType", ClientMetadata.getInstance().getActiveNetworkType());
                result.put("pkgName", ClientMetadata.getInstance().getAppPackageName());
                result.put("userAgent", Networking.getUserAgent());
                result.put("uid", ClientMetadata.getUid());
                result.put("udid", ClientMetadata.getInstance().getUDID());
                JSONObject android = new JSONObject();
                android.put("imei", ClientMetadata.getInstance().getDeviceId());
                android.put("androidId", ClientMetadata.getInstance().getAndroidId());
                android.put("googleId", ClientMetadata.getInstance().getAdvertisingId());
                android.put("oaid", ClientMetadata.getInstance().getOAID());
                result.put("android", android);
                return result.toString();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @JavascriptInterface
        public String storage(String json) {
            try {
                if (TextUtils.isEmpty(json)) return null;

                Log.d(TAG, "-----------storage---------" + json);

                JSONObject jsonData = new JSONObject(json);
                String event = jsonData.optString("event");
                JSONObject args = jsonData.optJSONObject("args");

                int type = args.optInt("type");
                String key = args.optString("key");
                String value = args.optString("value");
                if (type == 1 || type == 2) {//localStorage//sessionStorage
                    if (mraidStorageManager == null) {
                        mraidStorageManager = new MraidStorageManager(getMraidBridge().mWebView.getContext());
                    }
                    switch (event) {
                        case "setItem":
                            mraidStorageManager.setItem(type, key, value);
                            break;
                        case "getItem":
                            return mraidStorageManager.getItem(type, key);
                        case "removeItem":
                            mraidStorageManager.removeItem(type, key);
                            break;
                        case "clear":
                            mraidStorageManager.clear(type);
                            break;
                        case "length":
                            return String.valueOf(mraidStorageManager.length(type));
                        case "addEventListener"://"storage_key"
                            mraidStorageManager.addEventListener(type, key, new MraidStorageManager.ValueChangeListener() {
                                @Override
                                public void valueChange(final JSONObject object) {
                                    if (getMraidBridge() != null && getMraidBridge().getWebView() != null) {
                                        getMraidBridge().getWebView().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                getMraidBridge().notifyStorageChangeEvent(object);
                                            }
                                        });
                                    }
                                }
                            });
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @JavascriptInterface
        public String handleMacro(JSONObject jsonData) {
            try {
                if (jsonData == null) return null;

                Log.d(TAG, "-----------handleMacro---------" + jsonData);

                String event = jsonData.optString("event");
                JSONObject args = jsonData.optJSONObject("args");

                if (args == null) return null;
                String key = args.optString("key");
                String value = args.optString("value");
                String vid = args.optString("vid");

                BaseAdUnit adUnit = getMraidBridge() != null ? getMraidBridge().getAdUnitByVid(vid) : null;

                switch (event) {
                    case "addAllMacros": {

                        JSONObject maps = args.optJSONObject("maps");

                        if (maps != null) {
                            Iterator<String> keys = maps.keys();
                            while (keys.hasNext()) {
                                String k = keys.next();
                                String v = maps.optString(k);
                                if (adUnit != null) {
                                    adUnit.getMacroCommon().addMarcoKey(k, v);
                                } else {
                                    Sigmob.getInstance().getMacroCommon().addMarcoKey(k, v);
                                }
                            }
                        }
                    }
                    break;
                    case "addMacro":
                        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                            return null;
                        }
                        if (adUnit != null) {
                            adUnit.getMacroCommon().addMarcoKey(key, value);
                        } else {
                            Sigmob.getInstance().getMacroCommon().addMarcoKey(key, value);
                        }
                        break;
                    case "getMacro":
                        if (TextUtils.isEmpty(key)) {
                            return null;
                        }
                        if (adUnit != null) {
                            return adUnit.getMacroCommon().getMarcoKey(key);
                        } else {
                            return Sigmob.getInstance().getMacroCommon().getMarcoKey(key);
                        }
                    case "removeMacro":
                        if (TextUtils.isEmpty(key)) {
                            return null;
                        }
                        if (adUnit != null) {
                            adUnit.getMacroCommon().removeMarcoKey(key);
                        } else {
                            Sigmob.getInstance().getMacroCommon().removeMarcoKey(key);
                        }
                        break;
                    case "clearMacro":
                        if (adUnit != null) {
                            adUnit.getMacroCommon().clearMacro();
                        } else {
                            Sigmob.getInstance().getMacroCommon().clearMacro();
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * {"event":"vpaid","subEvent":"OnInit","args":{"uniqueId":"vd_1_1655111854345"}}
         *
         * @param data
         */
        @JavascriptInterface
        public void postMessage(final String data) {
            if (TextUtils.isEmpty(data)) return;
            Log.d(TAG, "-----------postMessage---------" + data);
            final Mraid2Bridge mraidBridge = getMraidBridge();
            if (mraidBridge != null && mraidBridge.mWebView != null) {

                mraidBridge.mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mraidBridge.handlePostMessage(mraidBridge, data);
                    }
                });
            }
        }
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

    public List<BaseAdUnit> getRequestAdUnitList() {
        return requestAdUnitList;
    }

    private void handlePostMessage(final Mraid2Bridge mraidBridge, String data) {
        try {

            SigmobLog.d("handlePostMessage data: " + data);
            JSONObject json = new JSONObject(data);
            String event = json.optString("event");
            String subEvent = json.optString("subEvent");
            JSONObject args = json.optJSONObject("args");

            switch (event) {
                case "mraidJsLoaded": {
                    handleJsLoad();
                }
                break;
                case "ready": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onReady();
                    }
                }
                break;
                case "visible": {
                    if (args != null) {
                        boolean visible = args.optBoolean("visible");
                        if (visible) {
                            mWebView.setVisibility(View.VISIBLE);
                        } else {
                            mWebView.setVisibility(View.INVISIBLE);
                        }
                    }
                    mraidBridge.nativeCallComplete(data);
                }
                break;
                case "dispatch_after": {
                    final String eventName = args.optString("event");
                    int delay = args.optInt("delay");
                    if (!TextUtils.isEmpty(eventName) && delay >= 0)
                        mraidBridge.getWebView().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mraidBridge.notifyDispatchAfterEvent(eventName);
                            }
                        }, delay);
                }
                break;
                case "mraidLoadAd": {
                    final String ev = args.optString("event");
                    JSONObject object = args.optJSONObject("data");
                    Map<String, String> option = new HashMap<>();

                    if (object != null) {
                        for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                            String key = it.next();
                            option.put(key, object.optString(key));
                        }
                    }

                    if (adUnitList.get(0) != null) {
                        LoadAdRequest adRequest = adUnitList.get(0).getAdRequest();
                        adRequest.setOptions(option);
                        adRequest.setLastCampid(AdStackManager.shareInstance().getLast_campid());
                        adRequest.setLastCrid(AdStackManager.shareInstance().getLast_crid());
                        RequestFactory.LoadAd(adRequest, new RequestFactory.LoadAdRequestListener() {
                            @Override
                            public void onSuccess(final List<BaseAdUnit> adUnitList, LoadAdRequest loadAdRequest) {
                                if (adUnitList != null && adUnitList.size() > 0) {
                                    Log.d(TAG, "-----------onSuccess---------" + adUnitList.size());
                                    requestAdUnitList.addAll(adUnitList);
                                    mraidBridge.notifyAdLoad(ev, doBidResponseToJson(adUnitList), 0, null);

                                    PointEntitySigmobUtils.SigmobTracking(PointCategory.RESPOND, Constants.SUCCESS, null, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                        @Override
                                        public void onAddExtra(Object pointEntityBase) {
                                            if (pointEntityBase instanceof PointEntitySigmob) {
                                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                                HashMap<String, String> extData = new HashMap<>();
                                                extData.put("ad_count", String.valueOf(adUnitList.size()));
                                                extData.put("request_id", adUnitList.get(0).getRequestId());
                                                entitySigmob.setOptions(extData);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onErrorResponse(int error, String message, String request_id, LoadAdRequest loadAdRequest) {
                                Log.d(TAG, "-----------onErrorResponse---------" + error + ":" + message);
                                mraidBridge.notifyAdLoad(ev, null, error, message);
                                PointEntitySigmobUtils.SigmobTracking(PointCategory.RESPOND, Constants.FAIL, loadAdRequest);
                            }
                        });
                    }
                }
                break;
                case "feedbackByVid": {
                    String vid = args.optString("vid");
                    BaseAdUnit adUnit = null;
                    if (!TextUtils.isEmpty(vid)) {
                        JSONObject ext = args.optJSONObject("data");
                        if (ext == null) {
                            ext = new JSONObject();
                        }
                        ext.put("vid", vid);
                        adUnit = mraidBridge.getAdUnitByVid(vid);
                    }
                    if (mControllerListener != null) {
                        mControllerListener.showDislikeDialog(adUnit);
                    }
                }
                break;
                case "openByVid": {
                    String vid = args.optString("vid");

                    if (!TextUtils.isEmpty(vid)) {
                        JSONObject ext = args.optJSONObject("data");
                        if (ext == null) {
                            ext = new JSONObject();
                        }
                        ext.put("vid", vid);
                        BaseAdUnit adUnit = mraidBridge.getAdUnitByVid(vid);

                        final String deeplinkUrl = adUnit.getDeeplinkUrl();
                        final String landingPage = adUnit.getLanding_page();
                        AndroidMarket androidMarket = adUnit.getAndroidMarket();
                        int interaction_type = adUnit.getInteractionType();
                        WXProgramRes wxProgramRes = adUnit.getWXProgramRes();

                        JSONObject jsonObject = new JSONObject();

                        if (androidMarket != null && !TextUtils.isEmpty(androidMarket.market_url)) {

                            interaction_type = 3;

                            JSONObject market = new JSONObject();
                            market.put("market_url", androidMarket.market_url);
                            market.put("type", Wire.get(androidMarket.type, 0));

                            if (!TextUtils.isEmpty(androidMarket.app_package_name)) {
                                market.put("app_package_name", androidMarket.app_package_name);
                            }
                            if (!TextUtils.isEmpty(androidMarket.appstore_package_name)) {
                                market.put("appstore_package_name", androidMarket.appstore_package_name);
                            }

                            ext.put("market", market);
                        } else if (interaction_type == InterActionType.MiniProgramType && wxProgramRes != null) {

                            JSONObject program = new JSONObject();
                            program.put("wx_app_id", wxProgramRes.wx_app_id);
                            program.put("wx_app_username", wxProgramRes.wx_app_username);
                            program.put("wx_app_path", wxProgramRes.wx_app_path);

                            ext.put("program", program);

                        } else {

                            if (!TextUtils.isEmpty(deeplinkUrl) && interaction_type != InterActionType.DownloadType) {
                                jsonObject.put("url", deeplinkUrl);
                            } else {
                                jsonObject.put("url", landingPage);
                            }
                        }
                        ext.put("default_url", landingPage);
                        ext.put("in_app", !adUnit.isSkipSigmobBrowser());
                        ext.put("interaction_type", interaction_type);
                        ext.put("parse_302", !adUnit.getAd().forbiden_parse_landingpage);

                        jsonObject.put("event", event);
                        jsonObject.put("data", ext);

                        if (mBridgeV2Listener != null) {
                            mBridgeV2Listener.open(adUnit, jsonObject);
                        }
                    }
                    mraidBridge.nativeCallComplete(data);

                }
                break;
                case "open": {
                    String my_vid = null;
                    JSONObject ext = args.optJSONObject("data");
                    if (ext != null) {
                        my_vid = ext.optString("vid");
                    }

                    BaseAdUnit adUnit = null;
                    if (!TextUtils.isEmpty(my_vid)) {
                        adUnit = mraidBridge.getAdUnitByVid(my_vid);
                    }

                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.open(adUnit, args);
                    }
                    mraidBridge.nativeCallComplete(data);
                }
                break;
                case "close": {
                    if (mBridgeV2Listener != null) {//关闭所有/销毁所有
                        mBridgeV2Listener.onClose();
                    }
                }
                break;
                case "unload": {
                    if (mBridgeV2Listener != null) {//关闭当前/销毁所有
                        mBridgeV2Listener.onUnload();
                    }
                }
                break;
                case "reward": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onReward();
                    }
                }
                break;
                case "curPlayAd": {
                    String vid = args.optString("vid");
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.setCurPlayAd(vid);
                    }
                }
                break;
                case "addSubview": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onAddSubview(args);
                    }
                    mraidBridge.nativeCallComplete(data);
                }
                break;
                case "belowSubview": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onBelowSubview(args);
                    }
                    mraidBridge.nativeCallComplete(data);
                }
                break;
                case "useScrollView": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onUseScrollView(args);
                    }
                    mraidBridge.nativeCallComplete(data);
                }
                break;

                case "useCustomClose": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onUseCustomClose(args);
                    }
                }
                break;

                case "timer": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onTimerEvent(subEvent, args);
                    }
                    mraidBridge.nativeCallComplete(data);
                }
                break;

                case "vpaid": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onVpaidEvent(subEvent, args);
                    }
                    mraidBridge.nativeCallComplete(data);
                }
                break;

                case "webView": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onWebViewEvent(subEvent, args);
                    }
                    mraidBridge.nativeCallComplete(data);
                }
                break;
                case "animation": {
                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onAnimationEvent(args);
                    }
                }
                break;

                case "touchStart":
                    if (mWebView.getScrollTouchListener() != null) {
                        mWebView.getScrollTouchListener().onTouchStart(args);
                    }
                    break;
                case "touchMove":
                    if (mWebView.getScrollTouchListener() != null) {
                        mWebView.getScrollTouchListener().onTouchMove(args);
                    }
                    break;
                case "touchEnd":
                    if (mWebView.getScrollTouchListener() != null) {
                        mWebView.getScrollTouchListener().onTouchEnd(mWebView, args);
                    }
                    break;
                case "motion": {

                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onMotionEvent(json);
                    }
                    mraidBridge.nativeCallComplete(data);

                }
                break;
                case "motionView": {

                    if (mBridgeV2Listener != null) {
                        mBridgeV2Listener.onMotionViewEvent(json);
                    }
                    mraidBridge.nativeCallComplete(data);
                }
                break;
                case "backgroundColor": {

                    if (mControllerListener != null) {
                        int red = args.optInt("red");
                        int green = args.optInt("green");
                        int blue = args.optInt("blue");
                        int alpha = args.optInt("alpha");

                        mControllerListener.background(red, green, blue, alpha);
                    }

                }
                break;
                case "blurEffect": {

                    switch (subEvent) {
                        case "init": {
                            if (mControllerListener != null) {
                                mControllerListener.blurEffectStart();
                            }

                        }
                        break;
                        case "destroy": {
                            if (mControllerListener != null) {
                                mControllerListener.blurEffectEnd();
                            }
                        }
                    }
                    mraidBridge.nativeCallComplete(data);

                }
                break;

                case "subscribe": {
                    subscribeEvent(args);
                    mraidBridge.nativeCallComplete(data);

                }
                break;
                case "unsubscribe": {
                    unsubscribeEvent(args);
                    mraidBridge.nativeCallComplete(data);

                }
                break;
                case "publish": {
                    publishEvent(args);
                    mraidBridge.nativeCallComplete(data);


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unsubscribeEvent(JSONObject args) {
        if (args != null) {
            String uniqId = args.optString("uniqId");
            String event = args.optString("event");
            if (!TextUtils.isEmpty(uniqId) && !TextUtils.isEmpty(event)) {
                List<String> uniqIds = eventMap.get(uniqId + "~" + event);
                if (uniqIds != null) {
                    CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(uniqIds);
                    for (String id : list) {
                        if (id.equals(uniqId)) {
                            uniqIds.remove(id);
                        }
                    }
                }
            }
        }
    }


    private void publishEvent(JSONObject args) {
        if (args != null) {
            String uniqId = getWebView().getUniqueId();
            String event = args.optString("event");
            if (!TextUtils.isEmpty(uniqId) && !TextUtils.isEmpty(event)) {
                List<String> ids = eventMap.get(uniqId + "~" + event);
                if (ids != null && ids.size() > 0) {
                    HashMap<String, Mraid2WebView> mraidWebViews = Mraid2WebView.getMraidWebViews();
                    if (mraidWebViews != null) {
                        for (String id : ids) {
                            Mraid2WebView mraid2WebView = mraidWebViews.get(id);
                            if (mraid2WebView != null) {
                                Mraid2Bridge mraidBridge = mraid2WebView.getMraidBridge();
                                if (mraidBridge != null) {
                                    mraidBridge.notifyChangeEvent(uniqId, event, args);
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private void subscribeEvent(JSONObject args) {
        if (args != null) {

            String uniqId = args.optString("uniqId");
            String event = args.optString("event");

            if (!TextUtils.isEmpty(uniqId) && !TextUtils.isEmpty(event)) {
                List<String> uniqIds = eventMap.get(event);
                if (uniqIds == null) {
                    uniqIds = new ArrayList<>();
                    eventMap.put(uniqId + "~" + event, uniqIds);
                }
                uniqIds.add(mWebView.getUniqueId());
            }
        }
    }


    public void notifyWvFinished(String uniqueId) {
        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("uniqueId", uniqueId);
            json.put("wvFinished", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    public void notifyWvError(String uniqueId, int code, String message) {
        JSONObject json = new JSONObject();
        try {
            JSONObject error = new JSONObject();
            error.put("code", code);
            error.put("message", message);

            JSONObject object = new JSONObject();
            object.put("uniqueId", uniqueId);
            object.put("error", error);
            json.put("wvError", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    public void notifyScreenSize(AdSize adSize) {
        DisplayMetrics displayMetrics = mWebView.getContext().getResources().getDisplayMetrics();

        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            int width, height;
            if (adSize != null) {
                width = Dips.pixelsToIntDips(adSize.getWidth(), mWebView.getContext());
                height = Dips.pixelsToIntDips(adSize.getHeight(), mWebView.getContext());
            } else {
                width = Dips.pixelsToIntDips(displayMetrics.widthPixels, mWebView.getContext());
                height = Dips.pixelsToIntDips(displayMetrics.heightPixels, mWebView.getContext());
            }
            object.put("width", width);
            object.put("height", height);
            json.put("screenSize", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    void notifyViewAbility(boolean isViewable) {
        JSONObject json = new JSONObject();
        try {
            json.put("viewable", isViewable);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ")");
    }

    void notifyExposureChange(int exposedPercentage) {
        injectJavaScript("mraidbridge.fireChangeEvent({\"exposure\":" + exposedPercentage + "});");
    }

    void notifyReady() {
        injectJavaScript("mraidbridge.fireReadyEvent();");
    }

    void notifyBindDate(JSONObject jsonObject) {

        JSONObject json = new JSONObject();
        try {
            json.put("bindData", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    void notifyChangeEvent(String uniqid, String event, JSONObject data) {

        JSONObject json = new JSONObject();
        Object obj = data.opt("data");

        try {
            JSONObject object = new JSONObject();

            object.put("event", uniqid + "~" + event);
            if (obj != null) {
                object.put("data", obj);
            }
            json.put("onChangeEvent", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    void notifyBidResponse(BidResponse bidResponse) {
        String content = JSONSerializer.Serialize(bidResponse, "bidResponse", true, true);
        injectJavaScript("mraidbridge.fireChangeEvent(" + content + ");");
    }

    void notifyAppOrientation() {
        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("orientation", ClientMetadata.getInstance().getOrientationInt());
            object.put("locked", true);
            json.put("orientation", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    void notifySDKVersion() {
        JSONObject json = new JSONObject();
        try {
            json.put("sdkVersion", WindConstants.SDK_VERSION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ")");
    }


    void notifyVPaidReady(String uniqueId, int duration, int width, int height) {

        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("uniqueId", uniqueId);
            object.put("duration", duration / 1000.0f);
            object.put("width", width);
            object.put("height", height);
            json.put("vdReadyToPlay", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    public void notifyVPaidPlayStateChange(String uniqueId, int state) {

        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("uniqueId", uniqueId);
            object.put("state", state);
            json.put("vdPlayStateChanged", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    public void notifyVPaidLoadStateChange(String uniqueId, int state) {

        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("uniqueId", uniqueId);
            object.put("state", state);
            json.put("vdLoadStateChanged", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    public void notifyVPaidPlayCurrentTime(String uniqueId, int position, int duration) {
        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("uniqueId", uniqueId);
            object.put("currentTime", position / 1000.f);
            object.put("duration", duration / 1000.f);
            json.put("vdPlayCurrentTime", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    public void notifyVPaidPlayEnd(String uniqueId, int position) {
        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("uniqueId", uniqueId);
            object.put("currentTime", position / 1000.0f);
            json.put("vdPlayToEnd", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }


    public void notifyVPaidPlayError(String uniqueId, String error) {
        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("uniqueId", uniqueId);
            object.put("error", error);
            json.put("vdPlayError", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    void nativeCallComplete(String cmd) {
        injectJavaScript("mraidbridge.nativeCallComplete(" + JSONObject.quote(cmd) + ")");
    }

    void notifyStorageChangeEvent(JSONObject jsonObject) {
        injectJavaScript("mraidbridge.onStorageChanged(" + jsonObject.toString() + ");");
    }

    /**
     * 翻页通知
     *
     * @param type
     * @param index
     */
    void notifyPageChangeEvent(int type, int index) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("index", index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        injectJavaScript("mraidbridge.notifyPageChangeEvent(" + json + ");");
    }

    void notifyFireEvent(String key) {
        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("event", "fire_" + key);
            json.put("notify", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    public void notifyEvent(String event, HashMap<String, Object> args) {
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

    @Override
    public void notifyMotionEvent(String uniqueId, String type, String event, HashMap<String, Object> args) {
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

    void notifyDispatchAfterEvent(String key) {

        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("event", key);
            json.put("notify", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    void notifyAnimationEvent(String key) {

        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("event", key);
            json.put("notify", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    /**
     * @param event open_success\open_failed
     */
    void notifyOpenState(String event, String message) {

        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("event", event);
            if (!TextUtils.isEmpty(message)) {
                object.put("message", message);
            }
            json.put("notify", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    private void notifyShakeEvent(String key, Map extras) {

        JSONObject json = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("event", key);
            json.put("notify", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    void notifyAdLoad(String event, BidResponse bidResponse, int code, String message) {

        JSONObject json = new JSONObject();
        try {
            JSONObject object;
            if (bidResponse == null) {
                object = new JSONObject();
                object.put("data", null);
            } else {
                String content = JSONSerializer.Serialize(bidResponse, "data", true, true);
                object = new JSONObject(content);
            }

            object.put("code", code);
            object.put("message", message);

            JSONObject data = new JSONObject();
            data.put("event", event);
            data.put("message", object);

            json.put("notify", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        injectJavaScript("mraidbridge.fireChangeEvent(" + json + ");");
    }

    public void destroy() {
        if (mraidStorageManager != null) {
            mraidStorageManager = null;
        }
        if (mWebView != null) {
            mWebView = null;
        }

        setBridgeV2Listener(null);
        setControllerListener(null);
    }

    public interface PageChangedListener {
        void onPageChanged(Mraid2WebView view, int type, int index);
    }
}
