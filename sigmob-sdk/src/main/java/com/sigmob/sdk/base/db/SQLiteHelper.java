package com.sigmob.sdk.base.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.util.Log;

import com.czhj.sdk.common.Database.SQLiteBuider;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.utils.SigmobFileUtil;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_ADS = "ads";
    public static final String TABLE_FILE_REF = "file_reference";
    public static final String TABLE_INSERT_AFTER_INSERT_ONADS_TRIGGER = "trigger_insert";
    public static final String TABLE_INSERT_AFTER_DELETE_ONADS_TRIGGER = "trigger_delete";
    public static final String TABLE_TRACK = "tracks";
    public static final String TABLE_ADLOAD_EVENT = "adload_event";
    public static final String TABLE_ADLOAD_CACHE_EVENT = "adload_cache_event";
    public static final String TABLE_ADLOAD_READY_EVENT = "adload_ready_event";


    public static final String DATABASE_NAME = WindConstants.SDK_FOLDER + ".db";
    private static final int DATABASE_VERSION = 11;
    private static String create_ads_Sql = "CREATE TABLE ads ( endcard_md5 text  ,video_md5 text  ,ad blob  ,ad_source_channel text  ,ad_type integer  ,create_time integer  ,crid text  ,load_id text  ,adTrackersMap blob  ,adslot_id text  ,camp_id text  ,request_id text , primary key ( crid ,adslot_id) ); \n";
    private static String create_trigger_insert_Sql = "CREATE TRIGGER trigger_insert after insert on ads begin insert or replace into file_reference (crid,adslot_id,video_md5,endcard_md5) values(new.crid,new.adslot_id,new.video_md5,new.endcard_md5) ; end; \n";
    private static String create_trigger_delete_Sql = "CREATE TRIGGER trigger_delete after delete on ads begin delete from file_reference where crid = old.crid  and adslot_id == old.adslot_id; end;\n";
    private static String create_file_reference_Sql = "CREATE TABLE file_reference ( endcard_md5 blob  ,video_md5 blob  ,crid text  ,adslot_id text  , primary key ( crid ,adslot_id ) );\n";
    private static String create_tracks_Sql = "CREATE TABLE tracks ( id integer primary key AUTOINCREMENT ,retryNum integer   ,source text   ,event text   ,request_id text   ,url text   ,timestamp integer   );\n";
    private static String create_adload_Sql = "CREATE TABLE adload_event ( id integer primary key AUTOINCREMENT , adslot_id text  , error_code integer ,timestamp integer  );\n";
    private static String create_adload_cache_Sql = "CREATE TABLE adload_cache_event (  adslot_id text primary key, media_request_count integer ,media_ready_count integer ,pre_req_time integer , req_interval_time integer ,req_pool_size integer); \n";
    private static String create_adload_ready_Sql = "CREATE TABLE adload_ready_event (  adslot_id text primary key, media_request_count integer ,media_ready_count integer ); \n";

    // errors are negative, ok is 0, anything else is positive.
    private static final long DB_ERROR_NULL = -6;
    private static final long DB_ERROR_NOT_OPEN = -5;
    private static final long DB_ERROR_READ_ONLY = -4;
    public static final long DB_ERROR_BAD_INPUT = -2;
    public static final long DB_WRITE_ERROR = -1; // from SQLiteDatabase if an error occurred
    private static final long DB_OK = 0;


    private static SQLiteHelper gInstance = null;

    private static SQLiteDatabase writedb = null;


    private SQLiteHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setWriteAheadLoggingEnabled(true);
        } else {
            db.enableWriteAheadLogging();
        }
    }

    public static synchronized void initialize(final Context context) {
        if (gInstance == null) {
            gInstance = new SQLiteHelper(context);
        }
    }

    public static SQLiteHelper getInstance() {
        return gInstance;
    }


    public interface ExecCallBack {
        void onSuccess();

        void onFailed(Throwable e);
    }

    public void insert(SQLiteDatabase sqLiteDatabase, SQLiteBuider.Insert insert, ExecCallBack callBack) {
        sqLiteDatabase.beginTransaction();

        boolean result = false;
        try {

            SQLiteStatement sqlListStatment = sqLiteDatabase.compileStatement(insert.getSql());

            for (int i = 1; i <= insert.getColumns().size(); i++) {


                String colume = (String) insert.getColumns().get(i - 1);
                Object value = insert.getValues().get(colume);

                if (value == null) {
                    sqlListStatment.bindNull(i);
                } else if (value instanceof String) {
                    sqlListStatment.bindString(i, (String) value);
                } else if (value instanceof Double) {
                    sqlListStatment.bindDouble(i, (Double) value);
                } else if (value instanceof Number) {
                    sqlListStatment.bindLong(i, ((Number) value).longValue());
                } else if (value instanceof byte[]) {
                    sqlListStatment.bindBlob(i, (byte[]) value);
                } else {
                    sqlListStatment.bindNull(i);
                }
            }
            sqlListStatment.execute();
            sqLiteDatabase.setTransactionSuccessful();
            result = true;
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
            if (callBack != null) {
                callBack.onFailed(e);
            }
        } finally {
            try {
                sqLiteDatabase.endTransaction();
            } catch (Throwable e) {
                SigmobLog.e(e.getMessage());
                if (callBack != null) {
                    callBack.onFailed(e);
                }
            }
        }
        if (result && callBack != null) {
            callBack.onSuccess();
        }
    }


    public void transactionWriteExecSQL(SQLiteDatabase sqLiteDatabase, String sql, ExecCallBack callBack) {
        sqLiteDatabase.beginTransaction();

        boolean result = false;
        try {
            sqLiteDatabase.execSQL(sql, new Object[]{});
            sqLiteDatabase.setTransactionSuccessful();
            result = true;
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
            if (callBack != null) {
                callBack.onFailed(e);
            }
        } finally {
            sqLiteDatabase.endTransaction();
        }
        if (result && callBack != null) {
            callBack.onSuccess();
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {
//        sqLiteDatabase.execSQL(create_ads_Sql);
//        sqLiteDatabase.execSQL(create_trigger_insert_Sql);
//        sqLiteDatabase.execSQL(create_trigger_delete_Sql);
//        sqLiteDatabase.execSQL(create_file_reference_Sql);
//        sqLiteDatabase.execSQL(create_tracks_Sql);
        sqLiteDatabase.execSQL(create_adload_Sql);
        sqLiteDatabase.execSQL(create_adload_cache_Sql);
        sqLiteDatabase.execSQL(create_adload_ready_Sql);


    }

    private static long errorChecks(SQLiteDatabase db) {
        if (db == null) {
            return DB_ERROR_NULL;
        } else if (!db.isOpen()) {
            return DB_ERROR_NOT_OPEN;
        } else if (db.isReadOnly()) {
            return DB_ERROR_READ_ONLY;
        } else {
            return DB_OK;
        }
    }


    @Override
    public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(SQLiteHelper.class.getName(),
                "Downgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );

        recreateDb(database);


    }

    private void dropFieldWithTable(final SQLiteDatabase database, final String field, final String table) {
        String sql = "alter table " + table + " drop column " + field;

        database.execSQL(sql);
    }

    private void addFieldWithTable(final SQLiteDatabase database, final String field, final String type, final String table) {
        String sql = "alter table " + table + " add " + field + " " + type;

        database.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );

        recreateDb(database);
    }


    public void clearDb() {
        recreateDb(getWritableDatabase());
    }

    private void recreateDb(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ADS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_FILE_REF);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ADLOAD_EVENT);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ADLOAD_CACHE_EVENT);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ADLOAD_READY_EVENT);


        database.execSQL("DROP TRIGGER IF EXISTS " + TABLE_INSERT_AFTER_INSERT_ONADS_TRIGGER);
        database.execSQL("DROP TRIGGER IF EXISTS " + TABLE_INSERT_AFTER_DELETE_ONADS_TRIGGER);

        SigmobFileUtil.clearCache();

        onCreate(database);

    }

}
