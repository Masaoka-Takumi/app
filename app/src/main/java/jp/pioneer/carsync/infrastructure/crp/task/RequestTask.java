package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;
import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 要求タスク.
 * <p>
 * 車載機への要求（応答がある）を行う。
 *
 * @param <T> 結果の型
 */
public class RequestTask<T> extends SendTask {
    private OutgoingPacket mOutgoingPacket;
    private Callback<T> mCallback;

    /**
     * コンストラクタ.
     *
     * @param outgoingPacket 送信パケット
     * @param callback コールバック
     * @throws NullPointerException {@code outgoingPacket}がnull
     */
    public RequestTask(@NonNull OutgoingPacket outgoingPacket, @Nullable Callback<T> callback) {
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
    public RequestTask<T> inject(@NonNull CarRemoteSessionComponent component) {
        checkNotNull(component).inject(this);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public SendTaskId getSendTaskId() {
        return SendTaskId.REQUEST_TASK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {
        if (!request(mOutgoingPacket)) {
            Optional.ofNullable(mCallback)
                    .ifPresent(Callback::onError);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doResponsePacket(@NonNull IncomingPacket packet) throws Exception {
        ResponsePacketHandler<T> handler = getResponsePacketHandlerFactory().create(packet.getPacketIdType());
        handler.handle(packet);
        Optional.ofNullable(mCallback)
                .ifPresent(callback -> callback.onResult(handler.getResult()));
    }

    /**
     * コールバック.
     */
    public interface Callback<T> {
        /**
         * 応答時に呼ばれるハンドラ.
         *
         * @param result 結果
         */
        void onResult(T result);

        /**
         * エラー発生時に呼ばれるハンドラ.
         * <p>
         * 送信に失敗した、応答がない時に呼ばれる。
         */
        void onError();
    }
}
