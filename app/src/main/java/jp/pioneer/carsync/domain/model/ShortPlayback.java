package jp.pioneer.carsync.domain.model;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * SHORT PLAYBACK設定.
 */
public enum ShortPlayback {
    /** OFF. */
    OFF(0x00),
    /** 60s. */
    _60_SECOND(0x01),
    /** 90s. */
    _90_SECOND(0x02),
    /** 120s. */
    _120_SECOND(0x03),
    /** 150s. */
    _150_SECOND(0x04),
    /** 180s. */
    _180_SECOND(0x05)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ShortPlayback(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するShortPlayback
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ShortPlayback valueOf(byte code) {
        for (ShortPlayback value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
