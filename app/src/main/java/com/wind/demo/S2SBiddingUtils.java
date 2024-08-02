package com.wind.demo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sigmob.windad.WindAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Server bidding 模拟请求服务端进行比价的工具类
 */
public class S2SBiddingUtils {
    public static final ExecutorService SINGLE_THREAD_EXECUTOR =
            Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "BIDDING_THREAD");
                }
            });

    private static final String TEST_SERVER_BIDDING_URL = "https://adstage.sigmob.cn/hb/v2/bid";
    private static final String SERVER_BIDDING_URL = "https://adservice.sigmob.cn/hb/v2/bid";

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    private static int impNum = 1;
    public static boolean isTest;
    public static boolean isNormal;

    private static String getBidRequestData(String appid, String placementId, String sdkToken, int adcount) {

        JSONObject bidRequest = new JSONObject();
        try {
            bidRequest.put("id", UUID.randomUUID().toString());
            JSONArray implist = new JSONArray();

            JSONObject imp = new JSONObject();
            imp.put("id", String.valueOf(impNum++));
            imp.put("displaymanager", "sigmob");
            imp.put("placementid", placementId);
            imp.put("adcount", adcount);

            implist.put(imp);

            bidRequest.put("imp", implist);
            JSONObject app = new JSONObject();
            app.put("id", appid);
            bidRequest.put("app", app);
            bidRequest.put("ip", "120.133.42.133");
            bidRequest.put("tmax", 1000);
            bidRequest.put("test", isTest ? 1 : 0);
            bidRequest.put("sdktoken", sdkToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bidRequest.toString();

    }

    public static void sendNoticeUrl(final Context context, final String url) {
        SINGLE_THREAD_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection =
                            (HttpURLConnection) new URL(url).openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if (HttpURLConnection.HTTP_OK == responseCode) {
                        showToast(context, "发送NoticeUrl成功");

                    } else {
                        showToast(context, "发送NoticeUrl失败 " + responseCode);
                    }
                } catch (Throwable th) {
                    showToast(context, "发送NoticeUrl失败： " + th.getMessage());

                }
            }
        });
    }

    public static void requestBiddingToken(final Context context, final String appid, final String placementId, final int adcount, final RequestTokenCallBack callBack) {
        Map<String, Object> map = new HashMap<>();
        final String sdkToken = WindAds.sharedAds().getSDKToken();
        Log.d("WindSDK", "sdkToken: " + sdkToken);
        SINGLE_THREAD_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    String url = SERVER_BIDDING_URL;
                    if (!isNormal) {
                        url = TEST_SERVER_BIDDING_URL;
                    }

                    HttpURLConnection connection =
                            (HttpURLConnection) new URL(url).openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestMethod("POST");
                    String bidRequestData = getBidRequestData(appid, placementId, sdkToken, adcount);

                    Log.d("WindSDK", "bid request data: " + bidRequestData);
                    byte[] postDataBytes = bidRequestData.getBytes(Charset.forName("UTF-8"));
                    if (postDataBytes != null && postDataBytes.length > 0) {
                        OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                        out.write(postDataBytes);
                        out.flush();
                        out.close();
                    }
                    int responseCode = connection.getResponseCode();
                    if (HttpURLConnection.HTTP_OK == responseCode) {
                        String response = getStringContent(connection);
                        JSONObject jsonObject = new JSONObject(response);
                        final String token = jsonObject.optString("bidid");

                        if (TextUtils.isEmpty(token)) {
                            showToast(context, "回包中无 token" + response);
                        } else {
                            showToast(context, "请求 token 成功");
                            sHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (callBack != null) {
                                        Log.d("WindSDK", "requestBiddingToken: " + callBack);
                                        callBack.onSuccess(token);
                                    }
                                }
                            });

                        }
                    } else {
                        showToast(context, "请求 token 失败： " + responseCode);
                        Log.e("WindSDK",
                                "requestBiddingToken: responseCode: " + responseCode + ", msg:" + connection.getResponseMessage());
                    }
                } catch (IOException e) {
                    showToast(context, "请求 token 失败： " + e.getMessage());
                    e.printStackTrace();
                } catch (JSONException e) {
                    showToast(context, "请求 token 失败： " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public static byte[] getBytesContent(HttpURLConnection connection) throws IllegalStateException
            , IOException {
        InputStream in = connection.getInputStream();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = (in.read(buffer))) > 0) {
                bo.write(buffer, 0, len);
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignore) {
            }
        }
        return bo.toByteArray();
    }

    public static String getStringContent(HttpURLConnection connection) throws IOException {
        byte[] bytes = getBytesContent(connection);
        if (bytes == null) {
            return null;
        } else if (bytes.length == 0) {
            return "";
        }

        String charset = null;
        try {
            charset = connection.getContentEncoding();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (charset == null) {
            charset = "UTF-8";
        }
        return new String(bytes, charset);
    }

    private static void showToast(final Context context, final String msg) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface RequestTokenCallBack {
        void onSuccess(String token);
    }

}
