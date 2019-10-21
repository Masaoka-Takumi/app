package jp.pioneer.carsync.domain.model;

/**
 * 車載機エラー種別.
 */
public enum CarDeviceErrorType {
    /** AMP ERROR. */
    AMP_ERROR(0x00000001),
    /** CHECK USB. */
    CHECK_USB(0x00000002),
    /** CHECK TUNER. */
    CHECK_TUNER(0x00000003),
    /** CHECK ANTENNA. */
    CHECK_ANTENNA(0x00000004)
    ;

    /** プロトコルでの定義値. */
    public final long code;

    CarDeviceErrorType(long code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するCarDeviceErrorType。
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない。
     */
    public static CarDeviceErrorType valueOf(long code) {
        for (CarDeviceErrorType value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
