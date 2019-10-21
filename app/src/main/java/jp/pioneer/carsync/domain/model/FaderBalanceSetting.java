package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * Fader/Balance設定.
 */
public class FaderBalanceSetting {
    /** 最小FADER値. */
    public int minimumFader;
    /** 最大FADER値. */
    public int maximumFader;
    /** 最小BALANCE値. */
    public int minimumBalance;
    /** 最大BALANCE値. */
    public int maximumBalance;
    /** 現在のFADER値. */
    public int currentFader;
    /** 現在のBALANCE値. */
    public int currentBalance;

    /**
     * コンストラクタ.
     */
    public FaderBalanceSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        minimumFader = 0;
        maximumFader = 0;
        minimumBalance = 0;
        maximumBalance = 0;
        currentFader = 0;
        currentBalance = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("minimumFader", minimumFader)
                .add("maximumFader", maximumFader)
                .add("minimumBalance", minimumBalance)
                .add("maximumBalance", maximumBalance)
                .add("currentFader", currentFader)
                .add("currentBalance", currentBalance)
                .toString();
    }
}
