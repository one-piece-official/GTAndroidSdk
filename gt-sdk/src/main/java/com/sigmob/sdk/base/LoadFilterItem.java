package com.sigmob.sdk.base;

public class LoadFilterItem {
    public volatile int invalidLoadCount = 0;
    public volatile int loadCount = 0;
    public volatile int gdpr_filters = 0;
    public volatile int interval_filters = 0;
    public volatile int init_filters = 0;

    public volatile int personalized_filters = 0;
    public volatile int proguard_filters = 0;
    public volatile int loading_filters = 0;
    public volatile int bidToken_filters = 0;
    public volatile long lastLoadTime = 0;


    public void reset() {

        invalidLoadCount = 0;
        gdpr_filters = 0;
        interval_filters = 0;
        proguard_filters = 0;
        init_filters = 0;
        loading_filters = 0;
        loadCount = 0;
        lastLoadTime = System.currentTimeMillis();
    }


}
