package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * SLA設定.
 */
@SuppressFBWarnings("UWF_NULL_FIELD")
public class SlaSetting {
    /** 最小ステップ値. */
    public int minimumStep;
    /** 最大ステップ値. */
    public int maximumStep;
    /** 設定値. */
    public int currentStep;
    /** グループ情報. */
    public SlaGroup group;

    /**
     * リセット.
     */
    public void reset() {
        minimumStep = 0;
        maximumStep = 0;
        currentStep = 0;
        group = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("minimumStep", minimumStep)
                .add("maximumStep", maximumStep)
                .add("currentStep", currentStep)
                .add("group", group)
                .toString();
    }
}
