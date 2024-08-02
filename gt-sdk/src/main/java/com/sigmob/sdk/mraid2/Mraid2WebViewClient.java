// Copyright 2018-2019 Sigmob, Inc.
// Licensed under the Sigmob SDK License Agreement
// http://www.sigmob.com/legal/sdk-license-agreement/

package com.sigmob.sdk.mraid2;

import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.sigmob.sdk.base.SigmobWebViewClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Handles injecting the MRAID javascript when encountering mraid.js urls
 */
public class Mraid2WebViewClient extends SigmobWebViewClient {

    private static final String MRAID_JS = "mraid.js";
    private static final String MRAID2_JS = "mraid2.js";

    private static final String MRAID_INJECTION_JAVASCRIPT = "javascript:"
            + Mraid2Javascript.JAVASCRIPT_SOURCE;

    @SuppressWarnings("deprecation") // new method will simply call this one
    @Override
    public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {
        if (matchesInjectionUrl(url)) {
            return createMraidInjectionResponse();
        } else {
            return super.shouldInterceptRequest(view, url);
        }
    }


    private WebResourceResponse createMraidInjectionResponse() {
        InputStream data = new ByteArrayInputStream(MRAID_INJECTION_JAVASCRIPT.getBytes());
        return new WebResourceResponse("text/javascript", "UTF-8", data);
    }
}
