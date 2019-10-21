package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Standard ModeにおけるCutoff周波数設定.
 */
public enum StandardCutoffSetting implements CutoffSetting {
    /** 50Hz. */
    _50HZ(0x00, 50),
    /** 63Hz. */
    _63HZ(0x01, 63),
    /** 80Hz. */
    _80HZ(0x02, 80),
    /** 100Hz. */
    _100HZ(0x03, 100),
    /** 125Hz. */
    _125HZ(0x04, 125),
    /** 160Hz. */
    _160HZ(0x05, 160),
    /** 200Hz. */
    _200HZ(0x06, 200)
    ;

    private final int code;
    private final float frequency;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param frequency 周波数
     */
    StandardCutoffSetting(int code, float frequency) {
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
        StandardCutoffSetting[] values = values();
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
     * @return プロトコルでの定義値に該当するStandardCutoffSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static StandardCutoffSetting valueOf(byte code) {
        for (StandardCutoffSetting value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
