package com.sigmob.sdk.base.common;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.czhj.sdk.common.Database.SQLiteBuider;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.db.SQLiteHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadReadyItem {
    public int media_request_count;
    public int media_ready_count;
    private static HashMap<String, LoadReadyItem> loadReadyItemMap = new HashMap<String, LoadReadyItem>();
    public String placement_id;


    public static HashMap<String, LoadReadyItem> getLoadReadyItemMap() {
        return loadReadyItemMap;
    }

    public void clear() {
        if (TextUtils.isEmpty(placement_id)) return;
        media_request_count = 0;
        media_ready_count = 0;
        clearAdLoadEvent(placement_id);
    }

    public static synchronized LoadReadyItem loadReadyItem(String placement) {

        if (TextUtils.isEmpty(placement)) return null;

        LoadReadyItem loadReadyItem = loadReadyItemMap.get(placement);
        if (loadReadyItem == null) {

            loadReadyItem = new LoadReadyItem();
            loadReadyItem.placement_id = placement;
            Cursor cursor = null;
            try {
                SQLiteDatabase readableDatabase = SQLiteHelper.getInstance().getReadableDatabase();

                String sql = "select * from " + SQLiteHelper.TABLE_ADLOAD_READY_EVENT + " where adslot_id = '" + placement + "' group by adslot_id";
                cursor = readableDatabase.rawQuery(sql, null);

                convert(loadReadyItem, cursor);
            } catch (Throwable t) {
                SigmobLog.e(t.getMessage());

            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            loadReadyItemMap.put(placement, loadReadyItem);
        }

        return loadReadyItem;
    }

    private static void convert(LoadReadyItem cacheItem, Cursor cursor) {

        if (cursor != null && cursor.moveToFirst()) {

            do {
                cacheItem.media_request_count = cursor.getInt(cursor.getColumnIndexOrThrow("media_request_count"));
                cacheItem.media_ready_count = cursor.getInt(cursor.getColumnIndexOrThrow("media_ready_count"));

            } while (cursor.moveToNext());

        }

    }


    private void clearAdLoadEvent(String placementId) {
        if (TextUtils.isEmpty(placementId)) return;

        try {
            SQLiteDatabase writableDatabase = SQLiteHelper.getInstance().getWritableDatabase();
            writableDatabase.delete(SQLiteHelper.TABLE_ADLOAD_READY_EVENT, "adslot_id=?", new String[]{placementId});
        } catch (Throwable th) {
            SigmobLog.e(th.getMessage());

        }


    }


    public static void intLoadReadyMap() {
        List<LoadReadyItem> loadReadyItems = null;
        try {
            SQLiteDatabase readableDatabase = SQLiteHelper.getInstance().getReadableDatabase();
            String sql = "select * from " + SQLiteHelper.TABLE_ADLOAD_READY_EVENT;
            Cursor cursor = readableDatabase.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                loadReadyItems = new java.util.ArrayList<>();
                do {
                    LoadReadyItem item = new LoadReadyItem();
                    item.placement_id = cursor.getString(cursor.getColumnIndexOrThrow("adslot_id"));

                    item.media_request_count = cursor.getInt(cursor.getColumnIndexOrThrow("media_request_count"));
                    item.media_ready_count = cursor.getInt(cursor.getColumnIndexOrThrow("media_ready_count"));
                    loadReadyItems.add(item);
                    loadReadyItemMap.put(item.placement_id, item);

                } while (cursor.moveToNext());
            }
        } catch (Throwable t) {
            SigmobLog.e(t.getMessage());
        }
    }
    public static void addAdLoadReadyEvent(final String placementId, LoadReadyItem item) {
        if (TextUtils.isEmpty(placementId) || item == null) return;

        try {
            SQLiteDatabase writableDatabase = SQLiteHelper.getInstance().getWritableDatabase();
            SQLiteBuider.Insert.Builder builder = new SQLiteBuider.Insert.Builder();
            builder.setTableName(SQLiteHelper.TABLE_ADLOAD_READY_EVENT);
            Map<String, Object> values = new HashMap<>();
            values.put("adslot_id", placementId);
            values.put("media_request_count",item.media_request_count);
            values.put("media_ready_count",item.media_ready_count);

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
