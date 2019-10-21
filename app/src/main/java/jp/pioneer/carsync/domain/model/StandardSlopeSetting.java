package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * StandardモードにおけるSlope設定.
 */
public enum StandardSlopeSetting implements SlopeSetting {
    /** -6dB/oct. */
    _6DB(0x00, -6),
    /** -12dB/oct. */
    _12DB(0x01, -12),
    /** -18dB/oct. */
    _18DB(0x02, -18),
    /** -24dB/oct. */
    _24DB(0x03, -24)
    ;

    private final int code;
    private final int level;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param level レベル（dB/oct）
     */
    StandardSlopeSetting(int code, int level) {
        this.code = code;
        this.level = level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCode() {
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLevel() {
        return level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SlopeSetting toggle(int delta) {
        StandardSlopeSetting[] values = values();
        int pos = ordinal() + delta;
        if (pos < 0 || values.length <= pos) {
            return null;
        }

        return values[pos];
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するStandardSlopeSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static StandardSlopeSetting valueOf(byte code) {
        for (StandardSlopeSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
