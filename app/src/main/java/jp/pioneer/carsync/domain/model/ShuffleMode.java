package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * シャッフルモード.
 */
public enum ShuffleMode {
    /** OFF. */
    OFF(0x00) {
        @Override
        public ShuffleMode toggle() {
            return ON;
        }
    },
    /** ON. */
    ON(0x01) {
        @Override
        public ShuffleMode toggle() {
            return OFF;
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
    ShuffleMode(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するShuffleMode
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ShuffleMode valueOf(byte code) {
        for (ShuffleMode value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のShuffleMode
     */
    public abstract ShuffleMode toggle();
}
