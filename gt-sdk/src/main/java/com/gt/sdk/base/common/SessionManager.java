package com.gt.sdk.base.common;


import com.gt.sdk.base.models.BaseAdUnit;

public interface SessionManager {

    boolean createDisplaySession(BaseAdUnit adUnit);

    boolean recordDisplayEvent(BaseAdUnit adUnit, String event);

    boolean endDisplaySession(BaseAdUnit adUnit);

}
