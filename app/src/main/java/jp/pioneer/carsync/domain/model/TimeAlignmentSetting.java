package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * Time Alignment設定.
 */
public class TimeAlignmentSetting {
    /** 設定値. */
    public TimeAlignmentSettingMode mode;
    /** 最小ステップ値. */
    public int minimumStep;
    /** 最大ステップ値. */
    public int maximumStep;
    /** ステップ単位. */
    public TimeAlignmentStepUnit stepUnit;
    /** FrontLeft / HighLeft(2way mode)のステップ値. */
    public int frontLeftHighLeftStep;
    /** FrontRight / HighRight(2way mode)のステップ値. */
    public int frontRightHighRightStep;
    /** RearLeft / MidLeft(2way mode)のステップ値. */
    public int rearLeftMidLeftStep;
    /** RearRight / MidRight(2way mode)のステップ値. */
    public int rearRightMidRightStep;
    /** Subwooferのステップ値. */
    public int subwooferStep;

    /**
     * リセット.
     */
    public void reset() {
        mode = TimeAlignmentSettingMode.OFF;
        minimumStep = 0;
        maximumStep = 0;
        stepUnit = TimeAlignmentStepUnit._2_5CM;
        frontLeftHighLeftStep = 0;
        frontRightHighRightStep = 0;
        rearLeftMidLeftStep = 0;
        rearRightMidRightStep = 0;
        subwooferStep = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("mode", mode)
                .add("minimumStep", minimumStep)
                .add("maximumStep", maximumStep)
                .add("stepUnit", stepUnit)
                .add("frontLeftHighLeftStep", frontLeftHighLeftStep)
                .add("frontRightHighRightStep", frontRightHighRightStep)
                .add("rearLeftMidLeftStep", rearLeftMidLeftStep)
                .add("rearRightMidRightStep", rearRightMidRightStep)
                .add("subwooferStep", subwooferStep)
                .toString();
    }
}
