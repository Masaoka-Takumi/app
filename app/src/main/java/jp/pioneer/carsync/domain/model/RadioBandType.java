package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * Radioのバンド種別.
 */
public enum RadioBandType implements BandType {
    /** FM1. */
    FM1(0x01, R.string.ply_048),
    /** FM2. */
    FM2(0x02, R.string.ply_049),
    /** FM3. */
    FM3(0x03, R.string.ply_050),
    /** AM1. */
    AM1(0x09, R.string.ply_055),
    /** AM2. */
    AM2(0x0A, R.string.ply_056),
    /** AM. */
    AM(0x0B, R.string.ply_007),
    /** MW. */
    MW(0x0C, R.string.ply_018),
    /** LW. */
    LW(0x0D, R.string.ply_017),
    /** SW1. */
    SW1(0x0E, R.string.ply_051),
    /** SW2. */
    SW2(0x0F, R.string.ply_052)
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
    RadioBandType(int code, @StringRes int label) {
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
     * @return プロトコルでの定義値に該当するRadioBandType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static RadioBandType valueOf(byte code) {
        for (RadioBandType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * AM系か否か取得.
     *
     * @return {@code true}:AM系である。{@code false}:それ以外。
     */
    public boolean isAMVariant() {
        return this == AM || this == AM1 || this == AM2;
    }

    /**
     * FM系か否か取得.
     *
     * @return {@code true}:FM系である。{@code false}:それ以外。
     */
    public boolean isFMVariant() {
        return this == FM1 || this == FM2 || this == FM3;
    }
}
