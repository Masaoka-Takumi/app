package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * パーキングセンサー エラー状態.
 */
public enum ParkingSensorErrorStatus {
    /** ERROR無し. */
    NOT_ERROR(0x00),
    /** NO DATA ERROR. */
    NO_DATA_ERROR(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ParkingSensorErrorStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するParkingSensorErrorStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ParkingSensorErrorStatus valueOf(byte code) {
        for (ParkingSensorErrorStatus value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
