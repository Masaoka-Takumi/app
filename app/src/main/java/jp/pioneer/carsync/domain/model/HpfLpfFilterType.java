package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * [JASPER] FILTER種別（HPF/LPF設定）
 */
public enum HpfLpfFilterType {
    /** HPF. */
    HPF(0x00),
    /** LPF. */
    LPF(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    HpfLpfFilterType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するHpfLpfFilterType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static HpfLpfFilterType valueOf(byte code) {
        for (HpfLpfFilterType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("invalid value: " + code);
    }
}
