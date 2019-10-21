package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * センサー状態.
 */
public enum SensorStatus {
    /** 異常. */
    ERROR(0x00),
    /** 正常. */
    NORMAL(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SensorStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSensorStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SensorStatus valueOf(byte code) {
        for (SensorStatus value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
