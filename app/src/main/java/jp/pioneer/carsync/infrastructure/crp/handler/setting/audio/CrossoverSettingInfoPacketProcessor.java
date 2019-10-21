package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.Ac2StandardCutoffSetting;
import jp.pioneer.carsync.domain.model.Ac2StandardSubwooferSlopeSetting;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CrossoverSetting;
import jp.pioneer.carsync.domain.model.HpfLpfSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.StandardCutoffSetting;
import jp.pioneer.carsync.domain.model.StandardSlopeSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TwoWayNetworkMidHfpMidLfpHighHpfSlopeSetting;
import jp.pioneer.carsync.domain.model.TwoWayNetworkMidLpfHighHpfCutoffSetting;
import jp.pioneer.carsync.domain.model.TwoWayNetworkSubwooferLpfMidHpfCutoffSetting;
import jp.pioneer.carsync.domain.model.TwoWayNetworkSubwooferLpfSlopeSetting;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * CROSSOVER設定情報パケットプロセッサ.
 * <p>
 * CROSSOVER設定情報応答と通知で使用する。
 */
public class CrossoverSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 5;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public CrossoverSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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
            CrossoverSetting crossoverSetting = audioSetting.crossoverSetting;
            CarDeviceSpec carDeviceSpec = mStatusHolder.getCarDeviceSpec();
            byte b;
            // D1:スピーカー種別
            SpeakerType speakerType = SpeakerType.valueOf(data[1]);
            CrossoverSetting.SpeakerCrossoverSetting setting = crossoverSetting.findSpeakerCrossoverSetting(speakerType);
            // D2:HPF/LPF設定
            setting.hpfLpfSetting = HpfLpfSetting.valueOf(data[2]);
            // D3:カットオフ周波数設定値
            b = data[3];
            switch (setting.speakerType) {
                case FRONT:
                case REAR:
                case SUBWOOFER_STANDARD_MODE:
                    if (carDeviceSpec.ac2AudioSettingSupported) {
                        setting.cutoffSetting = Ac2StandardCutoffSetting.valueOf(b);
                    } else if (carDeviceSpec.audioSettingSupported) {
                        setting.cutoffSetting = StandardCutoffSetting.valueOf(b);
                    } else {
                        Timber.w("process() Unexpected audio menu status.");
                    }
                    break;
                case SUBWOOFER_2WAY_NETWORK_MODE:
                case MID_HPF:
                    setting.cutoffSetting = TwoWayNetworkSubwooferLpfMidHpfCutoffSetting.valueOf(b);
                    break;
                case MID_LPF:
                case HIGH:
                    setting.cutoffSetting = TwoWayNetworkMidLpfHighHpfCutoffSetting.valueOf(b);
                    break;
                default:
                    throw new AssertionError("can't happen.");
            }

            // D4:スロープ設定値
            b = data[4];
            switch (setting.speakerType) {
                case FRONT:
                case REAR:
                    setting.slopeSetting = StandardSlopeSetting.valueOf(b);
                    break;
                case SUBWOOFER_STANDARD_MODE:
                    if (carDeviceSpec.ac2AudioSettingSupported) {
                        setting.slopeSetting = Ac2StandardSubwooferSlopeSetting.valueOf(b);
                    } else if (carDeviceSpec.audioSettingSupported) {
                        setting.slopeSetting = StandardSlopeSetting.valueOf(b);
                    } else {
                        Timber.w("process() Unexpected audio menu status.");
                    }
                    break;
                case SUBWOOFER_2WAY_NETWORK_MODE:
                    setting.slopeSetting = TwoWayNetworkSubwooferLpfSlopeSetting.valueOf(b);
                    break;
                case MID_HPF:
                case MID_LPF:
                case HIGH:
                    setting.slopeSetting = TwoWayNetworkMidHfpMidLfpHighHpfSlopeSetting.valueOf(b);
                    break;
                default:
                    throw new AssertionError("can't happen.");
            }

            audioSetting.updateVersion();
            Timber.d("process() CrossoverSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
