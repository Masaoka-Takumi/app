package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.HdRadioSourceController;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.HdRadioFunctionType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * HdRadioSourceControllerの実装.
 */
public class HdRadioSourceControllerImpl extends SourceControllerImpl implements HdRadioSourceController {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ.
     */
    @Inject
    public HdRadioSourceControllerImpl() {
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
    public void callPreset(int presetNo) {
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
    public void registerPreset(@IntRange(from = 1) int listIndex){
        checkArgument(listIndex >= 1);
        Timber.i("registerPreset() presetIndex = %d", listIndex);

        OutgoingPacket packet = mPacketBuilder.createTunerListRegisterPresetNotification(listIndex);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectFavorite(int index, @NonNull HdRadioBandType bandType, int multicastChannelNumber) {
        checkNotNull(bandType);
        Timber.i("selectFavorite() index = %d, bandType = %s, multicastChannelNumber = %d", index, bandType, multicastChannelNumber);

        OutgoingPacket packet = mPacketBuilder.createFavoriteHdRadioSetCommand(index, bandType.getCode(), multicastChannelNumber);
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
    public void manualUp() {
        Timber.i("manualUp()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_RIGHT);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seekUp() {
        Timber.i("manualDown()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.SEEK_UP);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startBsm(boolean isStart) {
        Timber.i("startBsm()");

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
            MediaSourceType.HD_RADIO,
            HdRadioFunctionType.BSM.code,
            isStart ? 0x01: 0x00);
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
