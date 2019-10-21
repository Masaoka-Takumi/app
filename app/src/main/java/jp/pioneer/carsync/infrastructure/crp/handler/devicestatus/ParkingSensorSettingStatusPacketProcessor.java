package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.ParkingSensorSettingSpec;
import jp.pioneer.carsync.domain.model.ParkingSensorSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * パーキングセンサー設定ステータス情報パケットプロセッサ.
 * <p>
 * パーキングセンサー設定ステータス情報応答と通知で使用する。
 */
public class ParkingSensorSettingStatusPacketProcessor {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public ParkingSensorSettingStatusPacketProcessor(@NonNull StatusHolder statusHolder) {
        mStatusHolder = checkNotNull(statusHolder);
    }

    /**
     * 処理.
     *
     * @param packet 受信パケット
     * @return {@link Boolean#TRUE}:成功。{@link Boolean#FALSE}:それ以外。
     * @throws NullPointerException {@code packet}がnull
     */
    public Boolean process(@NonNull IncomingPacket packet) {
        try {
            byte[] data = checkNotNull(packet).getData();
            checkPacketDataLength(data, DATA_LENGTH);

            ParkingSensorSettingSpec spec = mStatusHolder.getCarDeviceSpec().parkingSensorSettingSpec;
            ParkingSensorSettingStatus status = mStatusHolder.getParkingSensorSettingStatus();
            byte b;
            // D1:有効Phone設定1
            b = data[1];
            status.backPolaritySettingEnabled = spec.backPolaritySettingSupported ? isBitOn(b, 2) : false;
            status.alarmOutputDestinationSettingEnabled = spec.alarmOutputDestinationSettingSupported ? isBitOn(b, 1) : false;
            status.alarmVolumeSettingEnabled = spec.alarmVolumeSettingSupported ? isBitOn(b, 0) : false;

            status.updateVersion();
            Timber.d("process() ParkingSensorSettingStatus = " + status);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
