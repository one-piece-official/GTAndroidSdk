package com.sigmob.sdk.base.common;

import static com.sigmob.sdk.base.WindConstants.PLAY_MODE_STREAM;

import android.text.TextUtils;
import android.util.Log;

import com.czhj.sdk.common.Constants;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * created by lance on   2022/7/29 : 3:08 下午
 */
public class AdListCacheManager implements AdStackManager.AdStackStatusListener {
    private String TAG = "AdListCacheManger";
    private final List<BaseAdUnit> adUnits;
    private final AdStackManager.AdStackStatusListener listener;
    private LoadAdRequest loadAdRequest;

    public AdListCacheManager(List<BaseAdUnit> adUnits, AdStackManager.AdStackStatusListener listener, LoadAdRequest loadAdRequest) {
        this.adUnits = adUnits;
        this.listener = listener;
        this.loadAdRequest = loadAdRequest;
        this.catchList = new ArrayList<>();
    }

    private List<BaseAdUnit> catchList;

    public boolean cacheList(boolean showAd) {
        if (adUnits != null) {

            if (showAd) {
                BaseAdUnit baseAdUnit = adUnits.get(0);

                if (baseAdUnit.getPlayMode() == PLAY_MODE_STREAM) {
                    catchList.add(baseAdUnit);
                    AdStackManager.shareInstance().cache(baseAdUnit, this);
                }

            } else {
                for (int i = 0; i < adUnits.size(); i++) {
                    BaseAdUnit baseAdUnit = adUnits.get(i);

                    if (baseAdUnit.getPlayMode() != PLAY_MODE_STREAM) {
                        catchList.add(baseAdUnit);
                        baseAdUnit.setCatchVideo(true);
                        AdStackManager.shareInstance().cache(baseAdUnit, this);
                    }
                }
            }


            Log.d(TAG, "--------cache--------" + catchList.size());

        }

        return catchList.size() > 0;
    }

    @Override
    public void loadStart(BaseAdUnit adUnit) {

        if (listener != null) {
            listener.loadStart(adUnit);
        }
    }

    @Override
    public void loadEnd(BaseAdUnit adUnit, String message) {
        if (adUnit != adUnits.get(0)) {
            SigmobTrackingRequest.sendTrackings(adUnit, TextUtils.isEmpty(message) ? ADEvent.AD_LOAD_SUCCESS : ADEvent.AD_LOAD_FAILURE);
            PointEntitySigmobUtils.SigmobTracking(PointCategory.LOADEND,
                    TextUtils.isEmpty(message) ? Constants.SUCCESS : Constants.FAIL, adUnit, null, loadAdRequest, null);
        } else {
            if (listener != null) {
                listener.loadEnd(adUnits.get(0), message);
            }
            Log.d(TAG, "--------loadEnd--------" + catchList.size());
        }
    }
}
