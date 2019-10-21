package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * BASS BOOSTERレベル設定.
 */
public class BassBoosterSetting {
    /** 最小ステップ値. */
    public int minimumStep;
    /** 最大ステップ値. */
    public int maximumStep;
    /** ステップ単位(dB). */
    public int stepUnit;
    /** 設定値. */
    public int currentStep;

    /**
     * リセット.
     */
    public void reset() {
        minimumStep = 0;
        maximumStep = 0;
        stepUnit = 0;
        currentStep = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("minimumStep", minimumStep)
                .add("maximumStep", maximumStep)
                .add("stepUnit", stepUnit)
                .add("currentStep", currentStep)
                .toString();
    }
}
