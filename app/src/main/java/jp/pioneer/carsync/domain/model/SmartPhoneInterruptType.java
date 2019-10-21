package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * SmartPhone割り込み情報種別
 */
public enum SmartPhoneInterruptType {
    /** 優先度低割り込み. */
    LOW(0x00),
    /** 優先度中割り込み. */
    MIDDLE(0x01),
    /** 優先度高割り込み. */
    HIGH(0x02),
    /** 割り込み解除. */
    RELEASE(0xFE)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SmartPhoneInterruptType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSmartPhoneInterruptType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SmartPhoneInterruptType valueOf(byte code) {
        for (SmartPhoneInterruptType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
