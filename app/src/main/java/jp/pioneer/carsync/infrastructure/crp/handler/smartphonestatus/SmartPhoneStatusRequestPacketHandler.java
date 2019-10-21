package jp.pioneer.carsync.infrastructure.crp.handler.smartphonestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * SmartPhoneステータス情報要求パケットハンドラ.
 */
public class SmartPhoneStatusRequestPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;
    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public SmartPhoneStatusRequestPacketHandler(@NonNull CarRemoteSession session) {
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

            ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
            SmartPhoneStatus status = mStatusHolder.getSmartPhoneStatus();
            return getPacketBuilder().createSmartPhoneStatusResponse(version, status);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
