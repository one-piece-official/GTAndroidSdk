package com.sigmob.sdk.base.common;

import com.sigmob.sdk.base.models.BaseAdUnit;

public interface SessionManager {
    void createDisplaySession(BaseAdUnit adUnit);

    void endDisplaySession();

    void recordDisplayEvent(String event, int currentPosition);

}
