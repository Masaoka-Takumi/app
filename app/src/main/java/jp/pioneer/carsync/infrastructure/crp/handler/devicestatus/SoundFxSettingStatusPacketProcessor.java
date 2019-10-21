package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * Sound FX設定ステータス情報パケットプロセッサ.
 * <p>
 * Sound FX設定ステータス情報応答と通知で使用する。
 */
public class SoundFxSettingStatusPacketProcessor {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public SoundFxSettingStatusPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            SoundFxSettingSpec spec = mStatusHolder.getCarDeviceSpec().soundFxSettingSpec;
            SoundFxSettingStatus status = mStatusHolder.getSoundFxSettingStatus();
            byte b;
            // D1:有効Sound FX設定1
            b = data[1];
            status.karaokeSettingEnabled = spec.karaokeSettingSupported ? isBitOn(b, 3) : false;
            status.liveSimulationSettingEnabled = spec.liveSimulationSettingSupported ? isBitOn(b, 2) : false;
            status.smallCarTaSettingEnabled = spec.smallCarTaSettingSupported ? isBitOn(b, 1) : false;
            status.superTodorokiSettingEnabled = spec.superTodorokiSettingSupported ? isBitOn(b, 0) : false;
            // D2:有効Sound FX設定2
            //  (RESERVED)

            status.updateVersion();
            Timber.d("process() SoundFxSettingStatus = " + status);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
