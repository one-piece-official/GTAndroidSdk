package com.sigmob.sdk.base.utils;


import android.content.Context;

import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.logger.SigmobLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public final class SigmobFileUtil {

    private static String cachePath = null;

    private static String cacheOutPath = null;
    private static File sigDownload = null;


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

    public static String getVideoCachePath() {
        String splashAdPath = cachePath + File.separator + "videoAd";
        File file = new File(splashAdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return splashAdPath;
    }

    public static String getWebCachePath(){
        String webCache = cachePath + File.separator + "webCache";
        File file = new File(webCache);
        if (!file.exists()) {
            file.mkdirs();
        }
        return webCache;
    }
    public static String getCacheOutPath() {
        return cacheOutPath;
    }


    public static String getSplashCachePath() {
        String splashAdPath = cachePath + File.separator + "splashAd";
        File file = new File(splashAdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return splashAdPath;
    }

    public static String getNativeCachePath() {
        String splashAdPath = cachePath + File.separator + "nativeAd";
        File file = new File(splashAdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return splashAdPath;
    }

    public static String getDownloadAPKLogPath() {
        String downloadAPKLogPath = cachePath + File.separator + "downloadAPKLog";
        File file = new File(downloadAPKLogPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return downloadAPKLogPath;
    }

    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        return lastIndexOfDot != -1 ? fileName.substring(lastIndexOfDot) : "";
    }
    public static String removeExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        return lastIndexOfDot != -1 ? fileName.substring(0, lastIndexOfDot) : fileName;
    }
    public static synchronized String getLogFilePath() {
        return cachePath + File.separator + "logger" + File.separator + "sdkLog.log";
    }

    public static synchronized String getSplashAdUnitFilePath(String placementId) {
        return cachePath + File.separator + "splashAdUnit" + File.separator + placementId;
    }

    public static File getDownloadAPKPathFile(Context context) {
        if (sigDownload == null) {
            sigDownload = new File(cacheOutPath, "SigDownload");
            if (!sigDownload.exists()) {
                sigDownload.mkdirs();
            }
        }
        return sigDownload;
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

    /**
     * 获取项目SigHtmlResource目录
     *
     * @return
     */
    public static File getSigHtmlDir(String fileDir) {
        File dir = new File(cacheOutPath, fileDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String SigHtmlResourceDir = "SigHtmlResource";
    public static String SigHtmlPrivacyDir = "SigHtmlPrivacy";

    public static String SigZipResourceDir = "SigZipResource";


    public static String SigCrashResourceDir = "SigCrashResource";

    /**
     * 获取项目SigZipResourceDir目录
     *
     * @return
     */
    public static File getSigZipDir(String fileDir) {
        File dir = new File(cacheOutPath, fileDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
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


    public static File dataToFile(String data, String fileName) {//主线程被调用
        File file = null;
        try {
            File dir = new File(cacheOutPath, SigmobFileUtil.SigHtmlResourceDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }
            File destFile = new File(dir, fileName);
            SigmobLog.d("SigHtmlResource: " + destFile.getAbsolutePath());
            if (destFile.exists()) {
                destFile.delete();
            }
            destFile.createNewFile();
            FileWriter fw = new FileWriter(destFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            //写入相关data到文件
            bw.write(data);
            bw.newLine();
            bw.close();
            fw.close();
            file = destFile;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File createCrashPath() {
        File file = null;
        try {
            File dir = new File(cacheOutPath, SigmobFileUtil.SigCrashResourceDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(dir, String.format("%d.log", System.currentTimeMillis() / 1000));

        } catch (Throwable e) {
            SigmobLog.d("createCrash fail", e);
        }
        return file;
    }

    public static File[] getCrashFiles() {
        try {
            File dir = new File(cacheOutPath, SigmobFileUtil.SigCrashResourceDir);

            if (!dir.exists()) {
                dir.mkdirs();
                return null;
            }

            return FileUtil.orderByDate(dir.getAbsolutePath());

        } catch (Throwable e) {

        }
        return null;
    }
}
