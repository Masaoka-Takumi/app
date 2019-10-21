package jp.pioneer.carsync.infrastructure.crp.handler;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * 応答コード.
 */
public enum ResponseCode {
    /** NG. */
    NG(0x00),
    /** OK. */
    OK(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ResponseCode(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するResponseCode
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ResponseCode valueOf(byte code) {
        for (ResponseCode value : values()) {
            if (value.code == ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
