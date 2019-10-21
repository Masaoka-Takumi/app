package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 2 Way NetworkモードのMid LPF/High HPFにおけるCutoff周波数設定.
 */
public enum TwoWayNetworkMidLpfHighHpfCutoffSetting implements CutoffSetting {
    /** 1.25kHz. */
    _1_25KHZ(0x00, 1250),
    /** 1.6KHz.*/
    _1_6KHZ(0x01, 1600),
    /** 2kHz. */
    _2KHZ(0x02, 2000),
    /** 2.5kHz. */
    _2_5KHZ(0x03, 2500),
    /** 3.15kHz. */
    _3_15KHZ(0x04, 3150),
    /** 4kHz. */
    _4KHZ(0x05, 4000),
    /** 5kHz. */
    _5KHZ(0x06, 5000),
    /** 6.3kHz. */
    _6_3KHZ(0x07, 6300),
    /** 8kHz. */
    _8KHZ(0x08, 8000),
    /** 10kHz. */
    _10KHZ(0x09, 10000),
    /** 12.5kHz. */
    _12_5KHZ(0x0A, 12500)
    ;

    private final int code;
    private final float frequency;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param frequency 周波数
     */
    TwoWayNetworkMidLpfHighHpfCutoffSetting(int code, float frequency) {
        this.code = code;
        this.frequency = frequency;
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
    public float getFrequency() {
        return frequency;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CutoffSetting toggle(int delta) {
        TwoWayNetworkMidLpfHighHpfCutoffSetting[] values = values();
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
     * @return プロトコルでの定義値に該当するTwoWayNetworkMidLpfHighHpfCutoffSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static TwoWayNetworkMidLpfHighHpfCutoffSetting valueOf(byte code) {
        for (TwoWayNetworkMidLpfHighHpfCutoffSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
