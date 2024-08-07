package com.gt.sdk.base.common;


import com.czhj.sdk.common.track.AdTracker;

public class SigAdTracker extends AdTracker {
    public SigAdTracker(final String trackingUrl, String event, final String request_id) {
        super(MessageType.TRACKING_URL, trackingUrl, event, request_id);
    }
}
