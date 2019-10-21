package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Auto EQ補正設定.
 */
public enum AutoEqCorrectionSetting {
    /** OFF. */
    OFF(0x00),
    /** ON. */
    ON(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    AutoEqCorrectionSetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するAutoEqCorrectionSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static AutoEqCorrectionSetting valueOf(byte code) {
        for (AutoEqCorrectionSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
