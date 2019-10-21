package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AbstractMediaInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
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
 * 楽曲再生時間通知パケットハンドラ.
 */
public class DeviceAudioPlaybackPositionNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 6;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceAudioPlaybackPositionNotificationPacketHandler(@NonNull CarRemoteSession session) {
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

            // D1:ソース情報
            MediaSourceType type = MediaSourceType.valueOf(data[1]);
            AbstractMediaInfo info = mStatusHolder.findMediaInfoByMediaSourceType(type);
            // D2-D3:総再生時間
            info.totalSecond = ushortToInt(data, 2);
            // D4-D5:現在の再生時間
            info.currentSecond = ushortToInt(data, 4);

            info.updateVersion();
            Timber.d("doHandle() MediaInfo = " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
