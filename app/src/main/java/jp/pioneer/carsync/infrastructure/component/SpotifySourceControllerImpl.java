package jp.pioneer.carsync.infrastructure.component;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.SpotifySourceController;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

/**
 * SpotifySourceControllerの実装.
 */
public class SpotifySourceControllerImpl extends SourceControllerImpl implements SpotifySourceController {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ.
     */
    @Inject
    public SpotifySourceControllerImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void togglePlay() {
        Timber.i("togglePlay()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.PAUSE);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skipNextTrack() {
        Timber.i("skipNextTrack()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_RIGHT);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skipPreviousTrack() {
        Timber.i("skipPreviousTrack()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_LEFT);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void volumeUp() {
        Timber.i("volumeUp()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.VOLUME_UP);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void volumeDown() {
        Timber.i("volumeDown()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.VOLUME_DOWN);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toggleRepeatMode() {
        Timber.i("toggleRepeatMode()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_6);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toggleShuffleMode() {
        Timber.i("toggleShuffleMode()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_5);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setThumbUp() {
        Timber.i("setThumbUp()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_1);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setThumbDown() {
        Timber.i("setThumbDown()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_2);
        mCarDeviceConnection.sendPacket(packet);
    }
}
