package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Time Alignment設定.
 */
public enum TimeAlignmentSettingMode {
    /** Initial. */
    INITIAL(0x00){
        @Override
        public TimeAlignmentSettingMode toggle() {
            return AUTO_TA;
        }
    },
    /** AutoTA. */
    AUTO_TA(0x01){
        @Override
        public TimeAlignmentSettingMode toggle() {
            return OFF;
        }
    },
    /** Custom. */
    CUSTOM(0x02){
        @Override
        public TimeAlignmentSettingMode toggle() {
            return INITIAL;
        }
    },
    /** OFF. */
    OFF(0x03){
        @Override
        public TimeAlignmentSettingMode toggle() {
            return CUSTOM;
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
    TimeAlignmentSettingMode(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するTimeAlignmentSettingMode
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static TimeAlignmentSettingMode valueOf(byte code) {
        for (TimeAlignmentSettingMode value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のTimeAlignmentSettingMode
     */
    public abstract TimeAlignmentSettingMode toggle();
}
