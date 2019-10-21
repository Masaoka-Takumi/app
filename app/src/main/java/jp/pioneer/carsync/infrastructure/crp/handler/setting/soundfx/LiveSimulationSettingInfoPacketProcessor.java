package jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * ライブシミュレーション設定情報パケットプロセッサ.
 * <p>
 * ライブシミュレーション設定情報応答と通知で使用する。
 */
public class LiveSimulationSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public LiveSimulationSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            SoundFxSetting soundFxSetting = mStatusHolder.getSoundFxSetting();
            LiveSimulationSetting setting = soundFxSetting.liveSimulationSetting;
            // D1:Sound Field Control設定
            setting.soundFieldControlSettingType = SoundFieldControlSettingType.valueOf(data[1]);
            // D2:Sound Effect設定
            SoundEffectSettingType type = SoundEffectSettingType.valueOf(data[2]);
            if(setting.soundFieldControlSettingType != SoundFieldControlSettingType.OFF) {
                setting.soundEffectSettingType = type;
            }

            setting.updateVersion();
            soundFxSetting.updateVersion();
            Timber.d("process() LiveSimulationSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
