package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * FM Tuner設定.
 */
public enum FMTunerSetting {
    /** Music. */
    MUSIC(0x00, R.string.val_112) {
        @Override
        public FMTunerSetting toggle() {
            return STANDARD;
        }
    },
    /** Standard. */
    STANDARD(0x01, R.string.val_113) {
        @Override
        public FMTunerSetting toggle() {
            return TALK;
        }
    },
    /** Talk. */
    TALK(0x02, R.string.val_114) {
        @Override
        public FMTunerSetting toggle() {
            return MUSIC;
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
    FMTunerSetting(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するFMTunerSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static FMTunerSetting valueOf(byte code) {
        for (FMTunerSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のFMTunerSetting
     */
    public abstract FMTunerSetting toggle();
}