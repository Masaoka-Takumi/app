package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.getBitsValue;

/**
 * Radioステータス情報通知パケットハンドラ.
 */
public class RadioStatusNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public RadioStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
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

            byte b;
            RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;
            // D1
            b = data[1];
            //  bit[0-3]:ステータス
            info.tunerStatus = TunerStatus.fromMediaSourceAndCode(MediaSourceType.RADIO, getBitsValue(b, 0, 4));

            info.updateVersion();
            Timber.d("doHandle() RadioStatus = " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createRadioStatusNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
