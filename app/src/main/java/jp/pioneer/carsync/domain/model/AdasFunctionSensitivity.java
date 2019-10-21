package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

/**
 * ADAS機能感度設定.
 */
public enum AdasFunctionSensitivity {
    /** OFF. */
    OFF(0, R.string.val_119),
    /** 感度LOW. */
    LOW(1, R.string.val_107){
        @Override
        public AdasFunctionSensitivity toggle() {
            return HIGH;
        }
    },
    /** 感度MIDDLE. */
    MIDDLE(2, R.string.val_108){
        @Override
        public AdasFunctionSensitivity toggle() {
            return LOW;
        }
    },
    /** 感度HIGH. */
    HIGH(3, R.string.val_109){
        @Override
        public AdasFunctionSensitivity toggle() {
            return MIDDLE;
        }
    };

    /** ADASライブラリの感度定義値 */
    public final int code;
    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ.
     *
     * @param code ADASライブラリの感度定義値
     * @param label 表示用文字列リソースID
     */
    AdasFunctionSensitivity(int code, @StringRes int label) {
        this.code = code;
        this.label = label;
    }

    public AdasFunctionSensitivity toggle(){
        return null;
    }
}
