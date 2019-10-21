package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 音声認識応答種別.
 */
public enum VoiceRecognitionResponseType {
    /** 終了応答. */
    FINISH(0x00),
    /** 開始応答. */
    START(0x01)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    VoiceRecognitionResponseType(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するVoiceRecognitionResponseType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static VoiceRecognitionResponseType valueOf(byte code) {
        for (VoiceRecognitionResponseType value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }
}
