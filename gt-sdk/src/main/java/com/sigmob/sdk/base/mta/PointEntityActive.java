package com.sigmob.sdk.base.mta;

public class PointEntityActive extends PointEntitySigmobSuper {
    private String active_id;



    private String request_id;
    private String vid;

    private String duration;

    public String getActive_id() {
        return active_id;
    }

    public void setActive_id(String active_id) {
        this.active_id = active_id;
    }

    public String getDuration() {
        return duration;
    }


    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getVid() {
        return vid;
    }

    public static PointEntityActive ActiveTracking(String category,  String active_id, String duration, String active_time){
        PointEntityActive entityWind = new PointEntityActive();
        
        entityWind.setAc_type(PointType.WIND_ACTIVE);
        entityWind.setCategory(category);
        entityWind.setActive_id(active_id);
        entityWind.setDuration(duration);
        entityWind.setTimestamp(active_time);
        entityWind.commit();
        return entityWind;
    }


}
