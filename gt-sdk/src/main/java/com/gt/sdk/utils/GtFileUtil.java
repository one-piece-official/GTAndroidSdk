package com.gt.sdk.utils;


import android.content.Context;

import com.czhj.sdk.logger.SigmobLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public final class GtFileUtil {

    private static String cachePath = null;
    private static String cacheOutPath = null;
    private static File apkDownload = null;

    /**
     * 创建App文件夹
     *
     * @param context
     * @param folderName
     * @return
     */
    public static void initSDKCacheFolder(Context context, String folderName) {
        File folder = context.getCacheDir();
        File outFolder = context.getExternalCacheDir();

        if (folderName != null) {
            folder = new File(folder, folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            outFolder = new File(outFolder, folderName);
            if (!outFolder.exists()) {
                outFolder.mkdirs();
            }
        }
        cachePath = folder.getAbsolutePath();
        cacheOutPath = outFolder.getAbsolutePath();
    }

    public static String getCachePath() {
        return cachePath;
    }

    public static String getCacheOutPath() {
        return cacheOutPath;
    }

    public static String getVideoCachePath() {
        String splashAdPath = cachePath + File.separator + "video";
        File file = new File(splashAdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return splashAdPath;
    }

    public static String getSplashCachePath() {
        String splashAdPath = cachePath + File.separator + "splash";
        File file = new File(splashAdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return splashAdPath;
    }

    public static String getNativeCachePath() {
        String splashAdPath = cachePath + File.separator + "native";
        File file = new File(splashAdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return splashAdPath;
    }

    public static File getDownloadAPKPathFile() {
        if (apkDownload == null) {
            apkDownload = new File(getCacheOutPath(), "apk");
            if (!apkDownload.exists()) {
                apkDownload.mkdirs();
            }
        }
        return apkDownload;
    }

    public static String getDownloadAPKLogPath() {
        String downloadAPKLogPath = cachePath + File.separator + "downloadLog";
        File file = new File(downloadAPKLogPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return downloadAPKLogPath;
    }

    public static File getPrivacyHtmlDir() {
        File dir = new File(cacheOutPath, "privacy");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getWebCachePath() {
        String webCache = cachePath + File.separator + "webCache";
        File file = new File(webCache);
        if (!file.exists()) {
            file.mkdirs();
        }
        return webCache;
    }

    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        return lastIndexOfDot != -1 ? fileName.substring(lastIndexOfDot) : "";
    }

    public static String removeExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        return lastIndexOfDot != -1 ? fileName.substring(0, lastIndexOfDot) : fileName;
    }


    public static void clearCache() {
        if (cachePath != null) {
            File file = new File(cachePath);
            if (file.exists()) {
                com.czhj.sdk.common.utils.FileUtil.deleteDirectory(cachePath);
            }
            if (!file.exists() || file.isFile()) {
                file.mkdirs();
            }
        }
    }

    public static File[] clearCacheFileByCount(File[] files, int count) {
        if (files == null || files.length == 0) return null;
        ArrayList<File> fileList = new ArrayList<>(Arrays.asList(files));

        for (int i = 0; i < files.length; i++) {
            if (fileList.size() <= count) {
                break;
            }

            File file = files[i];
            if (file.exists()) {
                file.delete();
                fileList.remove(file);
                SigmobLog.d("file delete " + file.getName());
            }
        }

        return fileList.toArray(new File[0]);
    }

}
