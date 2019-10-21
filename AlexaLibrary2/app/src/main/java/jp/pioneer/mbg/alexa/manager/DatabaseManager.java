package jp.pioneer.mbg.alexa.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;


/**
 * Alert機能 アラート保存用データベース管理クラス
 */
public class DatabaseManager {

    public static final String TAG = DatabaseManager.class.getSimpleName();
    private static final boolean DBG = true;

    public static final String DB_NAME = "alert.db";

    public static final int DB_VERSION = 1;

    /* コンテキスト. */
    private Context mContext = null;

    /* Helper. */
    private DatabaseHelper mHelper = null;

    public DatabaseManager(Context context) {
        mContext = context;
        mHelper = new DatabaseHelper(context);
    }

    /**
     * ヘルパークラス.
     */
    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (DBG) android.util.Log.d(TAG, "DatabaseHelper onCreate() s");

            db.beginTransaction();
            try {
                db.execSQL(createTable(DatabaseConstant.AlertTable.TABLE_NAME,
                        DatabaseConstant.AlertTable.AlertTableColumns
                                .getColumnNameList(),
                        DatabaseConstant.AlertTable.AlertTableColumns
                                .getColumnTypeList()));

                db.execSQL(createTable(DatabaseConstant.AssetPlayOderTable.TABLE_NAME,
                        DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns
                                .getColumnNameList(),
                        DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns
                                .getColumnTypeList()));

                db.execSQL(createTable(DatabaseConstant.AssetCacheTable.TABLE_NAME,
                        DatabaseConstant.AssetCacheTable.AssetCacheTableColumns
                                .getColumnNameList(),
                        DatabaseConstant.AssetCacheTable.AssetCacheTableColumns
                                .getColumnTypeList()));

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (DBG) android.util.Log.d(TAG, "DatabaseHelper onCreate() e");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (DBG) android.util.Log.d(TAG, "DatabaseHelper onUpgrade ");
            if (DBG) android.util.Log.d(TAG, "DatabaseHelper onUpgrade");
        }

        /**
         * テーブル作成を行うsqlの作成
         * @param tableName テーブル名
         * @param columns カラム名一覧
         * @param types カラムタイプ一覧
         * @return
         */
        private String createTable(String tableName, String[] columns,
                                   String[] types) {
            if (tableName == null || columns == null || types == null
                    || types.length != columns.length || types.length == 0) {
                throw new IllegalArgumentException(
                        "Invalid parameters for creating table " + tableName);
            } else {
                StringBuilder stringBuilder = new StringBuilder(
                        "create table if not exists ");

                stringBuilder.append(tableName);
                stringBuilder.append(" (");
                for (int n = 0, i = columns.length; n < i; n++) {
                    if (n > 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(columns[n]).append(' ')
                            .append(types[n]);
                }
                return stringBuilder.append(");").toString();
            }
        }
    }

    /**
     * query(データ取得)メソッド.
     * @param tableName
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    public Cursor query(String tableName, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        if (DBG) android.util.Log.d(TAG, "DatabaseManager query() s");

        SQLiteDatabase database = mHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(tableName);

        Cursor cursor = queryBuilder.query(database, projection, selection,
                selectionArgs, null, null, sortOrder);

        if (DBG) android.util.Log.d(TAG, "QueryCount = " + cursor.getCount());
        if (DBG) android.util.Log.d(TAG, "DatabaseManager query() e");
        return cursor;
    }

    /**
     * insert(データ挿入)メソッド.
     * @param tableName
     * @param values
     * @return
     */
    public long insert(String tableName, ContentValues values) {
        if (DBG) android.util.Log.d(TAG, "DatabaseManager insert s");
        long result = 0;

        SQLiteDatabase database = mHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            result = database.insert(tableName, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        if (DBG) android.util.Log.d(TAG, "DatabaseManager insert e");
        return result;
    }

    /**
     * delete(データ削除)メソッド.
     * @param tableName
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int delete(String tableName, String selection, String[] selectionArgs) {
        if (DBG) android.util.Log.d(TAG, "DatabaseManager delete s");

        int result;

        SQLiteDatabase database = mHelper.getWritableDatabase();
        result = database.delete(tableName, selection, selectionArgs);

        if (DBG) android.util.Log.d(TAG, "DeleteCount = " + result);
        if (DBG) android.util.Log.d(TAG, "DatabaseManager delete e");
        return result;
    }

    /**
     * update(データ更新)メソッド.
     * @param tableName
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int update(String tableName, ContentValues values, String selection, String[] selectionArgs) {
        if (DBG) android.util.Log.d(TAG, "DatabaseManager update s");

        int result;

        SQLiteDatabase database = mHelper.getWritableDatabase();
        result = database.update(tableName, values, selection, selectionArgs);

        if (DBG) android.util.Log.d(TAG, "UpdateCount = " + result);
        if (DBG) android.util.Log.d(TAG, "DatabaseManager update e");
        return result;
    }

    /**
     * 書き込み用のSQLiteDatabaseオブジェクトを取得する.
     * @return
     */
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase database = mHelper.getWritableDatabase();
        return database;
    }

    /**
     * 読み込み用のSQLiteDatabaseオブジェクトを取得する.
     * @return
     */
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase database = mHelper.getReadableDatabase();
        return database;
    }
}

