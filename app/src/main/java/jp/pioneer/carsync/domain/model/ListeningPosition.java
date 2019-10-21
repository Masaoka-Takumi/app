package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Small Car TA シート位置.
 */
public enum ListeningPosition {
    /** LEFT. */
    LEFT(0x00),
    /** RIGHT. */
    RIGHT(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ListeningPosition(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSmallCarTaSeatPosition
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ListeningPosition valueOf(byte code) {
        for (ListeningPosition value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
