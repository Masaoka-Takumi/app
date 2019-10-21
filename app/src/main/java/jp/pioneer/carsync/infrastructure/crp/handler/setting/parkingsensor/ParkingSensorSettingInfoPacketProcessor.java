package jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * パーキングセンサー設定情報パケットプロセッサ.
 * <p>
 * パーキングセンサー設定情報応答と通知で使用する。
 */
public class ParkingSensorSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public ParkingSensorSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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
            // D1:パーキングセンサー設定
            parkingSensorSetting.parkingSensorSetting = (ubyteToInt(data[1]) == 0x01);

            parkingSensorSetting.updateVersion();
            Timber.d("process() ParkingSensorSetting = " + parkingSensorSetting.parkingSensorSetting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
