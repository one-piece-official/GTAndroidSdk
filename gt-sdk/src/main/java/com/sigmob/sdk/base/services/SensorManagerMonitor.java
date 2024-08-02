package com.sigmob.sdk.base.services;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.mta.PointEntitySensor;
import com.sigmob.sdk.base.mta.PointType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SensorManagerMonitor implements SensorEventListener {
    private static Handler mSensorHandler;
    private static int MAX_SENSOR_SIZE = 500;
    DecimalFormat df = new DecimalFormat("#######.######");
    private SensorItem mSensorItem = new SensorItem();
    private List<SensorItem> mSensorList;
    private SensorManager mSensorManager;
    private int mSensordelayUnit = 300;
    private int mSensordelayNum = 50;

    public static SensorManagerMonitor getInstance() {

        return SensorManagerMonitorFactory.ginstance;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: {

                mSensorItem.accelerometer = event.values.clone();
            }
            break;
            case Sensor.TYPE_GRAVITY: {
                mSensorItem.gravity = event.values.clone();

            }
            break;
            case Sensor.TYPE_GYROSCOPE: {
                mSensorItem.gyroscope = event.values.clone();

            }
            break;
            case Sensor.TYPE_LIGHT: {
                mSensorItem.light = event.values.clone();

            }
            break;

            case Sensor.TYPE_MAGNETIC_FIELD: {
                mSensorItem.magneticField = event.values.clone();

            }
            break;
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                mSensorItem.linear_acceleration = event.values.clone();

            }
            break;
            default: {

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        SigmobLog.d(String.format("name :%s, accuracy %d", sensor.getName(), accuracy));
    }

    public void addSensorEvent(String event, String loadId) {

        if (WindSDKConfig.getInstance().enableAntiFraud() && WindSDKConfig.getInstance().filterAntiEvent(event)) {

            initSensorManager(SDKContext.getApplicationContext());

            Message msg = new Message();
            SensorSendEvent sendEvent = new SensorSendEvent();
            sendEvent.category = event;
            sendEvent.loadId = loadId;
            msg.what = SensorADEvent.SENSOR_EVENT_BEGIN.getValue();
            msg.obj = sendEvent;
            if (mSensorHandler != null) {
                mSensorHandler.sendMessage(msg);
            }
        }
    }

    public void initSensorManager(Context context) {


        try {

            if (mSensorManager != null) {
                if (!WindSDKConfig.getInstance().enableAntiFraud()) {
                    mSensorManager.unregisterListener(this);
                    mSensorHandler.removeCallbacksAndMessages(null);
                    mSensorList.clear();
                    mSensorManager = null;
                }
                return;
            } else if (WindSDKConfig.getInstance().enableAntiFraud()) {

                SigmobLog.i("start anti spam motion");
                mSensorList = new ArrayList();

                mSensordelayNum = WindSDKConfig.getInstance().getMotionCount();
                mSensordelayUnit = WindSDKConfig.getInstance().getMotionInterval();

                MAX_SENSOR_SIZE = WindSDKConfig.getInstance().getMotionQueueMax();

                // 实例化传感器管理者
                mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

                //光线传感器
                mSensorManager.registerListener((SensorEventListener) this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                        SensorManager.SENSOR_DELAY_NORMAL);
                SigmobLog.d("光线传感器 ");


                //加速传感器
                mSensorManager.registerListener((SensorEventListener) this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_NORMAL);
                SigmobLog.d("加速传感器 ");

                //磁场传感器
                mSensorManager.registerListener((SensorEventListener) this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                        SensorManager.SENSOR_DELAY_NORMAL);
                SigmobLog.d("磁场传感器 ");

                //陀螺仪
                mSensorManager.registerListener((SensorEventListener) this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                        SensorManager.SENSOR_DELAY_NORMAL);
                SigmobLog.d("陀螺仪 ");

                //重力传感器
                mSensorManager.registerListener((SensorEventListener) this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                        SensorManager.SENSOR_DELAY_NORMAL);
                SigmobLog.d("重力传感器 ");

                //线性加速器
                mSensorManager.registerListener((SensorEventListener) this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                        SensorManager.SENSOR_DELAY_NORMAL);
                SigmobLog.d("线性加速器 ");

                mSensorHandler = new Handler(Looper.getMainLooper()) {

                    @Override
                    public void handleMessage(Message msg) {

                        if (msg.what == SensorADEvent.SENSOR_EVENT_BEGIN.getValue()) {

                            SensorSendEvent event = (SensorSendEvent) msg.obj;
                            int index = mSensorList.size() - 1;
                            if (index < 0) {
                                index = 0;
                            }
                            event.sensorItem = mSensorList.get(index);
                            Message message = new Message();
                            message.what = SensorADEvent.SENSOR_EVENT_END.getValue();
                            message.obj = event;
                            mSensorHandler.sendMessageDelayed(message, mSensordelayUnit * (mSensordelayNum + 1));

                        } else if (msg.what == SensorADEvent.SENSOR_EVENT_END.getValue()) {

                            SensorSendEvent event = (SensorSendEvent) msg.obj;
                            int index = mSensorList.indexOf(event.sensorItem);
                            int startIndex = index - mSensordelayNum;
                            if (index < 0 || index < mSensordelayNum) {
                                startIndex = 0;
                            }

                            int endIndex = index + mSensordelayNum;
                            if (endIndex > mSensorList.size() - 1) {
                                endIndex = mSensorList.size() - 1;
                            }

                            if (mSensorList.size() > 0) {
                                List<SensorItem> beforeList = null;
                                List<SensorItem> afterList = null;
                                if (startIndex < index) {
                                    beforeList = new CopyOnWriteArrayList<>(mSensorList.subList(startIndex, index));
                                }
                                if (index < endIndex) {
                                    afterList = new CopyOnWriteArrayList<>(mSensorList.subList(index, endIndex));
                                }
                                if (beforeList != null || afterList != null) {
                                    sensorSend(beforeList, afterList, event.category, event.loadId);
                                }
                            }

                        } else if (msg.what == SensorADEvent.SENSOR_EVENT_PUSH.getValue()) {

                            try {
                                mSensorItem.timestamp = System.currentTimeMillis();
                                SensorItem item = mSensorItem.clone();

                                if (mSensorList.size() > MAX_SENSOR_SIZE) {
                                    mSensorList.remove(0);
                                }
                                if (item != null) {
                                    mSensorList.add(item);
                                }

                            } catch (CloneNotSupportedException e) {
                                SigmobLog.e(e.getMessage());
                            }

                            Message message = new Message();
                            message.what = SensorADEvent.SENSOR_EVENT_PUSH.getValue();
                            mSensorHandler.sendMessageDelayed(message, mSensordelayUnit);
                        }
                    }
                };

                Message message = new Message();
                message.what = SensorADEvent.SENSOR_EVENT_PUSH.getValue();

                if (mSensorHandler != null) {
                    mSensorHandler.sendMessage(message);

                }
            }


        } catch (Throwable throwable) {

        }


    }

    String numberToString(String num) {
        return num;
    }

    private String convertSenorListToJsonString(List<SensorItem> list) {


        StringBuilder result = new StringBuilder();

        if (list == null || list.size() == 0) {
            return result.toString();
        }
        ArrayList gravityX = new ArrayList<>();
        ArrayList gravityY = new ArrayList();
        ArrayList gravityZ = new ArrayList();
        ArrayList gyroscopeX = new ArrayList();
        ArrayList gyroscopeY = new ArrayList();
        ArrayList gyroscopeZ = new ArrayList();
        ArrayList magneticFieldX = new ArrayList();
        ArrayList magneticFieldY = new ArrayList();
        ArrayList magneticFieldZ = new ArrayList();
        ArrayList accelerometerX = new ArrayList();
        ArrayList accelerometerY = new ArrayList();
        ArrayList accelerometerZ = new ArrayList();
        ArrayList linear_accelerationX = new ArrayList();
        ArrayList linear_accelerationY = new ArrayList();
        ArrayList linear_accelerationZ = new ArrayList();

        ArrayList light = new ArrayList();


        for (int i = 0; i < list.size(); i++) {
            SensorItem item = list.get(i);
            if (item.gravity != null && item.gravity.length > 2) {
                gravityX.add(numberToString(df.format(item.gravity[0])));
                gravityY.add(numberToString(df.format(item.gravity[1])));
                gravityZ.add(numberToString(df.format(item.gravity[2])));
            } else {
                gravityX.add(numberToString("0"));
                gravityY.add(numberToString("0"));
                gravityZ.add(numberToString("0"));

            }

            if (item.gyroscope != null && item.gyroscope.length > 2) {
                gyroscopeX.add(numberToString(df.format(item.gyroscope[0])));
                gyroscopeY.add(numberToString(df.format(item.gyroscope[1])));
                gyroscopeZ.add(numberToString(df.format(item.gyroscope[2])));
            } else {
                gyroscopeX.add(numberToString("0"));
                gyroscopeY.add(numberToString("0"));
                gyroscopeZ.add(numberToString("0"));
            }

            if (item.magneticField != null && item.magneticField.length > 2) {
                magneticFieldX.add(numberToString(df.format(item.magneticField[0])));
                magneticFieldY.add(numberToString(df.format(item.magneticField[1])));
                magneticFieldZ.add(numberToString(df.format(item.magneticField[2])));
            } else {
                magneticFieldX.add(numberToString("0"));
                magneticFieldY.add(numberToString("0"));
                magneticFieldZ.add(numberToString("0"));
            }

            if (item.accelerometer != null && item.accelerometer.length > 2) {
                accelerometerX.add(numberToString(df.format(item.accelerometer[0])));
                accelerometerY.add(numberToString(df.format(item.accelerometer[1])));
                accelerometerZ.add(numberToString(df.format(item.accelerometer[2])));
            } else {
                accelerometerX.add(numberToString("0"));
                accelerometerY.add(numberToString("0"));
                accelerometerZ.add(numberToString("0"));
            }

            if (item.linear_acceleration != null && item.linear_acceleration.length > 2) {
                linear_accelerationX.add(numberToString(df.format(item.linear_acceleration[0])));
                linear_accelerationY.add(numberToString(df.format(item.linear_acceleration[1])));
                linear_accelerationZ.add(numberToString(df.format(item.linear_acceleration[2])));
            } else {
                linear_accelerationX.add(numberToString("0"));
                linear_accelerationY.add(numberToString("0"));
                linear_accelerationZ.add(numberToString("0"));
            }

            if (item.light != null && item.light.length > 0) {
                light.add(numberToString(df.format(item.light[0])));
            } else {
                light.add(numberToString("0"));
            }

        }

        result.append("{");
        result.append(String.format("\"gravity\":{%s}", String.format("\"x\":%s,\"y\":%s,\"z\":%s", gravityX.toString(), gravityY.toString(), gravityZ.toString())));
        result.append(",");
        result.append(String.format("\"gyroscope\":{%s}", String.format("\"x\":%s,\"y\":%s,\"z\":%s", gyroscopeX.toString(), gyroscopeY.toString(), gyroscopeZ.toString())));
        result.append(",");
        result.append(String.format("\"magnetic_field\":{%s}", String.format("\"x\":%s,\"y\":%s,\"z\":%s", magneticFieldX.toString(), magneticFieldY.toString(), magneticFieldZ.toString())));
        result.append(",");
        result.append(String.format("\"light\":%s", light.toString()));
        result.append(",");
        result.append(String.format("\"linear_acceleration\":{%s}", String.format("\"x\":%s,\"y\":%s,\"z\":%s", linear_accelerationX.toString(), linear_accelerationY.toString(), linear_accelerationZ.toString())));
        result.append(",");
        result.append(String.format("\"accelerometer\":{%s}", String.format("\"x\":%s,\"y\":%s,\"z\":%s", accelerometerX.toString(), accelerometerY.toString(), accelerometerZ.toString())));
        result.append("}");

        return Base64.encodeToString(result.toString().getBytes(), Base64.NO_WRAP);

    }

    public void setSensordelayUnit(int sensordelay) {
        mSensordelayUnit = sensordelay;
    }

    private void sensorSend(List<SensorItem> beforeList, List<SensorItem> afterList, String category, String loadId) {

        PointEntitySensor pointEntitySensor = new PointEntitySensor();

        pointEntitySensor.setAc_type(PointType.ANTI_SPAM);
        pointEntitySensor.setCategory(category);
        pointEntitySensor.setMotion_before(convertSenorListToJsonString(beforeList));

        pointEntitySensor.setMotion_after(convertSenorListToJsonString(afterList));
        pointEntitySensor.setMotion_interval(String.valueOf(mSensordelayUnit));
        pointEntitySensor.setLoad_id(loadId);

        pointEntitySensor.commit();
        ;

    }

    private static class SensorManagerMonitorFactory {
        private final static SensorManagerMonitor ginstance = new SensorManagerMonitor();
    }
}
