package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.EqualizerSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * [AC2] LEVEL設定情報パケットプロセッサ.
 * <p>
 * [AC2] LEVEL設定情報応答と通知で使用する。
 */
public class LevelSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 4;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public LevelSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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
            EqualizerSetting setting = audioSetting.equalizerSetting;
            // D1:最大LEVEL値
            setting.maximumLevel = ubyteToInt(data[1]);
            // D2:最小LEVEL値
            setting.minimumLevel = ubyteToInt(data[2]);
            // D3:LEVEL値
            setting.currentLevel = ubyteToInt(data[3]);

            audioSetting.updateVersion();
            Timber.d("process() (MaximumLevel, MinimumLevel, CurrentLevel) = %d, %d, %d",
                    setting.maximumLevel, setting.minimumLevel, setting.currentLevel);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
