package jp.pioneer.carsync.domain.model;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * USBリスト情報種別.
 */
public enum UsbInfoType {
    /** ファイル. */
    FILE(0x00),
    /** 曲有りフォルダ. */
    FOLDER_MUSIC_EXIST(0x01),
    /** 曲無しフォルダ. */
    FOLDER_MUSIC_NOT_EXIST(0x02)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    UsbInfoType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するUsbInfoType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static UsbInfoType valueOf(byte code) {
        for (UsbInfoType value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
