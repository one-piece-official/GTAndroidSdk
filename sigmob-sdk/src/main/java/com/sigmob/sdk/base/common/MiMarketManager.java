package com.sigmob.sdk.base.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.mta.PointEntitySigmob;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiMarketManager {

    private static final int INDEX_PACKAGE_NAME = 0;
    private static final int INDEX_INSTALL_FINISH_TIME = 1;
    private static final int INDEX_REFERRER = 2;
    private static final int INDEX_START_DOWNLOAD_TIME = 3;

    public static void referrerQuery(Context context, ArrayList<String> packageList) {
        Cursor cursor = null;
        try {

            cursor = context.getContentResolver().query(Uri.parse("content://com.xiaomi.market.provider.DirectMailProvider/referrer"),
                    null,
                    null,
                    null,
                    null);

            for (; ; ) {
                if (cursor != null && cursor.moveToFirst()) {
                    //已经下载文件大小
                    String packageName = cursor.getString(INDEX_PACKAGE_NAME);
                    //下载文件的总大小
                    String install_time = cursor.getString(INDEX_INSTALL_FINISH_TIME);
                    //下载状态
                    String referrer = cursor.getString(INDEX_REFERRER);
                    String download_start_time = cursor.getString(INDEX_START_DOWNLOAD_TIME);
                    continue;
                }
                break;
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void downloadQuery(Context context, ArrayList<String> packageList) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("packageNames", packageList);
        Bundle bundleResult = context.getContentResolver().call(Uri.parse("content://com.xiaomi.market.provider.DirectMailProvider/status"),
                "queryDownloadStatus",
                null,
                bundle);
        if (bundleResult != null) {
            List list = bundleResult.getParcelableArrayList("packageNames");
            if (list != null && list.size() > 0) {
                for (Object item : list) {
                    Bundle bundleItem = (Bundle) item;
                    String packageName = bundleItem.getString("packageName");
                    int status = bundleItem.getInt("status");
                    int progress = bundleItem.getInt("progress");
                    SigmobLog.d("packageName=" + packageName + ",status=" + status + ",progress=" + progress);
                }
            }
        }
    }

    public static class DirectMailStatusReceiver extends BroadcastReceiver {
        private static final String CHECK_RESULT = "com.xiaomi.market.DIRECT_MAIL_CHECK_RESULT";
        private static final String DM_STATUS = "com.xiaomi.market.DIRECT_MAIL_STATUS";
        private static final String DOWNLOAD_PROGRESS = "com.xiaomi.market.DIRECT_MAIL_DOWNLOAD_PROGRESS";
        private BaseAdUnit adUnit;

        public void register(Context context, BaseAdUnit adUnit) {
            if (context == null) {
                return;
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CHECK_RESULT);
            intentFilter.addAction(DM_STATUS);
            intentFilter.addAction(DOWNLOAD_PROGRESS);

            this.adUnit = adUnit;
            IntentUtil.registerReceiver(context,this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case CHECK_RESULT: {
                    Bundle data = intent.getExtras();
                    int styleResult = data.getInt("styleCheckResult");
                    String packageName = data.getString("packageName");


                    PointEntitySigmobUtils.SigmobTracking("mimarket_check_result", styleResult == 0 ? Constants.SUCCESS : Constants.FAIL, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {

                            if (pointEntityBase instanceof PointEntitySigmob) {
                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                Map<String, String> options = entitySigmob.getOptions();
                                options.put("packageName", packageName);
                            }
                        }
                    });
                    SigmobLog.d("MiMarketManager: " + "styleResult:" + styleResult + " packageName:" + packageName);
                }
                break;
                case DM_STATUS: {
                    Bundle data = intent.getExtras();
                    int statusCode = data.getInt("statusCode");
                    String packageName = data.getString("packageName");
                    SigmobLog.d("MiMarketManager: " + "statusCode:" + statusCode + " packageName:" + packageName);
                    PointEntitySigmobUtils.SigmobTracking("mimarket_status", String.valueOf(statusCode), adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {

                            if (pointEntityBase instanceof PointEntitySigmob) {
                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                Map<String, String> options = entitySigmob.getOptions();
                                options.put("packageName", packageName);
                            }
                        }
                    });

                }
                break;
                case DOWNLOAD_PROGRESS: {
                    Bundle data = intent.getExtras();
                    int downloadProgress = data.getInt("downloadProgress");
                    String packageName = data.getString("packageName");
                    PointEntitySigmobUtils.SigmobTracking("mimarket_download_progress", String.valueOf(downloadProgress), adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {

                            if (pointEntityBase instanceof PointEntitySigmob) {
                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                Map<String, String> options = entitySigmob.getOptions();
                                options.put("packageName", packageName);
                            }
                        }
                    });
                    SigmobLog.d("MiMarketManager: " + "downloadProgress:" + downloadProgress + " packageName:" + packageName);
                }
                break;
                default:
                    break;

            }
        }
    }
}
