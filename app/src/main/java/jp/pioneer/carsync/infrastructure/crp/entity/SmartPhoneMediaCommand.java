package jp.pioneer.carsync.infrastructure.crp.entity;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * SmartPhoneメディアコマンド.
 */
public enum SmartPhoneMediaCommand {
    /** Play / Pause. */
    PLAY_PAUSE(0x00),
    /** Play(Resume). */
    PLAY_RESUME(0x01),
    /** Pause. */
    PAUSE(0x02),
    /** Track up. */
    TRACK_UP(0x03),
    /** Track down. */
    TRACK_DOWN(0x04),
    /** Fast Forward. */
    FAST_FORWARD(0x05),
    /** Rewind. */
    REWIND(0x06),
    /** Repeat. */
    REPEAT(0x07),
    /** Random. */
    RANDOM(0x08)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SmartPhoneMediaCommand(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSmartPhoneMediaCommand
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SmartPhoneMediaCommand valueOf(byte code) {
        for (SmartPhoneMediaCommand value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
