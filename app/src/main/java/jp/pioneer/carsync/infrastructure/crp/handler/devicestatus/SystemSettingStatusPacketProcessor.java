package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SystemSettingSpec;
import jp.pioneer.carsync.domain.model.SystemSettingStatus;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * システム設定ステータス情報パケットプロセッサ.
 * <p>
 * システム設定ステータス情報応答と通知で使用する。
 */
public class SystemSettingStatusPacketProcessor {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public SystemSettingStatusPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            SystemSettingSpec spec = mStatusHolder.getCarDeviceSpec().systemSettingSpec;
            SystemSettingStatus status = mStatusHolder.getSystemSettingStatus();
            byte b;
            // D1:有効システム設定1
            b = data[1];
            status.auxSettingEnabled = spec.auxSettingSupported ? isBitOn(b, 7) : false;
            status.spotifySettingEnabled = spec.spotifySettingSupported ? isBitOn(b, 6) : false;
            status.pandoraSettingEnabled = spec.pandoraSettingSupported ? isBitOn(b, 5) : false;
            status.btAudioSettingEnabled = spec.btAudioSettingSupported ? isBitOn(b, 4) : false;
            status.powerSaveSettingEnabled = spec.powerSaveSettingSupported ? isBitOn(b, 3) : false;
            status.demoSettingEnabled = spec.demoSettingSupported ? isBitOn(b, 2) : false;
            status.attMuteSettingEnabled = spec.attMuteSettingSupported ? isBitOn(b, 1) : false;
            status.beepToneSettingEnabled = spec.beepToneSettingSupported ? isBitOn(b, 0) : false;
            // D2:有効システム設定2
            b = data[2];
            status.distanceUnitSettingEnabled = spec.distanceUnitSettingSupported ? isBitOn(b, 5) : false;
            status.displayOffSettingEnabled = spec.displayOffSettingSupported ? isBitOn(b, 4) : false;
            status.autoPiSettingEnabled = spec.autoPiSettingSupported ? isBitOn(b, 3) : false;
            status.steeringRemoteControlSettingEnabled = spec.steeringRemoteControlSettingSupported ? isBitOn(b, 2) : false;
            status.usbAutoSettingEnabled = spec.usbAutoSettingSupported ? isBitOn(b, 1) : false;
            status.appAutoStartSettingEnabled = spec.appAutoStartSettingSupported ? isBitOn(b, 0) : false;

            status.updateVersion();
            Timber.d("process() SystemSettingStatus = " + status);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
