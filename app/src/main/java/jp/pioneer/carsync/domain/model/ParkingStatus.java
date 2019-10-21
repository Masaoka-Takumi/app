package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * パーキング状態.
 */
public enum ParkingStatus {
    /** OFF. */
    OFF(0x00),
    /** ON. */
    ON(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ParkingStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するParkingStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ParkingStatus valueOf(byte code) {
        for (ParkingStatus value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
