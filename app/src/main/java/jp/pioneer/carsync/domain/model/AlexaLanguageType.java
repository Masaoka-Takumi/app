package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * Alexa言語種別.
 */
public enum AlexaLanguageType {
    /** English(US). */
    ENGLISH_US(0x00, R.string.set_308, R.string.alexa_language_locale_english_us),
    /** English(UK). */
    ENGLISH_UK(0x01, R.string.set_309, R.string.alexa_language_locale_english_uk),
    /** English(India). */
    ENGLISH_INDIA(0x02, R.string.set_310, R.string.alexa_language_locale_english_india),
    /** Japanese. */
    JAPANESE(0x03, R.string.set_311, R.string.alexa_language_locale_japanese),
    /** German. */
    GERMAN(0x04, R.string.set_312, R.string.alexa_language_locale_german),
    /** French. */
    FRENCH(0x05, R.string.set_313, R.string.alexa_language_locale_french),

    ;

    /** プロトコルでの定義値. */
    public final int code;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;
    /** 表示用文字列リソースID. */
    @StringRes public final int locale;
    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param label 表示用文字列リソースID
     */
    AlexaLanguageType(int code, @StringRes int label, @StringRes int locale) {
        this.code = code;
        this.label = label;
        this.locale = locale;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するMenuDisplayLanguageType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static AlexaLanguageType valueOf(byte code) {
        for (AlexaLanguageType value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
