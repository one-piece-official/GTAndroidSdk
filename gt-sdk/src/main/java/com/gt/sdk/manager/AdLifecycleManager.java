package com.gt.sdk.manager;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.logger.SigmobLog;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class AdLifecycleManager {

    private final Set<WeakReference<LifecycleListener>> mLifecycleListeners;

    private static AdLifecycleManager gInstance;

    private boolean isInit;

    AdLifecycleManager() {
        mLifecycleListeners = new HashSet<>();
    }

    public void initialize(final Application application) {
        try {

            if (isInit) {
                return;
            }

            activityCallBack(application);

            isInit = true;

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
    }


    public static AdLifecycleManager getInstance() {

        if (gInstance == null) {
            synchronized (AdLifecycleManager.class) {
                if (gInstance == null) {
                    gInstance = new AdLifecycleManager();
                }
            }
        }
        return gInstance;
    }

    private void activityCallBack(final Application application) {

        if (application == null) {
            SigmobLog.e("activityCallBack error, application is null");
            return;
        }
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                SigmobLog.d("onActivityCreated() called with: activity = [" + activity + "], savedInstanceState = [" + savedInstanceState + "]");
                onCreate(activity);

            }

            @Override
            public void onActivityStarted(Activity activity) {
                SigmobLog.d("onActivityStarted() called with: activity = [" + activity + "]");
                onStart(activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                SigmobLog.d("onActivityResumed() called with: activity = [" + activity + "]");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    WindowInsets windowInsets = activity.getWindow().getDecorView().getRootWindowInsets();

                    ClientMetadata.getInstance().setWindInsets(windowInsets);
                }

                onResume(activity);


            }

            @Override
            public void onActivityPaused(Activity activity) {
                SigmobLog.d("onActivityPaused() called with: activity = [" + activity + "]");
                onPause(activity);

            }

            @Override
            public void onActivityStopped(Activity activity) {
                SigmobLog.d("onActivityStopped() called with: activity = [" + activity + "]");
                onStop(activity);


            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                SigmobLog.d("onActivitySaveInstanceState() called with: activity = [" + activity + "], outState = [" + outState + "]");

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                SigmobLog.d("onActivityDestroyed() called with: activity = [" + activity + "]");
                onDestroy(activity);

            }
        });
    }

    public void addLifecycleListener(LifecycleListener listener) {
        try {
            // Get the instance or bail if not initialized.
            if (listener == null) {
                return;
            }

            if (!isContains(listener)) {
                mLifecycleListeners.add(new WeakReference<>(listener));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isContains(LifecycleListener listener) {
        try {
            CopyOnWriteArraySet<WeakReference<LifecycleListener>> tempList = new CopyOnWriteArraySet<>(mLifecycleListeners);
            for (WeakReference<LifecycleListener> listenerWeakReference : tempList) {
                if (listenerWeakReference.get() == listener) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private WeakReference<LifecycleListener> getLifecycleListenerWeakReference(LifecycleListener listener) {
        CopyOnWriteArraySet<WeakReference<LifecycleListener>> tempList = new CopyOnWriteArraySet<>(mLifecycleListeners);
        for (WeakReference<LifecycleListener> listenerWeakReference : tempList) {
            if (listenerWeakReference.get() == listener) {
                return listenerWeakReference;
            }
        }
        return null;
    }

    public void removeLifecycleListener(LifecycleListener listener) {
        // Get the instance or bail if not initialized.
        try {
            if (listener == null) {
                return;
            }

            Iterator<WeakReference<LifecycleListener>> iterator = mLifecycleListeners.iterator();
            while (iterator.hasNext()) {
                WeakReference<LifecycleListener> listenerWeakReference = iterator.next();
                if (listenerWeakReference.get() == listener) {
                    iterator.remove(); // 使用迭代器自身的remove方法
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onCreate(final Activity activity) {
        try {
            CopyOnWriteArraySet<WeakReference<LifecycleListener>> tempList = new CopyOnWriteArraySet<>(mLifecycleListeners);
            for (WeakReference<LifecycleListener> weakReference : tempList) {
                LifecycleListener listener = weakReference.get();
                if (listener != null) {
                    listener.onCreate(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onStart(final Activity activity) {
        try {
            CopyOnWriteArraySet<WeakReference<LifecycleListener>> tempList = new CopyOnWriteArraySet<>(mLifecycleListeners);
            for (WeakReference<LifecycleListener> weakReference : tempList) {
                LifecycleListener listener = weakReference.get();
                if (listener != null) {
                    listener.onStart(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onPause(final Activity activity) {
        try {
            CopyOnWriteArraySet<WeakReference<LifecycleListener>> tempList = new CopyOnWriteArraySet<>(mLifecycleListeners);
            for (WeakReference<LifecycleListener> weakReference : tempList) {
                LifecycleListener listener = weakReference.get();
                if (listener != null) {
                    listener.onPause(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onResume(final Activity activity) {
        try {
            CopyOnWriteArraySet<WeakReference<LifecycleListener>> tempList = new CopyOnWriteArraySet<>(mLifecycleListeners);
            for (WeakReference<LifecycleListener> weakReference : tempList) {
                LifecycleListener listener = weakReference.get();
                if (listener != null) {
                    listener.onResume(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onStop(final Activity activity) {
        try {
            CopyOnWriteArraySet<WeakReference<LifecycleListener>> tempList = new CopyOnWriteArraySet<>(mLifecycleListeners);
            for (WeakReference<LifecycleListener> weakReference : tempList) {
                LifecycleListener listener = weakReference.get();
                if (listener != null) {
                    listener.onStop(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onDestroy(final Activity activity) {
        try {
            CopyOnWriteArraySet<WeakReference<LifecycleListener>> tempList = new CopyOnWriteArraySet<>(mLifecycleListeners);
            for (WeakReference<LifecycleListener> weakReference : tempList) {
                LifecycleListener listener = weakReference.get();
                if (listener != null) {
                    listener.onDestroy(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface LifecycleListener {

        void onCreate(Activity activity);

        void onStart(Activity activity);

        void onPause(Activity activity);

        void onResume(Activity activity);

        void onStop(Activity activity);

        void onDestroy(Activity activity);
    }
}
