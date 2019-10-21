package jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AlarmOutputDestinationSetting;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * 警告音出力先設定情報パケットプロセッサ.
 * <p>
 * 警告音出力先設定情報応答と通知で使用する。
 */
public class AlarmOutputDestinationSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public AlarmOutputDestinationSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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
            // D1:警告音出力先設定
            parkingSensorSetting.alarmOutputDestinationSetting = AlarmOutputDestinationSetting.valueOf(data[1]);

            parkingSensorSetting.updateVersion();
            Timber.d("process() AlarmOutputDestinationSetting = " + parkingSensorSetting.alarmOutputDestinationSetting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
