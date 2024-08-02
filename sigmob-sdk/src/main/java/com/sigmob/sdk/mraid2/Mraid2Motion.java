package com.sigmob.sdk.mraid2;

import android.view.View;

import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.common.MotionManger;
import com.sigmob.sdk.mraid.MraidObject;

import java.util.HashMap;
import java.util.Map;

public class Mraid2Motion extends MraidObject {

    private static final String SHAKE_START = "motion_shake_start";
    private static final String SHAKE_END = "motion_shake_end";
    private static final String TWIST_START = "motion_twist_start";
    private static final String TWIST_END = "motion_twist_end";
    private MraidBridgeMotionListener mraidBridgeMotionListener;

    private MotionManger.Motion mMotion;


    private void initTwist() {
        mMotion = new MotionManger.OrientationMotion(SDKContext.getApplicationContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {
                HashMap<String, Object> args = new HashMap<>();

                if (mraidBridgeMotionListener != null) {
                    mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "twist", "began", args);
                }
            }

            @Override
            public void onMotionUpdate(float progress) {

            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {

                if (info != null) {
                    HashMap<String, Object> args = new HashMap<>();

                    Number turn_x = info.get("turn_x");
                    Number turn_y = info.get("turn_y");
                    Number turn_z = info.get("turn_z");
                    Number turn_time = info.get("turn_time");
                    args.put("x", turn_x);
                    args.put("y", turn_y);
                    args.put("z", turn_z);
                    args.put("time", turn_time);
                    if (mraidBridgeMotionListener != null) {
                        mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "twist", "end", args);
                    }

                }


            }
        }, MotionManger.OrientationMotionType.WRING);

    }

    private void initShake() {
        MotionManger.ShakeMotion motion = new MotionManger.ShakeMotion(SDKContext.getApplicationContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {
                HashMap<String, Object> args = new HashMap<>();

                if (mraidBridgeMotionListener != null) {
                    mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "shake", "began", args);
                }
            }

            @Override
            public void onMotionUpdate(float progress) {

            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null) {

                    HashMap<String, Object> args = new HashMap<>();
                    Number x_max_acc = info.get("x_max_acc");
                    Number y_max_acc = info.get("y_max_acc");
                    Number z_max_acc = info.get("z_max_acc");

                    args.put("x", String.valueOf(x_max_acc));
                    args.put("y", String.valueOf(y_max_acc));
                    args.put("z", String.valueOf(z_max_acc));

                    if (mraidBridgeMotionListener != null) {
                        mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "shake", "end", args);
                    }
                }
            }
        });
        motion.setFactor(1);
        mMotion = motion;
    }

    private void initSlope() {
        MotionManger.OrientationMotion motion = new MotionManger.OrientationMotion(SDKContext.getApplicationContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {
                HashMap<String, Object> args = new HashMap<>();

                if (mraidBridgeMotionListener != null) {
                    mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "slope", "began", args);
                }
            }

            @Override
            public void onMotionUpdate(float progress) {
                if (mraidBridgeMotionListener != null) {
                    HashMap<String, Object> args = new HashMap<>();
                    args.put("progress", (int) (progress * 100));
                    mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "slope", "progress", args);
                }

            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {

                if (info != null) {
                    HashMap<String, Object> args = new HashMap<>();

                    Number turn_x = info.get("x_max_acc");
                    Number turn_y = info.get("y_max_acc");
                    Number turn_z = info.get("z_max_acc");
                    args.put("x", String.valueOf(turn_x));
                    args.put("y", String.valueOf(turn_y));
                    args.put("z", String.valueOf(turn_z));
                    if (mraidBridgeMotionListener != null) {
                        mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "slope", "end", args);

                    }

                }


            }
        }, MotionManger.OrientationMotionType.SLOPE);
        motion.setFactor(1);
        mMotion = motion;
    }

    private void initSwing() {
        MotionManger.OrientationMotion motion = new MotionManger.OrientationMotion(SDKContext.getApplicationContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {
                HashMap<String, Object> args = new HashMap<>();

                if (mraidBridgeMotionListener != null) {
                    mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "swing", "began", args);
                }
            }

            @Override
            public void onMotionUpdate(float progress) {
                if (mraidBridgeMotionListener != null) {
                    HashMap<String, Object> args = new HashMap<>();
                    args.put("progress", (int) (progress * 100));
                    mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "swing", "progress", args);
                }
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {

                if (info != null) {
                    HashMap<String, Object> args = new HashMap<>();

                    Number turn_x = info.get("x_max_acc");
                    Number turn_y = info.get("y_max_acc");
                    Number turn_z = info.get("z_max_acc");
                    args.put("x", String.valueOf(turn_x));
                    args.put("y", String.valueOf(turn_y));
                    args.put("z", String.valueOf(turn_z));
                    if (mraidBridgeMotionListener != null) {
                        mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "swing", "end", args);
                    }

                }


            }
        }, MotionManger.OrientationMotionType.SWING);

        motion.setFactor(1);
        mMotion = motion;

    }

    public Mraid2Motion(String uniqueId, String type) {
        super(uniqueId);

        switch (type) {
            case "twist":
                initTwist();
                break;
            case "shake":
                initShake();
                break;
            case "slope":
                initSlope();
                break;
            case "swing":
                initSwing();
                break;
        }

    }

    public void setLevel(int level){

        if (mMotion != null) {
            mMotion.setLevel(level);
        }
    }

    public void setRawSensitivity(int sensitivity_raw) {
        if (mMotion != null){
            mMotion.setRawSensitivity(sensitivity_raw);
        }
    }

    @Override
    public View getView() {
        return null;
    }

    @Override
    public void destroy() {

        this.mraidBridgeMotionListener = null;
        if (mMotion != null) {
            mMotion.destroy();
        }
    }

    public void setMraidBridgeMotionListener(MraidBridgeMotionListener mraidBridgeMotionListener) {
        this.mraidBridgeMotionListener = mraidBridgeMotionListener;
    }

    public void start() {
        if (mMotion != null){
            mMotion.start();
        }
    }
}
