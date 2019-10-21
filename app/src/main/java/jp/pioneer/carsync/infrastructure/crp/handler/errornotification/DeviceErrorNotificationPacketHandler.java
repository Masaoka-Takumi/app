package jp.pioneer.carsync.infrastructure.crp.handler.errornotification;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.event.CarDeviceErrorEvent;
import jp.pioneer.carsync.domain.model.CarDeviceErrorType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * 車載機エラー通知パケットハンドラ.
 */
public class DeviceErrorNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 5;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceErrorNotificationPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mStatusHolder = session.getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            // D1-D4:エラーコード
            CarDeviceErrorType errorType = CarDeviceErrorType.valueOf(PacketUtil.uintToLong(packet.getData(), 1));

            Timber.d("doHandle() CarDeviceErrorType = " + errorType);
            getSession().publishEvent(new CarDeviceErrorEvent(errorType));
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createDeviceErrorNotificationResponse(ResponseCode.OK);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
