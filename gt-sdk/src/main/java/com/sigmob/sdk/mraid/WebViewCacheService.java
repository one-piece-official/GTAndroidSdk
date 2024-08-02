package com.sigmob.sdk.mraid;

import android.annotation.SuppressLint;
import android.os.Handler;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.ExternalViewabilitySessionManager;
import com.sigmob.sdk.base.common.Interstitial;
import com.sigmob.sdk.base.views.BaseWebView;
import com.czhj.sdk.common.utils.Preconditions;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Holds WebViews in memory until they are used.
 */
public class WebViewCacheService {
    public static class Config {

        private final BaseWebView mWebView;

        private final WeakReference<Interstitial> mWeakInterstitial;

        private final ExternalViewabilitySessionManager mViewabilityManager;

        private final MraidController mController;

        Config(final BaseWebView baseWebView,
               final Interstitial baseInterstitial,
               final ExternalViewabilitySessionManager viewabilityManager,
               final MraidController controller) {
            mWebView = baseWebView;
            mWeakInterstitial = new WeakReference<>(baseInterstitial);
            mViewabilityManager = viewabilityManager;
            mController = controller;
        }


        public BaseWebView getWebView() {
            return mWebView;
        }


        public WeakReference<Interstitial> getWeakInterstitial() {
            return mWeakInterstitial;
        }


        public ExternalViewabilitySessionManager getViewabilityManager() {
            return mViewabilityManager;
        }


        public MraidController getController() {
            return mController;
        }
    }

    /**
     * Maximum number of {@link BaseWebView}s that are cached. This limit is intended to be very
     * conservative; it is not recommended to cache more than a few BaseWebViews.
     */

    static final int MAX_SIZE = 50;


    private static final long FIFTEEN_MINUTES_MILLIS = 50 * 60 * 1000;

    static final long TRIM_CACHE_FREQUENCY_MILLIS = FIFTEEN_MINUTES_MILLIS;

    @SuppressLint("UseSparseArrays")

    private static final Map<String, Config> sWebViewConfigs = Collections.synchronizedMap(new HashMap<String, Config>());


    static final TrimCacheRunnable sTrimCacheRunnable = new TrimCacheRunnable();

    private static Handler sHandler = new Handler();

    private WebViewCacheService() {
    }

    /**
     * Stores the {@link BaseWebView} in the cache. This WebView will live until it is retrieved via
     * {@link } or when the base interstitial object is removed from memory.
     *
     * @param broadcastIdentifier The unique identifier associated with both the interstitial and the WebView
     * @param baseInterstitial    The interstitial managing this WebView
     * @param baseWebView         The BaseWebView to be stored
     * @param viewabilityManager  The associated viewability manager, which needs to be created
     *                            during Interstitial load and reutilized on show
     */

    public static void storeWebViewConfig(final String broadcastIdentifier,
                                          final MraidInterstitial baseInterstitial,
                                          final BaseWebView baseWebView,
                                          final ExternalViewabilitySessionManager viewabilityManager,
                                          final MraidController controller) {
        Preconditions.checkNotNull(broadcastIdentifier);
        Preconditions.checkNotNull(baseInterstitial);
        Preconditions.checkNotNull(baseWebView);

        trimCache();
        // Ignore request when max size is reached.
        if (sWebViewConfigs.size() >= MAX_SIZE) {
            SigmobLog.w(
                    "Unable to cache web view. Please destroy some via #destroy() and try again.");
            return;
        }

        sWebViewConfigs.put(broadcastIdentifier,
                new Config(baseWebView, baseInterstitial, viewabilityManager, controller));
    }


    public static Config popWebViewConfig(final String broadcastIdentifier) {
        Preconditions.checkNotNull(broadcastIdentifier);

        return sWebViewConfigs.remove(broadcastIdentifier);
    }


    static synchronized void trimCache() {
        final Iterator<Map.Entry<String, Config>> iterator = sWebViewConfigs.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, Config> entry = iterator.next();

            // If the Interstitial was removed from memory, end viewability manager tracking and
            // discard the entire associated Config.
            if (entry.getValue().getWeakInterstitial().get() == null) {
                entry.getValue().getViewabilityManager().endDisplaySession();
                iterator.remove();
            }
        }

        if (!sWebViewConfigs.isEmpty()) {
            sHandler.removeCallbacks(sTrimCacheRunnable);
            sHandler.postDelayed(sTrimCacheRunnable, TRIM_CACHE_FREQUENCY_MILLIS);
        }
    }

    private static class TrimCacheRunnable implements Runnable {
        @Override
        public void run() {
            trimCache();
        }
    }

    @Deprecated
    public static void clearAll() {
        sWebViewConfigs.clear();
        sHandler.removeCallbacks(sTrimCacheRunnable);
    }

    @Deprecated
    static Map<String, Config> getWebViewConfigs() {
        return sWebViewConfigs;
    }

    @Deprecated
    static void setHandler(final Handler handler) {
        sHandler = handler;
    }
}
