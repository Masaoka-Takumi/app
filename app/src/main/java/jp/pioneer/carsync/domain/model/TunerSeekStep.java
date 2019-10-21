package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * SEEK STEP状態.
 * <p>
 * RadioソースのAMのSEEK STEP状態。
 */
public enum TunerSeekStep {
    /** 9KHz. */
    _9KHZ(0x00),
    /** 10KHz. */
    _10KHZ(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    TunerSeekStep(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するTunerSeekStep
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static TunerSeekStep valueOf(byte code) {
        for (TunerSeekStep value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
