package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * FM STEP設定値.
 * <p>
 * FMのSEEK STEP値
 */
public enum FmStep {
    /** 50KHz. */
    _50KHZ(0x00, R.string.val_009) {
        @Override
        public FmStep toggle() {
            return _100KHZ;
        }
    },
    /** 100KHz. */
    _100KHZ(0x01, R.string.val_010) {
        @Override
        public FmStep toggle() {
            return _50KHZ;
        }
    };

    /** プロトコルでの定義値. */
    public final int code;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ.
     *
     * @param code  プロトコルでの定義値
     * @param label 表示用文字列リソースID
     */
    FmStep(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するFmStep
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static FmStep valueOf(byte code) {
        for (FmStep value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のFmStep
     */
    public abstract FmStep toggle();
}
