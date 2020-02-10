package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

/**
 *　音声認識種別
 */
public enum VoiceRecognizeType {

    /**
     * Pioneer Smart Sync
     */
    PIONEER_SMART_SYNC(0, R.string.set_325) {
        @Override
        public VoiceRecognizeType toggle() {
            return ALEXA;
        }
    },
    /**
     * Google Assistant
     */
    ANDROID_VR(1, R.string.set_415) {
        @Override
        public VoiceRecognizeType toggle() {
            return PIONEER_SMART_SYNC;
        }
    },
    /**
     * Alexa
     */
    ALEXA(2, R.string.set_324) {
        @Override
        public VoiceRecognizeType toggle() {
            return PIONEER_SMART_SYNC;
        }
    },
    ;

    public final int code;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    VoiceRecognizeType(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    /**
     * インデックスから取得.
     *
     * @param code インデックス
     * @return インデックスでの定義値に該当するVoiceRecognizeType
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static VoiceRecognizeType valueOf(int code) {
        for (VoiceRecognizeType value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return VoiceRecognizeType
     */
    public abstract VoiceRecognizeType toggle();
}
