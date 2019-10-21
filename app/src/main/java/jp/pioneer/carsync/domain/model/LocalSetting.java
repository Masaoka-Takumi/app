package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * LOCAL設定.
 */
public enum LocalSetting {
    /** OFF. */
    OFF(0x00, R.string.val_119),
    /** LEVEL1. */
    LEVEL1(0x01, R.string.val_115),
    /** LEVEL2. */
    LEVEL2(0x02, R.string.val_116),
    /** LEVEL3. */
    LEVEL3(0x03, R.string.val_117),
    /** LEVEL4. */
    LEVEL4(0x04, R.string.val_118)
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
    LocalSetting(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するLocalSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static LocalSetting valueOf(byte code) {
        for (LocalSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }
}
