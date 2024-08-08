package com.gt.sdk.base.models.point;


import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.mta.PointEntityGDPR;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.GtConstants;
import com.gt.sdk.admanager.WindSDKConfig;
import com.gt.sdk.admanager.PrivacyDataManager;
import com.gt.sdk.admanager.DeviceContextManager;

import java.util.List;

public class GtPointEntityPrivacy extends PointEntityGDPR {

    @Override
    public boolean isAcTypeBlock() {
        if (!PrivacyDataManager.canCollectPersonalInformation()) {
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
        return GtConstants.SDK_VERSION;
    }

    @Override
    public DeviceContext getDeviceContext() {
        return DeviceContextManager.sharedInstance().getDeviceContext();
    }
}
