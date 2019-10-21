package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * SLA設定のグループ情報.
 */
public enum SlaGroup {
    /** Base. */
    GROUP_00(0x00),
    /** Radio Sources. */
    GROUP_01(0x01),
    /** Disc Sources. */
    GROUP_02(0x02),
    /** USB Sources. */
    GROUP_03(0x03),
    /** AUX Source. */
    GROUP_04(0x04),
    /** BT Audio Sources. */
    GROUP_05(0x05),
    /** DAB Source. */
    GROUP_06(0x06),
    /** SiriusXM Source. */
    GROUP_07(0x07),
    /** Tel/Mute. */
    GROUP_09(0x09)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    SlaGroup(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するSlaGroup
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SlaGroup valueOf(byte code) {
        for (SlaGroup value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
