package jp.pioneer.carsync.domain.interactor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.domain.component.TextToSpeechController;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.application.util.AppUtil;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * テキスト読み上げ.
 * <p>
 * TextToSpeechエンジンを使用したテキストの読み上げを行う。
 * TextToSpeechエンジンの処理の都合上、初期化（{@link #initialize(TextToSpeechController.Callback)}）と
 * 終了（{@link #terminate()}）を明示的に行う必要がある。初期化と終了は、同一のインスタンスに対して行うこと。<br>
 */
public class ReadText {
    @Inject TextToSpeechController mTextToSpeechController;
    @Inject Context mContext;
    @Inject @ForDomain StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     */
    @Inject
    public ReadText() {
    }

    /**
     * 初期化.
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback}がnull
     * @see TextToSpeechController#initialize(TextToSpeechController.Callback)
     */
    public void initialize(@NonNull TextToSpeechController.Callback callback) {
        checkNotNull(callback);

        mTextToSpeechController.initialize(callback);
    }

    /**
     * 読み上げ開始.
     * <p>
     * TTSが現在の言語に対応しているかを判定し、
     * 対応していない場合はデフォルト言語(en_us)のリソースを使用して読み上げる。
     *
     * @param stringId 読み上げを行うテキストのResource id
     * @see TextToSpeechController#speak(String)
     */
    public void startReading(@StringRes int stringId) {
        String text;
        if(mStatusHolder.getAppStatus().isTtsSupportedCurrentLocale){
            text = mContext.getString(stringId);
        } else {
            text = AppUtil.getDefaultLocalizedResources(mContext).getString(stringId);
        }
        mTextToSpeechController.speak(text);
    }

    /**
     * 読み上げ停止.
     *
     * @see TextToSpeechController#stop()
     */
    public void stopReading() {
        mTextToSpeechController.stop();
    }

    /**
     * 終了.
     *
     * @see TextToSpeechController#terminate()
     */
    public void terminate() {
        mTextToSpeechController.terminate();
    }
}
