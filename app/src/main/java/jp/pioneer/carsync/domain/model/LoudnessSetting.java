package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * LOUDNESS設定.
 */
public enum LoudnessSetting {
    /** OFF. */
    OFF(0x00, R.string.com_001),
    /** LOW. */
    LOW(0x01, R.string.set_126),
    /** MID. */
    MID(0x02, R.string.set_139),
    /** HIGH. */
    HIGH(0x03, R.string.set_089)
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
    LoudnessSetting(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するLoudnessSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static LoudnessSetting valueOf(byte code) {
        for (LoudnessSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
