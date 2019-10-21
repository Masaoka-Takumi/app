package jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AlarmVolumeSetting;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * 警告音量設定情報パケットプロセッサ.
 * <p>
 * 警告音量設定情報応答と通知で使用する。
 */
public class AlarmVolumeSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 4;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public AlarmVolumeSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            ParkingSensorSetting parkingSensorSetting = mStatusHolder.getParkingSensorSetting();
            AlarmVolumeSetting setting = parkingSensorSetting.alarmVolumeSetting;
            // D1:最小値
            // D2:最大値
            // D3:現在の設定値
            setting.setValue(ubyteToInt(data[1]), ubyteToInt(data[2]), ubyteToInt(data[3]));

            parkingSensorSetting.updateVersion();
            Timber.d("process() AlarmVolumeSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
