package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 要求接続種別.
 */
public enum PhoneConnectRequestType {
    /** 切断要求. */
    DISCONNECT(0x00),
    /** 接続要求. */
    CONNECT(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    PhoneConnectRequestType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するPhoneConnectRequestType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static PhoneConnectRequestType valueOf(byte code) {
        for (PhoneConnectRequestType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
