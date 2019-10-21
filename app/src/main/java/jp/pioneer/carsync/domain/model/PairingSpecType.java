package jp.pioneer.carsync.domain.model;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * ペアリング規格種別.
 */
public enum PairingSpecType {
    /** クラシックBT. */
    CLASSIC_BT(0x00),
    /** BLE. */
    BLE(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    PairingSpecType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するPairingStandardType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static PairingSpecType valueOf(byte code) {
        for (PairingSpecType value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
