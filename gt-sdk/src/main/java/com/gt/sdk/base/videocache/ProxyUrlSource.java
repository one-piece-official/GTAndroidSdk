package com.gt.sdk.base.videocache;


import com.gt.sdk.base.videocache.headers.HeaderInjector;
import com.gt.sdk.base.videocache.sourcestorage.SourceInfoStorage;

public class ProxyUrlSource {

    public static Source getUrlSource(String url) {
        Source urlSource = null;
        if (urlSource == null) {
            urlSource = new HttpUrlSource(url);
        }

        return urlSource;
    }

    public static Source getUrlSource(String url, SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        Source urlSource = null;
        if (urlSource == null) {
            urlSource = new HttpUrlSource(url, sourceInfoStorage, headerInjector);
        }

        return urlSource;

    }

    public static Source getUrlSource(Source source) {
        Source urlSource = null;
        if (urlSource == null) {
            urlSource = new HttpUrlSource(source);
        }

        return urlSource;
    }
}
