package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * SiriusXMのバンド種別.
 */
public enum SxmBandType implements BandType {
    /** SXM1. */
    SXM1(0x00, R.string.ply_032),
    /** SXM2. */
    SXM2(0x01, R.string.ply_033),
    /** SXM3. */
    SXM3(0x02, R.string.ply_034)
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
    SxmBandType(int code, @StringRes int label) {
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
     * @return プロトコルでの定義値に該当するSxmBandType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SxmBandType valueOf(byte code) {
        for (SxmBandType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
