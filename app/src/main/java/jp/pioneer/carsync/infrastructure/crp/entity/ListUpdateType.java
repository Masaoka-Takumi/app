package jp.pioneer.carsync.infrastructure.crp.entity;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * リスト更新種別.
 */
public enum ListUpdateType {
    /** 更新. */
    UPDATE(0x00),
    /** 階層移動(Forward). */
    FORWARD(0x01),
    /** 階層移動(Back). */
    BACK(0x02)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ListUpdateType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するListUpdateType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static ListUpdateType valueOf(byte code) {
        for (ListUpdateType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
