package com.gt.sdk.base.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Locale;

public class SigUtils {

    public static int SYSTEM_UI = 0;

    public static String stringForTime(long timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = timeMs / 1000;
        int seconds = (int) (totalSeconds % 60);
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours = (int) (totalSeconds / 3600);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * This method requires the caller to hold the permission ACCESS_NETWORK_STATE.
     *
     * @param context context
     * @return if wifi is connected,return true
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Get activity from context object
     *
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    public static Activity scanForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    public static void setRequestedOrientation(Context context, int orientation) {
        Activity activity = SigUtils.scanForActivity(context);
        if (activity != null) {
            activity.setRequestedOrientation(orientation);
        }
    }

    public static Window getWindow(Context context) {
        Activity activity = SigUtils.scanForActivity(context);
        if (activity != null) {
            return activity.getWindow();
        }
        return null;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static void showStatusBar(Context context) {
        if (VideoAdView.TOOL_BAR_EXIST) {
            Window window = SigUtils.getWindow(context);
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    public static int getNavigationBarHeight(Activity activity) {
        if (isNavigationBarShow(activity)) {
            Resources resources = activity.getResources();
            if (resources == null) {
                return 0;
            }
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId == 0) {
                return 0;
            }
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;

    }


    //如果是沉浸式的，全屏前就没有状态栏
    public static void hideStatusBar(Context context) {
        if (VideoAdView.TOOL_BAR_EXIST) {
            Window window = SigUtils.getWindow(context);
            if (window != null) {
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    private static void hideSystemUI2(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // SigmobAndroid 5.0 以上 全透明
            Window window = SigUtils.getWindow(context);
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                int optionflags = window.getDecorView().getSystemUiVisibility();
                window.getDecorView().setSystemUiVisibility(optionflags | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                // 状态栏（以上几行代码必须，参考setStatusBarColor|setNavigationBarColor方法源码）
                window.setStatusBarColor(Color.TRANSPARENT);
                // 虚拟导航键
                window.setNavigationBarColor(Color.TRANSPARENT);
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // SigmobAndroid 4.4 以上 半透明
            Window window = SigUtils.getWindow(context);
            if (window != null) {
                // 状态栏
                //            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                // 虚拟导航键
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }
    }

    public static void hideSystemUI(Context context) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        Window window = SigUtils.getWindow(context);
        if (window != null) {
            SYSTEM_UI = window.getDecorView().getSystemUiVisibility();
            window.getDecorView().setSystemUiVisibility(uiOptions);
        }

    }

    public static void showSystemUI(Context context) {
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        Window window = SigUtils.getWindow(context);
        if (window != null) {
            window.getDecorView().setSystemUiVisibility(SYSTEM_UI);
        }
    }

    public static boolean isNavigationBarShow(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        } else {

            try {
                Class<?> clazz = Class.forName("com.android.internal.R$dimen");

                Object obj = clazz.newInstance();

                int height = Integer.parseInt(clazz.getField("status_bar_height").get(obj).toString());

                result = context.getResources().getDimensionPixelSize(height);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * java.lang.IllegalStateException: Only fullscreen opaque activities can request orientation
     * 修复android 8.0存在的问题
     * 在Activity中onCreate()中super之前调用
     *
     * @param activity
     */
    public static void hookOrientation(Activity activity) {
        //目标版本8.0及其以上
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating(activity)) {
            fixOrientation(activity);
        }
    }

    /**
     * 设置屏幕不固定，绕过检查
     *
     * @param activity
     */
    private static void fixOrientation(Activity activity) {
        try {
            Class<Activity> activityClass = Activity.class;
            Field mActivityInfoField = activityClass.getDeclaredField("mActivityInfo");
            mActivityInfoField.setAccessible(true);
            ActivityInfo activityInfo = (ActivityInfo) mActivityInfoField.get(activity);
            //设置屏幕不固定
            activityInfo.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查屏幕 横竖屏或者锁定就是固定
     *
     * @param activity
     * @return
     */
    public static boolean isTranslucentOrFloating(Activity activity) {
        boolean isTranslucentOrFloating = false;
        try {
            Class<?> styleableClass = Class.forName("com.android.internal.R$styleable");
            Field WindowField = styleableClass.getDeclaredField("Window");
            WindowField.setAccessible(true);
            int[] styleableRes = (int[]) WindowField.get(null);
            //先获取到TypedArray
            final TypedArray typedArray = activity.obtainStyledAttributes(styleableRes);
            Class<?> ActivityInfoClass = ActivityInfo.class;
            //调用检查是否屏幕旋转
            Method isTranslucentOrFloatingMethod = ActivityInfoClass.getDeclaredMethod("isTranslucentOrFloating", TypedArray.class);
            isTranslucentOrFloatingMethod.setAccessible(true);
            isTranslucentOrFloating = (boolean) isTranslucentOrFloatingMethod.invoke(null, typedArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }
}
