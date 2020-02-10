package jp.pioneer.carsync.infrastructure.crp.handler.auth;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * Protocol Version応答パケットハンドラ.
 * <p>
 * {@link StatusHolder}の値を更新するが、タスクで更新イベントを発行することを想定。
 */
public class ProtocolVersionResponsePacketHandler extends DataResponsePacketHandler {
    private static final int V2_DATA_LENGTH = 2;
    private static final int V3_DATA_LENGTH = 3;
    private static final int MIN_DATA_LENGTH = Math.min(V2_DATA_LENGTH, V3_DATA_LENGTH);
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public ProtocolVersionResponsePacketHandler(@NonNull CarRemoteSession session) {
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, MIN_DATA_LENGTH);

            ProtocolSpec spec = mStatusHolder.getProtocolSpec();
            ProtocolVersion version;
            // D1:Protocol version : Major
            // D2:Protocol version : Minor ※Protocol version 3以降
            if (data.length == V2_DATA_LENGTH) {
                version = new ProtocolVersion(data[1]);
            } else {
                version = new ProtocolVersion(data[1], data[2]);
            }

            spec.setDeviceProtocolVersion(version);

            Timber.d("handle() ProtocolVersion = " + version);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "handle()");
            setResult(Boolean.FALSE);
        }
    }
}
