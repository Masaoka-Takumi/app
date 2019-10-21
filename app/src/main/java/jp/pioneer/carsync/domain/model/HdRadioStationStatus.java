package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * HD放送局情報受信状態.
 */
public enum HdRadioStationStatus {
    /** HD局受信中以外. */
    NOT_RECEIVING(0x00),
    /** HD局受信中. */
    RECEIVING(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    HdRadioStationStatus(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するHdRadioStationStatus
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static HdRadioStationStatus valueOf(byte code) {
        for (HdRadioStationStatus value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }
}
