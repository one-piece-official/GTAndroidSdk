package com.sigmob.sdk.base.network;

import android.text.TextUtils;

import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.network.SigmobRequestUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.VolleyError;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.windad.WindAdError;

import java.net.URL;
import java.util.List;

public class RequestFactory {


    public static void LoadAd(final LoadAdRequest loadAdRequest,
                              final LoadAdRequestListener listener) {

        if (Networking.getSigRequestQueue() == null) {

            if (listener != null)
                listener.onErrorResponse(WindAdError.ERROR_SIGMOB_NETWORK.getErrorCode(), "request queue is null", null, loadAdRequest);
            return;
        }

        try {
            String url =loadAdRequest.getAdType() == AdFormat.UNIFIED_NATIVE ? WindSDKConfig.getInstance().getNativeAd(): WindSDKConfig.getInstance().getAdsUrl();
            try {
                if (!TextUtils.isEmpty(loadAdRequest.getBidToken())) {
                    url = WindSDKConfig.getInstance().getHbAdsUrl();
                }
            } catch (Throwable e) {
                SigmobLog.e(e.getMessage());
            }

            if (TextUtils.isEmpty(url)) {
                if (listener != null)
                    listener.onErrorResponse(WindAdError.ERROR_SIGMOB_BAD_REQUEST.getErrorCode(), WindAdError.ERROR_SIGMOB_BAD_REQUEST.getMessage(), null, loadAdRequest);
                return;
            }

            URL tempURL = new URL(url);
            if (!SigmobRequestUtil.isConnection(tempURL.getHost())) {
                if (listener != null)
                    listener.onErrorResponse(WindAdError.ERROR_SIGMOB_NETWORK.getErrorCode(), "network is disconnection", null, loadAdRequest);
                return;
            }
            AdsRequest request = new AdsRequest(url,loadAdRequest, listener);
            Networking.getSigRequestQueue().add(request);
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
            if (listener != null)
                listener.onErrorResponse(WindAdError.ERROR_SIGMOB_NETWORK.getErrorCode(), e.getMessage(), null, loadAdRequest);
        }

    }

    public interface LoadAdRequestListener {
        void onSuccess(List<BaseAdUnit> adUnit, LoadAdRequest loadAdRequest);

        void onErrorResponse(int error, String message, String request_id, LoadAdRequest loadAdRequest);
    }

    public interface RequestListener {
        void onSuccess();

        void onErrorResponse(VolleyError error);
    }


}
