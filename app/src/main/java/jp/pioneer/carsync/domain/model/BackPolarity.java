package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * バック信号極性.
 */
public enum BackPolarity {
    /** Battery. */
    BATTERY(0x00) {
        @Override
        public BackPolarity toggle() {
            return GROUND;
        }
    },
    /** Ground. */
    GROUND(0x01) {
        @Override
        public BackPolarity toggle() {
            return BATTERY;
        }
    }
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    BackPolarity(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するBackPolarity
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static BackPolarity valueOf(byte code) {
        for (BackPolarity value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のBackPolarity
     */
    public abstract BackPolarity toggle();
}
