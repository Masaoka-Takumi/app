package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.SystemSettingUpdater;
import jp.pioneer.carsync.domain.model.AttMuteSetting;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SystemSettingUpdaterの実装.
 */
public class SystemSettingUpdaterImpl implements SystemSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ.
     */
    @Inject
    public SystemSettingUpdaterImpl(){

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBeepTone(boolean enabled) {
        Timber.i("setBeepTone() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createBeepToneSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttMute(@NonNull AttMuteSetting setting) {
        Timber.i("setAttMute() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createAttMuteSettingNotification(
                setting
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDistanceUnit(@NonNull DistanceUnit setting) {
        Timber.i("setDistanceUnit() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createDistanceUnitSettingNotification(
                setting
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDemo(boolean enabled) {
        Timber.i("setDemo() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createDemoSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPowerSave(boolean enabled) {
        Timber.i("setPowerSave() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createPowerSaveSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBtAudio(boolean enabled) {
        Timber.i("setBtAudio() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createBtAudioSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPandora(boolean enabled) {
        Timber.i("setPandora() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createPandoraSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpotify(boolean enabled) {
        Timber.i("setSpotify() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createSpotifySettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAux(boolean enabled) {
        Timber.i("setAux() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createAuxSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAppAutoStart(boolean enabled) {
        Timber.i("setAppAutoStart() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createAppAutoStartSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUsbAuto(boolean enabled) {
        Timber.i("setUsbAuto() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createUsbAutoSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplayOff(boolean enabled) {
        Timber.i("setDisplayOff() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createDisplayOffSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSteeringRemoteControl(@NonNull SteeringRemoteControlSettingType type) {
        Timber.i("setSteeringRemoteControl() type = %s", type);
        checkNotNull(type);

        OutgoingPacket packet = mPacketBuilder.createSteeringRemoteControlSettingNotification(
                type
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoPi(boolean enabled) {
        Timber.i("setAutoPi() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createAutoPiSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }
}
