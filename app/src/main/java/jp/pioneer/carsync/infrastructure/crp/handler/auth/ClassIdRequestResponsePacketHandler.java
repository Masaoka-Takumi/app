package jp.pioneer.carsync.infrastructure.crp.handler.auth;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * Class ID応答パケットハンドラ.
 * <p>
 * {@link StatusHolder}の値を更新するが、タスクで更新イベントを発行することを想定。
 */
public class ClassIdRequestResponsePacketHandler extends DataResponsePacketHandler {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public ClassIdRequestResponsePacketHandler(@NonNull CarRemoteSession session) {
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            ProtocolSpec spec = mStatusHolder.getProtocolSpec();
            // D1:Class ID
            CarDeviceClassId classId = CarDeviceClassId.valueOf(data[1]);
            spec.setCarDeviceClassId(classId);

            Timber.d("handle() ClassId = " + classId);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "handle()");
            setResult(Boolean.FALSE);
        }
    }
}
