package jp.pioneer.carsync.presentation.model;

public enum CustomKey {
    SOURCE_CHANGE("ソース切換"), // ソース切替
    SOURCE_ON_OFF("ソースOFF/ON"), // ソースOFF/ON
    SOURCE_LIST("ソース一覧表示"), // ソース一覧表示
    SOURCE_DIRECT("ソース指定"), // ダイレクトソース切替
    THIRD_PARTY_APP("ミュージックアプリ") // 3rd App
    ;

    /** Analytics用文字列. */
    public final String strValue;

    /**
     * コンストラクタ.
     *
     * @param strValue   情報収集用の文字列
     */
    CustomKey(String strValue){
        this.strValue = strValue;
    }

    /**
     * Analytics用文字列取得.
     */
    public String getAnalyticsStr() {
        return strValue;
    }
}
