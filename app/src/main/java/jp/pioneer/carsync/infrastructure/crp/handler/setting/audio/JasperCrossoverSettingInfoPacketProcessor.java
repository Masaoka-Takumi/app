package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.CrossoverSetting;
import jp.pioneer.carsync.domain.model.HpfLpfSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.HpfLpfFilterType;
import jp.pioneer.carsync.domain.model.JasperCutoffSetting;
import jp.pioneer.carsync.domain.model.JasperSlopeSetting;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * [JASPER] CROSSOVER HPF/LPF設定情報パケットプロセッサ.
 * <p>
 * [JASPER] CROSSOVER HPF/LPF設定情報応答と通知で使用する。
 */
public class JasperCrossoverSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 5;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public JasperCrossoverSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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
            // D1:FILTER種別
            HpfLpfFilterType filterType = HpfLpfFilterType.valueOf(data[1]);
            CrossoverSetting.JasperCrossoverSetting setting = crossoverSetting.findJasperCrossoverSetting(filterType);
            // D2:HPF/LPF設定
            // ARCのコメントを要約
            // 「17Lowモデル車載機にはLowPassFilterをON/OFFする機能が無いので、LPF設定をON固定にするか、
            // 使用しないようにしてください」
            if(setting.hpfLpfFilterType == HpfLpfFilterType.LPF) {
                setting.hpfLpfSetting = HpfLpfSetting.ON_FIXED;
            } else {
                setting.hpfLpfSetting = HpfLpfSetting.valueOf(data[2]);
            }
            // D3:カットオフ周波数設定値
            setting.cutoffSetting = JasperCutoffSetting.valueOf(data[3]);
            // D4:スロープ設定値
            setting.slopeSetting = JasperSlopeSetting.valueOf(data[4]);

            audioSetting.updateVersion();
            Timber.d("process() CrossoverSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
