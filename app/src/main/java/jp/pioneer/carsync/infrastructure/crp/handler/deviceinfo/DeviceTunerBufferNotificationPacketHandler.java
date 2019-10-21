package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
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
 * Tunerソースバッファ再生時間通知パケットハンドラ.
 */
public class DeviceTunerBufferNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 8;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceTunerBufferNotificationPacketHandler(@NonNull CarRemoteSession session) {
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
            // D2-D3:総バッファ時間
            int totalBufferTime = ushortToInt(data, 2);
            // D4-D5:現在の再生時間
            int currentPosition = ushortToInt(data, 4);
            // D6-D7:現在のバッファ時間
            int currentBufferTime = ushortToInt(data, 6);

            if (type == MediaSourceType.DAB) {
                DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
                info.totalBufferTime = totalBufferTime;
                info.currentPosition = currentPosition;
                info.currentBufferTime = currentBufferTime;
                info.updateVersion();
                Timber.d("doHandle() DabInfo = " + info);
            } else if (type == MediaSourceType.SIRIUS_XM) {
                SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
                info.totalBufferTime = totalBufferTime;
                info.currentPosition = currentPosition;
                info.currentBufferTime = currentBufferTime;
                info.updateVersion();
                Timber.d("doHandle() SxmMediaInfo = " + info);
            } else {
                // noting to do
                return null;
            }

            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
