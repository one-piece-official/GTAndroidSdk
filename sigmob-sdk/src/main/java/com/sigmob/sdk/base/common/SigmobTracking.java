package com.sigmob.sdk.base.common;

import static com.sigmob.sdk.base.common.PointEntitySigmobUtils.updatePointEntitySigmob;
import static com.sigmob.sdk.base.common.PointEntitySigmobUtils.updateADunitInfo;
import static com.sigmob.sdk.base.common.PointEntitySigmobUtils.updateAdRequestInfo;
import static com.sigmob.sdk.base.common.PointEntitySigmobUtils.updateLoadRequestInfo;

import android.text.TextUtils;

import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.mta.PointType;
import com.sigmob.windad.WindAdRequest;

public class SigmobTracking {
    private String category;
    private String sub_category;
    private String ad_type;
    private String placementId;
    private String ac_type = PointType.SIGMOB_TRACKING;

    private WindAdRequest windAdRequest;
    private LoadAdRequest loadAdRequest;
    private PointEntitySigmobUtils.OnPointEntityExtraInfo onPointEntityExtraInfo;
    private BaseAdUnit adUnit;


    private SigmobTracking(){

    }

    public static SigmobTracking getSigmobTracking(String category){

        SigmobTracking sigmobTracking = new SigmobTracking();
        sigmobTracking.category = category;

        return sigmobTracking;
    }
    
    public SigmobTracking setAd_type(String ad_type) {
        this.ad_type = ad_type;
        return this;
    }


    public SigmobTracking setAc_Type(String ac_type) {
        this.ad_type = ad_type;
        return this;
    }


    public SigmobTracking setAdUnit(BaseAdUnit adUnit) {
        this.adUnit = adUnit;
        return this;

    }

    public SigmobTracking setSub_category(String sub_category) {
        this.sub_category = sub_category;
        return this;

    }

    public SigmobTracking setPlacementId(String placementId) {
        this.placementId = placementId;
        return this;
    }

    public SigmobTracking setLoadAdRequest(LoadAdRequest loadAdRequest) {
        this.loadAdRequest = loadAdRequest;
        return this;
    }

    public SigmobTracking setOnPointEntityExtraInfo(PointEntitySigmobUtils.OnPointEntityExtraInfo onPointEntityExtraInfo) {
        this.onPointEntityExtraInfo = onPointEntityExtraInfo;
        return this;
    }

    public SigmobTracking setWindAdRequest(WindAdRequest windAdRequest) {
        this.windAdRequest = windAdRequest;
        return this;

    }
    public void send(){

        PointEntitySigmob entitySigmob = new PointEntitySigmob();


        entitySigmob.setAc_type(ac_type);
        entitySigmob.setCategory(category);
        entitySigmob.setSub_category(sub_category);

        if(!TextUtils.isEmpty(ad_type)){
            entitySigmob.setAdtype(ad_type);
        }
        
        updateAdRequestInfo(category, sub_category, entitySigmob, windAdRequest);
        updateLoadRequestInfo(category, sub_category, entitySigmob, loadAdRequest);
        updateADunitInfo(category, sub_category, adUnit, entitySigmob);

        if (onPointEntityExtraInfo != null) {
            onPointEntityExtraInfo.onAddExtra(entitySigmob);
        }

        updatePointEntitySigmob(entitySigmob);

        entitySigmob.commit();
    }



}
