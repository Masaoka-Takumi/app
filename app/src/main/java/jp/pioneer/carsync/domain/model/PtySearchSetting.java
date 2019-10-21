package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * PTY SEARCH設定.
 */
public enum PtySearchSetting {
    /** News/Info. */
    NEWS_INFO(0x00),
    /** Popular. */
    POPULAR(0x01),
    /** Classics. */
    CLASSICS(0x02),
    /** Others. */
    OTHERS(0x03)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    PtySearchSetting(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するPtySearchSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static PtySearchSetting valueOf(byte code) {
        for (PtySearchSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }
}
