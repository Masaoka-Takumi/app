package jp.pioneer.carsync.application.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jp.pioneer.carsync.application.content.ProviderContract.*;

/**
 * データベースヘルパークラス.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "CarSync.db";
    private static final int DB_VERSION = 1;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public AppDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgradeTo1(db);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void onUpgradeTo1(SQLiteDatabase db) {
        String sql;

        sql = "CREATE TABLE " + Favorite.TABLE_NAME + " (" +
                Favorite._ID + " INTEGER PRIMARY KEY, " +
                Favorite.NAME + " TEXT, " +
                Favorite.DESCRIPTION + " TEXT, " +
                Favorite.SOURCE_ID + " INTEGER NOT NULL, " +
                Favorite.TUNER_CHANNEL_KEY1 + " INTEGER, " +
                Favorite.TUNER_CHANNEL_KEY2 + " INTEGER, " +
                Favorite.TUNER_FREQUENCY_INDEX + " INTEGER, " +
                Favorite.TUNER_BAND + " INTEGER, " +
                Favorite.TUNER_PARAM1 + " INTEGER, " +
                Favorite.TUNER_PARAM2 + " INTEGER, " +
                Favorite.TUNER_PARAM3 + " INTEGER," +
                Favorite.CREATE_DATE + " INTEGER" +
                ");";
        db.execSQL(sql);
    }
}
