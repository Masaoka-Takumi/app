package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 車載機の割り込み情報種別.
 */
public enum CarDeviceInterruptType {
    /** DAB TA割り込み. */
    DAB_TA(0x00),
    /** HD緊急放送割り込み. */
    HD_EMERGENCY_BROADCAST(0x01),
    /** RDS TA割り込み. */
    RDS_TA(0x02),
    /** RDS ALARM割り込み. */
    RDS_ALARM(0x03),
    /** RDS NEWS割り込み. */
    RDS_NEWS(0x04),
    /** TELL MUTE. */
    TELL_MUTE(0x05),
    /** 割り込み解除. */
    INTERRUPT_RESET(0xFE)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    CarDeviceInterruptType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するCarDeviceInterruptType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static CarDeviceInterruptType valueOf(byte code) {
        for (CarDeviceInterruptType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
