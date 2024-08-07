package com.gt.sdk.base.network;

import static android.util.Base64.NO_WRAP;

import android.text.TextUtils;
import android.util.Base64;

import com.czhj.sdk.common.json.JSONSerializer;
import com.czhj.sdk.common.network.SigmobRequest;
import com.czhj.sdk.common.utils.AESUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.DefaultRetryPolicy;
import com.czhj.volley.Response;
import com.czhj.volley.VolleyError;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.models.rtb.BidResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class FeedbackReportRequest extends SigmobRequest {


    private Listener mListener;

    private byte[] body;

    public interface Listener extends Response.ErrorListener {
        void onSuccess(JSONObject response);
    }


    private void convertByteStringToBase64(JSONObject json, String name) {
        if (json == null) return;
        JSONObject jsonObject = json.optJSONObject(name);
        if (jsonObject != null) {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                Object object = jsonObject.opt(next);
                if (object instanceof String) {
                    String str = (String) object;
                    if (!TextUtils.isEmpty(str)) {
                        String s = Base64.encodeToString(str.getBytes(), Base64.NO_WRAP);
                        json.remove(name);
                        try {
                            json.put(name, s);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    public FeedbackReportRequest(String url, BidResponse bidResponse, Listener mListener) {


        super(url, Method.POST, mListener);
        this.mListener = mListener;

        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        setRetryPolicy(retryPolicy);
        setShouldCache(false);

        if (WindConstants.ENCRYPT) {

            String s = convertToJson(bidResponse);
            if (s != null) {
                try {
                    body = AESUtil.Encrypt(s.getBytes(), SigmobRequest.AESKEY);
                } catch (Exception e) {
                    SigmobLog.e("feedback body bug");
                }
            }
        }
    }

    private String convertToJson(BidResponse bidResponse) {
        try {
            String serialize = JSONSerializer.Serialize(bidResponse);

            JSONObject json = new JSONObject(serialize);

            JSONObject scene = json.optJSONObject("scene");
            if (scene != null) {
                convertByteStringToBase64(scene, "context");
            }
            JSONArray ads = json.optJSONArray("ads");
            if (ads != null) {
                for (int i = 0; i < ads.length(); i++) {
                    JSONObject ad = ads.getJSONObject(i);
                    JSONArray materials = ad.getJSONArray("materials");
                    for (int j = 0; j < materials.length(); j++) {
                        JSONObject material = materials.getJSONObject(j);
                        convertByteStringToBase64(material, "html_snippet");
                        convertByteStringToBase64(material, "closecard_html_snippet");
                        JSONObject main_template = json.optJSONObject("main_template");
                        if (main_template != null) {
                            convertByteStringToBase64(main_template, "context");
                        }
                        JSONObject sub_template = json.optJSONObject("sub_template");
                        if (sub_template != null) {
                            convertByteStringToBase64(sub_template, "context");
                        }
                    }

                }

            }

            SigmobLog.d("json " + json);
            return json.toString();
        } catch (Throwable th) {
            SigmobLog.e("Serialize error", th);
        }
        return null;
    }

    @Override
    public byte[] getBody() {
        return body;
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
        return headers;
    }

    public String getBodyContentType() {
        return "application/json";
    }


    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
        if (mListener != null) {
            mListener.onErrorResponse(error);
        }
    }

    @Override
    protected void deliverResponse(Object response) {
        if (mListener != null) {
            mListener.onSuccess(null);
        }
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
