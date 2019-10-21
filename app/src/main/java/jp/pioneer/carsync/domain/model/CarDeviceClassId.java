package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * ClassID.
 * <p>
 * 主に製品群として車載機を分類するID。
 */
public enum CarDeviceClassId {
    /** DEH. */
    DEH(0x10),
    /** SPH */
    SPH(0x11),
    /** Marin */
    MARIN(0x20)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    CarDeviceClassId(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から{@link CarDeviceClassId}取得.
     *
     * @param value プロトコルでの定義値
     * @return プロトコルでの定義値に該当するClassID
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static CarDeviceClassId valueOf(byte value) {
        for (CarDeviceClassId classId : values()) {
            if (classId.code == PacketUtil.ubyteToInt(value)) {
                return classId;
            }
        }

        throw new IllegalArgumentException("invalid value: " + value);
    }
}
