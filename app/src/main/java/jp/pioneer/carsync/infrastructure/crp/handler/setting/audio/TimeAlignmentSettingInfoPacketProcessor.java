package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TimeAlignmentSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSettingMode;
import jp.pioneer.carsync.domain.model.TimeAlignmentStepUnit;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * TIME ALIGNMENT情報パケットプロセッサ.
 * <p>
 * TIME ALIGNMENT情報応答と通知で使用する。
 */
public class TimeAlignmentSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 10;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public TimeAlignmentSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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
            TimeAlignmentSetting setting = audioSetting.timeAlignmentSetting;
            // D1:設定値
            setting.mode = TimeAlignmentSettingMode.valueOf(data[1]);
            // D2:最大ステップ値
            setting.maximumStep = ubyteToInt(data[2]);
            // D3:最小ステップ値
            setting.minimumStep = ubyteToInt(data[3]);
            // D4:ステップ単位
            setting.stepUnit = TimeAlignmentStepUnit.valueOf(data[4]);
            // D5:FrontLeft / HghLeft(2way mode)のステップ値
            setting.frontLeftHighLeftStep = ubyteToInt(data[5]);
            // D6:FrontRight / HighRight(2way mode)のステップ値
            setting.frontRightHighRightStep = ubyteToInt(data[6]);
            // D7:RearLeft / MidLeft(2way mode)のステップ値
            setting.rearLeftMidLeftStep = ubyteToInt(data[7]);
            // D8:RearRight / MidRight(2way mode)のステップ値
            setting.rearRightMidRightStep = ubyteToInt(data[8]);
            // D9:Subwooferのステップ値
            setting.subwooferStep = ubyteToInt(data[9]);

            audioSetting.updateVersion();
            Timber.d("process() TimeAlignmentSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
