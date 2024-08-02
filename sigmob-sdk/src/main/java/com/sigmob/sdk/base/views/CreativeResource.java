package com.sigmob.sdk.base.views;

import android.text.TextUtils;

import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.logger.SigmobLog;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class CreativeResource implements Serializable {
    private static final long serialVersionUID = 0L;

    private static final List<String> VALID_IMAGE_TYPES =
            Arrays.asList("image/jpeg", "image/png", "image/bmp", "image/gif");
    private static final List<String> VALID_APPLICATION_TYPES =
            Arrays.asList("application/x-javascript");

    /**
     * The type of resource ordered according to priority.
     */
    public enum Type {
        STATIC_RESOURCE,
        HTML_RESOURCE,
        IFRAME_RESOURCE,
        NATIVE_RESOURCE,
        URL_RESOURCE
    }

    /**
     * The type of the static resource. Only static resources only will have values other than NONE.
     */
    public enum CreativeType {
        NONE,
        IMAGE,
        JAVASCRIPT
    }

    private final String mResource;
    private final Type mType;
    private final CreativeType mCreativeType;
    private final int mWidth;
    private final int mHeight;


    /**
     * Private constructor. Use fromCreativeResourceXmlManager() to create a CreativeResource.
     */
    public CreativeResource(final String resource, final Type type,
                            final CreativeType creativeType, final int width, final int height) {
        Preconditions.NoThrow.checkNotNull(resource);
        Preconditions.NoThrow.checkNotNull(type);
        Preconditions.NoThrow.checkNotNull(creativeType);

        mResource = resource;
        mType = type;
        mCreativeType = creativeType;
        mWidth = width;
        mHeight = height;
    }


    public String getResource() {
        return mResource;
    }


    public Type getType() {
        return mType;
    }


    public CreativeType getCreativeType() {
        return mCreativeType;
    }

    /**
     * Initializes a WebView used to display the resource.
     *
     * @param webView the resource's WebView.
     */
    public void initializeWebView(CreativeWebView webView) {
        Preconditions.NoThrow.checkNotNull(webView);

        switch (mType) {
            case IFRAME_RESOURCE:
                webView.loadData("<iframe frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" " +
                        "marginwidth=\"0\" style=\"border: 0px; margin: 0px;\" width=\"" + mWidth +
                        "\" height=\"" + mHeight + "\" src=\"" + mResource + "\"></iframe>");
                break;
            case HTML_RESOURCE:
                webView.loadData(mResource);
                break;
            case STATIC_RESOURCE:
                if (mCreativeType == CreativeType.IMAGE) {
                    String data = "<html>" +
                            "<head>" +
                            "</head>" +
                            // Set margin and padding to 0 in order to get rid of SigmobAndroid WebView
                            // default padding
                            "<body style=\"margin:0;padding:0\">" +
                            "<img src=\"" + mResource + "\" width=\"100%\" style=\"max-width:100%;max-height:100%;\" />" +
                            "</body>" +
                            "</html>";
                    webView.loadData(data);
                } else if (mCreativeType == CreativeType.JAVASCRIPT) {
                    String data = "<script src=\"" + mResource + "\"></script>";
                    webView.loadData(data);
                }
                break;
            case NATIVE_RESOURCE:
                if (mResource.toLowerCase().startsWith("file://")) {
                    webView.loadUrl(mResource);
                } else {
                    webView.loadUrl("file://" + mResource);

                }
                break;
            case URL_RESOURCE:
                webView.loadUrl(mResource);
                break;
        }
    }

    /**
     * Selects the correct click through url based on the type of resource.
     *
     * @param clickThroughUrl        The click through url as specified in the video document. This
     *                               is used with static images.
     * @param webViewClickThroughUrl The click through url when pertaining to Javascript, HTML,
     *                               IFrames that originated from a WebView.
     * @return String representing the correct click through for the resource type which may be
     * {@code null} if the correct click through url was not specified or {@code null}.
     */

    public String getCorrectClickThroughUrl(final String clickThroughUrl,
                                            final String webViewClickThroughUrl) {
        switch (mType) {
            case STATIC_RESOURCE:
                if (CreativeResource.CreativeType.IMAGE == mCreativeType) {
                    SigmobLog.d("CreativeType.IMAGE");

                    if (TextUtils.isEmpty(clickThroughUrl))
                        return webViewClickThroughUrl;

                    return clickThroughUrl;
                } else if (CreativeResource.CreativeType.JAVASCRIPT == mCreativeType) {
                    SigmobLog.d("CreativeType.JAVASCRIPT");

                    return webViewClickThroughUrl;
                }
                return null;
            case NATIVE_RESOURCE:
            case HTML_RESOURCE:
            case IFRAME_RESOURCE:
            case URL_RESOURCE:
                if (CreativeResource.CreativeType.IMAGE == mCreativeType) {
                    SigmobLog.d("CreativeType.IMAGE");

                    if (TextUtils.isEmpty(webViewClickThroughUrl)) {
                        return clickThroughUrl;
                    } else {
                        return null;
                    }
                } else if (CreativeResource.CreativeType.JAVASCRIPT == mCreativeType) {
                    SigmobLog.d("CreativeType.JAVASCRIPT");
                    return webViewClickThroughUrl;
                }
            default:
                return null;
        }
    }
}
