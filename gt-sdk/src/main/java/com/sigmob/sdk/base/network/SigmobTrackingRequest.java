package com.sigmob.sdk.base.network;

import android.text.TextUtils;

import com.czhj.sdk.common.track.TrackManager;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.VolleyError;
import com.sigmob.sdk.Sigmob;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.SigAdTracker;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.SigMacroCommon;

import java.util.List;

public class SigmobTrackingRequest {


    public static void sendTrackings(BaseAdUnit adUnit, String event, boolean isMulti) {
        if (event == null || adUnit == null || TextUtils.isEmpty(event)) return;

        List<SigAdTracker> sigAdTrackers = adUnit.getAdTracker(event);
        if (sigAdTrackers == null) return;

        for (SigAdTracker adTracking : sigAdTrackers) {

            SigMacroCommon baseMacroCommon = adUnit.getMacroCommon();
            baseMacroCommon.addMarcoKey(SigMacroCommon._PLAYFIRSTFRAME_, "1");

            SigmobTrackingRequest.sendTracking(
                    adTracking,
                    adUnit,
                    isMulti);
        }
    }

    public static void sendTrackings(BaseAdUnit adUnit, String event) {
        sendTrackings(adUnit, event, false);
    }

    public static int sendJsTrackings(BaseAdUnit adUnit, String eventName, boolean jsSend) {

        if (adUnit == null || TextUtils.isEmpty(eventName)) return -1;

        if (TextUtils.isEmpty(eventName)) return -1;

        List<SigAdTracker> sigAdTrackers = adUnit.getAdTracker(eventName);
        if (sigAdTrackers == null || sigAdTrackers.isEmpty()) return -2;

        for (SigAdTracker adTracking : sigAdTrackers) {

            if (jsSend) {
                adTracking.setSource("js");
            }

            SigMacroCommon baseMacroCommon = adUnit.getMacroCommon();
            baseMacroCommon.addMarcoKey(SigMacroCommon._PLAYFIRSTFRAME_, "1");

            SigmobTrackingRequest.sendTracking(
                    adTracking,
                    adUnit, false);
        }
        return 0;
    }


    public static void sendTracking(final com.czhj.sdk.common.track.AdTracker tracker,
                                    final BaseAdUnit adUnit, final boolean isMulti,
                                    final boolean inQueue, final boolean statistic,
                                    final TrackManager.Listener listener) {

        if (tracker != null) {

            if (tracker.getMessageType() == SigAdTracker.MessageType.TRACKING_URL && (!tracker.isTracked() || isMulti)) {

                String trackingUrl = tracker.getUrl();
                if (adUnit != null) {
                    trackingUrl = adUnit.getMacroCommon().macroProcess(trackingUrl, Sigmob.getInstance().getMacroCommon().getMacroMap());
                } else {
                    trackingUrl = Sigmob.getInstance().getMacroCommon().macroProcess(trackingUrl);
                }

                final String finalUrlString = trackingUrl;
                tracker.setUrl(finalUrlString);
                TrackManager.sendTracking(tracker, null, isMulti, inQueue, new TrackManager.Listener() {
                    @Override
                    public void onSuccess(com.czhj.sdk.common.track.AdTracker tracker, NetworkResponse response) {
                        if (statistic) {
                            PointEntitySigmobUtils.eventTracking(tracker, finalUrlString, adUnit, response, null);
                        }
                        if (listener != null) {
                            listener.onSuccess(tracker, response);
                        }
                    }

                    @Override
                    public void onErrorResponse(com.czhj.sdk.common.track.AdTracker tracker, VolleyError error) {
                        if (statistic) {
                            PointEntitySigmobUtils.eventTracking(tracker, finalUrlString, adUnit, error);
                        }
                        if (listener != null) {
                            listener.onErrorResponse(tracker, error);
                        }
                    }
                });

            }
        }

    }

    public static void sendTracking(final com.czhj.sdk.common.track.AdTracker tracker, final BaseAdUnit adUnit, final boolean isMulti) {

        sendTracking(tracker, adUnit, isMulti, true, true, null);

    }


}
