package com.sigmob.sdk.mraid2;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * created by lance on   2022/7/12 : 5:15 下午
 */
public class MraidTimer {

    private final String uniqueId;
    private int interval;
    private boolean repeats;
    private Mraid2Bridge bridge;

    private Timer mTimerOver = null;
    private TimerTask mTaskOver = null;

    public MraidTimer(Mraid2Bridge bridge, JSONObject args) {
        this.bridge = bridge;
        this.interval = args.optInt("interval");
        this.repeats = args.optBoolean("repeats");
        this.uniqueId = args.optString("uniqueId");
    }

    private void initCursorTimer() {
        mTimerOver = new Timer();
        mTaskOver = new TimerTask() {

            @Override
            public void run() {
                //bridge通知定时器完成
                if (bridge != null) {
                    bridge.getWebView().post(new Runnable() {
                        @Override
                        public void run() {
                            bridge.notifyFireEvent(uniqueId);
                        }
                    });
                }
            }
        };
    }

    public void pause() {
        invalidate();
    }

    /**
     * fire
     */
    public void fire() {
        try {

            invalidate();

            initCursorTimer();

            if (interval > 0) {
                if (repeats) {
                    mTimerOver.schedule(mTaskOver, interval, interval);
                } else {
                    mTimerOver.schedule(mTaskOver, interval);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void invalidate() {
        if (mTaskOver != null) {
            mTaskOver.cancel();
            mTaskOver = null;
        }
        if (mTimerOver != null) {
            mTimerOver.cancel();
            mTimerOver.purge();
            mTimerOver = null;
        }
    }
}
