package com.sigmob.sdk.base;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.webkit.WebResourceResponse;

import com.czhj.volley.DefaultRetryPolicy;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public final class InjectionWebViewResourceResponseOkhttp extends InjectionWebViewResourceResponse {


    private OkHttpClient.Builder clientBuilder;
    private  OkHttpClient client;

    public InjectionWebViewResourceResponseOkhttp() {
        try {
            clientBuilder = new OkHttpClient.Builder();
            clientBuilder.connectionPool(new ConnectionPool());
            clientBuilder.connectTimeout(DefaultRetryPolicy.DEFAULT_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            clientBuilder.readTimeout(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            clientBuilder.writeTimeout(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            client = clientBuilder.build();

        } catch (Throwable th) {

        }
    }

    public WebResourceResponse injectionResponse(String url, String method, Map<String, String> map) {
        if (!URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url) || method.equals("POST")) {
            return null;
        }
        try {

            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host == null || host.equals("127.0.0.1") || host.equals("localhost")) {
                return null;
            }

            Request.Builder builder = new Request.Builder()
                    .url(url);

            if (map != null && map.size() > 0) {
                for (Map.Entry next : map.entrySet()) {
                    builder.addHeader((String) next.getKey(), (String) next.getValue());
                }
            }
            String cookies = android.webkit.CookieManager.getInstance().getCookie(url);
            if (!TextUtils.isEmpty(cookies)) {
                String[] split = cookies.split(";");
                for (String cookie : split) {
                    builder.addHeader(COOKIE_REQUEST_HEADER.toLowerCase(), cookie.replace(" ", ""));
                }
            }
            Request okRequest = builder.build();
            okhttp3.Response response = client.newCall(okRequest).execute();


            return new WebResourceResponse(
                    response.body().contentType().type() + "/" + response.body().contentType().subtype(),
                    response.header("content-encoding", "utf-8"),
                    response.body().byteStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.injectionResponse(url,method,map);

    }

}