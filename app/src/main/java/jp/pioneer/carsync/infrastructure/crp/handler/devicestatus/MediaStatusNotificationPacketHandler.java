package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AbstractMediaInfo;
import jp.pioneer.carsync.domain.model.CarDeviceRepeatMode;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.getBitsValue;

/**
 * メディア系共通ステータス情報通知パケットハンドラ.
 */
public class MediaStatusNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public MediaStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
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
            // D1:ソース種別
            MediaSourceType type = MediaSourceType.valueOf(data[1]);
            AbstractMediaInfo info = mStatusHolder.findMediaInfoByMediaSourceType(type);
            // D2
            b = data[2];
            //  bit[5]:シャッフル状態
            info.shuffleMode = ShuffleMode.valueOf((byte) getBitsValue(b, 5, 1));
            //  bit[3-4]:リピート状態
            info.repeatMode = CarDeviceRepeatMode.valueOf((byte) getBitsValue(b, 3, 2));
            //  bit[0-2]:再生状態
            info.playbackMode = PlaybackMode.valueOf((byte) getBitsValue(b, 0, 3));

            info.updateVersion();
            Timber.d("doHandle() CommonMediaStatus = " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createMediaStatusNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
