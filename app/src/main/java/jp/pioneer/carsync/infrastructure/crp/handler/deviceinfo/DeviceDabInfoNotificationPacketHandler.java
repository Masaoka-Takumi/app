package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
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
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * 周波数通知 : DABパケットハンドラ.
 */
public class DeviceDabInfoNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 24;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceDabInfoNotificationPacketHandler(@NonNull CarRemoteSession session) {
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

            DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
            // D1-D4:有効最小周波数
            info.minimumFrequency = uintToLong(data, 1);
            // D5-D8:有効最大周波数
            info.maximumFrequency = uintToLong(data, 5);
            // D9-D12:現在の周波数
            info.currentFrequency = uintToLong(data, 9);
            // D13:周波数単位
            info.frequencyUnit = TunerFrequencyUnit.valueOf(data[13], MediaSourceType.DAB);
            // D14:Band
            info.band = DabBandType.valueOf(data[14]);
            // D15-D16:EID
            info.eid = ushortToInt(data, 15);
            // D17-D20:SID
            info.sid = uintToLong(data, 17);
            // D21-D22:SCIdS
            info.scids = ushortToInt(data, 21);
            // D23:INDEX
            info.index = ubyteToInt(data[23]);

            info.updateVersion();
            mStatusHolder.getPresetChannelDictionary().applyFrequency(
                    MediaSourceType.DAB,
                    info.band.code,
                    info.currentFrequency
            );
            Timber.d("doHandle() DabInfo = " + info);
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
