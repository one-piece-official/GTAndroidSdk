package com.gt.sdk.base.services;

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
import com.gt.sdk.base.common.ADEvent;
import com.gt.sdk.base.common.AdStackManager;
import com.gt.sdk.base.common.BaseBroadcastReceiver;
import com.gt.sdk.base.common.DownloadAPK;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;
import com.gt.sdk.base.models.point.PointCategory;
import com.gt.sdk.base.network.SigmobTrackingRequest;
import com.gt.sdk.utils.GtFileUtil;
import com.gt.sdk.utils.PointEntityUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class DownloadCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        DownloadAPK.removeDownloadItem(downId, null);

        File downloadAPKLogFile = new File(GtFileUtil.getDownloadAPKLogPath(), downId + ".log");
        Object object = FileUtil.readFromCache(downloadAPKLogFile.getAbsolutePath());

        BaseAdUnit adUnit = null;
        if (object instanceof BaseAdUnit) {
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
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        return manager;
    }

    private void handleDownloadComplete(final Context context, final long downId, final BaseAdUnit adUnit) {

        String message = "download info is null";
        if (adUnit != null) {

            Map<String, Object> downloadMap = DownloadAPK.getDownloadInfoWithDownloadID(downId);

            SigmobTrackingRequest.sendTracking(adUnit, ADEvent.AD_DOWNLOAD_FINISH);

            if (downloadMap != null) {
                final String fileNameURi = (String) downloadMap.get("fileName");
                int status = (int) downloadMap.get("status");
                int reason = (int) downloadMap.get("reason");

                Uri downloadUri = (Uri) downloadMap.get("uri");

                if (fileNameURi != null && status == DownloadManager.STATUS_SUCCESSFUL) {

                    try {
                        adUnit.enableUseDownloadApk(true);
                        String filePath = FileUtil.getRealFilePath(context, downloadUri);

                        PackageInfo info = ClientMetadata.getPackageInfoWithUri(context, filePath);
                        PointEntityUtils.eventRecord(PointCategory.DOWNLOAD_END, Constants.SUCCESS, adUnit);

                        HashMap<String, Object> ext = new HashMap<>();
                        ext.put("result", Constants.SUCCESS);
                        ext.put("downloadId", downId);

                        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_AD_DOWNLOAD_END);
                        DownloadAPK.installAPK(context, filePath, adUnit);
                    } catch (Throwable e) {
                        SigmobLog.e(e.getMessage());
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
                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
            } catch (Throwable throwable) {
                SigmobLog.e(throwable.getMessage());
            }

            HashMap<String, Object> ext = new HashMap<>();
            ext.put("result", Constants.FAIL);
            ext.put("downloadId", downId);

            BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_AD_DOWNLOAD_END);
        }
    }

}
