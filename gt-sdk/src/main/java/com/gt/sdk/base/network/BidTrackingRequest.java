package com.gt.sdk.base.network;

import android.text.TextUtils;
import android.util.Base64;

import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.network.SigmobRequest;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.DefaultRetryPolicy;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.Response;
import com.czhj.volley.VolleyError;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.SigmobTracking;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.windad.WindAdRequest;

public class BidTrackingRequest extends SigmobRequest<NetworkResponse> {

    private final Listener mListener;

    public BidTrackingRequest(String url, Listener mListener) {
        this(url, DefaultRetryPolicy.DEFAULT_CONNECT_TIMEOUT_MS, mListener);
    }

    public BidTrackingRequest(String url, int ConnectTimeoutMs, Listener mListener) {
        super(url, Method.GET, mListener);
        this.mListener = mListener;
        setShouldRetryServerErrors(true);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(
                ConnectTimeoutMs,
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        setRetryPolicy(retryPolicy);
        setShouldCache(false);
    }

    public static void sendTracking(final String trackingUrl, final String tracking_type, final WindAdRequest adRequest, final String request_id) {

        if (!TextUtils.isEmpty(trackingUrl)) {

            BidTrackingRequest request = new BidTrackingRequest(trackingUrl, new Listener() {
                @Override
                public void onSuccess(NetworkResponse response) {
                    recordPoint(trackingUrl, tracking_type, adRequest, response, request_id);
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse response = null;
                    if (error != null) {
                        response = error.networkResponse;
                    }
                    recordPoint(trackingUrl, tracking_type, adRequest, response, request_id);
                }
            });

            if (Networking.getSigRequestQueue() != null) {
                Networking.getSigRequestQueue().add(request);
            }
        }
    }

    public static void recordPoint(final String trackingUrl, final String tracking_type, WindAdRequest adRequest, final NetworkResponse response, final String request_id) {

        SigmobTracking.getSigmobTracking(PointCategory.HB_TRACKING)
                .setWindAdRequest(adRequest)
                .setOnPointEntityExtraInfo(new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        String base64 = null;

                        if(pointEntityBase instanceof PointEntitySigmob){

                            PointEntitySigmob pointEntitySigmob = (PointEntitySigmob)pointEntityBase;
                            pointEntitySigmob.setUrl(trackingUrl);
                            pointEntitySigmob.setTracking_type(tracking_type);
                            pointEntitySigmob.setRequest_id(request_id);
                            if (response != null) {
                                if (response.data != null) {
                                    base64 = Base64.encodeToString(response.data, Base64.NO_WRAP);
                                }
                                pointEntitySigmob.setResponse(base64);
                                pointEntitySigmob.setHttp_code(String.valueOf(response.statusCode));
                                pointEntitySigmob.setTime_spend(String.valueOf(response.networkTimeMs));
                                pointEntitySigmob.setContent_type(response.headers.get("Content-Type"));
                                pointEntitySigmob.setContent_length(response.headers.get("Content-Length"));
                            }
                        }

                    }
                }).send();
    }

    @Override
    public int getMaxLength() {
        return 100;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, null);
    }

    @Override
    public void deliverError(VolleyError error) {
        SigmobLog.e("send tracking: " + getUrl() + " fail");
        super.deliverError(error);
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        Listener listener;
        synchronized (mLock) {
            listener = mListener;
        }

        SigmobLog.i("send tracking: " + getUrl() + " success");

        if (listener != null) {
            listener.onSuccess(response);
        }
    }

    public interface Listener extends Response.ErrorListener {
        void onSuccess(NetworkResponse response);
    }

}
