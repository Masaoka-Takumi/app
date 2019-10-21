package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.SoundFxSettingUpdater;
import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SoundFxSettingUpdaterの実装.
 */
public class SoundFxSettingUpdaterImpl implements SoundFxSettingUpdater {
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject @ForInfrastructure
    StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     */
    @Inject
    public SoundFxSettingUpdaterImpl(){
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLiveSimulation(@NonNull SoundFieldControlSettingType soundFieldControlSettingType,
                                  @NonNull SoundEffectSettingType soundEffectSettingType) {
        Timber.i("setLiveSimulation() soundFieldControlSettingType = %s, soundEffectSettingType = %s" , soundFieldControlSettingType, soundEffectSettingType);
        checkNotNull(soundFieldControlSettingType);
        checkNotNull(soundEffectSettingType);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createLiveSimulationSettingNotification(soundFieldControlSettingType, soundEffectSettingType));
    }

    @Override
    public void setSuperTodoroki(@NonNull SuperTodorokiSetting setting) {
        Timber.i("setEqualizer() setting = %s", setting);
        checkNotNull(setting);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSuperTodorokiSettingNotification(setting));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSmallCarTa(@NonNull SmallCarTaSettingType type, @NonNull ListeningPosition position) {
        Timber.i("setSmallCarTa() type = %s, position = %s", type, position);
        checkNotNull(type);
        checkNotNull(position);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSmallCarTaSettingNotification(type, position));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setKaraokeSetting(boolean isEnabled) {
        Timber.i("setEqualizer() isEnabled = %s", isEnabled);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createKaraokeSettingNotification(isEnabled));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMicVolume(int volume) {
        Timber.i("setEqualizer() volume = %d", volume);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createMicVolumeSettingNotification(volume));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVocalCancel(boolean isEnabled) {
        Timber.i("setEqualizer() isEnabled = %s", isEnabled);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createVocalCancelSettingNotification(isEnabled));
    }
}
