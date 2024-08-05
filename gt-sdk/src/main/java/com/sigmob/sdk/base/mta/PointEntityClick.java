package com.sigmob.sdk.base.mta;


public class PointEntityClick extends PointEnitySigmobBase {

    private String location;
    private String click_duration;
    private String is_valid_click;
    private String touchType;
    private String pressure;
    private String touchSize;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getClick_duration() {
        return click_duration;
    }

    public void setClick_duration(String click_duration) {
        this.click_duration = click_duration;
    }

    public String getIs_valid_click() {
        return is_valid_click;
    }

    public void setIs_valid_click(String is_valid_click) {
        this.is_valid_click = is_valid_click;
    }

    public String getTouchType() {
        return touchType;
    }

    public void setTouchType(String touchType) {
        this.touchType = touchType;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getTouchSize() {
        return touchSize;
    }

    public void setTouchSize(String touchSize) {
        this.touchSize = touchSize;
    }
}
