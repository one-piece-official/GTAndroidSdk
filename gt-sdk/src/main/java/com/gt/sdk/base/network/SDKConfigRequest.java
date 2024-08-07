package com.gt.sdk.base.network;

import static android.util.Base64.NO_WRAP;
import static com.czhj.sdk.common.models.ModelBuilderCreator.createDevice;
import static com.czhj.sdk.common.models.ModelBuilderCreator.createDeviceId;
import static com.czhj.sdk.common.models.ModelBuilderCreator.createNetwork;
import static com.sigmob.sdk.base.network.AdsRequest.createPrivacy;
import static com.sigmob.sdk.base.network.AdsRequest.createUser;

import android.util.Base64;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.network.SigmobRequest;
import com.czhj.sdk.common.utils.AESUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.DefaultRetryPolicy;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.ParseError;
import com.czhj.volley.Response;
import com.czhj.volley.toolbox.HttpHeaderParser;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.models.config.SigmobSdkConfigRequest;
import com.sigmob.sdk.base.models.config.SigmobSdkConfigResponse;

import java.util.Map;

public class SDKConfigRequest extends SigmobRequest<SigmobSdkConfigResponse> {

    private final Listener mListener;
    private final SigmobSdkConfigRequest.Builder builder;

    public SDKConfigRequest(final String url,
                            final Listener listener) {
        super(url, Method.POST, listener);
        mListener = listener;
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        setRetryPolicy(retryPolicy);
        setShouldCache(false);

        builder = createSdkConfigRequest();

    }

    public static SigmobSdkConfigRequest.Builder createSdkConfigRequest() {
        SigmobSdkConfigRequest.Builder builder = new SigmobSdkConfigRequest.Builder();
        if (ClientMetadata.getInstance() != null && ClientMetadata.getInstance().getDeviceLocale() != null) {
            builder.app(AdsRequest.createApp().build());

            builder.user(createUser().build());
            builder.privacy(createPrivacy().build());
            builder.network(createNetwork(SDKContext.getDeviceContext()).build());
            builder.device(createDevice(SDKContext.getDeviceContext()).did(createDeviceId(SDKContext.getDeviceContext()).build()).build());
        }

        return builder;
    }

    public Listener getListener() {
        return mListener;
    }

    @Override
    public String getBodyContentType() {
        return "application/octet-stream";
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = super.getHeaders();

        if (WindConstants.ENCRYPT) {
            try {
                headers.put("agn", Base64.encodeToString(AESUtil.generateNonce(), NO_WRAP));
            } catch (NoSuchMethodError th) {
                headers.put("e", "1");
            }
        }

        headers.put("cp","1");
        return headers;
    }

    @Override
    public byte[] getBody() {


        SigmobSdkConfigRequest request = builder.build();

        byte[] body;

        try {
            body = request.encode();
            body = DeflateUtils.compress(body);
        }catch (Throwable e) {
            getHeaders().remove("cp");
            body = request.encode();
        }

        try {
            if (WindConstants.ENCRYPT) {
                body = AESUtil.Encrypt(body, SigmobRequest.AESKEY);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        return body;
    }

    @Override
    protected Response<SigmobSdkConfigResponse> parseNetworkResponse(final NetworkResponse networkResponse) {
        // NOTE: We never get status codes outside of {[200, 299], 304}. Those errors are sent to the
        // error listener.

        try {
            byte[] data;
            if (networkResponse.data == null) {
                return Response.error(new ParseError(networkResponse));
            }else{
                boolean cp = networkResponse.headers.containsKey("cp");
                if (cp && "1".equals(networkResponse.headers.get("cp"))){
                    data = DeflateUtils.decompress(networkResponse.data);
                }else {
                    data = networkResponse.data;
                }
            }

            SigmobSdkConfigResponse sigmobSdkConfigResponse = SigmobSdkConfigResponse.ADAPTER.decode(data);
            return Response.success(sigmobSdkConfigResponse,  // Cast needed for Response generic.
                    HttpHeaderParser.parseCacheHeaders(networkResponse));

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
            return Response.error(new ParseError(e));
        }

    }

    @Override
    protected void deliverResponse(final SigmobSdkConfigResponse sigmobSdkConfigResponse) {
        mListener.onSuccess(sigmobSdkConfigResponse);
    }


    public interface Listener extends Response.ErrorListener {
        void onSuccess(SigmobSdkConfigResponse sigmobSdkConfigResponse);
    }


}
