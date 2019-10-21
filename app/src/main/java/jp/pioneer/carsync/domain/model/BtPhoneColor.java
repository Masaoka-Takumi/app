package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * BT Phone Color設定.
 */
public enum BtPhoneColor {
    /** Flashing （蛍光風演出）. */
    FLASHING(0x00, R.string.val_036),
    /** Flashing Pattern1. */
    FLASHING_PATTERN1(0x01, R.string.val_037),
    /** Flashing Pattern2. */
    FLASHING_PATTERN2(0x02, R.string.val_038),
    /** Flashing Pattern3. */
    FLASHING_PATTERN3(0x03, R.string.val_039),
    /** Flashing Pattern4. */
    FLASHING_PATTERN4(0x04, R.string.val_040),
    /** Flashing Pattern5. */
    FLASHING_PATTERN5(0x05, R.string.val_041),
    /** Flashing Pattern6. */
    FLASHING_PATTERN6(0x06, R.string.val_042),
    /** OFF. */
    OFF(0x07, R.string.val_119)
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
    BtPhoneColor(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するBtPhoneColor
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static BtPhoneColor valueOf(byte code) {
        for (BtPhoneColor value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code))  {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
