package com.sigmob.sdk.base.views;

import android.app.Activity;
import android.content.Context;
import android.content.MutableContextWrapper;

import com.czhj.sdk.logger.SigmobLog;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class WebViewPools {
    private Queue<BaseWebView> mWebViews;
    private Object lock = new Object();
    private static WebViewPools mWebViewPools = null;
    private static final AtomicReference<WebViewPools> mAtomicReference = new AtomicReference<>();

    private static int poolSize = 0;

    private WebViewPools() {
        if (poolSize > 0) {
            mWebViews = new LinkedBlockingQueue<>(poolSize);
        }
    }

    public static WebViewPools getInstance() {
        for (; ; ) {
            if (mWebViewPools != null)
                return mWebViewPools;
            if (mAtomicReference.compareAndSet(null, new WebViewPools()))
                return mWebViewPools = mAtomicReference.get();
        }
    }

    public void recycle(BaseWebView webView) {
        recycleInternal(webView);
    }

    public BaseWebView acquireWebView(Context context) {
        return acquireWebViewInternal(context);
    }

    private BaseWebView acquireWebViewInternal(Context activity) {

        BaseWebView mWebView = null;
        if (mWebViews != null) {
            mWebView = mWebViews.poll();
        }
        SigmobLog.i("acquireWebViewInternal  webview:" + mWebView);
        if (mWebView == null) {
            synchronized (lock) {
                return new BaseWebView(new MutableContextWrapper(activity));
            }
        } else {
            MutableContextWrapper mMutableContextWrapper = (MutableContextWrapper) mWebView.getContext();
            mMutableContextWrapper.setBaseContext(activity);
            return mWebView;
        }
    }

    private void recycleInternal(BaseWebView webView) {
        try {

            if (webView.getContext() instanceof MutableContextWrapper) {
                MutableContextWrapper mContext = (MutableContextWrapper) webView.getContext();
                mContext.setBaseContext(mContext.getApplicationContext());
                SigmobLog.i("enqueue  webview:" + webView);

                if (poolSize > 0 && mWebViews != null) {
                    webView.reset();
                    mWebViews.offer(webView);
                } else {
                    webView.destroy();
                }
            }
            if (webView.getContext() instanceof Activity) {
                //throw new RuntimeException("leaked");
                SigmobLog.i("Abandon this webview  ， It will cause leak if enqueue !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
