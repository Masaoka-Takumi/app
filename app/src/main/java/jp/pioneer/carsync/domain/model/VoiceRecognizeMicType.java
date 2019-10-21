package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

public enum VoiceRecognizeMicType {
    /** 車載器 */
    HEADSET(0,R.string.set_360){
    @Override
    public VoiceRecognizeMicType toggle() {
        return PHONE;
        }
    },
    /** 端末 */
    PHONE(1, R.string.set_361){
        @Override
        public VoiceRecognizeMicType toggle() {
            return HEADSET;
        }
    }
    ;

    public final int code;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    VoiceRecognizeMicType(int code, @StringRes int label) {
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
    public static VoiceRecognizeMicType valueOf(int code) {
        for (VoiceRecognizeMicType value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * トグル.
     *
     * @return VoiceRecognizeMicType
     */
    public abstract VoiceRecognizeMicType toggle();
}
