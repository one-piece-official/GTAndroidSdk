package com.gt.adsdk.base;

import static com.czhj.sdk.common.models.AdStatus.AdStatusNone;

import android.text.TextUtils;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.models.AdStatus;
import com.gt.adsdk.AdRequest;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmobRequest;

import java.util.HashMap;

public abstract class GtBaseAd {

    public AdStatus adStatus = AdStatusNone;

    protected AdRequest mAdRequest;

    protected GtBaseAd(AdRequest adRequest) {
        this.mAdRequest = adRequest;
    }

    public boolean loadAdFilter() {
        return true;
    }

    protected void sendRequestEvent() {
        PointEntitySigmobUtils.SigmobRequestTracking(PointCategory.REQUEST, PointCategory.INIT, mRequest, null,new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmobRequest) {
                    PointEntitySigmobRequest entityInit = (PointEntitySigmobRequest) pointEntityBase;
                    entityInit.setLoad_count(String.valueOf(loadFilterItem.loadCount));
                    entityInit.setInvalid_load_count(String.valueOf(loadFilterItem.invalidLoadCount));
                    entityInit.setGdpr_filters(String.valueOf(loadFilterItem.gdpr_filters));
                    entityInit.setInterval_filters(String.valueOf(loadFilterItem.interval_filters));
                    entityInit.setPldempty_filters(String.valueOf(pIdEmpty_filters));
                    entityInit.setInit_filters(String.valueOf(loadFilterItem.init_filters));
                    entityInit.setLoading_filters(String.valueOf(loadFilterItem.loading_filters));
                    entityInit.setProguard_filters(String.valueOf(loadFilterItem.proguard_filters));
                    if (!TextUtils.isEmpty(bid_token)) {
                        entityInit.setBid_token(bid_token);
                    }
                    entityInit.setAdx_id(null);
                    HashMap<String, String> options = new HashMap<>();
                    options.put("is_minor", PrivacyManager.getInstance().isAdult() ? Constants.FAIL : Constants.SUCCESS);
                    options.put("is_unpersonalized", PrivacyManager.getInstance().isPersonalizedAdvertisingOn() ? Constants.FAIL : Constants.SUCCESS);
                    options.put("personalized_filters", String.valueOf(loadFilterItem.personalized_filters));
                    entityInit.setOptions(options);

                }
                reset();

            }
        });

    }

}
