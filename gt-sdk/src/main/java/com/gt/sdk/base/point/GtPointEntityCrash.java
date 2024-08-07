package com.gt.sdk.base.point;

import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.mta.PointEntityCrash;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.GtConstants;
import com.gt.sdk.admanager.GtConfigManager;
import com.gt.sdk.admanager.PrivacyDataManager;
import com.gt.sdk.utils.DeviceContextManager;


import java.util.List;

public class GtPointEntityCrash extends PointEntityCrash {

    public static GtPointEntityCrash AdCrash(String crash) {
        GtPointEntityCrash windCrash = new GtPointEntityCrash();
        windCrash.setAc_type(PointType.GT_CRASH);
        windCrash.setCategory(PointCategory.CRASH);
        windCrash.setCrashTime(System.currentTimeMillis());
        windCrash.setCrashMessage(crash);
        return windCrash;
    }

    @Override
    public boolean isAcTypeBlock() {
        if (!PrivacyDataManager.canCollectPersonalInformation()) {
            return true;
        }
        List<Integer> blackList = GtConfigManager.sharedInstance().getLogBlackList();

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
