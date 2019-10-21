package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * REAR出力設定.
 */
public enum RearOutputSetting {
    /** Rear. */
    REAR(0x00, R.string.val_016) {
        @Override
        public RearOutputSetting toggle() {
            return SUBWOOFER;
        }
    },
    /** Subwoofer. */
    SUBWOOFER(0x01, R.string.val_017) {
        @Override
        public RearOutputSetting toggle() {
            return REAR;
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
    RearOutputSetting(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するRearOutputSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static RearOutputSetting valueOf(byte code) {
        for (RearOutputSetting value : values()) {
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
    public abstract RearOutputSetting toggle();
}
