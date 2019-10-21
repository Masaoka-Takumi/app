package jp.pioneer.carsync.domain.model;

/**
 * CUSTOM発光要求種別.
 */
public enum CustomFlashRequestType {
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
    CustomFlashRequestType(int code) {
        this.code = code;
    }
}
