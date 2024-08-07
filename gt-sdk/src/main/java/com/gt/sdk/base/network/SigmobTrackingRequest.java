package com.gt.sdk.base.network;

import android.text.TextUtils;

import com.czhj.sdk.common.track.AdTracker;
import com.czhj.sdk.common.track.TrackManager;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.VolleyError;
import com.gt.sdk.base.BaseAdUnit;
import com.gt.sdk.base.common.SigAdTracker;
import com.gt.sdk.utils.PointEntityUtils;

import java.util.List;


public class SigmobTrackingRequest {

    public static void sendTracking(BaseAdUnit adUnit, String event) {
        sendTracking(adUnit, event, false);
    }

    public static void sendTracking(BaseAdUnit adUnit, String event, boolean isMulti) {
        if (event == null || adUnit == null || TextUtils.isEmpty(event)) return;
        List<SigAdTracker> sigAdTrackers = adUnit.getAdTracker(event);
        if (sigAdTrackers == null) return;

        for (SigAdTracker adTracking : sigAdTrackers) {
            SigmobTrackingRequest.sendTracking(adTracking, adUnit, isMulti);
        }
    }

    public static void sendTracking(final AdTracker tracker, final BaseAdUnit adUnit, final boolean isMulti, final boolean inQueue, final boolean statistic, final TrackManager.Listener listener) {

        if (tracker != null) {

            if (tracker.getMessageType() == SigAdTracker.MessageType.TRACKING_URL && (!tracker.isTracked() || isMulti)) {

                String trackingUrl = tracker.getUrl();
                if (adUnit != null) {
                    trackingUrl = adUnit.getMacroCommon().macroProcess(trackingUrl);
                }

                final String finalUrlString = trackingUrl;
                tracker.setUrl(finalUrlString);
                TrackManager.sendTracking(tracker, null, isMulti, inQueue, new TrackManager.Listener() {
                    @Override
                    public void onSuccess(com.czhj.sdk.common.track.AdTracker tracker, NetworkResponse response) {
                        if (statistic) {
                            PointEntityUtils.eventTracking(tracker, finalUrlString, adUnit, response, null);
                        }
                        if (listener != null) {
                            listener.onSuccess(tracker, response);
                        }
                    }

                    @Override
                    public void onErrorResponse(com.czhj.sdk.common.track.AdTracker tracker, VolleyError error) {
                        if (statistic) {
                            PointEntityUtils.eventTracking(tracker, finalUrlString, adUnit, error);
                        }
                        if (listener != null) {
                            listener.onErrorResponse(tracker, error);
                        }
                    }
                });

            }
        }
    }

    public static void sendTracking(final AdTracker tracker, final BaseAdUnit adUnit, final boolean isMulti) {
        sendTracking(tracker, adUnit, isMulti, true, true, null);
    }

}
