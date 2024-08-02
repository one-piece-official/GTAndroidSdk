package com.sigmob.sdk.videocache;

import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.videocache.headers.HeaderInjector;
import com.sigmob.sdk.videocache.sourcestorage.SourceInfoStorage;

public class ProxyUrlSource {


    public static Source getUrlSource(String url) {
        Source urlSource = null;

        boolean enableOkHttp3 = WindSDKConfig.getInstance().isEnableOkHttp3();
        if (enableOkHttp3) {
            try {
                urlSource = new OkHttp3UrlSource(url);
            } catch (Throwable t) {

            }
        }

        if (urlSource == null) {
            urlSource = new HttpUrlSource(url);
        }

        return urlSource;
    }

    public static Source getUrlSource(String url, SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        Source urlSource = null;
        boolean enableOkHttp3 = WindSDKConfig.getInstance().isEnableOkHttp3();
        if (enableOkHttp3) {
            try {
                urlSource = new OkHttp3UrlSource(url, sourceInfoStorage, headerInjector);
            } catch (Throwable t) {

            }
        }
        if (urlSource == null) {
            urlSource = new HttpUrlSource(url, sourceInfoStorage, headerInjector);
        }

        return urlSource;

    }

    public static Source getUrlSource(Source source) {
        Source urlSource = null;
        boolean enableOkHttp3 = WindSDKConfig.getInstance().isEnableOkHttp3();
        if (enableOkHttp3) {
            try {
                urlSource = new OkHttp3UrlSource(source);
            } catch (Throwable t) {

            }
        }
        if (urlSource == null) {
            urlSource = new HttpUrlSource(source);
        }

        return urlSource;
    }
}
