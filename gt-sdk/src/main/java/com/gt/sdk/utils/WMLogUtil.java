package com.gt.sdk.utils;

import android.util.Log;

/**
 * Create by lance on 2020/1/2/0002
 */
public class WMLogUtil {

    public static String TAG = "Happy";
    /**
     * 是否是调试版本
     */
    private static final boolean IS_DEBUG = true;

    public static boolean isEnableLog = true;

    public static void d(String TAG, String msg) {
        if (IS_DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String TAG, String msg) {
        if (IS_DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void e(String TAG, String msg) {
        if (IS_DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (isEnableLog) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (isEnableLog) {
            Log.i(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (isEnableLog) {
            Log.e(TAG, msg);
        }
    }

    public static void log(String tag, String msg) {
        if (isEnableLog) {
            Log.d(tag, msg);
        }
    }

    /**
     * 截断输出日志
     *
     * @param msg
     */
    public static void dd(String tag, String msg) {
        if (IS_DEBUG) {
            if (tag == null || tag.length() == 0
                    || msg == null || msg.length() == 0)
                return;
            int segmentSize = 3 * 1024;
            long length = msg.length();
            if (length <= segmentSize) {// 长度小于等于限制直接打印
                Log.d(tag, msg);
            } else {
                while (msg.length() > segmentSize) {// 循环分段打印日志
                    String logContent = msg.substring(0, segmentSize);
                    msg = msg.replace(logContent, "");
                    Log.d(tag, logContent);
                }
                Log.d(tag, msg);// 打印剩余日志
            }
        }
    }
}
