package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * RDS割り込み種別.
 */
public enum RdsInterruptionType {
    /** 通常. */
    NORMAL(0x00),
    /** TA. */
    TA(0x01),
    /** NEWS. */
    NEWS(0x02),
    /** ALARM. */
    ALARM(0x03)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    RdsInterruptionType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するRdsInterruptedType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static RdsInterruptionType valueOf(byte code) {
        for (RdsInterruptionType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
