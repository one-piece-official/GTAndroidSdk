package com.sigmob.sdk.base.services;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.widget.Toast;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.BaseBroadcastReceiver;
import com.sigmob.sdk.base.common.DownloadAPK;
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
import java.util.Map;

class DownloadCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        DownloadAPK.removeDownloadItem(downId, null);

        File downloadAPKLogFile = new File(SigmobFileUtil.getDownloadAPKLogPath(), downId + ".log");
        Object object = FileUtil.readFromCache(downloadAPKLogFile.getAbsolutePath());

        BaseAdUnit adUnit = null;
        if (object != null && object instanceof BaseAdUnit) {
            adUnit = (BaseAdUnit) object;
            BaseAdUnit playAdUnit = AdStackManager.getPlayAdUnit(adUnit.getUuid());
            if (playAdUnit != null) {
                adUnit = playAdUnit;
            }
        }

        FileUtil.deleteFile(downloadAPKLogFile.getAbsolutePath());
        switch (intent.getAction()) {
            case DownloadManager.ACTION_DOWNLOAD_COMPLETE: {
                if (adUnit == null) return;

                handleDownloadComplete(context, downId, adUnit);
            }
            break;
        }

    }

    DownloadManager getDownloadManager(Context context) {
        DownloadManager manager = (DownloadManager)
                context.getSystemService(Context.DOWNLOAD_SERVICE);

        return manager;
    }


    private void handleDownloadComplete(final Context context, final long downId, final BaseAdUnit adUnit) {

        String message = "download info is null";
        if (adUnit != null) {

            Map<String, Object> downloadMap = DownloadAPK.getDownloadInfoWithDownloadID(downId);

            SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_DOWNLOAD_FINISH);

            if (downloadMap != null) {
                final String fileNameURi = (String) downloadMap.get("fileName");
                int status = (int) downloadMap.get("status");
                int reason = (int) downloadMap.get("reason");

                Uri downloadUri = (Uri) downloadMap.get("uri");

                if (fileNameURi != null && status == DownloadManager.STATUS_SUCCESSFUL) {

                    try {
                        adUnit.enableUseDownloadApk(true);
                        String filePath = FileUtil.getRealFilePath(context, downloadUri);
//                        File file = new File(filePath);
//
//                        if (file.exists() && !TextUtils.isEmpty(adUnit.getApkName())){
//                            File newfile = new File(SigmobFileUtil.getDownloadAPKPathFile(SDKContext.getApplicationContext()), adUnit.getApkName());
//
//                           if (!filePath.equals(newfile.getAbsolutePath())){
//                               boolean b = file.renameTo(newfile);
//                               if (b){
//                                   filePath = newfile.getAbsolutePath();
//                               }
//                           }
//                        }


                        PackageInfo info = ClientMetadata.getPackageInfoWithUri(context, filePath);
                        PointEntitySigmobUtils.eventRecord(adUnit, PointCategory.DOWNLOAD_END, info, Constants.SUCCESS);

                        HashMap ext = new HashMap<>();
                        ext.put("result", Constants.SUCCESS);
                        ext.put("downloadId", downId);

                        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_END);

                        DownloadAPK.installAPK(context, filePath, adUnit);
                    } catch (Throwable e) {
                        SigmobLog.e(e.getMessage());
                        PointEntitySigmobUtils.SigmobError(PointCategory.APP_INSTALL_START, WindAdError.ERROR_SIGMOB_INSTALL_FAIL.getErrorCode(), e.getMessage(), adUnit);

                        PointEntitySigmobUtils.eventRecord(PointCategory.APP_INSTALL_START, Constants.FAIL, adUnit);
                    }
                    return;
                } else {
                    try {
                        message = String.format("status %d, reason %d", status, reason);
                        String filePath = FileUtil.getRealFilePath(context, downloadUri);
                        boolean deleteFile = FileUtil.deleteFile(filePath);
                        SigmobLog.i("handleDownloadComplete:fail:" + deleteFile);
                    } catch (Throwable throwable) {
                        SigmobLog.e(throwable.getMessage());
                    }
                }
            }

            try {
                SigToast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
            } catch (Throwable throwable) {
                SigmobLog.e(throwable.getMessage());
            }

            PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_FAILED, 2, message, adUnit);

            PointEntitySigmobUtils.eventRecord(PointCategory.DOWNLOAD_END, Constants.FAIL, adUnit);

            HashMap ext = new HashMap<>();
            ext.put("result", Constants.FAIL);
            ext.put("downloadId", downId);

            BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_INTERSTITIAL_DOWNLOAD_END);

        }

    }


}
