package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.UsbMediaInfo;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.getBitsValue;

/**
 * USBステータス情報通知パケットハンドラ.
 */
public class UsbStatusNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public UsbStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
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

            UsbMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().usbMediaInfo;
            // D1
            //  bit[0-2]:ステータス
            int status = getBitsValue(data[1], 0, 3);
            info.musicProtected = (status == 1);
            info.drmSkipped = (status == 2);
            info.unplayableFile = (status == 3);
            info.noUsbDevice = (status == 4);
            info.updateVersion();

            Timber.d("doHandle() UsbStatus = " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createUsbStatusNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
