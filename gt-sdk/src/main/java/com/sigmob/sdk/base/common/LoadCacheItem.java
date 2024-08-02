package com.sigmob.sdk.base.common;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.czhj.sdk.common.Database.SQLiteBuider;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.db.SQLiteHelper;
import com.sigmob.sdk.base.models.BaseAdUnit;

import java.util.HashMap;
import java.util.Map;

public class LoadCacheItem {
    public int media_request_count;
    public int media_ready_count;
    public long pre_req_time;
    public int req_interval_time = 10*60;
    public int req_pool_size = 30;
    public int media_expected_floor;
    public long load_time;

    private static HashMap<String, LoadCacheItem> loadCacheItemMap = new HashMap<String, LoadCacheItem>();


    public static synchronized LoadCacheItem loadCacheItem(String placement) {

        if (TextUtils.isEmpty(placement)) return null;

        LoadCacheItem loadCacheItem = loadCacheItemMap.get(placement);
        if (loadCacheItem == null) {

            loadCacheItem = new LoadCacheItem();
            Cursor cursor = null;
            try {
                SQLiteDatabase readableDatabase = SQLiteHelper.getInstance().getReadableDatabase();

                String sql = "select * from " + SQLiteHelper.TABLE_ADLOAD_CACHE_EVENT + " where adslot_id = '" + placement + "' group by adslot_id";
                cursor = readableDatabase.rawQuery(sql, null);

                convert(loadCacheItem, cursor);
            } catch (Throwable t) {
                SigmobLog.e(t.getMessage());

            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            loadCacheItemMap.put(placement, loadCacheItem);
        }

        return loadCacheItem;
    }

    private static void convert(LoadCacheItem cacheItem, Cursor cursor) {

        if (cursor != null && cursor.moveToFirst()) {

            do {
                cacheItem.media_request_count = cursor.getInt(cursor.getColumnIndexOrThrow("media_request_count"));
                cacheItem.media_ready_count = cursor.getInt(cursor.getColumnIndexOrThrow("media_ready_count"));
                cacheItem.pre_req_time = cursor.getLong(cursor.getColumnIndexOrThrow("pre_req_time"));
                cacheItem.load_time = cacheItem.pre_req_time;
                cacheItem.req_interval_time = cursor.getInt(cursor.getColumnIndexOrThrow("req_interval_time"));
                cacheItem.req_pool_size = cursor.getInt(cursor.getColumnIndexOrThrow("req_pool_size"));

            } while (cursor.moveToNext());

        }

    }


    private void clearAdLoadEvent(String placementId) {
        if (TextUtils.isEmpty(placementId)) return;

        try {
            SQLiteDatabase writableDatabase = SQLiteHelper.getInstance().getWritableDatabase();
            writableDatabase.delete(SQLiteHelper.TABLE_ADLOAD_CACHE_EVENT, "adslot_id=?", new String[]{placementId});
        } catch (Throwable th) {
            SigmobLog.e(th.getMessage());

        }


    }

    public static void addAdCacheLoadEvent(final String placementId, LoadCacheItem item) {
        if (TextUtils.isEmpty(placementId) || item == null) return;

        try {
            SQLiteDatabase writableDatabase = SQLiteHelper.getInstance().getWritableDatabase();
            SQLiteBuider.Insert.Builder builder = new SQLiteBuider.Insert.Builder();
            builder.setTableName(SQLiteHelper.TABLE_ADLOAD_CACHE_EVENT);
            Map<String, Object> values = new HashMap<>();

            values.put("adslot_id", placementId);
            values.put("media_request_count",item.media_request_count);
            values.put("media_ready_count",item.media_ready_count);
            values.put("pre_req_time",item.pre_req_time);
            values.put("req_interval_time",item.req_interval_time);
            values.put("req_pool_size",item.req_pool_size);

            builder.setColumnValues(values);
            SQLiteHelper.getInstance().insert(writableDatabase, builder.build(), new SQLiteHelper.ExecCallBack() {
                @Override
                public void onSuccess() {

                    SigmobLog.d(placementId + " insert load cache event " + placementId + " onSuccess: ");
                }

                @Override
                public void onFailed(Throwable e) {
                    SigmobLog.e(placementId + " insert  load cache event " + placementId + " onFailed: ", e);

                }
            });

        } catch (Throwable th) {

            SigmobLog.e(th.getMessage());
        }

    }
}
