package jp.pioneer.carsync.domain.model;

/**
 * Audio output mode.
 */
public enum AudioOutputMode {
    /** Standard mode.  */
    STANDARD(0),
    /** 2way network mode. */
    TWO_WAY_NETWORK(1)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ
     *
     * @param code プロトコルでの定義値
     */
    AudioOutputMode(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するAudioOutputMode
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static AudioOutputMode valueOf(int code) {
        for (AudioOutputMode value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
