package jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi;

import android.support.annotation.NonNull;

import com.google.common.base.Objects;

import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;

/**
 * DISP BRIGHTNESS設定情報通知（個別設定モデル用）パケットハンドラ.
 */
public class DisplayBrightnessSettingInfoNotificationPacketHandler extends AbstractPacketHandler {
    private DisplayBrightnessSettingInfoPacketProcessor mPacketProcessor;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DisplayBrightnessSettingInfoNotificationPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mPacketProcessor = new DisplayBrightnessSettingInfoPacketProcessor(session.getStatusHolder());
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
        return null;
    }
}
