package com.gt.sdk.base.network;

import android.text.TextUtils;

import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.network.SigmobRequestUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.AdError;
import com.gt.sdk.admanager.GtConfigManager;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.LoadAdRequest;

import java.net.URL;
import java.util.List;

public class RequestFactory {

    public static void LoadAd(final LoadAdRequest loadAdRequest, final LoadAdRequestListener listener) {

        if (Networking.getSigRequestQueue() == null) {
            if (listener != null)
                listener.onErrorResponse(AdError.ERROR_AD_NETWORK.getErrorCode(), "request queue is null", null, loadAdRequest);
            return;
        }

        try {
            String url = GtConfigManager.sharedInstance().getAdUrl();

            if (TextUtils.isEmpty(url)) {
                if (listener != null)
                    listener.onErrorResponse(AdError.ERROR_AD_BAD_REQUEST.getErrorCode(), AdError.ERROR_AD_BAD_REQUEST.getMessage(), null, loadAdRequest);
                return;
            }

            URL tempURL = new URL(url);
            if (!SigmobRequestUtil.isConnection(tempURL.getHost())) {
                if (listener != null)
                    listener.onErrorResponse(AdError.ERROR_AD_NETWORK.getErrorCode(), "network is disconnection", null, loadAdRequest);
                return;
            }
            AdsRequest request = new AdsRequest(url, loadAdRequest, listener);
            Networking.getSigRequestQueue().add(request);
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
            if (listener != null) {
                listener.onErrorResponse(AdError.ERROR_AD_NETWORK.getErrorCode(), e.getMessage(), null, loadAdRequest);
            }
        }
    }

    public interface LoadAdRequestListener {
        void onSuccess(List<BaseAdUnit> adUnits, LoadAdRequest loadAdRequest);

        void onErrorResponse(int code, String message, String request_id, LoadAdRequest loadAdRequest);
    }

}
