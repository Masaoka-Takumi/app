package jp.pioneer.carsync.domain.model;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * 警告音出力先設定.
 */
public enum AlarmOutputDestinationSetting {
    /** Front. */
    FRONT(0x00){
        @Override
        public AlarmOutputDestinationSetting toggle() {
            return FRONT_LEFT;
        }
    },
    /** Front-Left. */
    FRONT_LEFT(0x01) {
        @Override
        public AlarmOutputDestinationSetting toggle() {
            return FRONT_RIGHT;
        }
    },
    /** Front-Right. */
    FRONT_RIGHT(0x02) {
        @Override
        public AlarmOutputDestinationSetting toggle() {
            return FRONT;
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
    AlarmOutputDestinationSetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するAlarmOutputDestinationSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static AlarmOutputDestinationSetting valueOf(byte code) {
        for (AlarmOutputDestinationSetting value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のAlarmOutputDestinationSetting
     */
    public abstract AlarmOutputDestinationSetting toggle();
}
