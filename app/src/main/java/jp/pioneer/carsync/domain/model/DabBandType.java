package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * DABのバンド種別.
 */
public enum DabBandType implements BandType {
    /** Band1. */
    BAND1(0x00, R.string.dab_band_type_band1),
    /** Band2. */
    BAND2(0x01, R.string.dab_band_type_band2),
    /** Band3. */
    BAND3(0x02, R.string.dab_band_type_band3)
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param label 表示用文字列リソースID
     */
    DabBandType(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
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
    @StringRes
    @Override
    public int getLabel() {
        return label;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するDabBandType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static DabBandType valueOf(byte code) {
        for (DabBandType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
