package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * [SPH] BT PHONE COLOR設定.
 */
public enum SphBtPhoneColorSetting {
    /** OFF. */
    OFF(0x00, R.string.val_119),
    /** Color WHITE. */
    WHITE(0x01, R.string.val_043),
    /** Color RED. */
    RED(0x02, R.string.val_044),
    /** Color AMBER. */
    AMBER(0x03, R.string.val_045),
    /** Color ORANGE. */
    ORANGE(0x04, R.string.val_046),
    /** Color YELLOW. */
    YELLOW(0x05, R.string.val_047),
    /** Color PUREGREEN. */
    PUREGREEN(0x06, R.string.val_048),
    /** Color GREEN. */
    GREEN(0x07, R.string.val_049),
    /** Color TUEQUOISE. */
    TUEQUOISE(0x08, R.string.val_050),
    /** Color LIGHTBLUE. */
    LIGHTBLUE(0x09, R.string.val_051),
    /** Color BLUE. */
    BLUE(0x0A, R.string.val_052),
    /** Color PURPLE. */
    PURPLE(0x0B, R.string.val_053),
    /** Color PINK. */
    PINK(0x0C, R.string.val_054),
    /** Color CUSTOM. */
    CUSTOM(0x0D, R.string.val_055),
    /** Color REFER TO ZONE 1. */
    REFER_TO_ZONE_1(0x0E, R.string.val_056)
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
    SphBtPhoneColorSetting(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSphBtPhoneColor
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SphBtPhoneColorSetting valueOf(byte code) {
        for (SphBtPhoneColorSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code))  {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
