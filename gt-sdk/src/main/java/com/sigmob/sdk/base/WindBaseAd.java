package com.sigmob.sdk.base;

import static com.czhj.sdk.common.models.AdStatus.AdStatusLoading;
import static com.czhj.sdk.common.models.AdStatus.AdStatusNone;
import static com.czhj.sdk.common.models.AdStatus.AdStatusReady;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.Database.SQLiteBuider;
import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.Sigmob;
import com.sigmob.sdk.base.common.LoadCacheItem;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.db.SQLiteHelper;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmobRequest;
import com.sigmob.sdk.base.network.BidTrackingRequest;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAdRequest;
import com.sigmob.windad.WindAds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class WindBaseAd {
    public AdStatus mADStatus = AdStatusNone;

    public int pIdEmpty_filters = 0;
    public int init_filters = 0;
    public int loadCount = 0;

    private LoadFilterItem loadFilterItem;

    private static HashMap<String, LoadFilterItem> loadFilterMap = new HashMap<>();
    private int invalidLoadCount = 0;
    private boolean isBidType;
    private String bid_token;

    protected WindAdRequest mRequest;
    private int personalized_filters = 0;

    protected WindBaseAd(WindAdRequest windAdRequest, boolean isHalfInterstitial) {
        this.mRequest = windAdRequest;
        this.mRequest.setHalfInterstitial(isHalfInterstitial);
    }

    protected String getPlacementId() {
        return mRequest != null ? mRequest.getPlacementId() : null;
    }

    private void reset() {

        pIdEmpty_filters = 0;
        clearAdLoadEvent(getPlacementId());
        loadFilterItem.reset();
    }

    protected void setBidType(boolean isBidType) {
        this.isBidType = isBidType;
    }

    public boolean loadAd() {
        this.isBidType = false;
        this.bid_token = null;
        return false;
    }

    public String getBid_token() {
        return bid_token;
    }

    public boolean loadAd(String bid_token) {
        this.isBidType = true;
        this.bid_token = bid_token;
        return false;

    }

    protected abstract void onAdLoadFail(WindAdError adError);


    private void convert(LoadFilterItem loadFilterItem, Cursor cursor) {

        if (cursor != null && cursor.moveToFirst()) {

            int error_code = cursor.getInt(cursor.getColumnIndexOrThrow("error_code"));
            int num = cursor.getInt(cursor.getColumnIndexOrThrow("num"));

            do {

                if (error_code != 0) {
                    loadFilterItem.invalidLoadCount = num;
                }
                loadFilterItem.loadCount += num;

                switch (error_code) {
                    //ERROR_SIGMOB_NOT_INIT
                    case 600900: {

                        loadFilterItem.init_filters = num;
                    }
                    break;

                    //ERROR_AD_LOAD_FAIL_LOADING
                    case 610012: {
                        loadFilterItem.loading_filters = num;

                    }
                    break;
                    //ERROR_SIGMOB_GDPR_DENIED
                    case 600905: {
                        loadFilterItem.gdpr_filters = num;

                    }
                    break;
                    //ERROR_LOAD_FILTER_FOR_PROVIDER_XML_ERROR
                    case 600200:
                    case 600201:
                    case 600203:
                    case 600204: {
                        loadFilterItem.proguard_filters = num;

                    }
                    break;
                    //ERROR_AD_LOAD_FAIL_INTERVAL
                    case 610013: {
                        loadFilterItem.interval_filters = num;

                    }
                    break;
                    default: {

                    }
                }
            } while (cursor.moveToNext());

        }

    }


    private LoadFilterItem getLoadFilterEvent(String placement) {
        LoadFilterItem loadFilterItem = new LoadFilterItem();

        Cursor cursor = null;
        try {
            SQLiteDatabase readableDatabase = SQLiteHelper.getInstance().getReadableDatabase();

            String sql = "select error_code, count(*) as num from " + SQLiteHelper.TABLE_ADLOAD_EVENT + " where adslot_id = '" + placement + "' group by adslot_id";
            cursor = readableDatabase.rawQuery(sql, null);

            convert(loadFilterItem, cursor);
        } catch (Throwable t) {
            SigmobLog.e(t.getMessage());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return loadFilterItem;
    }


    private void clearAdLoadEvent(String placementId) {
        if (TextUtils.isEmpty(placementId)) return;

        try {
            SQLiteDatabase writableDatabase = SQLiteHelper.getInstance().getWritableDatabase();
            writableDatabase.delete(SQLiteHelper.TABLE_ADLOAD_EVENT, "adslot_id=?", new String[]{placementId});
        } catch (Throwable th) {
            SigmobLog.e(th.getMessage());

        }


    }

    private void addAdLoadErrorEvent(final String placementId, final int error_code) {
        if (TextUtils.isEmpty(placementId)) return;


        try {
            SQLiteDatabase writableDatabase = SQLiteHelper.getInstance().getWritableDatabase();
            SQLiteBuider.Insert.Builder builder = new SQLiteBuider.Insert.Builder();
            builder.setTableName(SQLiteHelper.TABLE_ADLOAD_EVENT);
            Map<String, Object> values = new HashMap<>();

            values.put("adslot_id", placementId);
            values.put("error_code", error_code);
            values.put("timestamp", System.currentTimeMillis());

            builder.setColumnValues(values);
            SQLiteHelper.getInstance().insert(writableDatabase, builder.build(), new SQLiteHelper.ExecCallBack() {
                @Override
                public void onSuccess() {

                    SigmobLog.d(placementId + " insert  load error event " + error_code + " onSuccess: ");
                }

                @Override
                public void onFailed(Throwable e) {
                    SigmobLog.e(placementId + " insert  load error event " + error_code + " onFailed: ", e);

                }
            });

        } catch (Throwable th) {

            SigmobLog.e(th.getMessage());
        }

    }


    public boolean loadAdFilter() {

        WindAdError adError = null;

        if (!WindAds.sharedAds().isInit()) {
            init_filters++;
            adError = WindAdError.ERROR_SIGMOB_NOT_INIT;
            SigmobLog.e("WindAds not initialize");

//        } else if (!WindAds.sharedAds().isPersonalizedAdvertisingOn()) {
//            personalized_filters++;
//            adError = WindAdError.ERROR_SIGMOB_PERSONALIZED_OFF;

        } else {
            SDKContext.setHasAdLoaded(true);

            if (mRequest == null || TextUtils.isEmpty(mRequest.getPlacementId())) {

                adError = WindAdError.ERROR_SIGMOB_PLACEMENTID_EMPTY;
                pIdEmpty_filters++;
                SigmobLog.e("PlacementId with WindAdRequest can't is null");

            } else {
                loadFilterItem = loadFilterMap.get(getPlacementId());
                if (loadFilterItem == null) {
                    loadFilterItem = getLoadFilterEvent(getPlacementId());
                    loadFilterItem.init_filters = init_filters;
                    loadFilterItem.personalized_filters = personalized_filters;
                    loadFilterMap.put(getPlacementId(), loadFilterItem);
                }

                if (isBidType && TextUtils.isEmpty(bid_token)) {
                    loadFilterItem.bidToken_filters++;
                    adError = WindAdError.ERROR_SIGMOB_BID_TOKEN_IS_EMPTY;

                } else if (mADStatus != AdStatusReady) {

                    if (mADStatus == AdStatusLoading) {
                        loadFilterItem.loading_filters++;
                        adError = WindAdError.ERROR_AD_LOAD_FAIL_LOADING;
                    } else if (Sigmob.getInstance().getSigMobError() != null) {
                        adError = Sigmob.getInstance().getSigMobError();
                        loadFilterItem.proguard_filters++;
                    } else if (!PrivacyManager.getInstance().canCollectPersonalInformation()) {

                        SigmobLog.e("User GDPR Consent Status is denied");
                        adError = WindAdError.ERROR_SIGMOB_GDPR_DENIED;
                        loadFilterItem.gdpr_filters++;

                    } else if (WindSDKConfig.getInstance().getLoadPeriodTime() > 0) {

                        long loadPeriodTime = WindSDKConfig.getInstance().getLoadPeriodTime();

                        long start = loadPeriodTime + loadFilterItem.lastLoadTime;
                        long end = System.currentTimeMillis();
                        if (start > end) {
                            SigmobLog.e("load interval Time error");
                            loadFilterItem.interval_filters++;
                            adError = WindAdError.ERROR_AD_LOAD_FAIL_INTERVAL;
                        }
                    }
                }
            }

        }


        if (loadFilterItem == null) {
            loadCount++;
        } else {
            loadFilterItem.loadCount += loadCount + 1;
            loadCount = 0;
        }

        if (adError != null) {

            if (loadFilterItem == null) {
                invalidLoadCount++;
            } else {
                addAdLoadErrorEvent(getPlacementId(), adError.getErrorCode());
                loadFilterItem.invalidLoadCount += invalidLoadCount + 1;
                invalidLoadCount = 0;
            }
//            PointEntitySigmobUtils.SigmobError(PointCategory.REQUEST,null, adError.getErrorCode(),adError.getMessage(), mRequest,null,null,new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
//                @Override
//                public void onAddExtra(Object pointEntityBase) {
//                    if (pointEntityBase instanceof PointEntitySigmobRequest) {
//                        PointEntitySigmobRequest entityError = (PointEntitySigmobRequest) pointEntityBase;
//                        entityError.setAdx_id(null);
//                    }
//                }
//            });
            onAdLoadFail(adError);
            return false;
        }


        ClientMetadata.setUserId(mRequest.getUserId());
        return true;
    }
    protected void sendRequestEvent(LoadCacheItem loadCacheItem) {
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
                    options.put("feed_pre_reque st_count", String.valueOf(loadCacheItem.media_request_count));
                    options.put("feed_pre_ready_count", String.valueOf(loadCacheItem.media_ready_count));
                    options.put("is_minor", PrivacyManager.getInstance().isAdult() ? Constants.FAIL : Constants.SUCCESS);
                    options.put("is_unpersonalized", PrivacyManager.getInstance().isPersonalizedAdvertisingOn() ? Constants.FAIL : Constants.SUCCESS);
                    options.put("personalized_filters", String.valueOf(loadFilterItem.personalized_filters));
                    entityInit.setOptions(options);

                }
                reset();

            }
        });

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

    /**
     * 设置广告的底价，单位：分（仅支持bidding模式）
     */

    public void setBidFloor(int bidFloor) {
        this.bidFloor = bidFloor;

    }

    public int getBidFloor() {
        return bidFloor;
    }

    private int bidFloor;

    public String getCurrency() {
        return currency;
    }

    private String currency = WindAds.CNY;

    /**
     * 参与竞价的结算模式(CNY:人民币；USD:美元) 默认：CNY
     *
     * @param currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * 媒体二次议价, 单位分
     *
     * @param bidEcpm
     */
    public void setBidEcpm(int bidEcpm) {
        doMacro(SigMacroCommon._PUBLISHERPRICE_, String.valueOf(bidEcpm));
    }

    /**
     * 返回广告对应的ecpm（该接口需要在广告加载成功之后会返回对应的ecpm）
     * 当非竞价广告时或者没有权限访问该部分会返回nil 单位：分
     *
     * @return
     */
    public abstract String getEcpm();

    protected abstract Map<String, BiddingResponse> getBidInfo();

    /**
     * 竞胜之后调用, 需要在调用广告 show 之前调用
     *
     * @param winInfo 字典类型，必填。支持的key有
     *                AUCTION_PRICE：竞胜价格 (单位: 分)，值类型为NSNumber
     *                HIGHEST_LOSS_PRICE：最高失败出价，值类型为NSNumber
     *                CURRENCY：币种，默认CNY，美元：USD, 值类型为NSString
     */
    public void sendWinNotificationWithInfo(Map<String, Object> winInfo) {
        try {
            if (getBidInfo() != null) {
                Set<String> keys = getBidInfo().keySet();
                if (keys != null && keys.size() > 0) {
                    for (String request_id : keys) {

                        BiddingResponse bidding_response = getBidInfo().get(request_id);
                        if (bidding_response != null) {
                            String win_url = bidding_response.win_url;
                            if (!TextUtils.isEmpty(win_url)) {

                                if (winInfo != null && winInfo.size() > 0) {
                                    if (winInfo.get(WindAds.AUCTION_PRICE) != null) {
                                        doMacro(SigMacroCommon._PUBLISHERPRICE_, String.valueOf(winInfo.get(WindAds.AUCTION_PRICE)));
                                        win_url = win_url.replace("__AUCTION_PRICE__", String.valueOf(winInfo.get(WindAds.AUCTION_PRICE)));
                                    }
                                    if (winInfo.get(WindAds.HIGHEST_LOSS_PRICE) != null) {
                                        doMacro(SigMacroCommon._HIGHESTLOSSPRICE_, String.valueOf(winInfo.get(WindAds.HIGHEST_LOSS_PRICE)));
                                        win_url = win_url.replace("__HIGHEST_LOSS_PRICE__", String.valueOf(winInfo.get(WindAds.HIGHEST_LOSS_PRICE)));
                                    }
                                    if (winInfo.get(WindAds.CURRENCY) != null) {
                                        doMacro(SigMacroCommon._CURRENCY_, String.valueOf(winInfo.get(WindAds.CURRENCY)));
                                        win_url = win_url.replace("__CURRENCY__", String.valueOf(winInfo.get(WindAds.CURRENCY)));
                                    } else {
                                        doMacro(SigMacroCommon._CURRENCY_, this.currency);
                                        win_url = win_url.replace("__CURRENCY__", this.currency);
                                    }
                                }

                                BidTrackingRequest.sendTracking(win_url, PointCategory.WIN, mRequest, request_id);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void doMacro(String key, String value);

    /**
     * 竞败之后或未参竞调用
     *
     * @pararm lossInfo 竞败信息，字典类型，必填，支持的key有
     * AUCTION_PRICE ：竞胜价格 (单位: 分)，选填。值类型为NSNumber
     * CURRENCY：币种，默认CNY，美元：USD, 值类型为NSString
     * LOSS_REASON ：广告竞败原因，必填。竞败原因参考枚举WindAdBiddingLossReason中的定义，值类型为NSNumber
     * ADN_ID ：竞胜方渠道ID，必填。值类型为NSString *
     * 1 ：sigmob；2：穿山甲；3: 腾讯广告；4:快手；5:百度；
     * 6:mtg；7:vungle；8:facebook；10001 ：其他
     */
    public void sendLossNotificationWithInfo(Map<String, Object> lossInfo) {
        try {
            if (getBidInfo() != null) {
                Set<String> keys = getBidInfo().keySet();
                if (keys != null && keys.size() > 0) {
                    for (String request_id : keys) {
                        BiddingResponse bidding_response = getBidInfo().get(request_id);
                        if (bidding_response != null) {
                            String lose_url = bidding_response.lose_url;
                            if (!TextUtils.isEmpty(lose_url)) {

                                if (lossInfo != null && lossInfo.size() > 0) {
                                    if (lossInfo.get(WindAds.AUCTION_PRICE) != null) {
                                        doMacro(SigMacroCommon._PUBLISHERPRICE_, String.valueOf(lossInfo.get(WindAds.AUCTION_PRICE)));
                                        lose_url = lose_url.replace("__AUCTION_PRICE__", String.valueOf(lossInfo.get(WindAds.AUCTION_PRICE)));
                                    }
                                    if (lossInfo.get(WindAds.CURRENCY) != null) {
                                        doMacro(SigMacroCommon._CURRENCY_, String.valueOf(lossInfo.get(WindAds.CURRENCY)));
                                        lose_url = lose_url.replace("__CURRENCY__", String.valueOf(lossInfo.get(WindAds.CURRENCY)));
                                    } else {
                                        doMacro(SigMacroCommon._CURRENCY_, this.currency);
                                        lose_url = lose_url.replace("__CURRENCY__", this.currency);
                                    }
                                    if (lossInfo.get(WindAds.LOSS_REASON) != null) {
                                        lose_url = lose_url.replace("_BIDLOSSCODE_", String.valueOf(lossInfo.get(WindAds.LOSS_REASON)));
                                    }
                                    if (lossInfo.get(WindAds.ADN_ID) != null) {
                                        lose_url = lose_url.replace("_WINADNID_", String.valueOf(lossInfo.get(WindAds.ADN_ID)));
                                    }
                                }

                                BidTrackingRequest.sendTracking(lose_url, PointCategory.LOSE, mRequest, request_id);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
