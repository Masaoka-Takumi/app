package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 車載機仕向け情報.
 */
public enum CarDeviceDestinationInfo {
    /** UC. */
    UC(0x00),
    /** EW5. */
    EW5(0x01),
    /** JP. */
    JP(0x02),
    /** ID. */
    ID(0x03),
    /** BR. */
    BR(0x04),
    /** ES. */
    ES(0x05),
    /** ME. */
    ME(0x06),
    /** CS. */
    CS(0x07),
    /** GS. */
    GS(0x08),
    /**
     * 不明.
     * <p>
     * TIPS取得用の初回起動時向け。
     */
    UNKNOWN(0xFF)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    CarDeviceDestinationInfo(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するCarDeviceDestinationInfo
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static CarDeviceDestinationInfo valueOf(byte code) {
        for (CarDeviceDestinationInfo value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
