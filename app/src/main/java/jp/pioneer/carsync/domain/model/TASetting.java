package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * TA設定.
 */
public enum TASetting {
    /** OFF. */
    OFF(0x00){
        @Override
        public TASetting toggle() {
            return DAB_RDS_TA_ON;
        }
    },
    /** RDS TA ON. */
    RDS_TA_ON(0x01){
        @Override
        public TASetting toggle() {
            return OFF;
        }
    },
    /** DAB&RDS TA ON. */
    DAB_RDS_TA_ON(0x02){
        @Override
        public TASetting toggle() {
            return RDS_TA_ON;
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
    TASetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するTASetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static TASetting valueOf(byte code) {
        for (TASetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }

    /**
     * トグル.
     *
     * @return トグル後のTASetting
     */
    public abstract TASetting toggle();
}
