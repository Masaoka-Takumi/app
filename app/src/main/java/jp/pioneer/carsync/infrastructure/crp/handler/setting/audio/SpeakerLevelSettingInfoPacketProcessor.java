package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.SpeakerLevelSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * SPEAKER LEVEL情報パケットプロセッサ.
 * <p>
 * SPEAKER LEVEL情報応答と通知で使用する。
 */
public class SpeakerLevelSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 8;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public SpeakerLevelSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
        mStatusHolder = checkNotNull(statusHolder);
    }

    /**
     * 処理.
     *
     * @param packet 受信パケット
     * @return {@link Boolean#TRUE}:成功。{@link Boolean#FALSE}:それ以外。
     */
    public Boolean process(@NonNull IncomingPacket packet) {
        try {
            byte[] data = checkNotNull(packet).getData();
            checkPacketDataLength(data, DATA_LENGTH);

            AudioSetting audioSetting = mStatusHolder.getAudioSetting();
            SpeakerLevelSetting setting = audioSetting.speakerLevelSetting;
            // D1:最大レベル値
            setting.maximumLevel = data[1];
            // D2:最小レベル値
            setting.minimumLevel = data[2];
            // D3:FrontLeft / HghLeft(2way mode)のレベル値
            setting.frontLeftHighLeftLevel = data[3];
            // D4:FrontRight / HighRight(2way mode)のレベル値
            setting.frontRightHighRightLevel = data[4];
            // D5:RearLeft / MidLeft(2way mode)のレベル値
            setting.rearLeftMidLeftLevel = data[5];
            // D6:RearRight / MidRight(2way mode)のレベル値
            setting.rearRightMidRightLevel = data[6];
            // D7:Subwooferのレベル値
            setting.subwooferLevel = data[7];

            audioSetting.updateVersion();
            Timber.d("process() SpeakerLevelSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
