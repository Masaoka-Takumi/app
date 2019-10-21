package jp.pioneer.carsync.infrastructure.crp.handler.interruptinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.CarDeviceInterrupt;
import jp.pioneer.carsync.domain.model.CarDeviceInterruptType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.util.TextBytesUtil;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * 車載機割り込み情報通知パケットハンドラ.
 */
public class DeviceInterruptNotificationPacketHandler extends AbstractPacketHandler {
    private static final int MIN_DATA_LENGTH = 4;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceInterruptNotificationPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, MIN_DATA_LENGTH);

            CarDeviceInterrupt info;
            // D1:情報種別
            CarDeviceInterruptType type = CarDeviceInterruptType.valueOf(data[1]);
            if (type == CarDeviceInterruptType.INTERRUPT_RESET) {
                info = null;
            } else {
                // D2:文字コード
                // D3:割り込み情報
                String message = TextBytesUtil.extractText(data, 2);
                info = new CarDeviceInterrupt(type, message);
            }

            mStatusHolder.setCarDeviceInterrupt(info);
            Timber.d("doHandle() CarDeviceInterrupt = " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createDeviceInterruptNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
