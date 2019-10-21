package jp.pioneer.carsync.domain.model;

import android.support.annotation.CallSuper;

import com.google.common.base.MoreObjects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tuner系共通情報.
 */
@SuppressFBWarnings({"UWF_NULL_FIELD", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public abstract class AbstractTunerInfo extends SerialVersion {
    /** 有効最小周波数. */
    public long minimumFrequency;
    /** 有効最大周波数. */
    public long maximumFrequency;
    /** 現在の周波数. */
    public long currentFrequency;
    /** 周波数単位. */
    public TunerFrequencyUnit frequencyUnit;
    /** INDEX. */
    public int index;
    /** 現在のアンテナレベル. */
    public int antennaLevel;
    /** アンテナレベルの最大値. */
    public int maxAntennaLevel;
    /** チューナー状態. */
    public TunerStatus tunerStatus;

    /**
     * リセット.
     */
    @CallSuper
    public void reset() {
        minimumFrequency = 0;
        maximumFrequency = 0;
        currentFrequency = 0;
        frequencyUnit = null;
        antennaLevel = 0;
        maxAntennaLevel = 1;
        tunerStatus = TunerStatus.NORMAL;
        updateVersion();
    }

    /**
     * バンド種別取得.
     *
     * @return バンド種別
     */
    public abstract BandType getBand();

    /**
     * 検索状態か否か取得.
     *
     * @return {@code true}:検索状態である。{@code false}:それ以外。
     */
    public abstract boolean isSearchStatus();

    /**
     * 文字列追加.
     * <p>
     * {@link #toString()}でスーパークラスとサブクラスの情報を同列に出力するための仕掛け。
     * サブクラスはスーパークラスの本メソッドを呼んだ後にサブクラスで追加したフィールドの
     * 情報を追加する。
     * サブクラスは{@link #toString()}をオーバーライドしないこと。
     *
     * <pre>{@code
     *  MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
     *      return super.addToString(helper)
     *              .add("サブクラスで追加したフィールド1", 値)
     *              .add("サブクラスで追加したフィールド2", 値)
     *              …
     *  }
     * }</pre>
     *
     * @param helper MoreObjects.ToStringHelper
     * @return MoreObjects.ToStringHelper
     */
    @CallSuper
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return MoreObjects.toStringHelper("")
                .add("minimumFrequency", minimumFrequency)
                .add("maximumFrequency", maximumFrequency)
                .add("currentFrequency", currentFrequency)
                .add("frequencyUnit", frequencyUnit)
                .add("index", index)
                .add("antennaLevel", antennaLevel)
                .add("maxAntennaLevel", maxAntennaLevel)
                .add("tunerStatus", tunerStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return addToString(MoreObjects.toStringHelper(""))
                .toString();
    }
}
