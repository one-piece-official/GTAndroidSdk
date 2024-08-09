package com.gt.sdk.base.common;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.czhj.sdk.logger.SigmobLog;

import java.util.HashMap;
import java.util.Map;

public class SingleMotionManger {

    public interface MotionListener {
        void onMotionStart();

        void onMotionUpdate(float progress);

        void onMotionEnd(Map<String, Number> info);

    }

    public enum MotionType {
        SWING, WRING, SLOPE, SHAKE
    }

    public static class Motion implements SensorEventListener {
        protected int level = 2;

        static int[] Sensitivit_Int_Values = {25, 60, 50, 45, 35, 25, 20, 15, 10, 5, 1};
        static float[] Sensitivit_Float_Values = {4, 10, 8, 6, 5, 4, 3, 2, 1.8f, 1.5f, 1};

        private final SensorManager mSensorManager;
        private float[] accelerometerValues;
        private float[] magneticFieldValues;
        float[] rotationMatrix = new float[9];
        float[] orientation = new float[3];
        Float previousGyroscopeData;
        private static float sensitivity = 40f;

        private MotionListener motionListener;
        private float x_max_acc, y_max_acc, z_max_acc;
        private long lastShakeTime;
        private boolean isShaking;

        private int yCounter = 0;
        private float rotationRateX, rotationRateY, rotationRateZ;
        private MotionType type;
        private long startTime;
        private float max_roll, max_pitch, max_yaw;
        private boolean isRunning;
        private float lastValue;
        private int factor = 100;
        private int TIME_INTERVAL = 2000;
        private long lastEndTime;
        private Integer rawSensitivity;

        // 静态变量，用于跟踪当前活动的SingleMotion对象

        public void setFactor(int factor) {
            if (factor > 0) {
                this.factor = factor;
            }
        }

        public Motion(Context context, MotionListener motionListener, MotionType type) {

            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            this.motionListener = motionListener;
            this.type = type;
        }

        public void start() {

            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
                mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);

                if (type == MotionType.SHAKE) {
                    return;
                }
                mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);

            }
            previousGyroscopeData = null;

        }

        public void pause() {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
            }
        }

        public void destroy() {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
            }

            max_pitch = 0;
            max_yaw = 0;
            max_roll = 0;
            x_max_acc = 0;
            y_max_acc = 0;
            z_max_acc = 0;
            lastValue = 0;
            lastEndTime = 0;
            previousGyroscopeData = null;
            motionListener = null;
//            if (motionListener != null) {
//                motionListener.onMotionEnd(null);
//            }

        }

        private void handleGyroData(float[] rotationRate) {
            float magnitude = (float) Math.sqrt(Math.pow(rotationRate[0], 2) + Math.pow(rotationRate[1], 2) + Math.pow(rotationRate[2], 2));
            long currentTime = System.currentTimeMillis();
            boolean interval = currentTime - lastShakeTime >= TIME_INTERVAL;

            if (Math.abs(x_max_acc) < Math.abs(rotationRate[0])) {
                x_max_acc = rotationRate[0];
            }
            if (Math.abs(y_max_acc) < Math.abs(rotationRate[1])) {
                y_max_acc = rotationRate[1];
            }
            if (Math.abs(z_max_acc) < Math.abs(rotationRate[2])) {
                z_max_acc = rotationRate[2];
            }

            if (rawSensitivity != null) {
                sensitivity = rawSensitivity;
            } else {
                if (level <= 0 || level > 10) {
                    sensitivity = 4f;
                } else {
                    sensitivity = Sensitivit_Float_Values[level];
                }
            }

            SigmobLog.d("shake magnitude" + magnitude);
            if (magnitude > sensitivity && !isShaking && interval) {
                isShaking = true;
                if (motionListener != null) {
                    motionListener.onMotionStart();
                }
            } else if (magnitude < sensitivity && isShaking && interval) {
                isShaking = false;
                lastShakeTime = currentTime;
                Map<String, Number> map = new HashMap<>();
                map.put("x_max_acc", (x_max_acc * factor));
                map.put("y_max_acc", (y_max_acc * factor));
                map.put("z_max_acc", (z_max_acc * factor));
                x_max_acc = 0;
                y_max_acc = 0;
                z_max_acc = 0;
                if (motionListener != null) {
                    motionListener.onMotionEnd(map);
                }
            }
        }


        public void setRawSensitivity(int sensitivity_raw) {
            this.rawSensitivity = sensitivity_raw;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                if (type == MotionType.SHAKE) {
                    handleGyroData(event.values);
                    return;
                }
                rotationRateX = event.values[0];
                rotationRateY = event.values[1];
                rotationRateZ = event.values[2];
            }

            if (accelerometerValues == null || magneticFieldValues == null) return;

            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticFieldValues);
            // 获取方向信息
            SensorManager.getOrientation(rotationMatrix, orientation);
            float yaw = (float) Math.toDegrees(orientation[0]);
            float pitch = (float) Math.toDegrees(orientation[1]);
            float roll = (float) Math.toDegrees(orientation[2]);


            switch (this.type) {
                case SLOPE: {

                    long currentTime = System.currentTimeMillis();
                    boolean interval = currentTime - lastEndTime >= TIME_INTERVAL;

                    if (!interval) {
                        return;
                    }
                    if (previousGyroscopeData == null) {

                        previousGyroscopeData = pitch;
                        startTime = System.currentTimeMillis();
                        if (motionListener != null) {
                            motionListener.onMotionStart();
                        }
                        return;
                    }
                    if (rawSensitivity != null) {
                        sensitivity = rawSensitivity;
                    } else {
                        if (level <= 0 || level > 10) {
                            sensitivity = 25f;
                        } else {
                            sensitivity = Sensitivit_Int_Values[level];
                        }
                    }


                    if (Math.abs(x_max_acc) < Math.abs(rotationRateX)) {
                        x_max_acc = rotationRateX;
                    }
                    if (Math.abs(y_max_acc) < Math.abs(rotationRateY)) {
                        y_max_acc = rotationRateY;
                    }
                    if (Math.abs(z_max_acc) < Math.abs(rotationRateZ)) {
                        z_max_acc = rotationRateZ;
                    }


                    float offset = Math.abs(previousGyroscopeData - pitch);

                    if (offset < 5) {
                        return;
                    }

                    float value = offset > sensitivity ? 1 : offset / sensitivity;

                    if ((int) (lastValue * 100) != (int) (value * 100)) {
                        lastValue = value;
                        if (motionListener != null) {
                            motionListener.onMotionUpdate(value);
                        }
                    }

                    if (value > 0.98f) {
                        Map<String, Number> map = new HashMap<>();
                        map.put("x_max_acc", (x_max_acc * factor));
                        map.put("y_max_acc", (y_max_acc * factor));
                        map.put("z_max_acc", (z_max_acc * factor));
                        lastEndTime = System.currentTimeMillis();

                        if (motionListener != null) {
                            motionListener.onMotionEnd(map);
                        }
                        x_max_acc = 0;
                        y_max_acc = 0;
                        z_max_acc = 0;
                        lastValue = 0;
                        previousGyroscopeData = null;
                    }
                }
                break;
                case SWING: {
                    long currentTime = System.currentTimeMillis();
                    boolean interval = currentTime - lastEndTime >= TIME_INTERVAL;

                    if (!interval) {
                        return;
                    }
                    if (previousGyroscopeData == null) {
                        previousGyroscopeData = roll;
                        startTime = System.currentTimeMillis();

                        if (motionListener != null) {
                            motionListener.onMotionStart();
                        }
                        return;
                    }

                    if (Math.abs(x_max_acc) < Math.abs(rotationRateX)) {
                        x_max_acc = rotationRateX;
                    }
                    if (Math.abs(y_max_acc) < Math.abs(rotationRateY)) {
                        y_max_acc = rotationRateY;
                    }
                    if (Math.abs(z_max_acc) < Math.abs(rotationRateZ)) {
                        z_max_acc = rotationRateZ;
                    }
                    if (rawSensitivity != null) {
                        sensitivity = rawSensitivity;
                    } else {
                        if (level <= 0 || level > 10) {
                            sensitivity = 25f;
                        } else {
                            sensitivity = Sensitivit_Int_Values[level];
                        }
                    }

                    float offset = Math.abs(previousGyroscopeData - roll);

                    float value = offset > sensitivity ? 1 : offset / sensitivity;

                    if ((int) (lastValue * 100) != (int) (value * 100)) {
                        lastValue = value;
                        if (motionListener != null) {
                            motionListener.onMotionUpdate(value);
                        }
                    }

                    if (value > 0.98f) {
                        lastEndTime = System.currentTimeMillis();

                        if (motionListener != null) {
                            Map<String, Number> map = new HashMap<>();
                            map.put("x_max_acc", (x_max_acc * factor));
                            map.put("y_max_acc", (y_max_acc * factor));
                            map.put("z_max_acc", (z_max_acc * factor));
                            motionListener.onMotionEnd(map);
                        }
                        x_max_acc = 0;
                        y_max_acc = 0;
                        z_max_acc = 0;
                        lastValue = 0;
                        previousGyroscopeData = null;
                    }

                }
                break;
                case WRING: {
                    long currentTime = System.currentTimeMillis();
                    boolean interval = currentTime - lastEndTime >= TIME_INTERVAL;

                    if (!interval) {
                        return;
                    }

                    SigmobLog.d("wring rotationRateY" + rotationRateY + " rotationRateX " + rotationRateX + " rotationRateZ " + rotationRateZ);

                    if (Math.abs(rotationRateY) < 1 && !isRunning) {
                        SigmobLog.d("wring reset");

                        startTime = 0;
                        return;
                    }
                    if (rawSensitivity != null) {
                        sensitivity = rawSensitivity;
                    } else {
                        if (level <= 0 || level > 10) {
                            sensitivity = 4f;
                        } else {
                            sensitivity = Sensitivit_Float_Values[level];
                        }
                    }
                    isRunning = Math.abs(rotationRateY) > 0.1;

                    if (startTime == 0 && isRunning) {
                        startTime = System.currentTimeMillis();
                        SigmobLog.d("wring start");
                        if (motionListener != null) {
                            motionListener.onMotionStart();
                        }
                        return;
                    }

                    if (System.currentTimeMillis() - startTime < 200) {
                        return;
                    }


                    if (Math.abs(max_pitch) < Math.abs(pitch)) {
                        max_pitch = pitch;
                    }
                    if (Math.abs(max_yaw) < Math.abs(yaw)) {
                        max_yaw = yaw;
                    }
                    if (Math.abs(max_roll) < Math.abs(roll)) {
                        max_roll = roll;
                    }
                    if (startTime > 0 && (Math.abs(rotationRateX) > sensitivity || Math.abs(rotationRateY) > sensitivity || Math.abs(rotationRateZ) > sensitivity)) {

                        lastEndTime = System.currentTimeMillis();
                        SigmobLog.d("wring end");

                        if (motionListener != null) {
                            Map<String, Number> map = new HashMap<>();
                            map.put("turn_x", (int) max_pitch);
                            map.put("turn_y", (int) max_roll);
                            map.put("turn_z", (int) max_yaw);
                            long end = System.currentTimeMillis();

                            map.put("turn_time", end - startTime);

                            motionListener.onMotionEnd(map);
                        }
                        startTime = 0;
                        max_pitch = 0;
                        max_yaw = 0;
                        max_roll = 0;
                        isRunning = false;
                    }
                }
                break;
                default: {

                }
                break;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}
