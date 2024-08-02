package com.sigmob.sdk.base.common;

import static android.Manifest.permission.QUERY_ALL_PACKAGES;
import static android.Manifest.permission.REQUEST_INSTALL_PACKAGES;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.text.TextUtils;

import com.czhj.sdk.common.mta.PointEntitySuper;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.models.AppInfo;
import com.sigmob.sdk.base.models.rtb.Ad;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.windad.WindAdError;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AdLoadCheckUtil {

    private static Boolean canInstallPackage = null;
    public static String[] getPermissions() {
        Context context = SDKContext.getApplicationContext();

        if (context == null) {
            return null;
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo pack = null;
        try {
            pack = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] permissionStrings = pack.requestedPermissions;
            return permissionStrings;
        } catch (Throwable e) {

        }

        return null;
    }

    public static WindAdError doCheck() {

        Context context = SDKContext.getApplicationContext();


        /**
         * 检查activity
         *
         * AdActivity
         */
        try {

            SigmobLog.e("checkActivity: " + AdActivity.class.getName());
            ComponentName component = new ComponentName(context, AdActivity.class);
            ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(component, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            SigmobLog.e("checkActivity: " + e.getMessage());
            return WindAdError.ERROR_LOAD_FILTER_FOR_ACTIVITY_ERROR;
        } catch (Throwable e) {
            e.printStackTrace();
        }


        /**
         * 检查混淆
         */

        try {

//            String sub = Ad.class.getName().substring(Ad.class.getName().indexOf(".") + 1);
//            String ad = sub.substring(sub.indexOf(".") + 1);

            if (!Ad.class.getName().endsWith("base.models.rtb.Ad")) {
                SigmobLog.e("checkProguard Ad: " + Ad.class.getName());
                return WindAdError.ERROR_LOAD_FILTER_FOR_PROGUARD_ERROR;
            }

            if (!MaterialMeta.class.getName().endsWith("base.models.rtb.MaterialMeta")) {
                SigmobLog.e("checkProguard Strategy: " + MaterialMeta.class.getName());
                return WindAdError.ERROR_LOAD_FILTER_FOR_PROGUARD_ERROR;
            }

            if (!AppInfo.class.getName().endsWith("base.models.AppInfo")) {
                SigmobLog.e("checkProguard AppInfo: " + AppInfo.class.getName());
                return WindAdError.ERROR_LOAD_FILTER_FOR_PROGUARD_ERROR;
            }

            if (!PointEntitySuper.class.getName().endsWith("common.mta.PointEntitySuper")) {
                SigmobLog.e("checkProguard PointCategory: " + PointEntitySuper.class.getName());
                return WindAdError.ERROR_LOAD_FILTER_FOR_PROGUARD_ERROR;
            }
        } catch (Throwable e) {
            SigmobLog.e("checkProguard: " + e.getMessage());
        }

        return null;
    }
    public static synchronized boolean canInstallPackage() {

        if (canInstallPackage != null) {
            return canInstallPackage;
        }
        String permissionReq = "";

        /**
         * 检查REQUEST_INSTALL_PACKAGES
         */
        try {

            canInstallPackage = false;

            String[] permissions = getPermissions();
            if (permissions != null) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(REQUEST_INSTALL_PACKAGES)) {
                        permissionReq = permissions[i];
                    }
                }
            }

        } catch (Throwable e) {
            SigmobLog.e("checkPermission: " + e.getMessage());
        }
        if (!TextUtils.isEmpty(permissionReq)) {
            /**
             *
             * 检查provider
             *
             * Get all provider classes in the AndroidManifest.xml
             */
            String targetAuthority = "";
            int xml = -1;

            try {
                String packageName = SDKContext.getApplicationContext().getPackageName();
                PackageInfo packageInfo = SDKContext.getApplicationContext().getPackageManager().getPackageInfo(packageName, PackageManager.GET_PROVIDERS);
                if (packageInfo.providers != null) {
                    for (ProviderInfo provider : packageInfo.providers) {
                        String authority = provider.authority;
                        if (authority != null && authority.equals(packageName + ".sigprovider")) {
                            targetAuthority = authority;
                            ComponentName component = new ComponentName(SDKContext.getApplicationContext(), provider.name);
                            ProviderInfo providerInfo = SDKContext.getApplicationContext().getPackageManager().getProviderInfo(component, PackageManager.GET_META_DATA);
                            Bundle metaData = providerInfo.metaData;
                            if (metaData != null) {
                                xml = metaData.getInt("android.support.FILE_PROVIDER_PATHS");
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                SigmobLog.e("check targetAuthority:" + e.getMessage());
            }

            if (xml != -1) {
                String name = "";
                String path = "";
                XmlResourceParser xpp = SDKContext.getApplicationContext().getResources().getXml(xml);
                int eventType = 0;
                try {
                    eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("external-cache-path")) {
                                int attributeCount = xpp.getAttributeCount();
                                if (attributeCount >= 2) {
                                    name = xpp.getAttributeValue(0);
                                    path = xpp.getAttributeValue(1);
                                    if (name.equals("SigMob_root")) {
                                        canInstallPackage = true;
                                        SigmobLog.e("check name: " + name + " path :" + path);
                                        break;
                                    }
                                }
                            }
                        }
                        eventType = xpp.next();
                    }
                    xpp.close();
                } catch (Throwable e) {

                }
            }
        }

        return canInstallPackage;
    }

    public static boolean hasQueryPackagePermission() {
        String[] permissions = getPermissions();
        if (permissions != null) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(QUERY_ALL_PACKAGES)) {
                    return true;
                }
            }
        }
        return false;
    }
}
