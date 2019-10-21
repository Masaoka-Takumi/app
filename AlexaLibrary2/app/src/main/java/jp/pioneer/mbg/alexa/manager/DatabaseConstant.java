package jp.pioneer.mbg.alexa.manager;

/**
 * Created by esft-hatori on 2017/12/06.
 */


import android.net.Uri;
import android.provider.BaseColumns;
import java.util.ArrayList;

/**
 * Alert機能 アラート保存用データベースのパラメタ定義
 */
public class DatabaseConstant {
    /* ### カラムタイプ. ### */
    /* 符号付整数. */
    private static final String TYPE_INTEGER = "integer";
    /* 浮動小数点. */
    private static final String TYPE_REAL = "real";
    /* テキスト. */
    private static final String TYPE_TEXT = "text";
    /* バイナリデータ. */
    private static final String TYPE_BLOB = "blob";
    /* NULL値. */
    private static final String TYPE_NULL = "null";

    /* ### 制約. ### */
    /* オートインクリメント. */
    private static final String REST_AUTOINCREMENT = "autoincrement";
    /* 主キー. */
    private static final String REST_PRIMARY_KEY = "primary key";
    /* NOT NULL. */
    private static final String REST_NOT_NULL = "not null";

    private static final String REST_DEFAULT = "default";


    /**
     * 設定した文字列を空白1つ挟んで繋げるメソッド.
     * @param typeParams
     * @return
     */
    private static String joinColumnType(String... typeParams) {
        String result = null;

        if (typeParams != null) {
            StringBuilder builder = new StringBuilder();
            boolean isFirst = true;
            for (String typeParam : typeParams) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(" ");
                }
                builder.append(typeParam);
            }
            result = builder.toString();
        }

        return result;
    }

    /**
     * Alertテーブル.
     */
    public static class AlertTable {

        /** テーブル名. */
        public static final String TABLE_NAME = "alert_table";

        /**
         * カラム.
         */
        public enum AlertTableColumns {

            _ID("_id", joinColumnType(TYPE_INTEGER, REST_PRIMARY_KEY, REST_AUTOINCREMENT)),
            TOKEN("token", joinColumnType(TYPE_TEXT,REST_NOT_NULL)),
            TYPE("type", joinColumnType(TYPE_TEXT,REST_NOT_NULL)),
            SCHEDULED_TIME("scheduled_time", joinColumnType(TYPE_TEXT, REST_NOT_NULL)),
            BACKGROUND_ALERT_ASSET("background_alert_asset", TYPE_TEXT),
            LOOP_COUNT("loop_count", TYPE_INTEGER),
            LOOP_PAUSE_IN_MILLI_SECONDS("loop_pause_in_milli_seconds", TYPE_INTEGER),
            STOP_FLAG("stop_flag", joinColumnType(TYPE_INTEGER,REST_DEFAULT,"0"));

            private String mColumnType;
            private String mColumnName;

            AlertTableColumns (String columnName, String columnType){
                this.mColumnType = columnType;
                this.mColumnName = columnName;
            }

            /**
             * カラム名取得
             */
            public String getColumnName(){
                return mColumnName;
            }

            /**
             * カラムの型取得
             */
            public String getColumnType() {
                return mColumnType;
            }

            /**
             * カラム名一覧の取得
             */
            public static String[] getColumnNameList(){
                ArrayList<String> positions = new ArrayList<String>();
                AlertTableColumns [] resultColumn = AlertTableColumns.values();
                for(int i = 0; i < resultColumn.length; i++){
                    positions.add(resultColumn[i].getColumnName());
                }
                return (String[])positions.toArray(new String[]{});
            }

            /**
             * カラムの型一覧の取得
             */
            public static String[] getColumnTypeList(){
                ArrayList<String> positions = new ArrayList<String>();
                AlertTableColumns [] resultColumn = AlertTableColumns.values();
                for(int i = 0; i < resultColumn.length; i++){
                    positions.add(resultColumn[i].getColumnType());
                }
                return (String[])positions.toArray(new String[]{});
            }
        }
    }

    /**
     * Assetテーブル.
     */
    public static class AssetPlayOderTable {

        /** テーブル名. */
        public static final String TABLE_NAME = "asset_play_oder_table";

        /**
         * カラム.
         */
        public enum AssetPlayOderTableColumns {

            ALERT_ID("alert_id", joinColumnType(TYPE_INTEGER, REST_NOT_NULL)),
            SEQUENCE_NUMBER("sequence_number", joinColumnType(TYPE_INTEGER, REST_NOT_NULL)),
            ASSET_ID("asset_id", joinColumnType(TYPE_TEXT, REST_NOT_NULL));

            private String mColumnType;
            private String mColumnName;

            AssetPlayOderTableColumns (String columnName, String columnType){
                this.mColumnType = columnType;
                this.mColumnName = columnName;
            }

            /**
             * カラム名取得
             */
            public String getColumnName(){
                return mColumnName;
            }

            /**
             * カラムの型取得
             */
            public String getColumnType() {
                return mColumnType;
            }

            /**
             * カラム名一覧の取得
             */
            public static String[] getColumnNameList(){
                ArrayList<String> positions = new ArrayList<String>();
                AssetPlayOderTableColumns [] resultColumn = AssetPlayOderTableColumns.values();
                for(int i = 0; i < resultColumn.length; i++){
                    positions.add(resultColumn[i].getColumnName());
                }
                return (String[])positions.toArray(new String[]{});
            }

            /**
             * カラムの型一覧の取得
             */
            public static String[] getColumnTypeList(){
                ArrayList<String> positions = new ArrayList<String>();
                AssetPlayOderTableColumns [] resultColumn = AssetPlayOderTableColumns.values();
                for(int i = 0; i < resultColumn.length; i++){
                    positions.add(resultColumn[i].getColumnType());
                }
                return (String[])positions.toArray(new String[]{});
            }
        }
    }

    /**
     * Assetテーブル.
     */
    public static class AssetCacheTable {

        /** テーブル名. */
        public static final String TABLE_NAME = "asset_cache_table";

        /**
         * カラム.
         */
        public enum AssetCacheTableColumns {
            ALERT_ID("alert_id", joinColumnType(TYPE_TEXT, REST_NOT_NULL)),
            ASSET_ID("asset_id", joinColumnType(TYPE_TEXT, REST_NOT_NULL)),
            URL("url", joinColumnType(TYPE_TEXT, REST_NOT_NULL)),
            CACHE("cache", TYPE_BLOB);

            private String mColumnType;
            private String mColumnName;

            AssetCacheTableColumns (String columnName, String columnType){
                this.mColumnType = columnType;
                this.mColumnName = columnName;
            }

            /**
             * カラム名取得
             */
            public String getColumnName(){
                return mColumnName;
            }

            /**
             * カラムの型取得
             */
            public String getColumnType() {
                return mColumnType;
            }

            /**
             * カラム名一覧の取得
             */
            public static String[] getColumnNameList(){
                ArrayList<String> positions = new ArrayList<String>();
                AssetCacheTableColumns [] resultColumn = AssetCacheTableColumns.values();
                for(int i = 0; i < resultColumn.length; i++){
                    positions.add(resultColumn[i].getColumnName());
                }
                return (String[])positions.toArray(new String[]{});
            }

            /**
             * カラムの型一覧の取得
             */
            public static String[] getColumnTypeList(){
                ArrayList<String> positions = new ArrayList<String>();
                AssetCacheTableColumns [] resultColumn = AssetCacheTableColumns.values();
                for(int i = 0; i < resultColumn.length; i++){
                    positions.add(resultColumn[i].getColumnType());
                }
                return (String[])positions.toArray(new String[]{});
            }
        }
    }
}

