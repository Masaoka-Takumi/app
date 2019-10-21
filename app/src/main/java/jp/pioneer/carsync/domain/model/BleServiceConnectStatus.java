package jp.pioneer.carsync.domain.model;

/**
 * BLE Service接続状態.
 */
public enum BleServiceConnectStatus {
    /** 未接続. */
    NOT_CONNECT(0x00),
    /** 接続処理中. */
    CONNECTING(0x01),
    /** 接続済. */
    CONNECTED(0x02),
    /** Connection error. */
    CONNECTION_ERROR(0x08),
    /** Not found */
    NOT_FOUND(0x08),
    /** Pairing failed */
    PAIRING_FAILED(0x08),
    /** BLE module error */
    BLE_MODULE_ERROR(0x08),
    /** Memory Clear Stop */
    MEMORY_CLEAR_STOP(0x08)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    BleServiceConnectStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するBleServiceConnectStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static BleServiceConnectStatus valueOf(int code) {
        for (BleServiceConnectStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
