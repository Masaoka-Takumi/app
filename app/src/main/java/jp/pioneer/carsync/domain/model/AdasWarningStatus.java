package jp.pioneer.carsync.domain.model;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * ADAS警告状態.
 */
public enum AdasWarningStatus {
    /** 警告なし. */
    NONE(0x00),
    /** 単発警告. */
    SINGLE(0x01),
    /** 連続警告. */
    CONTINUOUS(0x02)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    AdasWarningStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するAdasWarningStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static AdasWarningStatus valueOf(byte code) {
        for (AdasWarningStatus value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

}
