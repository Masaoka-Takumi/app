package jp.pioneer.carsync.domain.model;

/**
 * スピーカー種別（混合）.
 * <p>
 * Standard Mode/2Way Network Modeのスピーカー種別を並列で扱う場合に使用する。
 */
public enum MixedSpeakerType {
    /** FrontLeft / HighLeft. */
    FRONT_LEFT_HIGH_LEFT(0x00),
    /** FrontRight / HighRight. */
    FRONT_RIGHT_HIGH_RIGHT(0x01),
    /** RearLeft / MidLeft. */
    REAR_LEFT_MID_LEFT(0x02),
    /** RearRight / MidRight. */
    REAR_RIGHT_MID_RIGHT(0x03),
    /** Subwoofer. */
    SUBWOOFER(0x04)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    MixedSpeakerType(int code) {
        this.code = code;
    }
}
