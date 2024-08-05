package com.sigmob.sdk.base.mta;

public class PointEntitySigmobRequest extends PointEntitySigmob {
    private String load_count;
    private String invalid_load_count;
    private String gdpr_filters;
    private String pldempty_filters;
    private String loading_filters;
    private String playing_filters;
    private String interval_filters;
    private String init_filters;
    private String proguard_filters;

    public String getLoad_count() {
        return load_count;
    }

    public void setLoad_count(String load_count) {
        this.load_count = load_count;
    }

    public String getGdpr_filters() {
        return gdpr_filters;
    }

    public void setGdpr_filters(String gdpr_filters) {
        this.gdpr_filters = gdpr_filters;
    }

    public String getPldempty_filters() {
        return pldempty_filters;
    }

    public void setPldempty_filters(String pldempty_filters) {
        this.pldempty_filters = pldempty_filters;
    }

    public String getLoading_filters() {
        return loading_filters;
    }

    public void setLoading_filters(String loading_filters) {
        this.loading_filters = loading_filters;
    }

    public String getPlaying_filters() {
        return playing_filters;
    }

    public void setPlaying_filters(String playing_filters) {
        this.playing_filters = playing_filters;
    }

    public String getInterval_filters() {
        return interval_filters;
    }

    public void setInterval_filters(String interval_filters) {
        this.interval_filters = interval_filters;
    }

    public String getInit_filters() {
        return init_filters;
    }

    public void setInit_filters(String init_filters) {
        this.init_filters = init_filters;
    }

    public String getProguard_filters() {
        return proguard_filters;
    }

    public void setProguard_filters(String proguard_filters) {
        this.proguard_filters = proguard_filters;
    }

    public String getInvalid_load_count() {
        return invalid_load_count;
    }

    public void setInvalid_load_count(String invalid_load_count) {
        this.invalid_load_count = invalid_load_count;
    }
}
