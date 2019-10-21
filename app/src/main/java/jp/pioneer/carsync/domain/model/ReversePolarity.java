package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * リバース極性.
 */
public enum ReversePolarity {
    /** LOW. */
    LOW(0x00),
    /** HIGH. */
    HIGH(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ReversePolarity(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するReversePolarity
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ReversePolarity valueOf(byte code) {
        for (ReversePolarity value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
