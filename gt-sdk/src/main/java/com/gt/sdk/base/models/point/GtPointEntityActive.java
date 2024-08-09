package com.gt.sdk.base.models.point;


import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.mta.PointEntitySuper;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.WindConstants;
import com.gt.sdk.manager.WindSDKConfig;
import com.gt.sdk.manager.PrivacyManager;
import com.gt.sdk.manager.DeviceContextManager;

import java.util.List;

/**
 * created by lance on   2022/4/6 : 2:34 下午
 */
public class GtPointEntityActive extends PointEntitySuper {

    private String active_id;

    private String duration;

    public String getActive_id() {
        return active_id;
    }

    public void setActive_id(String active_id) {
        this.active_id = active_id;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public boolean isAcTypeBlock() {
        if (!PrivacyManager.canCollectPersonalInformation()) {
            return true;
        }
        List<Integer> blackList = WindSDKConfig.getInstance().getLogBlackList();

        for (Integer acType : blackList) {
            if (getAc_type().equals(String.valueOf(acType))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String appId() {
        return GtAdSdk.sharedAds().getAppId();
    }

    @Override
    public String getSdkVersion() {
        return WindConstants.SDK_VERSION;
    }

    @Override
    public DeviceContext getDeviceContext() {
        return DeviceContextManager.sharedInstance().getDeviceContext();
    }
}
