package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Time AlignmentのStep単位.
 */
public enum TimeAlignmentStepUnit {
    /** 2.5 cm/step. */
    _2_5CM(0x00, 2.5f),
    /** 1 inch/step. */
    _1INCH(0x01, 1),
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
    TimeAlignmentStepUnit(int code, float multiplier) {
        this.code = code;
        this.multiplier = multiplier;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するTimeAlignmentStepUnit
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static TimeAlignmentStepUnit valueOf(byte code) {
        for (TimeAlignmentStepUnit value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
