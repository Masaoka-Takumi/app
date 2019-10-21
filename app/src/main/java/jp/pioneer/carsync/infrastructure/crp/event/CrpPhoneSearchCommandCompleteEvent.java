package jp.pioneer.carsync.infrastructure.crp.event;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * サーチコマンド完了イベント.
 * <p>
 * サーチコマンドが完了した場合に発生する。
 */
public class CrpPhoneSearchCommandCompleteEvent {
    /** 結果. */
    public final ResponseCode result;

    /**
     * コンストラクタ.
     *
     * @param result 結果
     * @throws NullPointerException {@code result}がnull
     */
    public CrpPhoneSearchCommandCompleteEvent(@NonNull ResponseCode result) {
        this.result = checkNotNull(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("result", result)
                .toString();
    }

}
