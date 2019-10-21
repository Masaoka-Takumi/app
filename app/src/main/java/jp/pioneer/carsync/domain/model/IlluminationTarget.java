package jp.pioneer.carsync.domain.model;

/**
 * イルミ設定対象.
 */
public enum IlluminationTarget {
    /** KEY. */
    KEY(0x00),
    /** DISP. */
    DISP(0x01);

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    IlluminationTarget(int code) {
        this.code = code;
    }
}
