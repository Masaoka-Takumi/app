package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import com.google.common.base.Objects;

import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;

/**
 * オーディオ設定ステータス情報通知パケットハンドラ.
 */
public class AudioSettingStatusNotificationPacketHandler extends AbstractPacketHandler {
    private AudioSettingStatusPacketProcessor mPacketProcessor;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public AudioSettingStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mPacketProcessor = new AudioSettingStatusPacketProcessor(session.getStatusHolder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        if (!Objects.equal(mPacketProcessor.process(packet), Boolean.TRUE)) {
            throw new BadPacketException("Bad packet.");
        }

        getSession().publishStatusUpdateEvent(packet.getPacketIdType());
        return getPacketBuilder().createAudioSettingStatusNotificationResponse();
    }
}
