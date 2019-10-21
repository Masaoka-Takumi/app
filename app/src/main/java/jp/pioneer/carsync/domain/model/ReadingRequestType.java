package jp.pioneer.carsync.domain.model;

/**
 * 読み上げ要求種別.
 */
public enum ReadingRequestType {
    /** 終了要求. */
    FINISH(0x00),
    /** 開始要求. */
    START(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ReadingRequestType(int code) {
        this.code = code;
    }
}
