package jp.pioneer.carsync.domain.model;

/**
 * 接続数.
 * <p>
 * HF接続数、ペアリング端末数で使用。
 */
public enum ConnectedDevicesCountStatus {
    /** なし. */
    NONE(0x00),
    /** あり(最大数未満). */
    NOT_FULL(0x01),
    /** あり(最大数). */
    FULL(0x02)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ConnectedDevicesCountStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するConnectedDevicesCountStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ConnectedDevicesCountStatus valueOf(int code) {
        for (ConnectedDevicesCountStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
