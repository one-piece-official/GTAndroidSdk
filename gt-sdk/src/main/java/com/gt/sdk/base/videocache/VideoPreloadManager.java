package com.gt.sdk.base.videocache;

import android.content.Context;
import android.text.TextUtils;

import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.toolbox.StringUtil;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoPreloadManager {
    private static VideoPreloadManager mInstance;

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(8);

    private Map<String, WeakReference<VideoPreLoadRunnable>> mRunnableMap = new HashMap<String, WeakReference<VideoPreLoadRunnable>>();

    private Context mContext;

    public static synchronized VideoPreloadManager getInstance(Context context) {
        if (mInstance == null && context != null) {
            Context applicationContext = context.getApplicationContext();
            mInstance = new VideoPreloadManager(applicationContext);
        }
        return mInstance;
    }


    private VideoPreloadManager(Context context) {
        mContext = context;
    }

    /**
     * 预加载url对应的视频文件
     *
     * @param url
     */
    public void preloadVideo(String url) {
        if (!TextUtils.isEmpty(url)) {
            url = StringUtil.getUrl(url);
            if (url.startsWith("http")) {
                SigmobLog.d(String.format("preloadVideo,网络文件,开始下载:ulr=%s", url));
                VideoPreLoadRunnable runnable = new VideoPreLoadRunnable(url);
                mRunnableMap.put(url, new WeakReference<VideoPreLoadRunnable>(runnable));
                mExecutorService.execute(runnable);
            } else {
                SigmobLog.d(String.format("preloadVideo,本地文件:ulr=%s", url));
            }
        }
    }

    /**
     * 取消预加载url对应的视频文件
     *
     * @param url
     */
    public void cancelPreLoadVideo(String url) {
        SigmobLog.d(String.format("cancelPreLoadVideo,ulr=%s", url));
        if (mRunnableMap != null && !TextUtils.isEmpty(url)) {
            WeakReference<VideoPreLoadRunnable> runnableWeakReference = mRunnableMap.get(url);
            VideoPreLoadRunnable runnable = null;
            if (runnableWeakReference != null && (runnable = runnableWeakReference.get()) != null) {
                SigmobLog.d(String.format("cancelPreLoadVideo,取消网络请求,ulr=%s", url));
                runnable.setCanceled(true);
            }
        }

    }

    private static class VideoPreLoadRunnable implements Runnable {

        private String mUrl;
        private AtomicBoolean isCanceled = new AtomicBoolean(false);

        public VideoPreLoadRunnable(String url) {
            this.mUrl = url;
        }

        public void setCanceled(boolean cancel) {
            isCanceled.set(cancel);
        }

        @Override
        public void run() {
            if (isCanceled.get()) {//如果已经取消，则直接返回
                SigmobLog.i(String.format("cancelPreLoadVideo,取消网络请求,成功(未下载数据),url=%s", mUrl));
                return;
            }
            doRequestWithoutReturn(mUrl);
        }

        private void doRequestWithoutReturn(String getURL) {

            InputStream inStream = null;
            HttpURLConnection connection = null;
            try {

                URL url = new URL(getURL);

                connection = (HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(5000);

                connection.setReadTimeout(5000);

                connection.connect();

                byte[] buf = new byte[1024];

                inStream = connection.getInputStream();

                for (int n; (n = inStream.read(buf)) != -1; ) {

                    if (isCanceled.get()) {
                        SigmobLog.d(String.format("cancelPreLoadVideo,取消网络请求,成功(正在下载数据),url=%s", mUrl));
                        break;
                    }
                }
                SigmobLog.i(String.format("preloadVideo,网络文件,成功,url=%s", mUrl));
            } catch (Exception e) {
                SigmobLog.e(String.format("preloadVideo,网络文件,失败,msg=%s,url=%s", e.getMessage(), mUrl));
            } finally {
                try {
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
