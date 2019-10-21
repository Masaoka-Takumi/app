package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ushortToInt;

/**
 * CHANNEL NUMBER : SiriusXMパケットハンドラ.
 */
public class DeviceSxmChannelNumberNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 9;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceSxmChannelNumberNotificationPacketHandler(@NonNull CarRemoteSession session) {
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
            checkPacketDataLength(data, DATA_LENGTH);

            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
            // D1-D2:有効最小CHANNEL NUMBER
            info.minimumChannelNumber = ushortToInt(data, 1);
            // D3-D4:有効最大CHANNEL NUMBER
            info.maximumChannelNumber = ushortToInt(data, 3);
            // D5-D6:現在のCHANNEL NUMBER
            info.currentChannelNumber = ushortToInt(data, 5);
            // D7:Band
            info.band = SxmBandType.valueOf(data[7]);
            // D8-D9:SID
            info.sid = ushortToInt(data, 8);

            info.updateVersion();
            Timber.d("doHandle() SxmMediaInfo = " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
