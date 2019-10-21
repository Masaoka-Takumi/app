package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 車載機メディアのリピートモード.
 * <p>
 * SmartPhoneメディア（AppMusic）は{@link SmartPhoneRepeatMode}
 */
public enum CarDeviceRepeatMode {
    /** OFF. */
    OFF(0x00),
    /** 1曲リピート. */
    ONE(0x01),
    /** フォルダリピート. */
    FOLDER(0x02),
    /** 全曲リピート. */
    ALL(0x03)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    CarDeviceRepeatMode(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するCarDeviceRepeatMode
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static CarDeviceRepeatMode valueOf(byte code) {
        for (CarDeviceRepeatMode value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
