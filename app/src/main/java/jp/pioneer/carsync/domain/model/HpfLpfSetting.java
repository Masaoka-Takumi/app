package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * HPF/LPF設定.
 */
public enum HpfLpfSetting {
    /** OFF. */
    OFF(0x00){
        @Override
        public HpfLpfSetting toggle() {
            return ON;
        }
    },
    /** ON. */
    ON(0x01){
        @Override
        public HpfLpfSetting toggle() {
            return OFF;
        }
    },
    /** OFF固定. */
    OFF_FIXED(0x02){
        @Override
        public HpfLpfSetting toggle() {
            return this;
        }
    },
    /** ON固定. */
    ON_FIXED(0x03){
        @Override
        public HpfLpfSetting toggle() {
            return this;
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
    HpfLpfSetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するHpfLpfSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static HpfLpfSetting valueOf(byte code) {
        for (HpfLpfSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のHpfLpfSetting
     */
    public abstract HpfLpfSetting toggle();
}
