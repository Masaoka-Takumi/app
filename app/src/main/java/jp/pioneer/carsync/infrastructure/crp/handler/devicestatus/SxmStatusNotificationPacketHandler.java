package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
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
 * SiriusXMステータス情報通知パケットハンドラ.
 */
public class SxmStatusNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public SxmStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
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
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
            // D1
            b = data[1];
            //  bit[7]:Replay Mode 遷移可能状態
            info.replayModeAvailable = isBitOn(b, 7);
            //  bit[6]:Replay Mode 状態
            info.inReplayMode = isBitOn(b, 6);
            //  bit[5]:Tune Mix遷移可能状態
            info.tuneMixAvailable = spec.tuneMixSupported ? isBitOn(b, 5) : false;
            //  bit[4]:Tune Mix 状態
            info.inTuneMix = isBitOn(b, 4);
            //  bit[0-3]:ステータス
            info.tunerStatus = TunerStatus.fromMediaSourceAndCode(MediaSourceType.SIRIUS_XM, getBitsValue(b, 0, 4));

            // D2
            b = data[2];
            //  bit[3]:Subscription update表示状態
            info.subscriptionUpdatingShowing = isBitOn(b, 3);
            //  bit[0-2]:再生状態
            int code = getBitsValue(b, 0, 3);
            switch (code) {
                case 0:
                    info.playbackMode = PlaybackMode.PAUSE;
                    break;
                case 1:
                    info.playbackMode = PlaybackMode.PLAY;
                    break;
                case 2:
                    info.playbackMode = PlaybackMode.FAST_FORWARD;
                    break;
                case 3:
                    info.playbackMode = PlaybackMode.REWIND;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid playback mode code: " + code);
            }

            info.updateVersion();
            Timber.d("doHandle() SxmStatus " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createSxmStatusNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
