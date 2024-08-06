package com.gt.sdk.base.network;


import com.czhj.sdk.common.network.SigmobRequest;
import com.czhj.volley.DefaultRetryPolicy;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.ParseError;
import com.czhj.volley.Response;
import com.czhj.volley.toolbox.HttpHeaderParser;
import com.gt.sdk.base.models.ModelBuilderCreator;
import com.gt.sdk.base.models.SdkConfigRequest;
import com.gt.sdk.base.models.SdkConfigResponse;
import com.gt.sdk.utils.DeviceContextManager;
import com.gt.sdk.utils.WMLogUtil;

import java.util.Map;

public class GtConfigRequest extends SigmobRequest<SdkConfigResponse> {

    private final Listener mListener;
    private final SdkConfigRequest.Builder builder;

    public GtConfigRequest(final String url, final Listener listener) {
        super(url, Method.POST, listener);
        mListener = listener;
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        setRetryPolicy(retryPolicy);
        setShouldCache(false);
        builder = new SdkConfigRequest.Builder();
    }

    public Listener getListener() {
        return mListener;
    }

    @Override
    public String getBodyContentType() {
        return "application/octet-stream";
    }

    @Override
    public byte[] getBody() {

        builder.app(ModelBuilderCreator.createApp().build());

        builder.device(ModelBuilderCreator.createDevice(DeviceContextManager.sharedInstance().getDeviceContext()).build());

        builder.user(ModelBuilderCreator.createUser().build());

        SdkConfigRequest request = builder.build();

        WMLogUtil.dd(WMLogUtil.TAG, getUrl() + " send config request:" + request.toString());

        return request.encode();
    }

    @Override
    public Map<String, String> getHeaders() {
        return super.getHeaders();
    }

    @Override
    public Response<SdkConfigResponse> parseNetworkResponse(final NetworkResponse networkResponse) {
        try {
            SdkConfigResponse sdkConfigResponse = SdkConfigResponse.ADAPTER.decode(networkResponse.data);
            return Response.success(sdkConfigResponse,  // Cast needed for Response generic.
                    HttpHeaderParser.parseCacheHeaders(networkResponse));
        } catch (Throwable e) {
            WMLogUtil.e(e.getMessage());
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public void deliverResponse(final SdkConfigResponse sdkConfigResponse) {
        if (mListener != null) {
            mListener.onSuccess(sdkConfigResponse);
        }
    }

    public interface Listener extends Response.ErrorListener {
        void onSuccess(SdkConfigResponse sdkConfigResponse);
    }

}
