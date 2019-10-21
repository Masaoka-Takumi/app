package jp.pioneer.carsync.domain.event;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.CarDeviceErrorType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 車載機エラーイベント.
 * <p>
 * 車載機からエラーが通知された場合に発生する。
 * …が、エラーコードの定義が無い（別の通知に置き換わる等して無くなった）ので、現状発生しないはずである。
 * 通信仕様書ではエラーコード（16進値）とエラー文字列をSmartPhoneの画面に表示することを期待する記載になっている。
 */
public class CarDeviceErrorEvent {
    /** 車載機エラーコード. */
    public CarDeviceErrorType errorType;

    /**
     * コンストラクタ.
     *
     * @param errorType 車載器エラー種別
     * @throws NullPointerException {@code errorType}がnull
     */
    public CarDeviceErrorEvent(@NonNull CarDeviceErrorType errorType) {
        this.errorType = checkNotNull(errorType);
    }

}
