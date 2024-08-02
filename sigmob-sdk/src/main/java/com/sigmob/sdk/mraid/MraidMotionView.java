package com.sigmob.sdk.mraid;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.common.MotionManger;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.base.views.MotionView;
import com.sigmob.sdk.base.views.ShakeNewView;
import com.sigmob.sdk.base.views.SlopeView;
import com.sigmob.sdk.base.views.SwingView;
import com.sigmob.sdk.base.views.WringView;
import com.sigmob.sdk.mraid2.MraidBridgeMotionListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MraidMotionView extends MraidObject {

    private int type;
    private MotionView mMotionView;
    private MotionManger.Motion mMotion;
    private MraidBridgeMotionListener mraidBridgeMotionListener;


    public MraidMotionView(String uniqueId) {
        super(uniqueId);
    }


    public MraidMotionView(Context context, String uniqueId, int type) {
        super(uniqueId);
        this.type = type;

        switch (type) {
            //摇一摇
            case 0: {
                mMotionView = new ShakeNewView(context);
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
            break;
            //扭一扭
            case 1: {
                mMotionView = new WringView(context);
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
                            args.put("x", String.valueOf(turn_x));
                            args.put("y", String.valueOf(turn_y));
                            args.put("z", String.valueOf(turn_z));
                            args.put("time", turn_time);
                            if (mraidBridgeMotionListener != null) {
                                mraidBridgeMotionListener.notifyMotionEvent(uniqueId, "twist", "end", args);
                            }

                        }


                    }
                }, MotionManger.OrientationMotionType.WRING);

            }
            break;
            //倾斜
            case 2: {
                mMotionView = new SlopeView(context);
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
                        if (mMotionView instanceof SlopeView) {
                            ((SlopeView) mMotionView).updateScreen(progress);
                        }
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
            break;
            //摇摆
            case 3: {
                mMotionView = new SwingView(context);
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
                        if (mMotionView instanceof SwingView) {
                            ((SwingView) mMotionView).updateProcess(progress);
                        }
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
            break;
            default: {
                SigmobLog.e("MMotionView type is not support");
            }
            break;
        }
        if (mMotionView != null){
            mMotionView.startAnimator();
        }
        mMotion.setLevel(3);

    }


    public void setHidden(boolean hidden) {
        if (mMotionView != null) {
            if (hidden) {
                mMotionView.setVisibility(View.INVISIBLE);
                mMotion.pause();
            } else {
                mMotionView.setVisibility(View.VISIBLE);
                mMotion.start();
            }
        }
    }

    public void sensitivity(int sensitivity) {
        if (mMotion != null) {
            if (sensitivity == 0) {
                sensitivity = 3;
            } else if (sensitivity == 2) {
                sensitivity = 1;
            } else if (sensitivity == 1) {
                sensitivity = 2;
            }
            mMotion.setLevel(sensitivity);
        }
    }

    public void start() {
        if (mMotion != null) {
            mMotion.start();
        }
    }

    public void setMotionListener(MraidBridgeMotionListener mraidBridgeMotionListener) {
        this.mraidBridgeMotionListener = mraidBridgeMotionListener;
    }

    @Override
    public View getView() {
        return mMotionView;
    }

    @Override
    public void destroy() {
        if (mMotionView != null) {
            ViewUtil.removeFromParent(mMotionView);
            mMotionView = null;
        }
        if (mMotion != null) {
            mMotion.destroy();
        }
    }

    public void OnFrame(JSONObject args) {

        if (mMotionView != null) {
            JSONObject frame = args.optJSONObject("frame");

            int top = (int) frame.optDouble("x", 0);
            int left = (int) frame.optDouble("y", 0);
            int width = (int) frame.optDouble("w", -1);
            int height = (int) frame.optDouble("h", -1);
            int realwidth = width;
            int realheight = height;
            if (width > 0) {
                realwidth = Dips.dipsToIntPixels(width, SDKContext.getApplicationContext());
            }

            if (height > 0) {
                realheight = Dips.dipsToIntPixels(height, SDKContext.getApplicationContext());
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(realwidth, realheight);
//            layoutParams.topMargin = Dips.dipsToIntPixels(top, SDKContext.getApplicationContext());
//            layoutParams.leftMargin = Dips.dipsToIntPixels(left, SDKContext.getApplicationContext());
            mMotionView.setX(Dips.dipsToIntPixels(top, SDKContext.getApplicationContext()));
            mMotionView.setY(Dips.dipsToIntPixels(left, SDKContext.getApplicationContext()));
            mMotionView.setLayoutParams(layoutParams);
            mMotionView.requestLayout();
        }
    }

    public void setRawSensitivity(int sensitivity_raw) {
        if (mMotion != null){
            mMotion.setRawSensitivity(sensitivity_raw);
        }
    }

}
