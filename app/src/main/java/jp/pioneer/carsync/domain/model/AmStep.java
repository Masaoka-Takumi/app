package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * AM STEP設定値.
 * <p>
 * AMのSEEK STEP値
 */
public enum AmStep {
    /** 9KHz. */
    _9KHZ(0x00, R.string.val_011) {
        @Override
        public AmStep toggle() {
            return _10KHZ;
        }
    },
    /** 10KHz. */
    _10KHZ(0x01, R.string.val_012) {
        @Override
        public AmStep toggle() {
            return _9KHZ;
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
    AmStep(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するAmStep
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static AmStep valueOf(byte code) {
        for (AmStep value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のAmStep
     */
    public abstract AmStep toggle();
}
