package jp.pioneer.carsync.domain.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.component.TextToSpeechController;
import jp.pioneer.carsync.domain.model.Notification;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 通知読み上げ.
 * <p>
 * TextToSpeechエンジンを使用した通知の読み上げを行う。
 * TextToSpeechエンジンの処理の都合上、初期化（{@link #initialize(TextToSpeechController.Callback)}）と
 * 終了（{@link #terminate()}）を明示的に行う必要がある。初期化と終了は、同一のインスタンスに対して行うこと。<br>
 * 読み上げる通知のインスタンスは通知読み上げ設定が有効でないと取得出来ないため、通知読み上げ設定は参照していない。
 */
public class ReadNotification {
    @Inject Context mContext;
    @Inject TextToSpeechController mTextToSpeechController;

    /**
     * コンストラクタ.
     */
    @Inject
    public ReadNotification() {
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
     *
     * @param notification 読み上げを行う通知
     * @throws NullPointerException {@code notification}がnull
     * @see TextToSpeechController#speak(String)
     */
    public void startReading(@NonNull Notification notification) {
        checkNotNull(notification);

        StringBuilder sb = new StringBuilder();
        // 読み上げ宣言
        sb.append(mContext.getString(R.string.mes_009, notification.getTitle()));
        sb.append("\n");
        // 本文
        sb.append(notification.getText());

        mTextToSpeechController.speak(sb.toString());
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
