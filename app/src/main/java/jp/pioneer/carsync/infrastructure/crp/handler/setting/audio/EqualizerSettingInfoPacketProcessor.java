package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.AudioSettingEqualizerType;
import jp.pioneer.carsync.domain.model.EqualizerSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * [OPAL] EQ設定情報パケットプロセッサ.
 * <p>
 * [OPAL] EQ設定情報応答と通知で使用する。
 */
public class EqualizerSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 18;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public EqualizerSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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
            // D2:SPECIAL EQ種別
            setting.specialEqType = data[2];
            // D3:最小ステップ値
            setting.minimumStep = data[3];
            // D4:最大ステップ値
            setting.maximumStep = data[4];
            // D5:BAND1(50Hz)
            setting.band1 = data[5];
            // D6:BAND2(80Hz)
            setting.band2 = data[6];
            // D7:BAND3(125Hz)
            setting.band3 = data[7];
            // D8:BAND4(200Hz)
            setting.band4 = data[8];
            // D9:BAND5(315Hz)
            setting.band5 = data[9];
            // D10:BAND6(500Hz)
            setting.band6 = data[10];
            // D11:BAND7(800Hz)
            setting.band7 = data[11];
            // D12:BAND8(1.25KHz)
            setting.band8 = data[12];
            // D13:BAND9(2KHz)
            setting.band9 = data[13];
            // D14:BAND10(3.15KHz)
            setting.band10 = data[14];
            // D15:BAND11(5KHz)
            setting.band11 = data[15];
            // D16:BAND12(8KHz)
            setting.band12 = data[16];
            // D17:BAND13(12.5KHz)
            setting.band13 = data[17];

            setting.updateVersion();
            audioSetting.updateVersion();
            Timber.d("process() EqualizerSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
