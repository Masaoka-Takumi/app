package jp.pioneer.carsync.domain.model;

/**
 * 音声認識要求種別.
 */
public enum VoiceRecognitionRequestType {
    /** 終了要求. */
    FINISH(0x00),
    /** 開始要求. */
    START(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    VoiceRecognitionRequestType(int code) {
        this.code = code;
    }
}
