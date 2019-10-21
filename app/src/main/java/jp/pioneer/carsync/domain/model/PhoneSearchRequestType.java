package jp.pioneer.carsync.domain.model;

/**
 * 要求種別.
 */
public enum PhoneSearchRequestType {
    /** 停止要求. */
    STOP(0x00),
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
    PhoneSearchRequestType(int code) {
        this.code = code;
    }
}
