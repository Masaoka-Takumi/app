package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.SiriusXmSourceController;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SiriusXmSourceControllerの実装.
 */
public class SiriusXmSourceControllerImpl extends SourceControllerImpl implements SiriusXmSourceController {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ
     */
    @Inject
    public SiriusXmSourceControllerImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toggleBand() {
        Timber.i("toggleBand()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.BAND_ESC);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callPreset(@IntRange(from = 1, to = 6) int presetNo) {
        Timber.i("callPreset() presetNo = %d", presetNo);
        checkArgument(presetNo >= 1);
        checkArgument(presetNo <= 6);

        final CarDeviceControlCommand command = CarDeviceControlCommand.presetKey(presetNo);
        if (command == null) {
            Timber.w("This case is impossible.");
            return;
        }
        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(command);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectFavorite(int channelNo, @NonNull SxmBandType bandType, int sId) {
        Timber.i("selectFavorite() channelNo = %d, bandType = %s, sId = %d", channelNo, bandType, sId);
        checkNotNull(bandType);

        OutgoingPacket packet = mPacketBuilder.createFavoriteSiriusXmSetCommand(channelNo, bandType.getCode(), sId);
        mCarDeviceConnection.sendPacket(packet);
    }

    @Override
    public void channelUp() {
        Timber.i("channelUp()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_RIGHT);
        mCarDeviceConnection.sendPacket(packet);
    }

    @Override
    public void channelDown() {
        Timber.i("channelDown()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_LEFT);
        mCarDeviceConnection.sendPacket(packet);
    }

    @Override
    public void scanUp() {
        Timber.i("scanUp()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_RIGHT);
        mCarDeviceConnection.sendPacket(packet);
    }

    @Override
    public void scanDown() {
        Timber.i("scanDown()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_LEFT);
        mCarDeviceConnection.sendPacket(packet);
    }

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
    public void presetUp() {
        Timber.i("presetUp()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_UP);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void presetDown() {
        Timber.i("presetDown()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_DOWN);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toggleLiveMode() {
        Timber.i("toggleLiveMode()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.LIVE);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toggleChannelModeOrReplayMode() {
        Timber.i("toggleChannelMode()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.REPLAY_MODE);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toggleTuneMix() {
        Timber.i("toggleTuneMix()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.TUNE_MIX);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseSubscriptionUpdating() {
        Timber.i("releaseSubscriptionUpdating()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.OFF_HOOK);
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
}
