package jp.pioneer.carsync.infrastructure.crp.handler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 応答パケットハンドラ.
 * <p>
 * 車載機への要求に対する応答を扱う基本クラス。
 * サブクラスは、{@link #handle(IncomingPacket)}の実装で{@link #setResult(Object)}を呼び出すこと。
 *
 * @param <T> 結果の型
 */
public abstract class ResponsePacketHandler<T> implements PacketHandler {
    private T mResult;

    /**
     * 結果取得.
     * <p>
     * {@link #handle(IncomingPacket)}実行後に呼び出すこと。
     *
     * @return 結果
     */
    @Nullable
    public T getResult() {
        return mResult;
    }

    /**
     * 結果設定.
     *
     * @param result 結果
     * @throws NullPointerException {@code result}がnull
     */
    protected void setResult(@NonNull T result) {
        mResult = checkNotNull(result);
    }
}
