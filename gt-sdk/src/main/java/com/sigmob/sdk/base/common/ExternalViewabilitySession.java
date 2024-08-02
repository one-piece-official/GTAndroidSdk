package com.sigmob.sdk.base.common;


import com.sigmob.sdk.base.models.BaseAdUnit;

public interface ExternalViewabilitySession {

    // Display only
    boolean createDisplaySession(final BaseAdUnit adUnit);

    boolean recordDisplayEvent(BaseAdUnit adUnit, final String event, final int playheadMillis);

    boolean ondDisplayShowSkip(BaseAdUnit adUnit, final boolean isForceShow, final int playheadMillis);

    boolean onVideoPrepared(BaseAdUnit adUnit, final int duration, final int endTime);

    boolean endDisplaySession(BaseAdUnit adUnit);

}
