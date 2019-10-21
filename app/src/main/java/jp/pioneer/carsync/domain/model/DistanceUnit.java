package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 距離単位.
 */
public enum DistanceUnit {
    /** Feet/Mile */
    FEET_MILE(0x00) {
        @Override
        public DistanceUnit toggle() {
            return METER_KILOMETER;
        }
    },
    /** Meter/Kilometer */
    METER_KILOMETER(0x01){
        @Override
        public DistanceUnit toggle() {
            return FEET_MILE;
        }
    }
    ;
    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    DistanceUnit(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するAttMuteSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static DistanceUnit valueOf(byte code) {
        for (DistanceUnit value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
    /**
     * トグル.
     *
     * @return トグル後のAttMuteSetting
     */
    public abstract DistanceUnit toggle();
}
