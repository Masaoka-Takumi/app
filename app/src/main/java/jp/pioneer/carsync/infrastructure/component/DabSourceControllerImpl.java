package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.DabSourceController;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * DabSourceControllerの実装.
 */
public class DabSourceControllerImpl extends SourceControllerImpl implements DabSourceController {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ.
     */
    @Inject
    public DabSourceControllerImpl() {
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
    public void toggleLiveMode() {
        Timber.i("toggleLiveMode()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.LIVE);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toggleTimeShiftMode() {
        Timber.i("toggleTimeShiftMode()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.TIME_SHIFT);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void togglePlay() {
        Timber.i("togglePlay()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.PLAY_PAUSE);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seekUp() {
        Timber.i("seekUp()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.SEEK_UP);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seekCancel() {
        Timber.i("seekCancel()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_RIGHT);
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
    public void callPreset(@IntRange(from = 1, to = 6) int presetNo) {
        checkArgument(presetNo >= 1);
        checkArgument(presetNo <= 6);
        Timber.i("callPreset() presetNo = %d", presetNo);

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
    public void updateList() {
        Timber.i("updateList()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CANCEL_LIST_UPDATE);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectFavorite(int index, @NonNull DabBandType bandType, int eid, long sid, int scids) {
        checkNotNull(bandType);
        Timber.i("selectFavorite() index = %d, bandType = %s, eid = %d, sid = %d, scids = %d", index, bandType, eid, sid, scids);

        OutgoingPacket packet = mPacketBuilder.createFavoriteDabSetCommand(index, bandType.getCode(), eid, sid, scids);
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
