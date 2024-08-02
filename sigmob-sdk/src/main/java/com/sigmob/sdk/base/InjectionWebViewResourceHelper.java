package com.sigmob.sdk.base;

import static com.sigmob.sdk.base.SigmobWebViewClient.matchesInjectionUrl;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.webkit.WebResourceResponse;

import java.util.Map;

public final class InjectionWebViewResourceHelper {

   static InjectionWebViewResourceResponse helper;
   static {
       try {
           if (WindSDKConfig.getInstance().isEnableOkHttp3()){
               helper = new InjectionWebViewResourceResponseOkhttp();
           }

       }catch (Throwable t){

       }
       if (helper == null){
           helper = new InjectionWebViewResourceResponse();
       }
    }

    public static boolean filterUrl(String url){
       try {
           Uri uri = Uri.parse(url);
           String host = uri.getHost();
           String lastPathSegment = uri.getLastPathSegment();
           if (host == null || matchesInjectionUrl(url) || !uri.getScheme().startsWith("http")  || host.equals("localhost")  || host.equals("127.0.0.1") || !TextUtils.isEmpty(uri.getQueryParameter("no-cache")) || !((lastPathSegment.endsWith(".js") || lastPathSegment.endsWith(".css")))) {
               return true;
           }
       }catch (Throwable th){

       }
       return false;
    }
    public static WebResourceResponse injectionSigmobHtmlResponse(Uri url){


        return null;
    }
    static WebResourceResponse injectionResponse(String url, String method, Map<String, String> map) {
        if (!URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url) || method.equals("POST")) {
            return null;
        }
        try {

            if (helper != null) {
                WebResourceResponse resourceResponse = helper.injectionResponse(url, method, map);
                return resourceResponse;
            }
        }catch (Throwable th){

        }


        return null;
    }
}