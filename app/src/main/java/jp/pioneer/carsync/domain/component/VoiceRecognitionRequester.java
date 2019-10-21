package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

/**
 * 音声認識要求者.
 * <p>
 * 音声認識開始/終了要求をするためのクラス.
 */
public interface VoiceRecognitionRequester {

    /**
     * 開始要求.
     * <p>
     * 要求に成功した場合、{@link Callback#onSuccess()}が呼ばれる
     * 要求に失敗した場合、{@link Callback#onError()}が呼ばれる
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback}がnull
     */
    void startRequest(@NonNull Callback callback);

    /**
     * 終了要求.
     * <p>
     * 要求に成功した場合、{@link Callback#onSuccess()}が呼ばれる
     * 要求に失敗した場合、{@link Callback#onError()}が呼ばれる
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback}がnull
     */
    void finishRequest(@NonNull Callback callback);

    /**
     * コールバック.
     * <p>
     * 要求の成功もしくは失敗時に呼び出されるコールバック
     */
    interface Callback{

        /**
         * 音声認識開始要求に成功.
         *
         * {@see #startRequest}
         */
        @UiThread
        void onSuccess();

        /**
         * 音声認識開始要求に失敗.
         *
         * {@see #startRequest}
         */
        @UiThread
        void onError();
    }
}
