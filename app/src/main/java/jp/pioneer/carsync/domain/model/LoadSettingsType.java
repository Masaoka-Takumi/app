package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * LOAD SETTING種別.
 */
public enum LoadSettingsType {
    /** AEQ SETTING. */
    AEQ(0x00),
    /** SOUND SETTING. */
    SOUND(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    LoadSettingsType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するLoadSettingsType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static LoadSettingsType valueOf(byte code) {
        for (LoadSettingsType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
