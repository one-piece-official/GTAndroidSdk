package com.sigmob.sdk.base.services;


import com.czhj.sdk.common.ClientMetadata;

public class ServiceFactory {

    public final static String LocationServiceName = "LocationService";
    public final static String AppInstallServiceName = "AppInstallService";
    public final static String WifiScanServiceName = "WifiScanService";
    public final static String DownloadServiceName = "DownloadService";

    private static Service mLocationService;
    private static Service mAppInstallService;
    private static Service mWifiScanService;
    private static Service mDownloadService;

    public static synchronized Service creatorService(String name) {

        switch (name) {
            case LocationServiceName:
                if (mLocationService == null) {
                    return mLocationService = new LocationService();
                } else {
                    return mLocationService;
                }
            case AppInstallServiceName:
                if (mAppInstallService == null) {
                    return mAppInstallService = new AppInstallService();
                } else {
                    return mAppInstallService;
                }
            case WifiScanServiceName:
                if (mWifiScanService == null) {
                    return mWifiScanService = new WifiScanService();
                } else {
                    return mWifiScanService;
                }
            case DownloadServiceName:
                if (mDownloadService == null) {
                    return mDownloadService = new DownloadService();
                } else {
                    return mDownloadService;
                }
            default:
                return null;
        }
    }

    public static void updateService(String serviceName, boolean enabled) {
        switch (serviceName) {
            case LocationServiceName:
                if (enabled) {
                    creatorService(serviceName).startService();
                } else {
                    if (mLocationService != null) {
                        mLocationService.stopService();
                    }
                }
                ClientMetadata.getInstance().setEnableLocation(enabled);
                break;
            case AppInstallServiceName:
                if (enabled) {
                    creatorService(serviceName).startService();
                } else {
                    if (mAppInstallService != null) {
                        mAppInstallService.stopService();
                    }
                }
                break;
            case WifiScanServiceName:
                if (enabled) {
                    creatorService(serviceName).startService();
                } else {
                    if (mWifiScanService != null) {
                        mWifiScanService.stopService();
                    }
                }
                break;
            case DownloadServiceName:
                if (enabled) {
                    creatorService(serviceName).startService();
                } else {
                    if (mDownloadService != null) {
                        mDownloadService.stopService();
                    }
                }
                break;
            default:
                return;
        }
    }

    public static Service getAppInstallService() {
        return mAppInstallService;
    }

    public static Service getWifiScanService() {
        return mWifiScanService;
    }

    public static Service getDownloadService() {
        return mDownloadService;
    }

    public static Service getLocationService() {
        return mLocationService;
    }

    public interface Service {
        boolean startService();

        ServiceStatus getStatus();

        void stopService();

        Error getError();
    }

}

