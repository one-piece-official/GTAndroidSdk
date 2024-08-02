package com.sigmob.sdk.nativead;

import android.os.Handler;
import android.os.Looper;

/**
 * created by lance on   2021/8/3 : 4:00 PM
 */

public class TimerHandler extends Handler {

    static final int MSG_TIMER_ID = 87108;
    long interval = 100;
    boolean isStopped = true;

    public TimerHandler() {
        super();
    }

    public TimerHandler(Looper looper) {
        super(looper);
    }

    public void tick() {
        sendEmptyMessageDelayed(TimerHandler.MSG_TIMER_ID, interval);
    }

    public boolean isStopped() {
        return isStopped;
    }

    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }


}