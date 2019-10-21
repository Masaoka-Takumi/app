package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * パーキングセンサーの距離単位.
 */
public enum SensorDistanceUnit {
    /** 0.1 m/step. */
    _0_1M(0x00, 0.1f),
    /** 1 inch/step. */
    _1INCH(0x01, 1f),
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** 乗数. */
    public final float multiplier;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param multiplier 乗数
     */
    SensorDistanceUnit(int code, float multiplier) {
        this.code = code;
        this.multiplier = multiplier;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するParkingSensorDistanceUnit
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SensorDistanceUnit valueOf(byte code) {
        for (SensorDistanceUnit value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
