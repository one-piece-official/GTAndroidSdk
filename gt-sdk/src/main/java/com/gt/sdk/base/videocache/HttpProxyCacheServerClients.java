package com.gt.sdk.base.videocache;


import static com.gt.sdk.base.videocache.Preconditions.checkNotNull;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.base.videocache.file.FileCache;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client for {@link HttpProxyCacheServer}
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
final class HttpProxyCacheServerClients {

    private static final int cacheAvailableEvent = 1;
    private static final int CacheUnAvailableEvent = 2;

    private final AtomicInteger clientsCount = new AtomicInteger(0);
    private final String url;
    private volatile HttpProxyCache proxyCache;
    private final List<CacheListener> listeners = new CopyOnWriteArrayList<>();
    private final CacheListener uiCacheListener;
    private final Config config;

    public HttpProxyCacheServerClients(String url, Config config) {
        this.url = checkNotNull(url);
        this.config = checkNotNull(config);
        this.uiCacheListener = new UiListenerHandler(url, listeners);
    }

    public void processRequest(GetRequest request, Socket socket) throws ProxyCacheException, IOException {
        startProcessRequest();
        try {
            clientsCount.incrementAndGet();
            proxyCache.processRequest(request, socket);
        } catch (Throwable th) {
            SigmobLog.e("processRequest error:", th);
        } finally {
            finishProcessRequest();
        }
    }

    private synchronized void startProcessRequest() throws ProxyCacheException {
        proxyCache = proxyCache == null ? newHttpProxyCache() : proxyCache;
    }

    private synchronized void finishProcessRequest() {
        if (clientsCount.decrementAndGet() <= 0) {
            proxyCache.shutdown();
            proxyCache = null;
        }
    }

    public void registerCacheListener(CacheListener cacheListener) {
        listeners.add(cacheListener);
    }

    public void unregisterCacheListener(CacheListener cacheListener) {
        listeners.remove(cacheListener);
    }

    public void shutdown() {
        listeners.clear();
        if (proxyCache != null) {
            proxyCache.registerCacheListener(null);
            proxyCache.shutdown();
            proxyCache = null;
        }
        clientsCount.set(0);
    }

    public int getClientsCount() {
        return clientsCount.get();
    }

    private HttpProxyCache newHttpProxyCache() throws ProxyCacheException {
        Source source = ProxyUrlSource.getUrlSource(url, config.sourceInfoStorage, config.headerInjector);
        FileCache cache = new FileCache(config.generateCacheFile(url), config.diskUsage);
        HttpProxyCache httpProxyCache = new HttpProxyCache(source, cache);
        httpProxyCache.registerCacheListener(uiCacheListener);
        return httpProxyCache;
    }

    private static final class UiListenerHandler extends Handler implements CacheListener {

        private final String url;
        private final List<CacheListener> listeners;

        public UiListenerHandler(String url, List<CacheListener> listeners) {
            super(Looper.getMainLooper());
            this.url = url;
            this.listeners = listeners;
        }

        @Override
        public void onCacheAvailable(File file, String url, int percentsAvailable) {
            Message message = obtainMessage();
            message.arg1 = percentsAvailable;
            message.obj = file;
            message.what = cacheAvailableEvent;
            sendMessage(message);
        }

        @Override
        public void onCacheUnavailable(String url, Throwable throwable) {
            Message message = obtainMessage();
            message.obj = throwable;
            message.what = CacheUnAvailableEvent;
            sendMessage(message);
        }

        @Override
        public void handleMessage(Message msg) {
            for (CacheListener cacheListener : listeners) {
                switch (msg.what) {
                    case cacheAvailableEvent: {
                        cacheListener.onCacheAvailable((File) msg.obj, url, msg.arg1);
                    }
                    break;
                    case CacheUnAvailableEvent: {
                        cacheListener.onCacheUnavailable(url, (Throwable) msg.obj);
                    }
                    break;
                }
            }
        }
    }
}
