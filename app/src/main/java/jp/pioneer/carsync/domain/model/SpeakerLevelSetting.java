package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * Speaker Level設定.
 */
public class SpeakerLevelSetting {
    /** 最小レベル値. */
    public int minimumLevel;
    /** 最大レベル値. */
    public int maximumLevel;
    /** FrontLeft / HighLeft(2way mode)のレベル値. */
    public int frontLeftHighLeftLevel;
    /** FrontRight / HighRight(2way mode)のレベル値. */
    public int frontRightHighRightLevel;
    /** RearLeft / MidLeft(2way mode)のレベル値. */
    public int rearLeftMidLeftLevel;
    /** RearRight / MidRight(2way mode)のレベル値. */
    public int rearRightMidRightLevel;
    /** Subwooferのレベル値. */
    public int subwooferLevel;

    /**
     * コンストラクタ.
     */
    public SpeakerLevelSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        minimumLevel = 0;
        maximumLevel = 0;
        frontLeftHighLeftLevel = 0;
        frontRightHighRightLevel = 0;
        rearLeftMidLeftLevel = 0;
        rearRightMidRightLevel = 0;
        subwooferLevel = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("minimumLevel", minimumLevel)
                .add("maximumLevel", maximumLevel)
                .add("frontLeftHighLeftLevel", frontLeftHighLeftLevel)
                .add("frontRightHighRightLevel", frontRightHighRightLevel)
                .add("rearLeftMidLeftLevel", rearLeftMidLeftLevel)
                .add("rearRightMidRightLevel", rearRightMidRightLevel)
                .add("subwooferLevel", subwooferLevel)
                .toString();
    }
}
