package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * カスタムEQ種別.
 */
public enum CustomEqType {
    /** CUSTOM1. */
    CUSTOM1(0x00),
    /** CUSTOM2. */
    CUSTOM2(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    CustomEqType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するCustomEqType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static CustomEqType valueOf(byte code) {
        for (CustomEqType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}

