package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * REAR出力設定 / REAR出力設定.
 */
public enum RearOutputPreoutOutputSetting {
    /** Rear / Rear. */
    REAR_REAR(0x00, R.string.val_013){
        @Override
        public RearOutputPreoutOutputSetting toggle() {
            return REAR_SUBWOOFER;
        }
    },
    /** Rear / Subwoofer. */
    REAR_SUBWOOFER(0x01, R.string.val_014){
        @Override
        public RearOutputPreoutOutputSetting toggle() {
            return SUBWOOFER_SUBWOOFER;
        }
    },
    /** Subwoofer / Subwoofer */
    SUBWOOFER_SUBWOOFER(0x02, R.string.val_015){
        @Override
        public RearOutputPreoutOutputSetting toggle() {
            return REAR_REAR;
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
    RearOutputPreoutOutputSetting(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するRearOutputPreoutOutputSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static RearOutputPreoutOutputSetting valueOf(byte code) {
        for (RearOutputPreoutOutputSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のRearOutputPreoutOutputSetting
     */
    public abstract RearOutputPreoutOutputSetting toggle();
}
