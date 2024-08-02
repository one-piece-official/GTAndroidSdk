package com.sigmob.sdk.nativead;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.LoadCacheItem;
import com.sigmob.sdk.base.common.LoadReadyItem;
import com.sigmob.sdk.base.common.NativeLoadReadyRecordManager;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.RequestFactory;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.WindNativeAdData;
import com.sigmob.windad.natives.WindNativeAdRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigmobNativeAd implements RequestFactory.LoadAdRequestListener {

    public static final int ADVIEW_MODEL_VIDEO = 1;
    public static final int ADVIEW_MODEL_BIG_IMAGE = 2;
    public static final int ADVIEW_MODEL_THREE_IMAGES = 3;
    private Handler mHandler;
    private List<BaseAdUnit> adUnits;
    private SigmobNativeAdLoadListener mSigmobNativeAdLoadListener;
    private WindNativeAdRequest mAdRequest;
    private boolean isLoading;
    private final int MSG_LOAD = 0x5001;

    public SigmobNativeAd(final WindNativeAdRequest adRequest, SigmobNativeAdLoadListener sigmobNativeAdLoadListener) {

        mAdRequest = adRequest;
        mSigmobNativeAdLoadListener = sigmobNativeAdLoadListener;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_LOAD: {//总的广告加载时常
                        if (isLoading) {
                            mHandler.removeMessages(MSG_LOAD);
                            notifyLoadResult(null, WindAdError.ERROR_SIGMOB_AD_TIME_OUT.getErrorCode(), WindAdError.ERROR_SIGMOB_AD_TIME_OUT.getMessage());
                        }
                    }
                    break;
                }
            }
        };

    }

    private void notifyLoadResult(final List<WindNativeAdData> adUnit, final int error, final String message) {
        isLoading = false;
        mHandler.removeMessages(MSG_LOAD);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSigmobNativeAdLoadListener != null) {
                    if (error != 0) {
                        mSigmobNativeAdLoadListener.onNativeAdLoadFail(error, message);
                    } else {
                        mSigmobNativeAdLoadListener.onNativeAdLoaded(adUnit);
                    }
                }
            }
        });
    }


    public int loadAd(int adCount, String bidToken, int bidFloor, String currency) {


        int type = 0;
        isLoading = true;


        LoadCacheItem loadCacheItem = LoadCacheItem.loadCacheItem(mAdRequest.getPlacementId());
        LoadAdRequest loadAdRequest = new LoadAdRequest(mAdRequest);
        loadAdRequest.setBidToken(bidToken);
        loadAdRequest.setBidFloor(bidFloor);
        loadAdRequest.setCurrency(currency);
        loadAdRequest.setAd_count(adCount);

        loadCacheItem.media_request_count++;

        LoadReadyItem loadReadyItem = LoadReadyItem.loadReadyItem(mAdRequest.getPlacementId());

        loadReadyItem.media_request_count++;

        if (adCount < 1){
            notifyLoadResult(null, WindAdError.ERROR_SIGMOB_NATIVE_ADCOUNT.getErrorCode(), WindAdError.ERROR_SIGMOB_NATIVE_ADCOUNT.getMessage());
        }else {
            if ( loadCacheItem.load_time>0 && loadCacheItem.load_time + loadCacheItem.req_interval_time*1000 > System.currentTimeMillis()) {

                int bid_floor = loadCacheItem.media_expected_floor;

                List<BaseAdUnit> baseAdUnits = AdStackManager.getAdCacheList(mAdRequest.getPlacementId(), bid_floor, adCount);
                if (baseAdUnits.isEmpty()){
                    notifyLoadResult(null, WindAdError.ERROR_NO_AD.getErrorCode(), WindAdError.ERROR_NO_AD.getMessage());
                }else{
                    this.adUnits = baseAdUnits;
                    ArrayList<WindNativeAdData> adList = new ArrayList<>();


                    for (int i = 0; i < baseAdUnits.size(); i++) {
                        BaseAdUnit adUnit = baseAdUnits.get(i);

                        adUnit.setLoad_id(loadAdRequest.getLoadId());
                        WindNativeAdData ad = getSigmobNativeAdUnit(adUnit);
                        adList.add(ad);
                        if (adUnit.getAd_source_channel().equalsIgnoreCase(WindConstants.SIGMOB_CHANNEL)) {
                            AdStackManager.shareInstance().addHistoryAdCache(adUnit);
                        }
                    }
                    PointEntitySigmobUtils.SigmobTracking(PointCategory.READY, null, adUnits.get(0), loadAdRequest, null);
                    loadCacheItem.media_ready_count += baseAdUnits.size();

                    loadReadyItem.media_ready_count += baseAdUnits.size();

                    notifyLoadResult(adList, 0, null);
                }
            }else{
                mHandler.sendEmptyMessageDelayed(MSG_LOAD, WindSDKConfig.getInstance().loadNativeAdTimeout());
                loadCacheItem.load_time = System.currentTimeMillis();
                RequestFactory.LoadAd(loadAdRequest, this);
                type = 1;
            }
        }
        LoadCacheItem.addAdCacheLoadEvent(mAdRequest.getPlacementId(), loadCacheItem);

        LoadReadyItem.addAdLoadReadyEvent(mAdRequest.getPlacementId(), loadReadyItem);

        return type;

    }

    private WindNativeAdData getSigmobNativeAdUnit(BaseAdUnit adUnit) {
        WindNativeAdData nativeAdUnit = new WindNativeAdUnitObject(adUnit);
        return nativeAdUnit;
    }

    @Override
    public void onSuccess(final List<BaseAdUnit> adUnits, final LoadAdRequest loadAdRequest) {

        PointEntitySigmobUtils.SigmobTracking(PointCategory.RESPOND, Constants.SUCCESS, null, loadAdRequest, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {
                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                    HashMap<String, String> extData = new HashMap<>();
                    extData.put("ad_count", String.valueOf(adUnits.size()));
                    entitySigmob.setOptions(extData);
                }
            }
        });
        LoadCacheItem loadCacheItem = LoadCacheItem.loadCacheItem(mAdRequest.getPlacementId());


        if(loadCacheItem.req_pool_size >0) {
            List<BaseAdUnit> baseAdUnits = AdStackManager.getAdCacheList(mAdRequest.getPlacementId(), loadCacheItem.media_expected_floor, mAdRequest.getAdCount());
            if (baseAdUnits == null || baseAdUnits.isEmpty()) {
                this.adUnits = null;
                notifyLoadResult(null, WindAdError.ERROR_NO_AD.getErrorCode(), WindAdError.ERROR_NO_AD.getMessage());
                return;
            }
            this.adUnits = baseAdUnits;
        }else{
            this.adUnits = adUnits;
        }


        LoadReadyItem loadReadyItem = LoadReadyItem.loadReadyItem(mAdRequest.getPlacementId());
        loadCacheItem.media_ready_count += this.adUnits.size();

        loadReadyItem.media_ready_count +=  this.adUnits.size();

        LoadCacheItem.addAdCacheLoadEvent(mAdRequest.getPlacementId(), loadCacheItem);
        LoadReadyItem.addAdLoadReadyEvent(mAdRequest.getPlacementId(), loadReadyItem);

//        SigmobLog.i("native_ad cache getAdCacheList load success adUnit size  "+ this.adUnits.size());

        ArrayList<WindNativeAdData> adList = new ArrayList<>();
        for (int i = 0; i < this.adUnits.size(); i++) {
            BaseAdUnit adUnit = this.adUnits.get(i);
            WindNativeAdData ad = getSigmobNativeAdUnit(adUnit);
            adList.add(ad);
            if (adUnit.getAd_source_channel().equalsIgnoreCase(WindConstants.SIGMOB_CHANNEL)) {
                AdStackManager.shareInstance().addHistoryAdCache(adUnit);
            }
        }

        PointEntitySigmobUtils.SigmobTracking(PointCategory.READY, null, this.adUnits.get(0), loadAdRequest, null);
        notifyLoadResult(adList, 0, null);
    }

    @Override
    public void onErrorResponse(int error, String message, String request_id, final LoadAdRequest loadAdRequest) {

        PointEntitySigmobUtils.SigmobTracking(PointCategory.RESPOND, Constants.FAIL, loadAdRequest);

        notifyLoadResult(null, error, message);
    }


    public String getEcpm() {
        if (adUnits != null && adUnits.size() > 0) {
            BaseAdUnit baseAdUnit = adUnits.get(0);
            if (baseAdUnit != null && baseAdUnit.bidding_response != null) {
                return String.valueOf(baseAdUnit.bidding_response.ecpm);
            }
        }
        return null;
    }

    public Map<String, BiddingResponse> getBidInfo() {
        if (adUnits != null) {
            BaseAdUnit baseAdUnit = adUnits.get(0);
            if (baseAdUnit != null && baseAdUnit.bidding_response != null) {
                HashMap<String, BiddingResponse> bidInfo = new HashMap<>();
                bidInfo.put(baseAdUnit.getRequestId(), baseAdUnit.bidding_response);
                return bidInfo;
            }
        }
        return null;
    }


    public void doMacro(String key, String value) {
        if (adUnits != null) {
            BaseAdUnit baseAdUnit = adUnits.get(0);
            if (baseAdUnit != null && baseAdUnit.bidding_response != null) {
                baseAdUnit.getMacroCommon().addMarcoKey(key, value);
            }
        }
    }
}
