package com.czhj.devicehelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.czhj.devicehelper.oaId.helpers.DevicesIDsHelper;
import com.czhj.sdk.logger.SigmobLog;

public final class DeviceHelper {

    private static final String SIM_STATE = "getSimState";
    private static final String SIM_IMEI = "getImei";
    private static final String SIM_LINE_NUMBER = "getLine1Number";
    private static String mOAID;
    private static String mAAID;
    private static String mVAID;
    private static String mOAID_API = "";
    private static long oaid_Limt = 0;
    private static long oaid_API_Limt = 0;

    private static Handler mHandler;
    private static Handler mOAID_API_Handler;
    private static int oaidRetryCount = 0;
    private static boolean oaidWaitCallback = false;

    private static String mWifiName;
    private static String mBssid;
    private static String mMacAddress;
    private static Thread mOAID_API_Task;
    private static Thread mOAID_Task;
    private static boolean oaidSDKWaitCallback;
    private static int oaidSDKRetryCount;


    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        try {
            SigmobLog.d("private :getIMEI");

            String deviceUniqueIdentifier = null;
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) return null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deviceUniqueIdentifier = tm.getImei();

                if (TextUtils.isEmpty(deviceUniqueIdentifier)) {
                    try {
                        return tm.getDeviceId();
                    } catch (Throwable ignored) {

                    }

                    return tm.getMeid();
                }
            } else {
                deviceUniqueIdentifier = tm.getDeviceId();
            }
            if (deviceUniqueIdentifier != null) {
                return deviceUniqueIdentifier;
            }
        } catch (Exception e) {
            //TODO: handle exception
        }

        return null;
    }


    public static void getOAID_API(final Context context, final DevicesIDsHelper.AppIdsUpdater appIdsUpdater) {
        if (TextUtils.isEmpty(mOAID_API)) {
            if (oaidRetryCount > 10 || oaidWaitCallback || System.currentTimeMillis() - oaid_API_Limt < 1000) {
                if (appIdsUpdater != null) {
                    appIdsUpdater.OnIdsAvalid("");
                }
                return;
            }

            oaid_API_Limt = System.currentTimeMillis();
            if (mOAID_API_Task == null) {
                Log.d("", "Thread create ,current thread num :" + Thread.activeCount());
                mOAID_API_Task = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            DevicesIDsHelper devicesIDsHelper = new DevicesIDsHelper();

                            SigmobLog.d("private  getOAID_API");
                            devicesIDsHelper.getOAIDSrc(context, new DevicesIDsHelper.AppIdsUpdater() {
                                @Override
                                public void OnIdsAvalid(String oaid) {
                                    oaidWaitCallback = false;
                                    if (!TextUtils.isEmpty(oaid)) {
                                        mOAID_API = oaid;
                                        if (appIdsUpdater != null) {
                                            appIdsUpdater.OnIdsAvalid(mOAID_API);
                                        }
                                    }

                                    Log.d("oaid", "oaid_src: " + oaid);
                                    mOAID_API_Task = null;
                                }
                            });
                        } catch (Exception e) {
                            SigmobLog.e(e.getMessage());
                        }
                    }
                });

                mOAID_API_Task.start();

            }

            oaidRetryCount++;
            oaidWaitCallback = true;

        } else {
            if (appIdsUpdater != null) {
                appIdsUpdater.OnIdsAvalid(mOAID_API);
            }
        }

    }


    public static String getVAID() {
        return mVAID;
    }


    public static void getOAID(final Context context, final DevicesIDsHelper.AppIdsUpdater appIdsUpdater) {

        if (TextUtils.isEmpty(mOAID)) {
            if (oaidSDKRetryCount > 10 ) {
                if (appIdsUpdater != null) {
                    appIdsUpdater.OnIdsAvalid("");
                }
                return;
            }


            oaid_Limt = System.currentTimeMillis();

            if (mOAID_Task == null) {
                Log.d("", "Thread create ,current thread num :" + Thread.activeCount());
                mOAID_Task = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SigmobLog.d("private  getOAID");

                        DevicesIDsHelper.getOAID(context, new DevicesIDsHelper.AppIdsUpdater() {
                            @Override
                            public void OnIdsAvalid(String oaid) {
                                mOAID = oaid;

                                if (appIdsUpdater != null) {
                                    appIdsUpdater.OnIdsAvalid(oaid);
                                }

                                oaidSDKWaitCallback = false;
                                if (mHandler != null) {
                                    mHandler.removeCallbacksAndMessages(null);
                                    mHandler = null;
                                }
                            }
                        });
                    }
                });
                mOAID_Task.start();
                oaidSDKRetryCount++;
                oaidSDKWaitCallback = true;
                mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mOAID_Task != null) {
                            oaidSDKWaitCallback = false;
                            oaidSDKRetryCount = 11;
                            if (appIdsUpdater != null) {
                                appIdsUpdater.OnIdsAvalid("");
                            }
                            if (mHandler != null){
                                mHandler.removeCallbacksAndMessages(null);
                                mHandler = null;
                            }
                        }
                    }
                }, 10 * 1000);
            }


        } else {
            if (appIdsUpdater != null) {
                appIdsUpdater.OnIdsAvalid(mAAID);
            }
        }


    }

    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context, int index) {
        try {

            SigmobLog.d("private :getIMEI " + index);

            String deviceUniqueIdentifier = null;
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) return null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deviceUniqueIdentifier = tm.getImei(index);

                if (TextUtils.isEmpty(deviceUniqueIdentifier)) {
                    try {
                        return tm.getDeviceId(index);
                    } catch (Throwable e) {

                    }

                    return tm.getMeid(index);
                }
            } else {
                deviceUniqueIdentifier = tm.getDeviceId();
            }
            if (deviceUniqueIdentifier != null) {
                return deviceUniqueIdentifier;
            }
        } catch (Exception e) {
            //TODO: handle exception
        }
        return null;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getIMSI(Context context) {

//        try {
//            if (!isCanUsePhoneState(context) || !isCanRetryIMEI()) return null;
//
//            final TelephonyManager telephonyManager =
//                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            if (telephonyManager != null) {
//
//                return telephonyManager.getSubscriberId();
//
//            }
//        } catch (Throwable e) {
//            SigmobLog.e(e.getMessage());
//        }
        return null;
    }


    /**
     * 获取 Wifi MAC 地址
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
     */

    public static String getMacAddress() {
        //wifi mac地址

//        try {
//
//            if (!TextUtils.isEmpty(mMacAddress)) {
//                return mMacAddress;
//            }
//
//            String interfaceName = "wlan0";
//            List<NetworkInterface> interfaces = Collections
//                    .list(NetworkInterface.getNetworkInterfaces());
//            for (NetworkInterface intf : interfaces) {
//
//                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
//                    continue;
//                }
//                byte[] mac = intf.getHardwareAddress();
//                if (mac == null) {
//                    return null;
//                }
//
//                StringBuilder buf = new StringBuilder();
//                for (byte aMac : mac) {
//                    buf.append(String.format("%02X:", aMac));
//                }
//                if (buf.length() > 0) {
//                    buf.deleteCharAt(buf.length() - 1);
//                }
//                mMacAddress = buf.toString();
//            }
//        } catch (Throwable th) {
//
//            SigmobLog.e(th.getMessage());
//        }

        return "";
    }


    @SuppressLint("MissingPermission")
    public static String getWifimac(Context context) {
//        try {
//
//
//            //wifi mac地址
//            WifiManager wifi = getWifiManager(context);
//
//            if (wifi == null) return null;
//
//            if (TextUtils.isEmpty(mBssid)) {
//                WifiInfo info = wifi.getConnectionInfo();
//                mBssid = info.getBSSID();
//            }
//
//            return mBssid;
//        } catch (Throwable e) {
//
//        }
        return "";

    }


    @SuppressLint("MissingPermission")
    public static String getWifiName(Context context) {


//        try {
//            //wifi mac地址
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                WifiManager wifi = getWifiManager(context);
//                if (wifi == null) return null;
//                WifiInfo info = wifi.getConnectionInfo();
//                mWifiName = info.getSSID();
//
//            } else {
//                NetworkInfo wifiInfo = getConnectivityManager(context).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//                String wifiName = wifiInfo.getExtraInfo();
//
//                if (wifiName.startsWith("\"")) {
//                    wifiName = wifiName.substring(1, wifiName.length());
//                }
//                if (wifiName.endsWith("\"")) {
//                    wifiName = wifiName.substring(0, wifiName.length() - 1);
//                }
//
//                mWifiName = wifiName;
//            }
//
//        } catch (Throwable t) {
//
//        }
        return mWifiName;

    }


}