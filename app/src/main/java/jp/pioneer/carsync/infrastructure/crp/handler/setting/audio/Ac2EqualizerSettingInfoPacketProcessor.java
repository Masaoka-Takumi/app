package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.EqualizerSetting;
import jp.pioneer.carsync.domain.model.AudioSettingEqualizerType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * [AC2] EQ設定情報パケットプロセッサ.
 * <p>
 * [AC2] EQ設定情報応答と通知で使用する。
 */
public class Ac2EqualizerSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 9;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public Ac2EqualizerSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            AudioSetting audioSetting = mStatusHolder.getAudioSetting();
            EqualizerSetting setting = audioSetting.equalizerSetting;
            // D1:EQ種別
            setting.audioSettingEqualizerType = AudioSettingEqualizerType.valueOf(data[1]);
            // D2:最小ステップ値
            setting.minimumStep = data[2];
            // D3:最大ステップ値
            setting.maximumStep = data[3];
            // D4:BAND1(80Hz)
            setting.band1 = data[4];
            // D5:BAND2(250Hz)
            setting.band2 = data[5];
            // D6:BAND3(800Hz)
            setting.band3 = data[6];
            // D7:BAND4(2.5KHz)
            setting.band4 = data[7];
            // D8:BAND5(8KHz)
            setting.band5 = data[8];

            audioSetting.updateVersion();
            Timber.d("process() EqualizerSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
