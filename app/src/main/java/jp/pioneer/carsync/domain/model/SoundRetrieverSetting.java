package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Sound Retriever設定.
 */
public enum SoundRetrieverSetting {
    /** OFF. */
    OFF(0x00, R.string.val_119) {
        @Override
        public SoundRetrieverSetting toggle() {
            return MODE1;
        }
    },
    /** MODE1. */
    MODE1(0x01, R.string.val_239) {
        @Override
        public SoundRetrieverSetting toggle() {
            return MODE2;
        }
    },
    /** MODE2. */
    MODE2(0x02, R.string.val_240) {
        @Override
        public SoundRetrieverSetting toggle() {
            return OFF;
        }
    };

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
    SoundRetrieverSetting(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSoundRetrieverSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SoundRetrieverSetting valueOf(byte code) {
        for (SoundRetrieverSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のRearOutputSetting
     */
    public abstract SoundRetrieverSetting toggle();
}
