package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * [AC2] Standard ModeにおけるCutoff周波数設定.
 */
public enum Ac2StandardCutoffSetting implements CutoffSetting {
    /** 25Hz. */
    _25HZ(0x00, 25),
    /** 31.5Hz. */
    _31_5HZ(0x01, 31.5f),
    /** 40Hz. */
    _40HZ(0x02, 40),
    /** 50Hz. */
    _50HZ(0x03, 50),
    /** 63Hz. */
    _63HZ(0x04, 63),
    /** 80Hz. */
    _80HZ(0x05, 80),
    /** 100Hz. */
    _100HZ(0x06, 100),
    /** 125Hz. */
    _125HZ(0x07, 125),
    /** 160Hz. */
    _160HZ(0x08, 160),
    /** 200Hz. */
    _200HZ(0x09, 200),
    /** 250Hz. */
    _250HZ(0x0A, 250)
    ;

    private final int code;
    private final float frequency;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param frequency 周波数
     */
    Ac2StandardCutoffSetting(int code, float frequency) {
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
        Ac2StandardCutoffSetting[] values = values();
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
     * @return プロトコルでの定義値に該当するAc2StandardCutoffSetting
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static Ac2StandardCutoffSetting valueOf(byte code) {
        for (Ac2StandardCutoffSetting value: values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
