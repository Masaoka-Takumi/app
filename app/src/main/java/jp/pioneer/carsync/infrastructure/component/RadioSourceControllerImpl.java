package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.RadioSourceController;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PtySearchSetting;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.TunerFunctionType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * RadioSourceControllerの実装.
 */
public class RadioSourceControllerImpl extends SourceControllerImpl implements RadioSourceController {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ.
     */
    @Inject
    public RadioSourceControllerImpl() {
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
    public void selectFavorite(int index, @NonNull RadioBandType bandType, int pi) {
        checkNotNull(bandType);
        Timber.i("selectFavorite() index = %d, bandType = %s, pi = %d", index, bandType, pi);

        OutgoingPacket packet = mPacketBuilder.createFavoriteRadioSetCommand(index, bandType.getCode(), pi);
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
    public void manualDown() {
        Timber.i("manualDown()");

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_LEFT);
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
    public void seekUp() {
        Timber.i("seekUp()");

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
                MediaSourceType.RADIO,
                TunerFunctionType.BSM.code,
                isStart ? 0x01: 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startPtySearch(@NonNull PtySearchSetting setting) {
        Timber.i("startPtySearch() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
                MediaSourceType.RADIO,
                TunerFunctionType.PTY_SEARCH.code,
                setting.code);
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
