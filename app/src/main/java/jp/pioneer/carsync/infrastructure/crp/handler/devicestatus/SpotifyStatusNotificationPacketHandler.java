package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.SpotifyMediaInfo;
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
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * Spotifyステータス情報通知パケットハンドラ.
 */
public class SpotifyStatusNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public SpotifyStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
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
            SpotifyMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().spotifyMediaInfo;
            byte b;
            // D1
            b = data[1];
            //  bit[0]:ステータス
            info.radioPlaying = isBitOn(b, 0);

            if (majorVer >= 3) {
                v3(data, info);
            }

            info.updateVersion();
            Timber.d("doHandle() SpotifyStatus = " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createSpotifyStatusNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }

    private void v3(byte[] data, SpotifyMediaInfo info) {
        byte b;
        // D1
        b = data[1];
        //  bit[1-2]:Rate設定状態
        info.thumbStatus = ThumbStatus.valueOf((byte) PacketUtil.getBitsValue(b, 1, 2));
    }
}
