package com.gt.sdk.base.common;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.common.utils.Md5Util;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.SigmobFileProvider;
import com.gt.sdk.manager.WindSDKConfig;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;
import com.gt.sdk.base.models.point.PointCategory;
import com.gt.sdk.base.network.SigmobTrackingRequest;
import com.gt.sdk.utils.GtFileUtil;
import com.gt.sdk.utils.PointEntityUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class DownloadAPK {
    /**
     * 下载Apk, 并设置Apk地址,
     * 默认位置: /storage/sdcard0/Download
     *
     * @param context     上下文
     * @param downLoadUrl 下载地址
     * @param apkName     apk名称
     */
    private static final Map<String, String> downloadMap = new Hashtable<>();
    private static final Set<DownloadAPKItem> downloadAPKList = new CopyOnWriteArraySet<>();

    private static long downloadApk(String downLoadUrl, String apkName, String title) {

        try {
            String appUrl = downLoadUrl;
            if (appUrl == null || appUrl.isEmpty()) {

                return -1;
            }
            appUrl = appUrl.trim(); // 去掉首尾空格

            Uri parse = Uri.parse(appUrl);
            String scheme = parse.getScheme();
            if (TextUtils.isEmpty(scheme)) {
                appUrl = "http://" + appUrl; // 添加Http信息
            }

            if (!appUrl.startsWith("http")) {
                return -2;
            }

            DownloadManager.Request request;

            request = new DownloadManager.Request(Uri.parse(appUrl));

            File apkFile = new File(apkName);

            request.setTitle(title);

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setAllowedOverRoaming(true);

            request.setVisibleInDownloadsUi(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            File dirFile = apkFile.getParentFile();
            if (dirFile.exists()) {
                if (!dirFile.isDirectory()) {
                    dirFile.delete();
                    if (!dirFile.mkdirs()) {
//                SigToast.makeText(context, "下载文件夹不存在", Toast.LENGTH_SHORT).show();
                        return -3;
                    }
                }
            } else {
                if (!dirFile.mkdirs()) {
//                SigToast.makeText(context, "下载文件夹不存在", Toast.LENGTH_SHORT).show();
                    return -4;
                }
            }

            request.addRequestHeader("User-Agent", Networking.getUserAgent());
            request.setDestinationUri(Uri.fromFile(apkFile));
            DownloadManager manager = getDownloadManger();

            return manager.enqueue(request);

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
        return -1;


    }

    public static boolean getUnInstallApkInfo(Context context, String filePath) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                result = true;//完整
            }
        } catch (Exception e) {
            result = false;//不完整
        }
        return result;
    }

    private static void OnDownloadStart(Context context, BaseAdUnit adUnit, String url, File file) {
        if (adUnit.isRecord()) {
            PointEntityUtils.eventRecord(PointCategory.DOWNLOAD_START, Constants.SUCCESS, adUnit);
        }

        HashMap<String, Object> ext = new HashMap<>();
        ext.put("result", Constants.SUCCESS);
        ext.put("downloadId", adUnit.getDownloadId());

        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_AD_DOWNLOAD_START);
    }

    private static void downloadAPK(String apkName, final String url, BaseAdUnit adUnit) {

        Context context = GtAdSdk.sharedAds().getContext();
        try {
            String mApkName = apkName;

            if (TextUtils.isEmpty(apkName)) {
                mApkName = Md5Util.md5(url) + ".apk";
            }
            /**
             * 检查当前文件是否在下载中
             */
            final File file = new File(GtFileUtil.getDownloadAPKPathFile(), mApkName);

            Long downloadid = isDownloading(-1, file.getAbsolutePath());
            if (downloadid != null && downloadid > 0) {
                try {
                    Toast.makeText(context, "正在下载", Toast.LENGTH_LONG).show();
                    if (adUnit.getDownloadId() == null) {
                        adUnit.setDownloadId(downloadid);

                        File downloadAPKLogFile = new File(GtFileUtil.getDownloadAPKLogPath(), downloadid + ".log");
                        if (downloadAPKLogFile.exists()) {
                            downloadAPKLogFile.delete();
                        }
                        FileUtil.writeToCache(adUnit, downloadAPKLogFile.getAbsolutePath());
                        OnDownloadStart(context, adUnit, url, file);
                    }
                } catch (Throwable ignored) {

                }
                return;
            }


            HashMap<String, Object> downloadedMap = getDownloadingTask();

            if (downloadedMap != null) {
                for (String filePath : downloadedMap.keySet()) {
                    if (!TextUtils.isEmpty(filePath) && filePath.equals(file.getAbsolutePath())) {
                        try {
                            Toast.makeText(context, "正在下载", Toast.LENGTH_LONG).show();
                        } catch (Throwable ignored) {

                        }
                        try {
                            Object downloadId = downloadedMap.get(filePath);
                            if (downloadId instanceof Long) {
                                adUnit.setDownloadId((Long) downloadId);

                                File downloadAPKLogFile = new File(GtFileUtil.getDownloadAPKLogPath(), downloadid + ".log");
                                if (downloadAPKLogFile.exists()) {
                                    downloadAPKLogFile.delete();
                                }
                                FileUtil.writeToCache(adUnit, downloadAPKLogFile.getAbsolutePath());
                                OnDownloadStart(context, adUnit, url, file);
                            }
                        } catch (Throwable ignored) {

                        }
                        return;
                    }
                }
            }
            /**
             * 判断文件是否存在并且有效
             */
            boolean containsKey = downloadMap.containsKey(adUnit.getUuid());

            if (file.exists() && !containsKey && !getUnInstallApkInfo(context, file.getAbsolutePath())) {//删除以前因为种种原因异常残留的缓存文件
                boolean deleteFile = FileUtil.deleteFile(file.getAbsolutePath());
                SigmobLog.i("cacheFile deleteFile:" + deleteFile);
            }

            SigmobLog.i(mApkName + " exists:" + file.exists() + " containsKey:" + containsKey);

            if (file.exists()) {
                if (containsKey) {
                    DownloadAPK.installAPK(context, file.getAbsolutePath(), adUnit);
                    return;
                } else {
                    long modified = file.lastModified();
                    int apk_expired_time = WindSDKConfig.getInstance().getApk_expired_time();
                    if (apk_expired_time == 0) {//默认过期
                        //删除文件
                        boolean deleteFile = FileUtil.deleteFile(file.getAbsolutePath());
                        SigmobLog.i("default deleteFile:" + deleteFile);
                    } else {
                        if ((System.currentTimeMillis() - modified) > (apk_expired_time * 1000L)) {//过期
                            //删除文件
                            boolean deleteFile = FileUtil.deleteFile(file.getAbsolutePath());
                            SigmobLog.i("timeOut deleteFile:" + deleteFile);
                        } else {
                            DownloadAPK.installAPK(context, file.getAbsolutePath(), adUnit);
                            return;
                        }
                    }
                }
            }

            downloadMap.put(adUnit.getUuid(), mApkName);
            String title = adUnit.getAppName();

            if (TextUtils.isEmpty(title)) {
                title = file.getName();
            }
            final long downloadId = downloadApk(url, file.getAbsolutePath(), title);

            if (downloadId >= 0) {
                try {
                    Toast.makeText(context, "已开始下载，可在通知栏尝试取消", Toast.LENGTH_LONG).show();
                } catch (Throwable throwable) {
                    SigmobLog.e(throwable.getMessage());
                }
                addDownloadItem(downloadId, file.getAbsolutePath());

                File downloadAPKLogFile = new File(GtFileUtil.getDownloadAPKLogPath(), downloadId + ".log");

                adUnit.setDownloadId(downloadId);
                FileUtil.writeToCache(adUnit, downloadAPKLogFile.getAbsolutePath());

                OnDownloadStart(context, adUnit, url, file);

            } else {
                if (adUnit.isRecord()) {
                    PointEntityUtils.eventRecord(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit);
                }

                Toast.makeText(context, "下载失败,错误码 " + downloadId, Toast.LENGTH_SHORT).show();
                HashMap<String, Object> ext = new HashMap<>();
                ext.put("result", Constants.FAIL);
                ext.put("downloadId", -1);

                BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_AD_DOWNLOAD_START);
            }

            if (adUnit.isRecord()) {
                SigmobTrackingRequest.sendTracking(adUnit, ADEvent.AD_DOWNLOAD_START);
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());

            HashMap<String, Object> ext = new HashMap<>();
            ext.put("result", Constants.FAIL);
            ext.put("downloadId", -1);

            BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_AD_DOWNLOAD_START);

            try {
                Toast.makeText(context, "请先给予应用权限", Toast.LENGTH_LONG).show();
            } catch (Throwable ignored) {
            }
        }
    }

    public static void addDownloadItem(long downloadId, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        downloadAPKList.add(new DownloadAPKItem(filePath, null, downloadId));
    }

    public static void removeDownloadItem(long downloadId, String filePath) {
        for (DownloadAPKItem item : downloadAPKList) {

            if (item.downloadId == downloadId || item.filePath.equalsIgnoreCase(filePath)) {
                downloadAPKList.remove(item);
                return;
            }
        }
    }

    /**
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
     *
     * @param downloadId
     * @return
     */
    public static long[] getDownloadIdBytesAndStatus(Context context, Long downloadId) {

        long[] bytesAndStatus = new long[]{-1, -1, 0};

        if (downloadId == null || downloadId < 0) return bytesAndStatus;

        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = null;
        try {
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            cursor = manager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                //已经下载文件大小
                bytesAndStatus[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                //下载文件的总大小
                bytesAndStatus[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                //下载状态
                bytesAndStatus[2] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bytesAndStatus;
    }


    public static Long isDownloading(long downloadId, String filePath) {

        for (DownloadAPKItem item : downloadAPKList) {
            if (item.downloadId == downloadId || item.filePath.equalsIgnoreCase(filePath)) {

                return item.downloadId;
            }
        }
        return null;
    }

    private static String getNotExistName(List<String> nameList, String name) {
        while (true) {
            if (!nameList.contains(name)) return name;

            Integer point = name.lastIndexOf(".");
            String fileName = "";
            String ext = "";
            if (point != -1) {
                fileName = name.substring(0, point);
                ext = name.substring(point);
            }
            Integer left = fileName.lastIndexOf("(");
            if (left == -1) {
                return getNotExistName(nameList, fileName + "(1)" + ext);
            }
            Integer right = fileName.indexOf(")", left);
            if (right == -1) {
                return getNotExistName(nameList, fileName + "(1)" + ext);
            }
            if (right < fileName.length() - 1)
                return getNotExistName(nameList, fileName + "(1)" + ext);
            ;
            String substring = fileName.substring(left + 1, right);
            try {
                Integer i = Integer.parseInt(substring) + 1;
                return getNotExistName(nameList, fileName.substring(0, left) + "(" + i + ")" + ext);

            } catch (Throwable e) {
                return getNotExistName(nameList, fileName + "(1)" + ext);
            }
        }
    }


    public static void downloadApk(final String url, final BaseAdUnit adUnit) {
        Context context = GtAdSdk.sharedAds().getContext();
        try {
            SigmobLog.i("download apk:" + url);

            AdStackManager.addAdUnit(adUnit);

            String apkMd5 = adUnit.getApkMd5();

            String apkName;

            if (!TextUtils.isEmpty(apkMd5)) {
                apkName = apkMd5 + ".apk";
            } else {
                apkName = Md5Util.md5(url) + ".apk";
            }
            adUnit.setApkName(apkName);
            adUnit.setDownloadUrl(url);

            int type = adUnit.getApkDownloadType();
            if (type == 0) {
                downloadAPK(apkName, url, adUnit);
            } else {
                SigmobLog.e("not support Download Type: " + type);
                throw new Exception("not support Download Type: " + type);
            }
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
            try {
                Toast.makeText(context, "下载失败", Toast.LENGTH_LONG).show();
            } catch (Throwable throwable) {
                SigmobLog.e(throwable.getMessage());
            }
        }

    }

    /**
     * 重点在这里
     */
    public static void installAPK(Context context, String filePath, BaseAdUnit adUnit) {

        File file = new File(filePath);

        SigmobLog.i("installAPK:" + file.getAbsolutePath() + ":" + file.exists());


        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        try {
            Toast.makeText(context, "开始安装", Toast.LENGTH_LONG).show();
        } catch (Throwable throwable) {
            SigmobLog.e(throwable.getMessage());
        }

        PackageInfo info = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));
            info = ClientMetadata.getPackageInfoWithUri(context, filePath);

        } else {
            Uri uriForFile = null;
            try {

                uriForFile = SigmobFileProvider.getUriForFile(context.getApplicationContext(), context.getApplicationContext().getPackageName() + ".sigprovider", file);
                if (uriForFile != null) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uriForFile, "application/vnd.android.package-archive");
                }
                info = ClientMetadata.getPackageInfoWithUri(context, filePath);

            } catch (Exception e) {
                SigmobLog.e(e.getMessage());
            }
        }

        if (info != null) {
            File downloadAPKLogFile = new File(GtFileUtil.getDownloadAPKLogPath(), info.packageName + ".log");

            try {
                FileUtil.writeToCache(adUnit, downloadAPKLogFile.getAbsolutePath());

                context.startActivity(intent);

                SigmobTrackingRequest.sendTracking(adUnit, ADEvent.AD_INSTALL_START);
                HashMap<String, Object> ext = new HashMap<>();
                ext.put("result", Constants.SUCCESS);
                BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_AD_INSTALL_START);
                if (adUnit.isRecord()) {
                    PointEntityUtils.eventRecord(PointCategory.APP_INSTALL_START, Constants.SUCCESS, adUnit);
                }
                return;
            } catch (Throwable e) {
                if (downloadAPKLogFile != null && downloadAPKLogFile.exists()) {
                    downloadAPKLogFile.delete();
                }
                SigmobLog.e("install apk fail", e);
            }

        }

        FileUtil.deleteFile(filePath);
        SigmobTrackingRequest.sendTracking(adUnit, ADEvent.AD_INSTALL_START);
        HashMap<String, Object> ext = new HashMap<>();
        ext.put("result", Constants.FAIL);
        BaseBroadcastReceiver.broadcastAction(context, adUnit.getUuid(), ext, IntentActions.ACTION_AD_INSTALL_START);
        if (adUnit.isRecord()) {
            PointEntityUtils.eventRecord(PointCategory.APP_INSTALL_START, Constants.FAIL, adUnit);
        }
    }

    public static String getMIMEType(File file) {
        String type = "";
        String var2 = file.getName();
        String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length()).toLowerCase();
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return type;
    }

    public static boolean isValidApk(File file) {
        if (file != null && file.exists()) {
            PackageInfo info = ClientMetadata.getPackageInfoWithUri(GtAdSdk.sharedAds().getContext(), file.getAbsolutePath());
            if (info != null) {
                return true;
            }
        }
        return false;
    }

    public static DownloadManager getDownloadManger() {
        Context context = GtAdSdk.sharedAds().getContext();
        if (context != null) {
            return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        return null;
    }

    public static boolean isDownloading(String downloadUrl) {

        if (TextUtils.isEmpty(downloadUrl)) return false;

        Cursor cursor = null;
        try {
            DownloadManager manager = getDownloadManger();
            if (manager != null) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);

                cursor = manager.query(query);
                if (!cursor.moveToFirst()) {
                    cursor.close();
                    return false;
                }

                List<String> fileNames = new ArrayList<>();

                while (true) {
                    String url = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_URI));
                    if (!TextUtils.isEmpty(url) && downloadUrl.equalsIgnoreCase(url)) {
                        return true;
                    }

                    if (!cursor.moveToNext()) break;
                }

                cursor.close();
                cursor = null;
                return false;
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    public static HashMap<String, Object> getDownloadingTask() {

        Cursor cursor = null;
        try {
            DownloadManager downloadManger = getDownloadManger();
            if (downloadManger != null) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);

                cursor = downloadManger.query(query);
                if (!cursor.moveToFirst()) {
                    cursor.close();
                    return null;
                }

                HashMap<String, Object> downloadMap = new HashMap<>();


                while (true) {
                    Long downloadId = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
                    String downloadFileLocalUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));

                    if (!TextUtils.isEmpty(downloadFileLocalUri))
                        downloadMap.put(downloadFileLocalUri, downloadId);
                    if (!cursor.moveToNext()) {
                        break;
                    }
                }

                cursor.close();
                cursor = null;
                return downloadMap;
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;

    }

    public static Map<String, Object> getDownloadInfoWithDownloadID(long downloadID) {

        Cursor cursor = null;
        try {
            DownloadManager manager = getDownloadManger();
            if (manager != null) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadID);

                Uri downloadUri = manager.getUriForDownloadedFile(downloadID);

                cursor = manager.query(query);
                if (!cursor.moveToFirst()) {
                    cursor.close();
                    return null;
                }
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                int reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));

                String downloadFileLocalUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                HashMap<String, Object> map = new HashMap();
                map.put("fileName", downloadFileLocalUri);
                map.put("status", status);
                map.put("uri", downloadUri);
                map.put("reason", reason);

                cursor.close();
                cursor = null;
                return map;
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    private static class DownloadAPKItem {
        String filePath;
        String downloadUrl;
        long downloadId;

        public DownloadAPKItem(String filePath, String downloadUrl, long downloadId) {
            this.filePath = filePath;
            this.downloadUrl = downloadUrl;
            this.downloadId = downloadId;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public long getDownloadId() {
            return downloadId;
        }
    }

}
