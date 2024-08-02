package com.sigmob.sdk.base.common;


public class SigAdTracker extends com.czhj.sdk.common.track.AdTracker {
    public SigAdTracker(final String trackingUrl, String event, final String request_id) {
        super(MessageType.TRACKING_URL, trackingUrl, event, request_id);
    }


}
