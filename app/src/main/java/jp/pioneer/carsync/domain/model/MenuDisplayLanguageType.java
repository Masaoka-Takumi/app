package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * MENU表示言語種別.
 */
public enum MenuDisplayLanguageType {
    /** English. */
    ENGLISH(0x00, R.string.val_001),
    /** Brazil Portuguese. */
    BRAZIL_PORTUGUESE(0x01, R.string.val_002),
    /** Russian. */
    RUSSIAN(0x02, R.string.val_003),
    /** Turkish. */
    TURKISH(0x03, R.string.val_004),
    /** French. */
    FRENCH(0x04, R.string.val_005),
    /** German. */
    GERMAN(0x05, R.string.val_006),
    /** 南米Spanish. */
    SOUTH_AMERICAN_SPANISH(0x06, R.string.val_007),
    /** Canadian French */
    CANADIAN_FRENCH(0x07, R.string.val_008)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param label 表示用文字列リソースID
     */
    MenuDisplayLanguageType(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するMenuDisplayLanguageType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static MenuDisplayLanguageType valueOf(byte code) {
        for (MenuDisplayLanguageType value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
