package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Subwoofer位相設定.
 */
public enum SubwooferPhaseSetting {
    /** NORMAL. */
    NORMAL(0x00){
        @Override
        public SubwooferPhaseSetting toggle() {
            return REVERSE;
        }
    },
    /** REVERSE. */
    REVERSE(0x01){
        @Override
        public SubwooferPhaseSetting toggle() {
            return NORMAL;
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
    SubwooferPhaseSetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSubwooferPhaseSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SubwooferPhaseSetting valueOf(byte code) {
        for (SubwooferPhaseSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のSubwooferPhaseSetting
     */
    public abstract SubwooferPhaseSetting toggle();
}
