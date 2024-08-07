package com.gt.sdk.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.text.TextUtils;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.mta.DeviceContext;
import com.gt.sdk.api.GtCustomController;

import java.lang.ref.WeakReference;

public class DeviceContextManager {

    private static DeviceContextManager gInstance;

    private static WeakReference<Activity> mActivityWeakReference;

    private DeviceContextManager() {

    }

    public static DeviceContextManager sharedInstance() {
        if (gInstance == null) {
            synchronized (DeviceContextManager.class) {
                if (gInstance == null) {
                    gInstance = new DeviceContextManager();
                }
            }
        }
        return gInstance;
    }

    private GtCustomController controller;

    public void init(GtCustomController controller) {
        this.controller = controller;
    }

    private DeviceContext deviceContext;

    public DeviceContext getDeviceContext() {

        if (deviceContext != null) {
            return deviceContext;
        }

        deviceContext = new DeviceContext() {
            @Override
            public boolean isCustomOaId() {
                if (controller != null && !TextUtils.isEmpty(controller.getOaid())) {
                    return true;
                }
                return super.isCustomOaId();
            }

            @Override
            public String getOaid() {
                if (controller != null) {
                    if (!TextUtils.isEmpty(controller.getOaid())) {
                        return controller.getOaid();
                    } else {
                        return ClientMetadata.getInstance().getOAID();
                    }
                }
                return ClientMetadata.getInstance().getOAID();
            }

            @Override
            public boolean isCustomAndroidId() {
                if (controller != null) {
                    return controller.canUseAndroidId();
                }
                return super.isCustomAndroidId();
            }

            @Override
            public String getAndroidId() {
                if (controller != null) {
                    if (controller.canUseAndroidId()) {
                        return ClientMetadata.getInstance().getAndroidId();
                    } else {
                        return controller.getAndroidId();
                    }
                } else {
                    return ClientMetadata.getInstance().getAndroidId();
                }
            }

            @Override
            public boolean isCustomPhoneState() {
                if (controller != null) {
                    return controller.canUsePhoneState();
                }
                return super.isCustomPhoneState();
            }

            @Override
            public String getImei() {
                if (controller != null) {
                    if (controller.canUsePhoneState()) {
                        return ClientMetadata.getInstance().getDeviceId();
                    } else {
                        return controller.getImei();
                    }
                } else {
                    return ClientMetadata.getInstance().getDeviceId();
                }
            }

            @Override
            public String getImei1() {
                if (controller != null) {
                    if (controller.canUsePhoneState()) {
                        return ClientMetadata.getInstance().getDeviceId(0);
                    } else {
                        return controller.getImei();
                    }
                } else {
                    return ClientMetadata.getInstance().getDeviceId(0);
                }
            }

            @Override
            public String getImei2() {
                if (controller != null) {
                    if (controller.canUsePhoneState()) {
                        return ClientMetadata.getInstance().getDeviceId(1);
                    } else {
                        return controller.getImei();
                    }
                } else {
                    return ClientMetadata.getInstance().getDeviceId(1);
                }
            }

            @Override
            public Location getLocation() {
                if (controller != null) {
                    if (controller.canReadLocation()) {
                        return ClientMetadata.getInstance().getLocation();
                    } else {
                        return controller.getLocation();
                    }
                } else {
                    return ClientMetadata.getInstance().getLocation();
                }
            }

            @Override
            public String getCarrier() {
                if (controller != null) {
                    if (controller.canUsePhoneState()) {
                        return ClientMetadata.getInstance().getNetworkOperatorForUrl();
                    } else {
                        return super.getCarrier();
                    }
                } else {
                    return ClientMetadata.getInstance().getNetworkOperatorForUrl();
                }
            }

            @Override
            public String getCarrierName() {
                if (controller != null) {
                    if (controller.canUsePhoneState()) {
                        return ClientMetadata.getInstance().getNetworkOperatorName();
                    } else {
                        return super.getCarrier();
                    }
                } else {
                    return ClientMetadata.getInstance().getNetworkOperatorName();
                }
            }
        };

        return deviceContext;
    }


    public static void setTopActivity(Activity activity) {
        if (activity != null) {
            mActivityWeakReference = new WeakReference<>(activity);
        }
    }

    public static Activity getTopActivity() {
        if (mActivityWeakReference != null) {
            return mActivityWeakReference.get();
        }
        return null;
    }


}
