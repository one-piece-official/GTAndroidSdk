package com.sigmob.sdk.base.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.common.utils.AppPackageUtil;
import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.BaseBroadcastReceiver;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.SigToast;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.IntentActions;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.windad.WindAdError;

import java.io.File;
import java.util.HashMap;

class AppInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            PackageManager manager = context.getPackageManager();
            String packageName = intent.getData().getSchemeSpecificPart();

            PackageInfo info = null;

            boolean isReplace = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            String action = Base64.encodeToString(intent.getAction().getBytes(), Base64.NO_WRAP);
            if (action.equals(WindConstants.APP_REMOVE) && !isReplace) {

                return;
            }


            try {
                if (PrivacyManager.getInstance().canCollectPersonalInformation()) {
                    if (!WindSDKConfig.getInstance().isDisableUpAppInfo()) {
                        info = manager.getPackageInfo(packageName, 0);
                        PointEntitySigmobUtils.appInfoListTracking(info,1);
                    }
                }

            } catch (PackageManager.NameNotFoundException e) {

            }


            BaseAdUnit adUnit = null;
            if (!TextUtils.isEmpty(packageName)) {
                File downloadAPKLogFile = new File(SigmobFileUtil.getDownloadAPKLogPath(), packageName + ".log");
                Object object = FileUtil.readFromCache(downloadAPKLogFile.getAbsolutePath());

                if (object != null && object instanceof BaseAdUnit) {
                    adUnit = (BaseAdUnit) object;
                    BaseAdUnit playAdUnit = AdStackManager.getPlayAdUnit(adUnit.getUuid());
                    if (playAdUnit != null) {
                        adUnit = playAdUnit;
                    }
                }

                FileUtil.deleteFile(downloadAPKLogFile.getAbsolutePath());
            }

            if (adUnit == null) {
//                install_Monitor(context, info, intent.getAction());
                return;
            }

            String title = adUnit.getAppName();

            if (TextUtils.isEmpty(title)){
                try {
                    title = String.valueOf(AppPackageUtil.getPackageManager(SDKContext.getApplicationContext()).
                            getApplicationLabel(info.applicationInfo));
                } catch (Throwable e) {
                    title = packageName;
                }
            }

            switch (action) {
                case WindConstants.APP_ADD: {

                    if (WindConstants.IS_DEBUG) {
                        SigToast.makeText(context, title +" 安装成功" , Toast.LENGTH_LONG).show();
                    }
                    install_finish(context, adUnit, info);

                    break;
                }
                case WindConstants.APP_REMOVE: {
                    if (isReplace) {
                        SigToast.makeText(context,  title+" 替换成功" , Toast.LENGTH_LONG).show();

                        install_finish(context, adUnit, info);
                    } else {
                        if (WindConstants.IS_DEBUG) {
                            SigToast.makeText(context, title+" 卸载成功" , Toast.LENGTH_LONG).show();
                        }
                    }

                    break;
                }
                case WindConstants.APP_REPLACE: {
                    if (WindConstants.IS_DEBUG) {
                        SigToast.makeText(context, title+" 替换成功", Toast.LENGTH_LONG).show();
                    }
                    install_finish(context, adUnit, info);


                    break;
                }
                case WindConstants.APP_INSTALL_FAIL: {
                    if (WindConstants.IS_DEBUG) {
                        SigToast.makeText(context, title+" 安装失败", Toast.LENGTH_SHORT).show();
                    }
                    PointEntitySigmobUtils.eventRecord(adUnit, PointCategory.APP_INSTALL_END, info, WindAdError.ERROR_SIGMOB_INSTALL_FAIL.toString() + "," + "System");
                    PointEntitySigmobUtils.SigmobError(PointCategory.APP_INSTALL_END, WindAdError.ERROR_SIGMOB_INSTALL_FAIL.getErrorCode(), "system", adUnit);

                    HashMap ext = new HashMap<>();
                    ext.put("result", Constants.FAIL);
                    BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_INSTALL_END);

                }
                break;
            }

        } catch (Throwable throwable) {
            SigmobLog.e(throwable.getMessage());
        }

    }

    private void install_Monitor(Context context, PackageInfo info, String action) {
        PointEntitySigmobUtils.eventRecord(null, PointCategory.APP_INSTALL_MONITOR, info, action);
    }


    private void install_finish(final Context context, final BaseAdUnit adUnit, final PackageInfo info) {
        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_INSTALL_FINISH);
        PointEntitySigmobUtils.eventRecord(adUnit, PointCategory.APP_INSTALL_END, info, Constants.SUCCESS);

        HashMap ext = new HashMap<>();
        ext.put("result", Constants.SUCCESS);
        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_INSTALL_END);

        adUnit.setApkPackageName(info.packageName);
        ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                File file = new File(SigmobFileUtil.getDownloadAPKPathFile(context.getApplicationContext()), adUnit.getApkName());
                FileUtil.deleteFile(file.getAbsolutePath());
            }
        });


        final String deeplinkUrl = adUnit.getDeeplinkUrl();
        if (TextUtils.isEmpty(deeplinkUrl)) {
            return;
        }

//        WindAds.sharedAds().getHandler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent =new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_HOME);
////                intent.setPackage(info.packageName);
//                try {
//                    for (ResolveInfo resolveInfo : context.getPackageManager().queryBroadcastReceivers(intent, 0)) {
//                        ActivityInfo activityInfo = resolveInfo.activityInfo;
//
//                        SigmobLog.d(""+activityInfo.toString());
//                    }
////                    Runtime.getRuntime().exec("adb shell am start " + info.packageName);
//                    IntentUtil.launchApplicationIntent(context, intent);
//                } catch (Throwable e) {
//                    SigmobLog.e(e.getMessage());
//                }
//            }
//        },500);
//
//        WindAds.sharedAds().getHandler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (adUnit.getInteractionType() == InterActionType.DownloadOpenDeepLinkType
//                        || adUnit.getBubInteractionType() == 2) {
//
//                    try {
//                        final String jumpUrl = adUnit.getMacroCommon().macroProcess(deeplinkUrl);
//                        Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(jumpUrl));
//                        intent.setPackage(info.packageName);
//                        IntentUtil.launchApplicationIntent(context, intent);
//                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK);
//                        PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_DEEPLINK, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
//                            @Override
//                            public void onAddExtra(Object pointEntityBase) {
//                                if (pointEntityBase instanceof PointEntitySigmob) {
//                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
//                                    entitySigmob.setFinal_url(jumpUrl);
//                                }
//                            }
//                        });
//
//                    } catch (Throwable throwable) {
//                        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK_FAIL);
//                        PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_DEEPLINK_FAILED, null, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
//                            @Override
//                            public void onAddExtra(Object pointEntityBase) {
//                                if (pointEntityBase instanceof PointEntitySigmob) {
//                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
//                                    entitySigmob.setFinal_url(deeplinkUrl);
//                                }
//                            }
//                        });
//                        SigmobLog.e(throwable.getMessage());
//                    }
//
//                }
//            }
//        },1500);


    }


}
