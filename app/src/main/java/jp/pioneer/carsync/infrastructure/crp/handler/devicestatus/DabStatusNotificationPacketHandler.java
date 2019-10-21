package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.getBitsValue;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * DABステータス情報通知パケットハンドラ.
 */
public class DabStatusNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DabStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
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
            CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
            DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
            // D1
            b = data[1];
            //  bit[5]:TimeShift Mode 遷移可能状態
            info.timeShiftModeAvailable = spec.timeShiftSupported ? isBitOn(data[1], 5) : false;
            //  bit[4]:TimeShift Mode状態
            info.timeShiftMode = isBitOn(data[1], 4);
            //  bit[0-3]:ステータス
            info.tunerStatus = TunerStatus.fromMediaSourceAndCode(MediaSourceType.DAB, getBitsValue(b, 0, 4));
            // D2
            b = data[2];
            //  bit[0-2]:再生状態
            int code = getBitsValue(b, 0, 3);
            switch (code) {
                case 0:
                    info.playbackMode = PlaybackMode.PAUSE;
                    break;
                case 1:
                    info.playbackMode = PlaybackMode.PLAY;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid playback mode code: " + code);
            }

            info.updateVersion();
            Timber.d("doHandle() DabStatus " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createDabStatusNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
