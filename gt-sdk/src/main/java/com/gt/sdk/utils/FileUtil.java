package com.gt.sdk.utils;

import android.content.Context;
import android.text.TextUtils;

import com.czhj.sdk.logger.SigmobLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Comparator;

public final class FileUtil {

    private static String cachePath = null;

    private static String cacheOutPath = null;


    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        try {
            // 如果dir不以文件分隔符结尾，自动添加文件分隔符
            if (!dir.endsWith(File.separator))
                dir = dir + File.separator;
            File dirFile = new File(dir);
            // 如果dir对应的文件不存在，或者不是一个目录，则退出
            if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
                SigmobLog.d("删除目录失败：" + dir + "不存在！");
                return false;
            }
            boolean flag = true;
            // 删除文件夹中的所有文件包括子目录
            File[] files = dirFile.listFiles();
            for (File file : files) {
                // 删除子文件
                if (file.isFile()) {
                    flag = deleteFile(file.getAbsolutePath());
                    if (!flag)
                        break;
                }
                // 删除子目录
                else if (file.isDirectory()) {
                    flag = deleteDirectory(file.getAbsolutePath());
                    if (!flag)
                        break;
                }
            }
            if (!flag) {
                return false;
            }
            // 删除当前目录
            if (dirFile.delete()) {
                SigmobLog.d("删除目录" + dir + "成功！");
                return true;
            } else {
                return false;
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

        return false;

    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        try {
            SecurityManager checker = new SecurityManager();

            checker.checkDelete(fileName);
            File file = new File(fileName);
            // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    SigmobLog.d("删除单个文件" + fileName + "成功！");
                    return true;
                } else {
                    SigmobLog.d("删除单个文件" + fileName + "失败！");
                    return false;
                }
            } else {
                SigmobLog.d("删除单个文件失败：" + fileName + "不存在！");
                return false;
            }
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
        return false;
    }

    /**
     * 读取指定文件的输出
     *
     * @param path
     */
    public static String readFileToString(File path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path), 8192);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append("\n").append(line);
            }
            bufferedReader.close();
            return sb.toString();
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
        return null;
    }

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

    public static String getStrategyCachePath() {
        if (!TextUtils.isEmpty(cachePath)) {
            File folder = new File(cachePath, "strategy");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            return folder.getAbsolutePath();
        }
        return cachePath;
    }

    public static String getCacheOutPath() {
        return cacheOutPath;
    }

    public static File[] orderByDate(String filePath) {
        File file = new File(filePath);
        File[] fs = file.listFiles();
        if (fs != null) {
            Arrays.sort(fs, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0)
                        return 1;
                    else if (diff == 0)
                        return 0;
                    else
                        return -1;
                }

                public boolean equals(Object obj) {
                    return true;
                }

            });
        }

        return fs;
    }

    public static void clearCache() {
        if (cachePath != null) {
            File file = new File(cachePath);
            if (file.exists()) {
                deleteDirectory(cachePath);
            }
            if (!file.exists() || file.isFile()) {
                file.mkdirs();
            }
        }
    }
}
