package com.gt.sdk.base.models.point;


public class GtPointEntityAdTrack extends GtPointEntityAd {

    private String url;
    private String tracking_type;
    private String http_code;
    private String response;
    private String time_spend;
    private String content_length;
    private String content_type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTracking_type() {
        return tracking_type;
    }

    public void setTracking_type(String tracking_type) {
        this.tracking_type = tracking_type;
    }

    public String getHttp_code() {
        return http_code;
    }

    public void setHttp_code(String http_code) {
        this.http_code = http_code;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getTime_spend() {
        return time_spend;
    }

    public void setTime_spend(String time_spend) {
        this.time_spend = time_spend;
    }

    public String getContent_length() {
        return content_length;
    }

    public void setContent_length(String content_length) {
        this.content_length = content_length;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public static GtPointEntityAdTrack AdTracking(String category, String acType, String placement_id, String adType) {
        GtPointEntityAdTrack entityWind = new GtPointEntityAdTrack();
        entityWind.setAc_type(acType);
        entityWind.setCategory(category);
        entityWind.setAd_type(adType);
        entityWind.setCode_id(placement_id);

        return entityWind;
    }
}
