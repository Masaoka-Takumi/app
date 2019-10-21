package jp.pioneer.carsync.infrastructure.crp;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.task.InitialAuthTask;
import jp.pioneer.carsync.infrastructure.crp.task.SessionStartTask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 認証例外.
 * <p>
 * 認証のシーケンス（初期認証～セッション開始）でエラーが発生した場合にスローする例外。
 *
 * @see InitialAuthTask
 * @see SessionStartTask
 */
public class AuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 2650264331753711559L;
    /** エラーとなった受信パケットIDタイプ. */
    public final IncomingPacketIdType errorIdType;

    /**
     * コンストラクタ.
     *
     * @param errorIdType エラーとなった受信パケットIDタイプ
     * @throws NullPointerException {@code errorIdType}がnull
     */
    public AuthenticationException(@NonNull IncomingPacketIdType errorIdType) {
        this.errorIdType = checkNotNull(errorIdType);
    }
}
