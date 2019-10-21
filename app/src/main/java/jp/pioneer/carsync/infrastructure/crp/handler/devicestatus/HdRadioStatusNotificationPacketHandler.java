package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.HdRadioDigitalAudioStatus;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.HdRadioStationStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
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

/**
 * HD Radioステータス情報通知パケットハンドラ.
 */
public class HdRadioStatusNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public HdRadioStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
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
            HdRadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().hdRadioInfo;
            // D1
            b = data[1];
            //  bit[5]:HDデジタル音声受信状態
            info.hdRadioDigitalAudioStatus = HdRadioDigitalAudioStatus.valueOf((byte) getBitsValue(b, 5, 1));
            //  bit[4]:HD放送局情報受信状態
            info.hdRadioStationStatus = HdRadioStationStatus.valueOf((byte) getBitsValue(b, 4, 1));
            //  bit[0-3]:ステータス
            info.tunerStatus = TunerStatus.fromMediaSourceAndCode(MediaSourceType.HD_RADIO, getBitsValue(b, 0, 4));

            info.updateVersion();
            Timber.d("doHandle() HdRadioStatus " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createHdRadioStatusNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
