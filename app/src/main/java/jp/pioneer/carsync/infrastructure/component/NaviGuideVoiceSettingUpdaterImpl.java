package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.NaviGuideVoiceSettingUpdater;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * NaviGuideVoiceSettingUpdaterの実装.
 */
public class NaviGuideVoiceSettingUpdaterImpl implements NaviGuideVoiceSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ.
     */
    @Inject
    public NaviGuideVoiceSettingUpdaterImpl(){
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNaviGuideVoice(boolean enabled) {
        Timber.i("setNaviGuideVoice() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createNaviGuideVoiceSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNaviGuideVoiceVolume(@NonNull NaviGuideVoiceVolumeSetting setting) {
        Timber.i("setNaviGuideVoiceVolume() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createNaviGuideVoiceVolumeSettingNotification(
                checkNotNull(setting)
        );
        mCarDeviceConnection.sendPacket(packet);
    }
}
