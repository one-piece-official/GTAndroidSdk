package com.sigmob.sdk.videoAd;

import com.czhj.sdk.common.track.AdTracker;
import com.czhj.sdk.common.utils.Preconditions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FractionalProgressAdTracker extends AdTracker implements Comparable<FractionalProgressAdTracker>, Serializable {
    private static final long serialVersionUID = 0L;
    private final float mFraction;


    public FractionalProgressAdTracker(final String event, float trackingFraction) {
        super(MessageType.QUARTILE_EVENT, null, event, null);
        Preconditions.NoThrow.checkArgument(trackingFraction >= 0);
        mFraction = trackingFraction;
    }

    private float trackingFraction() {
        return mFraction;
    }

    @Override
    public int compareTo(final FractionalProgressAdTracker other) {
        float you = other.trackingFraction();
        float me = trackingFraction();

        return Double.compare(me, you);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%2f: %s", mFraction, getUrl());
    }


    public static List<FractionalProgressAdTracker>  getUntriggeredTrackersBefore(List<FractionalProgressAdTracker> fractionalTrackers,String event,long mCurrentPosition, long mDuration) {

        if (mDuration > 0 && mCurrentPosition >= 0) {
            float progressFraction = mCurrentPosition / (float) (mDuration);
            List<FractionalProgressAdTracker> untriggeredTrackers = new ArrayList<>();

            final FractionalProgressAdTracker fractionalTest = new FractionalProgressAdTracker(event, progressFraction);
            int fractionalTrackerCount = fractionalTrackers.size();
            for (int i = 0; i < fractionalTrackerCount; i++) {
                FractionalProgressAdTracker tracker = fractionalTrackers.get(i);
                if (tracker.compareTo(fractionalTest) > 0) {
                    break;
                }
                if (!tracker.isTracked()) {
                    untriggeredTrackers.add(tracker);
                }
            }

            return untriggeredTrackers;
        } else {
            return Collections.emptyList();
        }
    }
}
