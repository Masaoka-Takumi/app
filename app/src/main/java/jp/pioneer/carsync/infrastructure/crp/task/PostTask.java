package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;
import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 通知タスク.
 * <p>
 * 車載機への通知（応答がない）を行う。
 */
public class PostTask extends SendTask {
    private OutgoingPacket mOutgoingPacket;
    private Callback mCallback;

    /**
     * コンストラクタ.
     *
     * @param outgoingPacket 送信パケット
     * @param callback コールバック
     * @throws NullPointerException {@code outgoingPacket}がnull
     */
    public PostTask(@NonNull OutgoingPacket outgoingPacket, @Nullable Callback callback) {
        mOutgoingPacket = checkNotNull(outgoingPacket);
        mCallback = callback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("taskId", getSendTaskId())
                .add("packetIdType", mOutgoingPacket.packetIdType)
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendTask inject(@NonNull CarRemoteSessionComponent component) {
        checkNotNull(component).inject(this);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public SendTaskId getSendTaskId() {
        return SendTaskId.NOTIFY_TASK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {
        if (post(mOutgoingPacket)) {
            Optional.ofNullable(mCallback)
                    .ifPresent(Callback::onSuccess);
        } else {
            Optional.ofNullable(mCallback)
                    .ifPresent(Callback::onError);
        }
    }

    /**
     * コールバック.
     */
    public interface Callback {
        /**
         * 通知成功時に呼ばれるハンドラ.
         */
        void onSuccess();

        /**
         * 通知失敗時に呼ばれるハンドラ.
         */
        void onError();
    }
}
