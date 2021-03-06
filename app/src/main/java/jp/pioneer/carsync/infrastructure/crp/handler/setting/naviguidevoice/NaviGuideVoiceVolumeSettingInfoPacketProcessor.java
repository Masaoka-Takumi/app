package jp.pioneer.carsync.infrastructure.crp.handler.setting.naviguidevoice;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.NaviGuideVoiceSetting;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * ナビガイド音声ボリューム設定情報パケットプロセッサ.
 * <p>
 * ナビガイド音声ボリューム設定情報応答と通知で使用する。
 */
public class NaviGuideVoiceVolumeSettingInfoPacketProcessor {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public NaviGuideVoiceVolumeSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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

            NaviGuideVoiceSetting naviGuideVoiceSetting = mStatusHolder.getNaviGuideVoiceSetting();
            // D1:ナビガイド音声ボリューム設定
            naviGuideVoiceSetting.naviGuideVoiceVolumeSetting = NaviGuideVoiceVolumeSetting.valueOf(data[1]);

            naviGuideVoiceSetting.updateVersion();
            Timber.d("process() NaviGuideVoiceVolumeSetting = " + naviGuideVoiceSetting.naviGuideVoiceVolumeSetting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
