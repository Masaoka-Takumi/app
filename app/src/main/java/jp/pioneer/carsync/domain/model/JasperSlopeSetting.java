package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * [JASPER] Slope設定.
 */
public enum JasperSlopeSetting implements SlopeSetting {
    /** -12dB/oct. */
    _12DB(0x00, -12),
    /** -24dB/oct. */
    _24DB(0x01, -24)
    ;

    private final int code;
    private final int level;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param level レベル（dB/oct）
     */
    JasperSlopeSetting(int code, int level) {
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
    public JasperSlopeSetting toggle(int delta) {
        JasperSlopeSetting[] values = values();
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
     * @return プロトコルでの定義値に該当するJasperSlopeSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static JasperSlopeSetting valueOf(byte code) {
        for (JasperSlopeSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
