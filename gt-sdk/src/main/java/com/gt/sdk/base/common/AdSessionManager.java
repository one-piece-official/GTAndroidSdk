package com.gt.sdk.base.common;

import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.base.BaseAdUnit;
import com.gt.sdk.base.network.SigmobTrackingRequest;
import com.gt.sdk.base.point.PointCategory;
import com.gt.sdk.utils.PointEntityUtils;

import java.util.HashMap;

public class AdSessionManager implements SessionManager {

    @Override
    public boolean createDisplaySession(BaseAdUnit adUnit) {
        return true;
    }

    @Override
    public boolean recordDisplayEvent(BaseAdUnit adUnit, String event) {
        try {
            switch (event) {
                case ADEvent.AD_SHOW: {
                    SigmobTrackingRequest.sendTracking(adUnit, ADEvent.AD_SHOW);
                    eventRecord(adUnit, PointCategory.SHOW, null, null);
                }
                break;
                case ADEvent.AD_SKIP: {
                    SigmobTrackingRequest.sendTracking(adUnit, ADEvent.AD_SKIP);
                    eventRecord(adUnit, PointCategory.SKIP, null, null);
                }
                break;
                case ADEvent.AD_CLICK: {
                    SigmobTrackingRequest.sendTracking(adUnit, ADEvent.AD_CLICK);
                    eventRecord(adUnit, PointCategory.CLICK, null, null);
                }
                break;
                case ADEvent.AD_FOUR_ELEMENTS_SHOW: {
                    eventRecord(adUnit, PointCategory.FOUR_ELEMENTS, PointCategory.SHOW, null);
                }
                break;
                case ADEvent.AD_FOUR_ELEMENTS_CLOSE: {
                    eventRecord(adUnit, PointCategory.FOUR_ELEMENTS, PointCategory.CLOSE, null);
                }
                break;
            }
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean endDisplaySession(BaseAdUnit adUnit) {
        SigmobTrackingRequest.sendTracking(adUnit, ADEvent.AD_CLOSE);
        eventRecord(adUnit, PointCategory.CLOSE, null, null);
        return true;
    }

    private void eventRecord(BaseAdUnit adUnit, final String event, String sub, final HashMap<String, String> options) {
        PointEntityUtils.GtTracking(event, sub, adUnit, new PointEntityUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {

            }
        });
    }
}
