package com.gt.sdk.base.services;

import android.annotation.SuppressLint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.logger.SigmobLog;


public class LocationService implements ServiceFactory.Service {

    private static final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if (ClientMetadata.getInstance() == null) return;

            ClientMetadata.getInstance().setLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    private static LocationManager locationManager;
    private static String bestProviderString = LocationManager.NETWORK_PROVIDER;
    private ServiceStatus status = ServiceStatus.STOP;


    LocationService() {
        if (ClientMetadata.getInstance() == null) return;
        LocationManager locationManager = getLocationManager();
        if (locationManager != null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);// 设置为最大精度
            criteria.setAltitudeRequired(false);//不要求海拔信息
            criteria.setBearingRequired(false);// 不要求方位信息
            criteria.setCostAllowed(false);//是否允许付费
            criteria.setPowerRequirement(Criteria.POWER_LOW);// 对电量的要求
            bestProviderString = locationManager.getBestProvider(criteria, false);
        }
    }

    static LocationManager getLocationManager() {
        if (locationManager == null) {
            synchronized (LocationService.class) {
                if (locationManager == null) {

                    locationManager = ClientMetadata.getInstance().getLocationManager();
                }
            }
        }
        return locationManager;
    }

    @SuppressLint("MissingPermission")
    private void stopUpdatalocation() {
        try {
            if (locationManager != null)
                locationManager.removeUpdates(locationListener);

            locationManager = null;
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

    }

    @SuppressLint("MissingPermission")
    private void startUpdateLocation() {
        try {
            LocationManager locationManager = getLocationManager();
            if (locationManager != null) {
                if (bestProviderString != null && locationManager.isProviderEnabled(bestProviderString)) {
                    SigmobLog.d("private :use_location ");
                    locationManager.requestLocationUpdates(bestProviderString, 10000, 10, locationListener);
                    status = ServiceStatus.RUNNING;
                }
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
    }

    @Override
    public boolean startService() {
        if (status != ServiceStatus.RUNNING) {
            startUpdateLocation();
            return true;
        }
        return false;
    }

    @Override
    public ServiceStatus getStatus() {
        return status;
    }

    @Override
    public void stopService() {
        if (status == ServiceStatus.RUNNING) {
            stopUpdatalocation();
            status = ServiceStatus.STOP;
        }


    }

    @Override
    public Error getError() {
        return null;
    }
}
