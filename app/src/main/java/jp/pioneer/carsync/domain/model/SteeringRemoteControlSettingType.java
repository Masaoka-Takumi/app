package jp.pioneer.carsync.domain.model;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * ステアリングリモコン設定種別.
 */
public enum SteeringRemoteControlSettingType {
    /** OFF. */
    OFF(0x00),
    /** PIONEER. */
    PIONEER(0x01),
    /** TOYOTA. */
    TOYOTA(0x02),
    /** HONDA. */
    HONDA(0x03),
    /** SUZUKI. */
    SUZUKI(0x04),
    /** SUBARU1. */
    SUBARU1(0x05),
    /** SUBARU2. */
    SUBARU2(0x06),
    /** SUBARU3. */
    SUBARU3(0x07),
    /** SUBARU4. */
    SUBARU4(0x08),
    /** MAZDA. */
    MAZDA(0x09),
    /** NISSAN1. */
    NISSAN1(0x0A),
    /** NISSAN2. */
    NISSAN2(0x0B),
    /** NISSAN3. */
    NISSAN3(0x0C),
    /** NISSAN4. */
    NISSAN4(0x0D),
    /** MITSUBISHI */
    MITSUBISHI(0x0E),
    /** HYUNDAI. */
    HYUNDAI(0x0F),
    /** DAIHATSU. */
    DAIHATSU(0x10)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SteeringRemoteControlSettingType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSteeringRemoteControlSettingType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SteeringRemoteControlSettingType valueOf(byte code) {
        for (SteeringRemoteControlSettingType value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
