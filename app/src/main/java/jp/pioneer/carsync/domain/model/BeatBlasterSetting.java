package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Beat Blasterレベル設定.
 */
public enum BeatBlasterSetting {
    /** OFF. */
    OFF(0x00, R.string.val_119){
        @Override
        public BeatBlasterSetting toggle() {
            return LOW;
        }
    },
    /** LOW. */
    LOW(0x01, R.string.val_107){
        @Override
        public BeatBlasterSetting toggle() {
            return HIGH;
        }
    },
    /** HIGH. */
    HIGH(0x02, R.string.val_109){
        @Override
        public BeatBlasterSetting toggle() {
            return OFF;
        }
    }
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
    BeatBlasterSetting(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するBeatBlasterSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static BeatBlasterSetting valueOf(byte code) {
        for (BeatBlasterSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のBeatBlasterSetting
     */
    public abstract BeatBlasterSetting toggle();
}
