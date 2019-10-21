package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * MUTE状態.
 */
public enum MuteMode {
    /** OFF. */
    OFF(0x00),
    /** ON. */
    ON(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    MuteMode(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するMuteMode
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static MuteMode valueOf(byte code) {
        for (MuteMode value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
