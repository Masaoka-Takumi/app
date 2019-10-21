package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * EQ設定.
 * <p>
 * 車載機で定義されている種別
 */
public enum AudioSettingEqualizerType {
    /** EQ_SUPER_BASS. */
    SUPER_BASS(0x00),
    /** EQ_POWERFUL. */
    POWERFUL(0x01),
    /** EQ_NATURAL. */
    NATURAL(0x02),
    /** EQ_VOCAL. */
    VOCAL(0x03),
    /** EQ_TODOROKI. */
    TODOROKI(0x04),
    /** EQ_POP_ROCK. */
    POP_ROCK(0x05),
    /** EQ_ELECTRONICA. */
    ELECTRONICA(0x06),
    /** EQ_EQ_SAMBA. */
    EQ_SAMBA(0x07),
    /** EQ_SERTANEJO. */
    SERTANEJO(0x08),
    /** EQ_PRO. */
    PRO(0x09),
    /** EQ_FLAT. */
    FLAT(0x0A),
    /** EQ_COMMON_CUSTOM. */
    COMMON_CUSTOM(0x0B),
    /** EQ_COMMON_CUSTOM_2ND. */
    COMMON_CUSTOM_2ND(0x0C),
    /** EQ_CLEAR. */
    CLEAR(0x0D),
    /** EQ_SPECIAL. */
    SPECIAL(0x0E),
    /** EQ_VIVID. */
    VIVID(0x0F),
    /** EQ_DYNAMIC. */
    DYNAMIC(0x10),
    /** EQ_JAZZ. */
    JAZZ(0x11),
    /** EQ_FORRO. */
    FORRO(0x12),
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    AudioSettingEqualizerType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するAudioSettingEqualizerType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static AudioSettingEqualizerType valueOf(byte code) {
        for (AudioSettingEqualizerType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }
}
