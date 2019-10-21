package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 周波数単位.
 * <p>
 * Radio, DAB, HD Radioで使用。
 */
public enum TunerFrequencyUnit {
    /** kHz. */
    KHZ(0x00, R.string.unt_006, 1, 0),
    /** MHz （小数点以下1桁表示）. */
    MHZ(0x01, R.string.unt_005, 1000, 1),
    /** MHz （小数点以下2桁表示）. */
    MHZ2(0x02, R.string.unt_005, 1000, 2),
    /** MHz. */
    MHZ3(0x01, R.string.unt_005, 1000, 3)
    ;

    /** プロトコルでの定義値. */
    public final int code;
    /** 表示用文字列リソースID. */
    @StringRes public final int label;
    /** 割る数（kHzからの変換用）. */
    public final int divide;
    /** 表示する際の小数点以下桁数. */
    public final int fraction;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     * @param label 表示用文字列リソースID
     * @param divide 割る数（kHzからの変換用）
     * @param fraction 表示する際の小数点以下桁数
     */
    TunerFrequencyUnit(int code, @StringRes int label, int divide, int fraction) {
        this.code = code;
        this.label = label;
        this.divide = divide;
        this.fraction = fraction;
    }

    /**
     * ソース種別とプロトコルでの定義値から取得.
     * <p>
     * {@link #MHZ}, {@link #MHZ3}は{@code code}が同じ（0x01）でソース種別によって
     * 小数点の桁数が異なるため、ソース種別を必要とする。
     *
     * @param code プロトコルでの定義値
     * @param sourceType ソース種別
     * @return プロトコルでの定義値に該当するTunerFrequencyUnit
     * @throws NullPointerException {@code sourceType}がnull
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static TunerFrequencyUnit valueOf(byte code, @NonNull MediaSourceType sourceType) {
        for (TunerFrequencyUnit value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                if (value == MHZ || value == MHZ3) {
                    return (sourceType == MediaSourceType.DAB) ? MHZ3 : MHZ;
                } else {
                    return value;
                }
            }
        }

        throw new IllegalArgumentException(String.format("invalid code: %s, sourceType: %s", code, sourceType));
    }
}
