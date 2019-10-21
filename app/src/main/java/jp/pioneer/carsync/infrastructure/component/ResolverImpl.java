package jp.pioneer.carsync.infrastructure.component;

import android.app.Activity;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.common.api.Status;

import jp.pioneer.carsync.domain.component.Resolver;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolverの実装.
 */
public class ResolverImpl implements Resolver {
    private Status mStatus;

    /**
     * コンストラクタ
     *
     * @param status GoogleApiClient操作によって生成されたステータス
     * @throws NullPointerException {@code status}がnull
     */
    public ResolverImpl(@NonNull Status status) {
        checkNotNull(status);
        mStatus = status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startResolutionForResult(@NonNull Activity activity, int requestCode) throws IntentSender.SendIntentException {
        checkNotNull(activity);
        callStartResolutionForResult(activity, requestCode);
    }

    /**
     * Status.startResolutionForResultを呼び出す
     * <p>
     * UnitTest用
     *
     * @param activity アクティビティー
     * @param requestCode リクエストコード
     */
    @VisibleForTesting
    void callStartResolutionForResult(Activity activity, int requestCode) throws IntentSender.SendIntentException {
        mStatus.startResolutionForResult(activity, requestCode);
    }
}
