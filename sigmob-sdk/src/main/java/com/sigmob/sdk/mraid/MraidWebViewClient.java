// Copyright 2018-2019 Sigmob, Inc.
// Licensed under the Sigmob SDK License Agreement
// http://www.sigmob.com/legal/sdk-license-agreement/

package com.sigmob.sdk.mraid;

import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.czhj.volley.toolbox.StringUtil;
import com.sigmob.sdk.base.SigmobWebViewClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Handles injecting the MRAID javascript when encountering mraid.js urls
 */
public class MraidWebViewClient extends SigmobWebViewClient {

    private static final String MRAID_INJECTION_JAVASCRIPT_ = "javascript:"
            + MraidJavascript.JAVASCRIPT_SOURCE;
    private static  String MRAID_INJECTION_JAVASCRIPT = null;

    @SuppressWarnings("deprecation") // new method will simply call this one
    @Override
    public WebResourceResponse shouldInterceptRequest( final WebView view,
             final String url) {
        if (matchesInjectionUrl(url)) {

            if(MRAID_INJECTION_JAVASCRIPT == null){
                MRAID_INJECTION_JAVASCRIPT = MRAID_INJECTION_JAVASCRIPT_.replaceAll("xxx", StringUtil.decode(StringUtil.s));
            }
            return createMraidInjectionResponse(MRAID_INJECTION_JAVASCRIPT);
        } else {
            return super.shouldInterceptRequest(view, url);
        }
    }

    private WebResourceResponse createMraidInjectionResponse(String str) {

        InputStream data = new ByteArrayInputStream(str.getBytes());

        return new WebResourceResponse("text/javascript", "UTF-8", data);
    }
}
