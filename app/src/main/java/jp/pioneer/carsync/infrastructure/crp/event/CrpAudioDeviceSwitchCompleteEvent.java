package jp.pioneer.carsync.infrastructure.crp.event;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Audio Device 切替完了イベント.
 * <p>
 * Audio Device 切替が完了した場合に発生する。
 */
public class CrpAudioDeviceSwitchCompleteEvent {
    /** 結果. */
    public final ResponseCode result;
    /** BDアドレス. */
    public final String bdAddress;

    /**
     * コンストラクタ.
     *
     * @param result 結果
     * @param bdAddress BDアドレス
     * @throws NullPointerException {@code result}、または、{@code bdAddress}がnull
     */
    public CrpAudioDeviceSwitchCompleteEvent(@NonNull ResponseCode result, @NonNull String bdAddress) {
        this.result = checkNotNull(result);
        this.bdAddress = checkNotNull(bdAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("result", result)
                .add("bdAddress", bdAddress)
                .toString();
    }
}
