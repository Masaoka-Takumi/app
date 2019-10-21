package jp.pioneer.carsync.domain.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.TextToSpeechController;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TextToSpeechが利用可能かチェック.
 * <p>
 * TextToSpeech（以降TTS）は、ロケールに対応した音声データがインストールされて
 * いないと使用出来ない。
 * 通知読み上げ等、TTSを使用する機能を有効にする際は、TTSが利用可能かチェックし、
 * 利用可能でない場合は音声データをインストールする画面に誘導すること。
 * 音声データをインストールする画面へは、以下のインテントで遷移出来る。
 * <pre>{@code
 *  Intent intent = new Intent();
 *  intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 * }</pre>
 */
public class CheckAvailableTextToSpeech implements TextToSpeechController.Callback {
    @Inject TextToSpeechController mTextToSpeechController;
    private Callback mCallback;

    /**
     * コンストラクタ.
     */
    @Inject
    public CheckAvailableTextToSpeech() {
    }

    /**
     * 実行.
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback}がnull
     */
    public void execute(@NonNull Callback callback) {
        mCallback = checkNotNull(callback);
        mTextToSpeechController.initialize(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInitializeSuccess() {
        mTextToSpeechController.terminate();
        mCallback.onResult(Result.AVAILABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInitializeError(@NonNull TextToSpeechController.Error error) {
        mTextToSpeechController.terminate();
        switch (error) {
            case LANG_MISSING_DATA:
                mCallback.onResult(Result.LANG_MISSING_DATA);
                break;
            case LANG_NOT_SUPPORTED:
                mCallback.onResult(Result.LANG_NOT_SUPPORTED);
                break;
            case FAILURE:
                mCallback.onResult(Result.MAY_NOT_DISABLED);
                break;
            default:
                Timber.e("onInitializeError() can't happen. error = " + error);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSpeakStart() {
        // do not called
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSpeakDone() {
        // do not called
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSpeakError(@NonNull TextToSpeechController.Error error) {
        // do not called
    }

    /**
     * チェック結果.
     */
    public enum Result {
        /** 利用可能. */
        AVAILABLE,
        /** 現在のロケールの言語データがインストールされていない. */
        LANG_MISSING_DATA,
        /** 現在のロケールにTTSエンジンが対応していない. */
        LANG_NOT_SUPPORTED,
        /**
         * TTSエンジンが無効化されているかもしれない.
         * <p>
         * 明確に情報を取得出来ないため恐らく…
         */
        MAY_NOT_DISABLED
    }

    /**
     * コールバック.
     */
    public interface Callback {
        /**
         * チェック後に呼ばれるハンドラ.
         *
         * @param result チェック結果
         */
        @UiThread
        void onResult(@NonNull Result result);
    }
}

