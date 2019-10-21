package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

/**
 * TextToSpeech制御.
 */
public interface TextToSpeechController {
    /**
     * 初期化.
     * <p>
     * 初期化に成功した場合、{@link Callback#onInitializeSuccess()}が呼ばれる。
     * 初期化に失敗した場合、{@link Callback#onInitializeError(Error)}が呼ばれる。
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback}がnull
     */
    void initialize(@NonNull Callback callback);

    /**
     * 読み上げ開始.
     * <p>
     * 読み上げ開始時に、{@link Callback#onSpeakStart()}が呼ばれる。
     * 読み上げ終了時に、{@link Callback#onSpeakDone()}が呼ばれる。
     * 読み上げ中に呼び出した場合、前の読み上げを停止してから読み上げを行う。停止した読み上げに対しての
     * {@link Callback#onSpeakDone()}は呼ばれない。
     * 読み上げが開始される前に呼び出した場合、前の読み上げが行われるかどうかは不定。（タイミング次第）
     *
     * @param text 読み上げるテキスト
     * @throws NullPointerException {@code text}がnull
     * @throws IllegalStateException 未初期化
     */
    void speak(@NonNull String text);

    /**
     * 読み上げ停止.
     * <p>
     * {@link Callback#onSpeakDone()}は呼ばれない。
     * 読み上げ中でない場合無視する。
     *
     * @throws IllegalStateException 未初期化
     */
    void stop();

    /**
     * 終了.
     * <p>
     * 未初期化（終了済）の場合に呼び出した場合無視する。
     * {@link #initialize(Callback)}で再初期化可能である。
     */
    void terminate();

    /**
     * エラー.
     *
     * @see Callback#onInitializeError(Error)
     * @see Callback#onSpeakError(Error)
     */
    enum Error {
        /** 現在のロケールの言語データがインストールされていない. */
        LANG_MISSING_DATA,
        /** 現在のロケールにTTSエンジンが対応していない. */
        LANG_NOT_SUPPORTED,
        /** 何らかのエラー. */
        FAILURE,
        ;
    }

    /**
     * コールバック.
     */
    interface Callback {
        /**
         * {@link #initialize(Callback)}が成功した場合に呼ばれるハンドラ.
         */
        @UiThread
        void onInitializeSuccess();

        /**
         * {@link #initialize(Callback)}が失敗した場合に呼ばれるハンドラ.
         *
         * @param error エラー
         */
        @UiThread
        void onInitializeError(@NonNull Error error);

        /**
         * 読み上げ開始時に呼ばれるハンドラ.
         */
        @UiThread
        void onSpeakStart();

        /**
         * 読み上げ終了時に呼ばれるハンドラ.
         */
        @UiThread
        void onSpeakDone();

        /**
         * 読み上げ出来ない場合に呼ばれるハンドラ.
         *
         * @param error エラー
         */
        @UiThread
        void onSpeakError(@NonNull Error error);
    }
}
