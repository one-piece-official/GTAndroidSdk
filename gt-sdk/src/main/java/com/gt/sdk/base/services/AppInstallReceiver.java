package com.gt.sdk.base.services;

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
import com.gt.sdk.WindConstants;
import com.gt.sdk.base.common.ADEvent;
import com.gt.sdk.base.common.AdStackManager;
import com.gt.sdk.base.common.BaseBroadcastReceiver;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;
import com.gt.sdk.base.models.point.PointCategory;
import com.gt.sdk.base.network.SigmobTrackingRequest;
import com.gt.sdk.utils.GtFileUtil;
import com.gt.sdk.utils.PointEntityUtils;

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

            BaseAdUnit adUnit = null;
            if (!TextUtils.isEmpty(packageName)) {
                File downloadAPKLogFile = new File(GtFileUtil.getDownloadAPKLogPath(), packageName + ".log");
                Object object = FileUtil.readFromCache(downloadAPKLogFile.getAbsolutePath());
                if (object instanceof BaseAdUnit) {
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

            if (TextUtils.isEmpty(title)) {
                try {
                    title = String.valueOf(AppPackageUtil.getPackageManager(SDKContext.getApplicationContext()).getApplicationLabel(info.applicationInfo));
                } catch (Throwable e) {
                    title = packageName;
                }
            }

            switch (action) {
                case WindConstants.APP_ADD: {
                    Toast.makeText(context, title + " 安装成功", Toast.LENGTH_LONG).show();
                    install_finish(context, adUnit, info);
                    break;
                }
                case WindConstants.APP_REMOVE:
                case WindConstants.APP_REPLACE: {
                    Toast.makeText(context, title + " 替换成功", Toast.LENGTH_LONG).show();
                    install_finish(context, adUnit, info);
                    break;
                }
                case WindConstants.APP_INSTALL_FAIL: {
                    Toast.makeText(context, title + " 安装失败", Toast.LENGTH_SHORT).show();
                    HashMap<String, Object> ext = new HashMap<>();
                    ext.put("result", Constants.FAIL);
                    BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_AD_INSTALL_END);
                }
                break;
            }
        } catch (Throwable throwable) {
            SigmobLog.e(throwable.getMessage());
        }
    }

    private void install_finish(final Context context, final BaseAdUnit adUnit, final PackageInfo info) {
        SigmobTrackingRequest.sendTracking(adUnit, ADEvent.AD_INSTALL_FINISH);
        PointEntityUtils.eventRecord(PointCategory.APP_INSTALL_END, Constants.SUCCESS, adUnit);

        HashMap<String, Object> ext = new HashMap<>();
        ext.put("result", Constants.SUCCESS);
        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_AD_INSTALL_END);

        adUnit.setApkPackageName(info.packageName);
        ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                File file = new File(GtFileUtil.getDownloadAPKPathFile(), adUnit.getApkName());
                FileUtil.deleteFile(file.getAbsolutePath());
            }
        });
    }

}
