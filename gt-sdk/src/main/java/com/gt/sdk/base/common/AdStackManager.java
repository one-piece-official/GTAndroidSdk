package com.gt.sdk.base.common;


import android.text.TextUtils;

import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.common.utils.Md5Util;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.toolbox.DownloadItem;
import com.czhj.volley.toolbox.FileDownloadRequest;
import com.czhj.volley.toolbox.FileDownloader;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.base.videocache.HttpProxyCacheServer;
import com.gt.sdk.manager.WindSDKConfig;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.utils.GtFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class AdStackManager {

    private static AdStackManager gShareInstance = null;
    private static final Map<String, BaseAdUnit> AdUnitMap = new HashMap<>();
    private static volatile ImageManager mImageManager;
    private static BaseAdUnit clickAdUnit;
    private static HttpProxyCacheServer proxyCacheServer;

    private AdStackManager() {

    }

    public static synchronized HttpProxyCacheServer getHttProxyCacheServer() {
        if (proxyCacheServer == null) {
            initHttpProxyCacheServer();
        }
        return proxyCacheServer;
    }

    public static synchronized void initHttpProxyCacheServer() {
        HttpProxyCacheServer.Builder builder = new HttpProxyCacheServer.Builder(GtAdSdk.sharedAds().getContext());
        try {
            File file = new File(GtFileUtil.getVideoCachePath());
            builder.cacheDirectory(file);
        } catch (Throwable e) {
            SigmobLog.e("initHttpProxyCacheServer fail ", e);
        }

        proxyCacheServer = builder.build();
    }

    public static synchronized AdStackManager shareInstance() {
        if (gShareInstance == null) {
            gShareInstance = new AdStackManager();
        }
        return gShareInstance;
    }

    public static synchronized ImageManager getImageManger() {
        if (mImageManager == null) {
            File file = new File(GtFileUtil.getNativeCachePath());
            synchronized (ImageManager.class) {
                if (mImageManager == null) {
                    mImageManager = new ImageManager(GtAdSdk.sharedAds().getContext()).customCachePath(file);
                }
            }
        }
        return mImageManager;
    }

    public static void setPlayAdUnit(final BaseAdUnit adUnit) {
        addAdUnit(adUnit);
    }

    public static void addAdUnit(final BaseAdUnit adUnit) {
        if (adUnit != null && !TextUtils.isEmpty(adUnit.getUuid())) {
            AdUnitMap.put(adUnit.getUuid(), adUnit);
        }
    }

    public static void cleanPlayAdUnit(BaseAdUnit adUnit) {
        if (adUnit != null && !TextUtils.isEmpty(adUnit.getUuid())) {
            AdUnitMap.remove(adUnit.getUuid());
        }
    }

    public static BaseAdUnit getPlayAdUnit(String uuid) {
        return AdUnitMap.get(uuid);
    }


    public static BaseAdUnit getClickAdUnit() {
        return clickAdUnit;
    }

    public static void setClickAdUnit(BaseAdUnit adUnit) {
        clickAdUnit = adUnit;
    }

    public static void clearSplashAd() {
        try {
            File[] files = FileUtil.orderByDate(GtFileUtil.getSplashCachePath());
            files = clearCacheFileByDate(files, System.currentTimeMillis(), WindSDKConfig.getInstance().getAdExpiredTime());
            files = FileUtil.clearCacheFileByCount(files, WindSDKConfig.getInstance().getSplashCacheTop());
            if (files == null) {
                SigmobLog.i("splash ad file list is null");
            } else {
                SigmobLog.i("splash ad file remain num: " + files.length);
            }
        } catch (Throwable throwable) {
            SigmobLog.e("clean splash ad file error", throwable);
        }
    }

    public static void clearVideoAdCache() {
        try {
            File[] files = FileUtil.orderByDate(GtFileUtil.getVideoCachePath());

            files = FileUtil.clearCacheFileByCount(files, WindSDKConfig.getInstance().getRvCacheTop());

            if (files == null) {
                SigmobLog.i("splash ad file list is null");
            } else {
                SigmobLog.i("splash ad file remain num: " + files.length);
            }
        } catch (Throwable throwable) {

            SigmobLog.e("clean splash ad file error", throwable);
        }
    }

    public static void clearNativeAdCache() {
        try {
            File[] files = FileUtil.orderByDate(GtFileUtil.getNativeCachePath());

            files = FileUtil.clearCacheFileByCount(files, WindSDKConfig.getInstance().getNativeAdCacheTop());

            if (files == null) {
                SigmobLog.i("native ad file list is null");
            } else {
                SigmobLog.i("native ad file remain num: " + files.length);
            }
        } catch (Throwable throwable) {
            SigmobLog.e("clean native ad file error", throwable);
        }
    }

    public static void clearDownloadAPK() {
        try {
            File[] files = FileUtil.orderByDate(GtFileUtil.getDownloadAPKPathFile().getAbsolutePath());
            //然后删除7天过期文件
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if ((System.currentTimeMillis() - file.lastModified()) > 1000 * 60 * 60 * 24 * 7) {
                        boolean delete = file.delete();
                        SigmobLog.i("Download APK files timeOut seven day and delete:" + file.getName() + ":" + delete);
                    }
                }
            }

        } catch (Throwable throwable) {

            SigmobLog.e("clean Download APK file error", throwable);
        }
    }

    public static File[] clearCacheFileByDate(File[] files, long currentTime, long time) {

        if (files == null || files.length == 0) return null;

        ArrayList<File> fileList = new ArrayList<>(Arrays.asList(files));
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            long before = currentTime - time;
            if (file.exists() && before > file.lastModified()) {
                file.delete();
                fileList.remove(file);
                SigmobLog.d("file delete " + file.getName());
            }
        }
        return fileList.toArray(new File[0]);
    }

    private void AdCache(final BaseAdUnit adUnit, final AdStackStatusListener adStackStatusListener) {
        if (adUnit != null) {
            //下载隐私协议模板
            String privacy_template_url = WindSDKConfig.getInstance().getPrivacyUrl();
            if (!TextUtils.isEmpty(privacy_template_url)) {
                String fileName = Md5Util.md5(privacy_template_url);
                File sigHtmlDir = GtFileUtil.getPrivacyHtmlDir();
                File destFile = new File(sigHtmlDir, fileName + ".html");
                if (!destFile.exists()) {//不存在就要下载
                    //然后再去下载新的文件
                    DownloadItem downloadItem = new DownloadItem();
                    downloadItem.url = privacy_template_url;
                    downloadItem.filePath = destFile.getAbsolutePath();
                    downloadItem.type = DownloadItem.FileType.OTHER;
                    FileDownloader downloader = DownloaderFactory.getDownloader();
                    downloader.add(downloadItem, new FileDownloadRequest.FileDownloadListener() {
                        @Override
                        public void onSuccess(DownloadItem item) {
                            SigmobLog.i("onPostExecute onSuccess:" + item.url);
                        }

                        @Override
                        public void onCancel(DownloadItem item) {
                            SigmobLog.i("onPostExecute onCancel:" + item.url);
                        }


                        @Override
                        public void onErrorResponse(DownloadItem item) {
                            SigmobLog.i("onPostExecute onErrorResponse:" + item.url);
                        }

                        @Override
                        public void downloadProgress(DownloadItem item, long totalSize, long readSize) {

                        }
                    });
                } else {
                    SigmobLog.i("privacy_template_url:" + fileName + " is exists");
                }
            }

            File splashAdFile = new File(adUnit.getSplashFilePath());
            if (splashAdFile.exists()) {
                splashAdFile.setLastModified(System.currentTimeMillis());
                if (adStackStatusListener != null) {
                    adStackStatusListener.loadEnd(adUnit, null);
                }
            } else {
                FileDownloader downloader = DownloaderFactory.getDownloader();
                DownloadItem item = new DownloadItem();
                item.filePath = adUnit.getSplashFilePath();
                item.url = adUnit.getSplashURL();
                item.type = DownloadItem.FileType.OTHER;
                item.userRange = false;
                downloader.add(item, new FileDownloadRequest.FileDownloadListener() {
                    @Override
                    public void onSuccess(DownloadItem item) {
                        if (adStackStatusListener != null) {
                            adStackStatusListener.loadEnd(adUnit, null);
                        }
                    }

                    @Override
                    public void onCancel(DownloadItem item) {

                    }

                    @Override
                    public void onErrorResponse(DownloadItem item) {
                        String error = item.error.getMessage();
                        if (item.error.networkResponse != null) {
                            error = error + " status code " + item.error.networkResponse.statusCode;
                        }
                        if (adStackStatusListener != null) {
                            adStackStatusListener.loadEnd(adUnit, error);
                        }
                        SigmobLog.e("onErrorResponse: " + error);
                    }

                    @Override
                    public void downloadProgress(DownloadItem item, long totalSize, long readSize) {

                    }
                });
            }
        }
    }

    public void cache(final BaseAdUnit adUnit, AdStackStatusListener adStackStatusListener) {
        if (adStackStatusListener != null) {
            adStackStatusListener.loadStart(adUnit);
        }

        if (adUnit == null) {
            SigmobLog.e("adUnit is null");
            if (adStackStatusListener != null) {
                adStackStatusListener.loadEnd(null, "adUnit is null");
            }
            return;
        }

        AdCache(adUnit, adStackStatusListener);
    }

    public interface AdStackStatusListener {

        void loadStart(BaseAdUnit adUnit);

        void loadEnd(BaseAdUnit adUnit, String message);
    }

}
