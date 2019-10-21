package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * HD Radioのバンド種別.
 */
public enum HdRadioBandType implements BandType {
    /** FM1. */
    FM1(0x01, R.string.ply_048),
    /** FM2. */
    FM2(0x02, R.string.ply_049),
    /** FM3. */
    FM3(0x03, R.string.ply_050),
    /** AM. */
    AM(0x09, R.string.ply_007),
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
    HdRadioBandType(int code, @StringRes int label) {
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
     * @return プロトコルでの定義値に該当するHdRadioBandType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static HdRadioBandType valueOf(byte code) {
        for (HdRadioBandType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
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
