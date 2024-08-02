package com.sigmob.sdk.base;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceResponse;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjectionWebViewResourceResponse {

    static final String COOKIES_HEADER = "Set-Cookie";

    public static final String COOKIE_REQUEST_HEADER = "Cookie";
    private static final String TAG = InjectionWebViewResourceResponse.class.getSimpleName();
    private static CookieManager msCookieManager = new CookieManager();

    public static Map<String,String> multimapToSingle(Map<String, List<String>> maps){

        StringBuilder sb = new StringBuilder();
        Map<String,String> map = new HashMap<>();
        for (Map.Entry<String, List<String>> entry: maps.entrySet()) {
            List<String> values = entry.getValue();
            sb.delete(0,sb.length());
            if (values!=null&&values.size()>0){
                for (String v:values) {
                    sb.append(v);
                    sb.append(";");
                }
            }
            if (sb.length()>0){
                sb.deleteCharAt(sb.length()-1);
            }
            map.put(entry.getKey(),sb.toString());
        }
        return map;
    }


    public  WebResourceResponse injectionResponse(String str, String method, Map<String, String> map) {
        InputStream inputStream;

        try {

            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            httpURLConnection.setRequestMethod(method);
            if (map != null && map.size() > 0) {
                for (Map.Entry next : map.entrySet()) {
                    httpURLConnection.setRequestProperty((String) next.getKey(), (String) next.getValue());
                }
            }

            String cookies = android.webkit.CookieManager.getInstance().getCookie(str);


            if (!TextUtils.isEmpty(cookies)) {
                String[] split = cookies.split(";");
                for (String cookie : split) {
                    httpURLConnection.addRequestProperty(COOKIE_REQUEST_HEADER.toLowerCase(), cookie.replace(" ", ""));
                }
            }
            httpURLConnection.connect();
            String contentType = httpURLConnection.getContentType();
            String contentEncoding = httpURLConnection.getContentEncoding();
            InputStream inputStream2 = httpURLConnection.getInputStream();

            loadResponseCookies(httpURLConnection, msCookieManager);
            if (contentType.contains("text/html")) {
                contentType = "text/html";
                inputStream = byteArrayOutputStream(inputStream2, contentEncoding);
            } else {
                inputStream = inputStream2;
            }
            WebResourceResponse webResourceResponse = new WebResourceResponse(contentType, contentEncoding, inputStream);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();

                webResourceResponse.setResponseHeaders(multimapToSingle(headerFields));

            }
            return webResourceResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static InputStream byteArrayOutputStream(InputStream inputStream, String str) throws IOException {
        byte[] bArr = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int read = inputStream.read(bArr);
            if (read == -1) {
                break;
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
        String str2 = new String(byteArrayOutputStream.toByteArray());
        if (str == null) {
            str = Charset.defaultCharset().displayName();
        }
        return new ByteArrayInputStream(str2.getBytes(str));
    }

    public static void loadResponseCookies( HttpURLConnection conn,  CookieManager cookieManager) {

        //do nothing in case a null cokkie manager object is passed
        if (cookieManager == null || conn == null) {
            return;

        }

        Map<String, List<String>> headerFields = conn.getHeaderFields();
        Log.d(TAG,"headerFields : " + headerFields.toString());
        List<String> cookiesHeader = headerFields.get("Set-Cookie");
        if (cookiesHeader != null) {
            for (String cookieHeader : cookiesHeader) {
                List<HttpCookie> cookies;
                try {
                    cookies = HttpCookie.parse(cookieHeader);
                } catch (NullPointerException e) {
                    Log.e(TAG,MessageFormat.format("{0} -- Null header for the cookie : {1}", conn.getURL().toString(), cookieHeader.toString()));
                    //ignore the Null cookie header and proceed to the next cookie header
                    continue;
                }

                if (cookies != null) {
                    Log.d(TAG,"{0} -- Reading Cookies from the response :" + conn.getURL().toString());
                    Log.d(TAG,"{0} -- Reading Cookies from the response :" + cookies.get(0));

                    for (HttpCookie cookie : cookies) {
                        Log.d(TAG,cookie.toString());
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

}
