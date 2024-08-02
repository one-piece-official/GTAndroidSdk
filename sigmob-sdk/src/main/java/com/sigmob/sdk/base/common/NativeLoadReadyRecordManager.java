package com.sigmob.sdk.base.common;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.HashMap;
import java.util.Iterator;

public class NativeLoadReadyRecordManager {

    private static final String TAG = "NativeLoadReadyRecordManager";
    private boolean isRunning = false;

    private int log_interval_time = 120;

    private HandlerThread handlerThread;
    private Handler mHandler;
    private static NativeLoadReadyRecordManager instance;

    static {
        instance = new NativeLoadReadyRecordManager();
    }

    private NativeLoadReadyRecordManager() {

    }

    public static NativeLoadReadyRecordManager getInstance() {
        return instance;
    }



    private class RecordNativeLoadReadyCountRunnable implements Runnable  {

        @Override
        public void run(){

            if (isRunning){
                mHandler.removeCallbacksAndMessages(null);
                HashMap<String, LoadReadyItem> loadReadyItemMap = LoadReadyItem.getLoadReadyItemMap();
                if(!loadReadyItemMap.isEmpty()) {
                    Iterator<LoadReadyItem> iterator = loadReadyItemMap.values().iterator();
                    while (iterator.hasNext()) {
                        LoadReadyItem item = iterator.next();
                        if (item != null){
                            PointEntitySigmobUtils.TrackNativeLoadReady(item);
                            item.clear();
                        }
                    }
                }

                mHandler.postDelayed(this, log_interval_time *1000);
            }

        }
    }
    public synchronized void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        LoadReadyItem.intLoadReadyMap();
        if (handlerThread == null) {
            handlerThread = new HandlerThread(TAG);
            handlerThread.start();

        }

        if (mHandler == null) {
            mHandler = new Handler(handlerThread.getLooper());
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new RecordNativeLoadReadyCountRunnable(), log_interval_time * 1000);

    }

    public void stop() {
        isRunning = false;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
    public void update(int log_interval_time) {
        if (log_interval_time <= 0) {
            stop();
        } else {

            int time =  Math.max(log_interval_time, 10);
            if (this.log_interval_time == time) {
               return;
            }
            this.log_interval_time = time;
            if (!isRunning){
                start();
            }else {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(new RecordNativeLoadReadyCountRunnable(), log_interval_time * 1000);
            }

        }
    }
}
