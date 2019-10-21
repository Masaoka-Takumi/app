package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * イルミ設定色種別.
 */
public enum IlluminationColor {
    /** WHITE. */
    WHITE(0x00, R.string.val_043),
    /** RED. */
    RED(0x01, R.string.val_044),
    /** AMBER. */
    AMBER(0x02, R.string.val_045),
    /** ORANGE. */
    ORANGE(0x03, R.string.val_046),
    /** YELLOW. */
    YELLOW(0x04, R.string.val_047),
    /** PURE GREEN. */
    PURE_GREEN(0x05, R.string.val_048),
    /** GREEN. */
    GREEN(0x06, R.string.val_049),
    /** TURQUOISE. */
    TURQUOISE(0x07, R.string.val_050),
    /** LIGHT BLUE. */
    LIGHT_BLUE(0x08, R.string.val_051),
    /** BLUE. */
    BLUE(0x09, R.string.val_052),
    /** PURPLE. */
    PURPLE(0x0A, R.string.val_053),
    /** PINK. */
    PINK(0x0B, R.string.val_054),
    /** SCAN. */
    SCAN(0x0C, R.string.setting_theme_illumination_color_scan),
    /** CUSTOM. */
    CUSTOM(0x0D, R.string.val_055),
    /** FOR MY CAR. */
    FOR_MY_CAR(0x0E, R.string.setting_theme_illumination_color_for_my_car)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ
     *
     * @param code プロトコルでの定義値
     * @param label 表示用文字列リソースID
     */
    IlluminationColor(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するIlluminationColor
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static IlluminationColor valueOf(byte code) {
        for (IlluminationColor value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
