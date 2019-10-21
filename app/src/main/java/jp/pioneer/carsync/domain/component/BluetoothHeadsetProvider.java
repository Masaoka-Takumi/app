package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

/**
 * ヘッドセットプロバイダー.
 * <p>
 * 車載機とSCOモードのやりとりをするためのクラス。
 * {@link #prepareBluetoothHeadset}と{@link #finishBluetoothHeadset}は同時に実行できない。
 */
public interface BluetoothHeadsetProvider {

    /**
     * Bluetoothヘッドセットの開始.
     * <p>
     * 完了時に{@link Callback#onComplete(HeadsetRequestType, Result)}が呼ばれる。
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback}がnull
     */
    void prepareBluetoothHeadset(@NonNull Callback callback);

    /**
     * Bluetoothヘッドセットの終了.
     * <p>
     * 完了時に{@link Callback#onComplete(HeadsetRequestType, Result)}が呼ばれる。
     *
     * @param callback コールバック
     * @param isRinging 着信による終了か否か
     * @throws NullPointerException {@code callback}がnull
     */
    void finishBluetoothHeadset(@NonNull Callback callback, boolean isRinging);

    /**
     * 接続コールバック.
     */
    interface Callback{
        /**
         * Bluetoothヘッドセットの接続/切断の完了.
         * <p>
         * 成功時は{@code result}が{@link Result#COMPLETE}
         * 失敗時は{@code result}が{@link Result#ERROR}
         * となる。
         */
        @UiThread
        void onComplete(HeadsetRequestType type, Result result);
    }

    /**
     * 結果.
     *
     * @see Callback#onComplete(HeadsetRequestType, Result)
     */
    enum Result{
        /** 完了 */
        COMPLETE,
        /** エラー */
        ERROR
    }

    /**
     * ヘッドセットへの要求種別.
     *
     * @see Callback#onComplete(HeadsetRequestType, Result)
     */
    enum HeadsetRequestType{
        /** 接続 */
        CONNECT,
        /** 切断 */
        DISCONNECT
    }
}
