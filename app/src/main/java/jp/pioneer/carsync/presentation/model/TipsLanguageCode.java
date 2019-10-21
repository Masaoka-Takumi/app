package jp.pioneer.carsync.presentation.model;

/**
 * TIPS取得URLの言語コード.
 */
//TODO 対応している言語コードが分かり次第実装
public enum TipsLanguageCode {
    /** JPN. */
    JPN("ja");

    /** 言語コード. */
    public String code;

    /**
     * コンストラクタ.
     *
     * @param code 言語コード.
     */
    TipsLanguageCode(String code){
        this.code = code;
    }
}
