package com.gt.sdk.base.models;

import android.location.Location;
import android.os.Environment;
import android.text.TextUtils;


import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.AppPackageUtil;
import com.czhj.sdk.common.utils.DeviceUtils;
import com.czhj.sdk.common.utils.RomUtils;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.base.models.rtb.App;
import com.gt.sdk.base.models.rtb.Device;
import com.gt.sdk.base.models.rtb.Geo;
import com.gt.sdk.base.models.rtb.User;

import java.util.TimeZone;


public class ModelBuilderCreator {

    public static App.Builder createApp() {
        App.Builder builder = new App.Builder();
        try {

            if (!TextUtils.isEmpty(GtAdSdk.sharedAds().getAppId())) {
                builder.id(GtAdSdk.sharedAds().getAppId());
            }

            String appName = ClientMetadata.getInstance().getAppName();
            if (!TextUtils.isEmpty(appName)) {
                builder.name(appName);
            }

            String appVer = ClientMetadata.getInstance().getAppVersion();
            if (!TextUtils.isEmpty(appVer)) {
                builder.ver(appVer);
            }

            if (ClientMetadata.getInstance().getAppPackageName() != null) {
                builder.bundle(ClientMetadata.getInstance().getAppPackageName());
            }

            RomUtils.RomInfo romInfo = RomUtils.getRomInfo();
            if (romInfo != null) {
                String osMarket = romInfo.getOsMarket();
                if (!TextUtils.isEmpty(osMarket)) {
                    int packageVersionCode = AppPackageUtil.getPackageVersionCode(GtAdSdk.sharedAds().getContext(), osMarket);
                    if (packageVersionCode != -1) {
                        builder.store_ver(String.valueOf(packageVersionCode));
                    }
                }
            }
        } catch (Throwable e) {
            SigmobLog.e("App Builder failed", e);
        }
        return builder;
    }

    public static Device.Builder createDevice(DeviceContext deviceContext) {
        Device.Builder builder = new Device.Builder();
        builder.os(1);
        try {

            builder.osv(ClientMetadata.getDeviceOSLevel());

            String imei = deviceContext != null ? deviceContext.getImei() : ClientMetadata.getInstance().getDeviceId();
            if (!TextUtils.isEmpty(imei)) {
                builder.did(imei);
            }

            try {
                String o_aid = deviceContext != null ? deviceContext.getOaid() : ClientMetadata.getInstance().getOAID();
                if (!TextUtils.isEmpty(o_aid)) {
                    builder.oid(o_aid);
                }
            } catch (Throwable ignored) {

            }

            String androidId = deviceContext != null ? deviceContext.getAndroidId() : ClientMetadata.getInstance().getAndroidId();
            if (!TextUtils.isEmpty(androidId)) {
                builder.dpid(androidId);
            }

            builder.ip(ClientMetadata.getCell_ip());

            String ua = Networking.getUserAgent();
            if (!TextUtils.isEmpty(ua)) {
                builder.ua(ua);
            }

            builder.connectiontype(ClientMetadata.getInstance().getActiveNetworkType());

            boolean isTable = ClientMetadata.getInstance().isTablet();

            builder.devicetype(isTable ? 2 : 1);

            String brand = ClientMetadata.getDeviceBrand();
            if (!TextUtils.isEmpty(brand)) {
                builder.make(brand);
            }

            String model = ClientMetadata.getDeviceModel();
            if (!TextUtils.isEmpty(model)) {
                builder.model(ClientMetadata.getDeviceModel());
            }

            String hardWare = DeviceUtils.getProperty("ro.boot.hardware.revision");
            if (!TextUtils.isEmpty(hardWare)) {
                builder.hwv(hardWare);
            }

            builder.carrier(ClientMetadata.getInstance().getSimOperatorName());

            String macAddress = ClientMetadata.getMacAddress();
            if (!TextUtils.isEmpty(macAddress)) {
                builder.mac(macAddress);
            }

            builder.w(ClientMetadata.getInstance().getRealMetrics().widthPixels);
            builder.h(ClientMetadata.getInstance().getRealMetrics().heightPixels);
            builder.ppi(ClientMetadata.getInstance().getDensityDpi());

            builder.geo(createGeo(deviceContext).build());

            builder.ssid(ClientMetadata.getInstance().getWifiName());

            builder.wifi_mac(ClientMetadata.getInstance().getWifimac());

            builder.rom_version(ClientMetadata.getDeviceOsVersion());

            builder.installed_app(ClientMetadata.getInstance().getAppList());

//            String bootId = ClientMetadata.getInstance().getBootId();
//
//            if (!TextUtils.isEmpty(bootId)) {
//                builder.boot_mark(bootId);
//            }
//
//            String updateId = ClientMetadata.getInstance().getUpdateId();
//
//            if (!TextUtils.isEmpty(updateId)) {
//                builder.update_mark(updateId);
//            }

//            builder.update_time();
            builder.boot_time(String.valueOf(ClientMetadata.getBootSystemTime()));
            builder.sys_compiling_time(String.valueOf(ClientMetadata.getBuildTime()));
            builder.birth_time(String.valueOf(ClientMetadata.getRebootTime()));

//            builder.paid_1_4();
//            builder.hwv();
//            builder.hwag_ver();
//            builder.caid();

            builder.physical_memory(String.valueOf(ClientMetadata.getInstance().getSystemTotalMemorySize()));
            builder.hard_disk_size(String.valueOf(Environment.getDataDirectory().getTotalSpace()));
            builder.country(ClientMetadata.getInstance().getDeviceLocale().getCountry());
            builder.language(ClientMetadata.getInstance().getDeviceLocale().getLanguage().toUpperCase());
            builder.time_zone(TimeZone.getDefault().getID());
//            builder.cpu_num();
        } catch (Throwable e) {
            SigmobLog.e("Device Builder failed", e);
        }

        return builder;
    }

    public static Geo.Builder createGeo(DeviceContext deviceContext) {

        Geo.Builder geoBuild = new Geo.Builder();
        try {
            //set location
            Location location = deviceContext != null ? deviceContext.getLocation() : ClientMetadata.getInstance().getLocation();
            if (location != null) {
                geoBuild.lat((float) location.getLatitude());
                geoBuild.lon((float) location.getLongitude());
            }
        } catch (Throwable e) {
            SigmobLog.e("Geo Builder failed", e);
        }

        return geoBuild;
    }

    public static User.Builder createUser() {
        User.Builder builder = new User.Builder();
        try {
            if (!TextUtils.isEmpty(GtAdSdk.getUserId())) {
                builder.id(GtAdSdk.getUserId());
            }
        } catch (Throwable e) {
            SigmobLog.e("User Builder failed", e);
        }
        return builder;
    }

//    public static WXProgramReq.Builder createWXProgramReq() {
//
//        WXProgramReq.Builder builder = new WXProgramReq.Builder();
//
//        try {
////            IWXAPI api = WXAPIFactory.createWXAPI(this, appId, false);
////            boolean wxAppInstalled = api.isWXAppInstalled();
////            int wxAppSupportAPI = api.getWXAppSupportAPI();
////
////            int majorVersion = Build.getMajorVersion();
////            int minorVersion = Build.getMinorVersion();
////            int sdkInt = Build.SDK_INT;
////            String SDK_VERSION_NAME = Build.SDK_VERSION_NAME;
//
//            Class factory = Class.forName("com.tencent.mm.opensdk.openapi.WXAPIFactory");
//            java.lang.reflect.Method createWXAPI = factory.getMethod("createWXAPI", Context.class, String.class);
//            createWXAPI.setAccessible(true);
//            Object api = createWXAPI.invoke(factory, ClientMetadata.getInstance().getContext(), "");
//
//            java.lang.reflect.Method isWXAppInstalled = api.getClass().getMethod("isWXAppInstalled");
//            isWXAppInstalled.setAccessible(true);
//            boolean appInstalled = (boolean) isWXAppInstalled.invoke(api);
//
//            java.lang.reflect.Method getWXAppSupportAPI = api.getClass().getMethod("getWXAppSupportAPI");
//            getWXAppSupportAPI.setAccessible(true);
//            int appSupportAPI = (int) getWXAppSupportAPI.invoke(api);
//
//            Class build = Class.forName("com.tencent.mm.opensdk.constants.Build");
//
////            java.lang.reflect.Method getMajorVersion = build.getMethod("getMajorVersion");
////            getMajorVersion.setAccessible(true);
////            int majorVersion = (int) getMajorVersion.invoke(build);
//
//            Field f = build.getDeclaredField("SDK_INT");
//            f.setAccessible(true);
//            int SDK_INT = (int) f.get(null);
//
//            builder.wx_installed(appInstalled);
//            builder.wx_api_ver(appSupportAPI);
//            builder.opensdk_ver(String.valueOf(SDK_INT));
//        } catch (Throwable throwable) {
//            SigmobLog.e("createWXProgramReq failed" + throwable.getMessage());
//        }
//
//        return builder;
//    }

}
