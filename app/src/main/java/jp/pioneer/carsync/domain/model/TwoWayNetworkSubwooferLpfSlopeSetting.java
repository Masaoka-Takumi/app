package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 2 Way NetworkモードのSubwoofer LPFにおけるスロープ設定.
 */
public enum TwoWayNetworkSubwooferLpfSlopeSetting implements SlopeSetting {
    /** -12dB/oct. */
    _12DB(0x00, -12),
    /** -18dB/oct. */
    _18DB(0x01, -18),
    /** -24dB/oct. */
    _24DB(0x02, -24),
    /** -30dB/oct. */
    _30DB(0x03, -30),
    /** -36dB/oct. */
    _36DB(0x04, -36)
    ;

    private final int code;
    private final int level;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param level レベル（dB/oct）
     */
    TwoWayNetworkSubwooferLpfSlopeSetting(int code, int level) {
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
        TwoWayNetworkSubwooferLpfSlopeSetting[] values = values();
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
     * @return プロトコルでの定義値に該当するTwoWayNetworkSubwooferLpfSlopeSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static TwoWayNetworkSubwooferLpfSlopeSetting valueOf(byte code) {
        for (TwoWayNetworkSubwooferLpfSlopeSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
