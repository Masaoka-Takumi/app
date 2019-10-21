package jp.pioneer.carsync.infrastructure.crp.handler.controlcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSettingListUpdateEvent;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * 設定リスト情報更新通知パケットハンドラ.
 */
public class SettingListUpdatedNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public SettingListUpdatedNotificationPacketHandler(@NonNull CarRemoteSession session) {
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

            // D1:設定種別
            // D2:リスト種別
            SettingListType type = SettingListType.valueOf(data[1], data[2]);

            getSession().publishEvent(new CrpSettingListUpdateEvent(type));
            return getPacketBuilder().createSettingListUpdateNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
