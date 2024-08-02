package com.sigmob.sdk.base;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.utils.SigmobFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SigmobWebViewClient extends WebViewClient {


    static final String COOKIES_HEADER = "Set-Cookie";
    static final String REFERER_HEADER = "Referer";

    private static final String MRAID_JS = "mraid.js";
    private static final String MRAID2_JS = "mraid2.js";

    private static final String COOKIE_REQUEST_HEADER = "Cookie";
    private CookieManager msCookieManager = new CookieManager();
    private boolean isDisableRequestedWith = false;

    private boolean useCache = false;
    private String mReferer;


    public SigmobWebViewClient() {
//        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }
    /*
     */

    public void enableUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    /**
     * Gets Cookies from the response header and loads them into cookie manager
     *
     * @param conn          instance of {@link HttpURLConnection} object
     * @param cookieManager the cookie manager({@link CookieManager} instance) in which the cookies are to be loaded<p>In case a null object is passed, the function will not perform any action and return back to the caller. </p>
     */
    public static void loadResponseCookies( HttpURLConnection conn,  CookieManager cookieManager) {

        //do nothing in case a null cokkie manager object is passed
        if (cookieManager == null || conn == null) {
            return;

        }

        Map<String, List<String>> headerFields = conn.getHeaderFields();
        SigmobLog.d("headerFields : " + headerFields.toString());
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
        if (cookiesHeader != null) {
            for (String cookieHeader : cookiesHeader) {
                List<HttpCookie> cookies;
                try {
                    cookies = HttpCookie.parse(cookieHeader);
                } catch (NullPointerException e) {
                    SigmobLog.e(MessageFormat.format("{0} -- Null header for the cookie : {1}", conn.getURL().toString(), cookieHeader.toString()));
                    //ignore the Null cookie header and proceed to the next cookie header
                    continue;
                }

                if (cookies != null) {
                    SigmobLog.d("{0} -- Reading Cookies from the response :" + conn.getURL().toString());
                    SigmobLog.d("{0} -- Reading Cookies from the response :" + cookies.get(0));

                    for (HttpCookie cookie : cookies) {
                        SigmobLog.d(cookie.toString());
                        android.webkit.CookieManager.getInstance().setCookie(conn.getURL().toString(), cookie.getName() + "=" + cookie.getValue());


//                        cookieManager.getCookieStore().add(null, cookie);

                    }

                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                android.webkit.CookieManager.getInstance().flush();
            } else {
                CookieSyncManager.getInstance().sync();
            }

        }
    }

    public void disable_requested_with(boolean isDisable) {
//        isDisableRequestedWith = isDisable;
    }

    protected static boolean matchesInjectionUrl(final String url) {
        final Uri uri = Uri.parse(url.toLowerCase(Locale.US));
        return MRAID_JS.equals(uri.getLastPathSegment()) || MRAID2_JS.equals(uri.getLastPathSegment());
    }

    private WebResourceResponse useWebResourceCache(String url){
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            String lastPathSegment = uri.getLastPathSegment();
            if (host == null || matchesInjectionUrl(url)  || !uri.getScheme().startsWith("http") || host.equals("127.0.0.1") || !TextUtils.isEmpty(uri.getQueryParameter("no-cache")) || !(lastPathSegment.endsWith(".js") || lastPathSegment.endsWith(".css"))) {
                return null;
            }
            String contentType = null;

            File file = new File(SigmobFileUtil.getWebCachePath(),lastPathSegment);

            if (lastPathSegment.endsWith(".js")) {
                contentType = "text/javascript";
            } else if (lastPathSegment.endsWith(".css")) {
                contentType = "text/css";
            }
            if (file.exists()) {
                InputStream inputStream = new FileInputStream(file);
                WebResourceResponse webResourceResponse = new WebResourceResponse(contentType, "UTF-8", inputStream);

                HashMap<String, String> headers = new HashMap<>();
                headers.put("Cache-Control","no-store");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webResourceResponse.setResponseHeaders(headers);
                }
                return webResourceResponse;
            }
        } catch (Throwable e) {
            SigmobLog.e("useWebResourceCache ",e);
        }

        return null;
    }
    
    @Override
    @SuppressLint("NewApi")
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

        WebResourceResponse webResourceResponse = null;
        try {
            webResourceResponse = injectionSigmobHtmlResponse(request.getUrl());
            if (webResourceResponse != null){
                return webResourceResponse;
            }
        }catch (Throwable th){

        }

        if (useCache){
            webResourceResponse = useWebResourceCache(request.getUrl().toString());
            if (webResourceResponse != null){
                return webResourceResponse;
            }
        }


        if ((isDisableRequestedWith || (useCache && !InjectionWebViewResourceHelper.filterUrl(request.getUrl().toString()))) && !matchesInjectionUrl(request.getUrl().toString())) {
            webResourceResponse = InjectionWebViewResourceHelper.injectionResponse(request.getUrl().toString(), request.getMethod(), request.getRequestHeaders());
        }

        if (webResourceResponse == null){
            webResourceResponse = super.shouldInterceptRequest(view, request);
        }

        if (useCache) {
            if (webResourceResponse != null) {
                File file = saveWebResourceCache(webResourceResponse, request.getUrl().toString());
                if (file != null && file.exists()) {
                    try {
                        return new WebResourceResponse(webResourceResponse.getMimeType(), webResourceResponse.getEncoding(), new FileInputStream(file));
                    } catch (FileNotFoundException e) {

                    }
                }
            }
        }
        return webResourceResponse;

    }

    private static File saveWebResourceCache(WebResourceResponse webResourceResponse,String url){
        try {
            Uri uri = Uri.parse(url);
            String lastPathSegment = uri.getLastPathSegment();
            if(InjectionWebViewResourceHelper.filterUrl(url)){
                return null;
            }

            File file = new File(SigmobFileUtil.getWebCachePath(),lastPathSegment);
            FileUtil.writeToCache(webResourceResponse.getData(), file.getAbsolutePath());
            return file;
        }catch (Throwable th){

        }
        return null;
    }

    public static WebResourceResponse injectionSigmobHtmlResponse(Uri url){


        if (url.getScheme().startsWith(WindConstants.SIGMOBHTML)){
            try {
                WebResourceResponse webResourceResponse =  new WebResourceResponse("text/html", "utf-8", new FileInputStream(url.getPath()));
                return webResourceResponse;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

        WebResourceResponse webResourceResponse = null;
        try {
            webResourceResponse = injectionSigmobHtmlResponse(Uri.parse(url));
            if (webResourceResponse != null){
                return webResourceResponse;
            }
        }catch (Throwable th){

        }
        if (useCache){
            webResourceResponse = useWebResourceCache(url);
            if (webResourceResponse != null){
                return webResourceResponse;
            }
        }



        if ((isDisableRequestedWith || (useCache && !InjectionWebViewResourceHelper.filterUrl(url))) && !matchesInjectionUrl(url)) {
             webResourceResponse = InjectionWebViewResourceHelper.injectionResponse(url, "GET", null);
        }


        if (webResourceResponse == null){
            webResourceResponse = super.shouldInterceptRequest(view, url);
        }

        if (useCache){
            if (webResourceResponse != null){
                File file = saveWebResourceCache(webResourceResponse,url);
                if (file != null && file.exists()){
                    try {
                        return new WebResourceResponse(webResourceResponse.getMimeType(),webResourceResponse.getEncoding(),new FileInputStream(file));
                    } catch (FileNotFoundException e) {

                    }
                }
            }
        }

        return webResourceResponse;
    }

    public void setReferer(String url) {
        try {
            Uri uri = Uri.parse(url);
            mReferer = uri.getScheme() + "://" + uri.getHost() + "/" + uri.getPath();
        } catch (Throwable th) {

        }

    }
}
