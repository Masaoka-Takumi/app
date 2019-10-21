package jp.pioneer.carsync.domain.model;

/**
 * DIMMER時刻種別.
 */
public enum DimmerTimeType {
    /** 開始時刻. */
    START_TIME(0x00),
    /** 終了時刻. */
    END_TIME(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    DimmerTimeType(int code) {
        this.code = code;
    }
}
