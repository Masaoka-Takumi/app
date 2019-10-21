package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.FaderBalanceSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * FADER/BALANCE情報パケットプロセッサ.
 * <p>
 * FADER/BALANCE情報応答と通知で使用する。
 */
public class FaderBalanceSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 7;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public FaderBalanceSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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
            FaderBalanceSetting setting = audioSetting.faderBalanceSetting;
            // D1:最小FADER値
            setting.minimumFader = data[1];
            // D2:最大FADER値
            setting.maximumFader = data[2];
            // D3:最小BALANCE値
            setting.minimumBalance = data[3];
            // D4:最大BALANCE値
            setting.maximumBalance = data[4];
            // D5:現在のFADER値
            setting.currentFader = data[5];
            // D6:現在のBALANCE値
            setting.currentBalance = data[6];

            audioSetting.updateVersion();
            Timber.d("process() FaderBalanceSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
