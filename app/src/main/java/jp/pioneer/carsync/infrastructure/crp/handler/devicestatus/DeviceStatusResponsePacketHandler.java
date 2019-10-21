package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 車載機ステータス情報応答パケットハンドラ.
 * <p>
 * {@link StatusHolder}の値を更新するが、タスクで更新イベントを発行することを想定。
 */
public class DeviceStatusResponsePacketHandler extends DataResponsePacketHandler {
    private DeviceStatusPacketProcessor mPacketProcessor;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceStatusResponsePacketHandler(@NonNull CarRemoteSession session) {
        mPacketProcessor = new DeviceStatusPacketProcessor(checkNotNull(session).getStatusHolder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        setResult(mPacketProcessor.process(packet));
    }
}
