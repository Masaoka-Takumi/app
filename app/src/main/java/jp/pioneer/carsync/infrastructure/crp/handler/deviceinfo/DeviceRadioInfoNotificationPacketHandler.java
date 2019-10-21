package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.RdsInterruptionType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.uintToLong;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ushortToInt;

/**
 * 周波数通知 : Radio/TI パケットハンドラ.
 */
public class DeviceRadioInfoNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 19;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceRadioInfoNotificationPacketHandler(@NonNull CarRemoteSession session) {
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

            RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;
            // D1-D4:有効最小周波数
            info.minimumFrequency = uintToLong(data, 1);
            // D5-D8:有効最大周波数
            info.maximumFrequency = uintToLong(data, 5);
            // D9-D12:現在の周波数
            info.currentFrequency = uintToLong(data, 9);
            // D13:周波数単位
            info.frequencyUnit = TunerFrequencyUnit.valueOf(data[13], MediaSourceType.RADIO);
            // D14:Band
            info.band = RadioBandType.valueOf(data[14]);
            // D15-D16:PI
            info.pi = ushortToInt(data, 15);
            // D17-D18
            info.index = ushortToInt(data, 17);
            // D19
            info.rdsInterruptionType = RdsInterruptionType.valueOf(data[19]);

            info.updateVersion();
            Timber.d("doHandle() RadioInfo = " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
