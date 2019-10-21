package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * P.CH/MANUAL設定.
 */
public enum PCHManualSetting {
    /** MANUAL. */
    MANUAL(0x00) {
        @Override
        public PCHManualSetting toggle() {
            return PCH;
        }
    },
    /** P.CH. */
    PCH(0x01) {
        @Override
        public PCHManualSetting toggle() {
            return MANUAL;
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
    PCHManualSetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するPCHManualSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static PCHManualSetting valueOf(byte code) {
        for (PCHManualSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のPCHManualSetting
     */
    public PCHManualSetting toggle() {
        return this;
    }
}
