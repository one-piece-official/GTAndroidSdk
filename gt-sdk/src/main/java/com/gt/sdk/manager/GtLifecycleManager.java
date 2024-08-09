package com.gt.sdk.manager;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import com.czhj.sdk.common.json.JSONSerializer;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.base.activity.AdActivity;
import com.gt.sdk.base.models.point.PointCategory;
import com.gt.sdk.base.models.point.GtPointEntityActive;
import com.gt.sdk.base.models.point.PointType;
import com.gt.sdk.utils.WMLogUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * created by lance on   2022/4/6 : 2:09 下午
 */
public class GtLifecycleManager implements AdLifecycleManager.LifecycleListener {

    private static GtLifecycleManager gInstance;

    private int mActivityCount = 0;
    private boolean isAppAlive = true;  //judge is app alive;
    private boolean isSwitchActivity = false;  // judge is switch activity failFrom top to other in the stack of activity
    private boolean isAppExit = false;  //some times app have cleard the stack of activity but app is not exit . this boolean can help to static realive;
    private String topActivity;
    private Map<String, String> map = new HashMap<>();
    private long session_start = 0;
    private String active_id;
    private boolean isAdShow;

    private static WeakReference<Activity> lastActivity;

    public static Activity getLastActivity() {
        if (lastActivity != null) {
            return lastActivity.get();
        }
        return null;
    }

    private GtLifecycleManager(Application application) {
        try {
            AdLifecycleManager.getInstance().initialize(application);
            AdLifecycleManager.getInstance().addLifecycleListener(this);
            session_start = System.currentTimeMillis();
            active_id = UUID.randomUUID().toString();
            //session_start打点
            WMLogUtil.i("session_start: " + session_start + ":" + active_id);
            activeTracking(PointCategory.SESSION_START, active_id, "0", String.valueOf(session_start));
        } catch (Throwable e) {
            WMLogUtil.e(e.getMessage());
        }
    }

    public static GtLifecycleManager initialize(Application application) {
        if (gInstance == null) {
            synchronized (GtLifecycleManager.class) {
                if (gInstance == null) {
                    gInstance = new GtLifecycleManager(application);
                }
            }
        }
        return gInstance;
    }

    @Override
    public void onCreate(Activity activity) {
        topActivity = activity.getClass().getSimpleName();
        map.put(topActivity, topActivity);
        isAppAlive = true;
        isSwitchActivity = false;
    }

    @Override
    public void onStart(Activity activity) {
        mActivityCount++;
    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {
        if (activity instanceof AdActivity) {
            isAdShow = true;
        } else {
            if (!isAdShow) {
                lastActivity = new WeakReference<>(activity);
            }
        }

        isSwitchActivity = !activity.getClass().getSimpleName().equals(topActivity);
        topActivity = activity.getClass().getSimpleName();
        if (!isAppAlive || isAppExit) {
            isAppExit = false;
            //app进入活动状态
            active_id = UUID.randomUUID().toString();
            session_start = System.currentTimeMillis();
            isAppAlive = true;
            WMLogUtil.i("onActivityResumed session_start: " + session_start + ":" + active_id);
            activeTracking(PointCategory.SESSION_START, active_id, "0", String.valueOf(session_start));
        }
    }

    @Override
    public void onStop(Activity activity) {
        mActivityCount--;
        if (activity.getClass().getSimpleName().equals(topActivity)) {
            if (!isSwitchActivity || map.size() == 1) {
                long session_end = System.currentTimeMillis();
                long duration = (session_end - session_start) / 1000;
                //用户切换到后台
                WMLogUtil.i("onActivityStopped session_end: " + session_end + ":" + active_id + ":" + duration);
                activeTracking(PointCategory.SESSION_END, active_id, String.valueOf(duration), String.valueOf(session_end));
                session_start = System.currentTimeMillis();
                isAppAlive = false;
            }
        }
    }

    @Override
    public void onDestroy(Activity activity) {
        if (activity instanceof AdActivity) {
            isAdShow = false;
        }
        map.remove(activity.getClass().getSimpleName());
        if (map.isEmpty() && isAppAlive) {
            long session_end = System.currentTimeMillis();
            long duration = (session_end - session_start) / 1000;
            //用户切换到后台
            WMLogUtil.i("onActivityDestroyed session_end: " + session_end + ":" + active_id + ":" + duration);
            activeTracking(PointCategory.SESSION_END, active_id, String.valueOf(duration), String.valueOf(session_end));
            session_start = System.currentTimeMillis();
            isAppAlive = false;
        }
        if (map.isEmpty()) {
            isAppExit = true;
        }
    }

    public void activeTracking(String category, String active_id, String duration, String active_time) {
        GtPointEntityActive pointEntityActive = new GtPointEntityActive();
        pointEntityActive.setAc_type(PointType.GT_ACTIVE);
        pointEntityActive.setCategory(category);
        pointEntityActive.setActive_id(active_id);
        pointEntityActive.setDuration(duration);
        pointEntityActive.setTimestamp(active_time);
        try {
            if (GtAdSdk.sharedAds().getCustomData() != null && !GtAdSdk.sharedAds().getCustomData().isEmpty()) {
                String custom_info = JSONSerializer.Serialize(GtAdSdk.sharedAds().getCustomData());
                if (!TextUtils.isEmpty(custom_info)) {
                    Map<String, String> options = new HashMap<>();
                    options.put("custom_data", custom_info);
                    pointEntityActive.setOptions(options);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pointEntityActive.commit();
    }

    public int getActivityCount() {
        return mActivityCount;
    }
}
