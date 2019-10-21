package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * ロータリーアクション.
 */
public enum RotaryKeyAction {
    /** Push. */
    PUSH(0x00),
    /** 右回転. */
    CLOCKWISE(0x01),
    /** 左回転. */
    COUNTERCLOCKWISE(0x02)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    RotaryKeyAction(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するRotaryKeyAction
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static RotaryKeyAction valueOf(byte code) {
        for (RotaryKeyAction value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
