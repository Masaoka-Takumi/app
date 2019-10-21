package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.ParkingSensorSettingUpdater;
import jp.pioneer.carsync.domain.model.AlarmOutputDestinationSetting;
import jp.pioneer.carsync.domain.model.BackPolarity;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ParkingSensorSettingUpdaterの実装.
 */
public class ParkingSensorSettingUpdaterImpl implements ParkingSensorSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ.
     */
    @Inject
    public ParkingSensorSettingUpdaterImpl(){
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParkingSensorSetting(boolean isEnabled) {
        Timber.i("setParkingSensorSetting() isEnabled = %s", isEnabled);

        OutgoingPacket packet = mPacketBuilder.createParkingSensorSettingNotification(isEnabled);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlarmOutputDestination(@NonNull AlarmOutputDestinationSetting setting) {
        Timber.i("toggleAlarmOutputDestination() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createAlarmOutputDestinationSettingNotification(setting);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlarmVolume(int volume) {
        Timber.i("setAlarmVolume() volume = %d", volume);

        OutgoingPacket packet = mPacketBuilder.createAlarmVolumeSettingNotification(volume);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackPolarity(@NonNull BackPolarity setting) {
        Timber.i("setBackPolarity() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createBackPolaritySettingNotification(setting);
        mCarDeviceConnection.sendPacket(packet);
    }
}
