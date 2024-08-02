package com.sigmob.sdk.base.models;

import com.czhj.sdk.common.utils.TouchLocation;
import com.sigmob.sdk.base.ClickUIType;

import java.io.Serializable;

public class ClickCommon implements Serializable {
    public static final String CLICK_AREA_BTN = "btn";
    public static final String CLICK_AREA_COMPONENT = "component";
    public static final String CLICK_AREA_COMPANION = "companion";

    public static final String CLICK_AREA_MATERIAL = "material";
    public static final String CLICK_AREA_APPINFO = "appinfo";
    public static final String CLICK_SCENE_PREVIEW = "preview";
    public static final String CLICK_SCENE_TEMPLATE = "template";
    public static final String CLICK_SCENE_APPINFO = "appinfo";

    public static final String CLICK_SCENE_AD = "ad";
    public static final String CLICK_SCENE_ENDCARD = "endcard";


    public String click_area;
    public String click_scene;

    public TouchLocation down;

    public TouchLocation up;
    public ClickUIType clickUIType;

    public String clickCoordinate;
    public boolean is_final_click;
    public String isDeeplink;
    public String clickUrl;

    public String template_id;

    public String sld;

    public String adarea_x;

    public String adarea_y;

    public String adarea_w;

    public String adarea_h;

    public String x_max_acc;
    public String y_max_acc;
    public String z_max_acc;

    public String turn_x;
    public String turn_y;
    public String turn_z;
    public String turn_time;
    public int widget_id;

    public String getClickCoordinate() {
        if (down != null && up != null) {
            clickCoordinate = String.format("%s,%s,%s,%s", down.getX(), down.getY(), up.getX(), up.getY());
        }
        return clickCoordinate;
    }
}
