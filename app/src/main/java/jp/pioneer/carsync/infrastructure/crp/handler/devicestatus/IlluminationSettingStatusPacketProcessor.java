package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * イルミ設定ステータス情報パケットプロセッサ.
 * <p>
 * イルミ設定ステータス情報応答と通知で使用する。
 */
public class IlluminationSettingStatusPacketProcessor {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public IlluminationSettingStatusPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            int majorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;
            IlluminationSettingSpec spec = mStatusHolder.getCarDeviceSpec().illuminationSettingSpec;
            IlluminationSettingStatus status = mStatusHolder.getIlluminationSettingStatus();
            byte b;
            // D1:有効イルミ設定1
            b = data[1];
            status.hotaruNoHikariLikeSettingEnabled = spec.hotaruNoHikariLikeSettingSupported ? isBitOn(b, 7) : false;
            status.btPhoneColorSettingEnabled = spec.btPhoneColorSettingSupported ? isBitOn(b, 6) : false;
            status.brightnessSettingEnabled = spec.brightnessSettingSupported ? isBitOn(b, 5) : false;
            status.dimmerSettingEnabled = spec.dimmerSettingSupported ? isBitOn(b, 4) : false;
            status.colorCustomDispSettingEnabled = spec.colorCustomDispSettingSupported ? isBitOn(b, 3) : false;
            status.colorCustomKeySettingEnabled = spec.colorCustomKeySettingSupported ? isBitOn(b, 2) : false;
            status.dispColorSettingEnabled = spec.dispColorSettingSupported ? isBitOn(b, 1) : false;
            status.keyColorSettingEnabled = spec.keyColorSettingSupported ? isBitOn(b, 0) : false;

            if (majorVer >= 3) {
                v3(data, status);
            }

            if (majorVer >= 4) {
                v4(data, status);
            }

            status.updateVersion();
            Timber.d("process() IlluminationSettingStatus = " + status);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }

    private void v3(byte[] data, IlluminationSettingStatus status) {
        byte b;
        IlluminationSettingSpec spec = mStatusHolder.getCarDeviceSpec().illuminationSettingSpec;
        // D2:有効イルミ設定2
        b = data[2];
        status.dispBrightnessSettingEnabled = spec.dispBrightnessSettingSupported ? isBitOn(b, 1) : false;
        status.keyBrightnessSettingEnabled = spec.keyBrightnessSettingSupported ? isBitOn(b, 0) : false;
    }

    private void v4(byte[] data, IlluminationSettingStatus status) {
        byte b;
        IlluminationSettingSpec spec = mStatusHolder.getCarDeviceSpec().illuminationSettingSpec;
        // D2:有効イルミ設定2
        b = data[2];
        status.incomingMessageColorSettingEnabled = spec.incomingMessageColorSettingSupported ? isBitOn(b, 7) : false;
        status.commonColorCustomSettingEnabled = spec.commonColorCustomSettingSupported ? isBitOn(b, 6) : false;
        status.commonColorSettingEnabled = spec.commonColorSettingSupported ? isBitOn(b, 5) : false;
        status.sphBtPhoneColorSettingEnabled = spec.sphBtPhoneColorSettingSupported ? isBitOn(b, 4) : false;
        status.audioLevelMeterLinkedSettingEnabled = spec.audioLevelMeterLinkedSettingSupported ? isBitOn(b, 3) : false;
        status.customFlashPatternSettingEnabled = spec.customFlashPatternSettingSupported ? isBitOn(b, 2) : false;
    }
}
