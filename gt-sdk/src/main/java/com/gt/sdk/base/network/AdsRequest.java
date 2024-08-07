package com.gt.sdk.base.network;

import android.text.TextUtils;

import com.czhj.sdk.common.ClientMetadata;

import com.czhj.sdk.common.network.SigmobRequest;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.DefaultRetryPolicy;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.ParseError;
import com.czhj.volley.Response;
import com.czhj.volley.VolleyError;
import com.czhj.volley.toolbox.HttpHeaderParser;
import com.gt.sdk.AdError;
import com.gt.sdk.base.BaseAdUnit;
import com.gt.sdk.base.LoadAdRequest;
import com.gt.sdk.base.models.BidRequest;
import com.gt.sdk.base.models.BidResponse;
import com.gt.sdk.base.models.Imp;
import com.gt.sdk.base.models.ModelBuilderCreator;
import com.gt.sdk.base.models.SeatBid;
import com.gt.sdk.utils.DeviceContextManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class AdsRequest extends SigmobRequest<BidResponse> {

    private final RequestFactory.LoadAdRequestListener mListener;
    private final LoadAdRequest adRequest;

    public AdsRequest(String url, final LoadAdRequest loadAdRequest, final RequestFactory.LoadAdRequestListener listener) {

        super(url, Method.POST, null);
        Preconditions.NoThrow.checkNotNull(listener);
        mListener = listener;
        adRequest = loadAdRequest;
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        setRetryPolicy(retryPolicy);
        setShouldCache(false);

    }

    public RequestFactory.LoadAdRequestListener getListener() {
        return mListener;
    }

    public static BidRequest.Builder createBidRequest(LoadAdRequest loadAdRequest) {

        BidRequest.Builder builder = new BidRequest.Builder();
        try {
//            builder.token("");

            builder.id(loadAdRequest.getLoadId());//请求标识 ID，由媒体生成，请确保全局唯一

            Imp.Builder impBuild = new Imp.Builder();
            impBuild.id(UUID.randomUUID().toString());//曝光标识 ID，只在多次曝光有意义，媒体生成，请确保全局唯一
            impBuild.tagid(loadAdRequest.getPlacementId());
            impBuild.style(loadAdRequest.getAdType());
            impBuild.secure(0);
            impBuild.bidfloor(loadAdRequest.getBidFloor());
            impBuild.width(loadAdRequest.getWidth());
            impBuild.height(loadAdRequest.getHeight());
            impBuild.deeplink(1);

            List<Imp> impList = new ArrayList<>();
            impList.add(impBuild.build());

            builder.imp(impList);

            builder.app(ModelBuilderCreator.createApp().build());
            builder.device(ModelBuilderCreator.createDevice(DeviceContextManager.sharedInstance().getDeviceContext()).build());
            builder.user(ModelBuilderCreator.createUser().build());
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
        return builder;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = super.getHeaders();
        headers.put("cp", "1");
        return headers;
    }

    @Override
    public String getBodyContentType() {
        return "application/octet-stream";
    }

    @Override
    public byte[] getBody() {
        BidRequest.Builder builder;
        BidRequest request = null;
        try {
            if (adRequest != null) {
                builder = createBidRequest(adRequest);
                request = builder.build();
            }
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

        if (request == null) {
            SigmobLog.e("builder Ads Post entry fail ");
            return null;
        }

        byte[] body;
        try {
            body = request.encode();
            body = DeflateUtils.compress(body);
        } catch (Throwable e) {
            getHeaders().remove("cp");
            body = request.encode();
        }
        return body;
    }

    @Override
    protected Response<BidResponse> parseNetworkResponse(final NetworkResponse networkResponse) {
        // NOTE: We never get status codes outside of {[200, 299], 304}. Those errors are sent to the
        // error listener.
        try {
            byte[] data;
            if (networkResponse.data == null) {
                return Response.error(new ParseError(networkResponse));
            } else {
                boolean cp = networkResponse.headers.containsKey("cp");
                if (cp && "1".equals(networkResponse.headers.get("cp"))) {
                    data = DeflateUtils.decompress(networkResponse.data);
                } else {
                    data = networkResponse.data;
                }
            }

            BidResponse bidResponse = BidResponse.ADAPTER.decode(data);
            if (bidResponse != null) {
                return Response.success(bidResponse, HttpHeaderParser.parseCacheHeaders(networkResponse));
            } else {
                return Response.error(new ParseError(networkResponse));
            }
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(final BidResponse bidResponse) {
        if (bidResponse != null) {
            String uid = bidResponse.bidId;
            if (!TextUtils.isEmpty(uid)) {
                ClientMetadata.getInstance().setUid(uid);
            }
            if (bidResponse.code == 0 && !bidResponse.bids.isEmpty()) {
                try {
                    List<BaseAdUnit> adUnits = new ArrayList<>();
                    for (int i = 0; i < bidResponse.bids.size(); i++) {
                        SeatBid ad = bidResponse.bids.get(i);
                        BaseAdUnit adUnit = BaseAdUnit.adUnit(ad, bidResponse.request_id, adRequest, bidResponse.bidId);
                        adUnit.setAd_type(adRequest.getAdType());
                        adUnit.setAdslot_id(adRequest.getPlacementId());
                        adUnits.add(adUnit);
                    }

                    if (mListener != null) {
                        mListener.onSuccess(adUnits, adRequest);
                    }
                } catch (Throwable e) {
                    SigmobLog.e("ads Response: error ", e);
                }
            } else {
                mListener.onErrorResponse(bidResponse.code, AdError.ERROR_AD_NETWORK.getMessage(), bidResponse.request_id, adRequest);
            }
        } else {
            mListener.onErrorResponse(AdError.ERROR_AD_INFORMATION_LOSE.getErrorCode(), "bidResponse is null", null, adRequest);
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        if (error instanceof ParseError) {
            mListener.onErrorResponse(AdError.ERROR_AD_INFORMATION_LOSE.getErrorCode(), error.getMessage(), null, adRequest);
        } else {
            mListener.onErrorResponse(AdError.ERROR_AD_NETWORK.getErrorCode(), error.getMessage(), null, adRequest);
        }
    }

}

