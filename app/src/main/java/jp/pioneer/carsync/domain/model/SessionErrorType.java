package jp.pioneer.carsync.domain.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

public enum SessionErrorType {
    /** デバイスが見つからない. */
    USB_NO_SUCH_DEVICE(R.string.ntc_007);

    /** エラー通知テキスト. */
    @StringRes public final int text;

    /**
     * コンストラクタ.
     *
     * @param text エラー通知テキスト
     */
    SessionErrorType(@StringRes int text){
        this.text = text;
    }
}
