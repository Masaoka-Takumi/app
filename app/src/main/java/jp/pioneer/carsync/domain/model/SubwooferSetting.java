package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Subwoofer設定.
 */
public enum SubwooferSetting {
    /** OFF. */
    OFF(0x00){
        @Override
        public SubwooferSetting toggle() {
            return ON;
        }
    },
    /** ON. */
    ON(0x01){
        @Override
        public SubwooferSetting toggle() {
            return OFF;
        }
    },
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SubwooferSetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSubwooferSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SubwooferSetting valueOf(byte code) {
        for (SubwooferSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のSubwooferSetting
     */
    public abstract SubwooferSetting toggle();
}
