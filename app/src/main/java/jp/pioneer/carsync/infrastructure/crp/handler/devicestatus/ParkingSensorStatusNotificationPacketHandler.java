package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.ParkingSensorErrorStatus;
import jp.pioneer.carsync.domain.model.ParkingSensorStatus;
import jp.pioneer.carsync.domain.model.SensorDistanceUnit;
import jp.pioneer.carsync.domain.model.SensorStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.getBitsValue;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * パーキングセンサーステータス情報通知パケットハンドラ.
 */
public class ParkingSensorStatusNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 7;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public ParkingSensorStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
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
            ParkingSensorStatus status = mStatusHolder.getParkingSensorStatus();
            // D1
            b = data[1];
            //  bit[4]:エラー状態
            status.errorStatus = ParkingSensorErrorStatus.valueOf((byte) getBitsValue(b, 4, 1));
            //  bit[3]:センサーD状態
            status.sensorStatusD = SensorStatus.valueOf((byte) getBitsValue(b, 3, 1));
            //  bit[2]:センサーC状態
            status.sensorStatusC = SensorStatus.valueOf((byte) getBitsValue(b, 2, 1));
            //  bit[1]:センサーB状態
            status.sensorStatusB = SensorStatus.valueOf((byte) getBitsValue(b, 1, 1));
            //  bit[0]:センサーA状態
            status.sensorStatusA = SensorStatus.valueOf((byte) getBitsValue(b, 0, 1));
            // D2:距離単位
            b = data[2];
            status.sensorDistanceUnit = SensorDistanceUnit.valueOf(b);
            // D3:センサーA距離
            b = data[3];
            status.sensorDistanceA = ubyteToInt(b);
            // D4:センサーB距離
            b = data[4];
            status.sensorDistanceB = ubyteToInt(b);
            // D5:センサーC距離
            b = data[5];
            status.sensorDistanceC = ubyteToInt(b);
            // D6:センサーD距離
            b = data[6];
            status.sensorDistanceD = ubyteToInt(b);

            status.updateVersion();
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createParkingSensorStatusNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}