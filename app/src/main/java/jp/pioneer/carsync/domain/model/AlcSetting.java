package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * ALC設定.
 */
public enum AlcSetting {
    /** OFF. */
    OFF(0x00),
    /** MODE1. */
    MODE1(0x01),
    /** MODE2. */
    MODE2(0x02)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    AlcSetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するAlcSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static AlcSetting valueOf(byte code) {
        for (AlcSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
