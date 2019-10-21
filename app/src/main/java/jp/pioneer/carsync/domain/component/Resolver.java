package jp.pioneer.carsync.domain.component;

import android.app.Activity;
import android.content.IntentSender;
import android.support.annotation.NonNull;

/**
 * 解決者.
 */
public interface Resolver {
    /**
     * エラーの解決を実行する.
     *
     * @param activity アクティビティー
     * @param requestCode リクエストコード
     * @throws NullPointerException {@code activity}がnull
     */
    void startResolutionForResult(@NonNull Activity activity, int requestCode) throws IntentSender.SendIntentException;
}
