package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.PandoraMediaInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.ThumbStatus;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * Pandoraステータス情報通知パケットハンドラ.
 */
public class PandoraStatusNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public PandoraStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
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

            int majorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;
            PandoraMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().pandoraMediaInfo;
            if (majorVer >= 3) {
                v3(data, info);
            }

            info.updateVersion();
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createPandoraStatusNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }

    private void v3(byte[] data, PandoraMediaInfo info) {
        byte b;
        // D1
        b = data[1];
        //  bit[0]:(RESERVED)
        //  bit[1-2]:Rate設定状態
        info.thumbStatus = ThumbStatus.valueOf((byte) PacketUtil.getBitsValue(b, 1, 2));
    }
}
