package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Rate設定状態（Thumb Up/Downの状態）.
 */
public enum ThumbStatus {
    /** None. */
    NONE(0x00),
    /** Up. */
    UP(0x01),
    /** Down. */
    DOWN(0x02)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ThumbStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するThumbStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ThumbStatus valueOf(byte code) {
        for (ThumbStatus value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }
}
