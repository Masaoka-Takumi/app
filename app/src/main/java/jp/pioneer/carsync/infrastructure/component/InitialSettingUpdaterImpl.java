package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.InitialSettingUpdater;
import jp.pioneer.carsync.domain.model.AmStep;
import jp.pioneer.carsync.domain.model.FmStep;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.RearOutputSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * InitialSettingUpdaterの実装.
 */
public class InitialSettingUpdaterImpl implements InitialSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ.
     */
    @Inject
    public InitialSettingUpdaterImpl(){

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFmStep(@NonNull FmStep step) {
        Timber.i("setFmStep() type = %s", step);
        checkNotNull(step);

        OutgoingPacket packet = mPacketBuilder.createFmStepSettingNotification(
                step
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAmStep(@NonNull AmStep step) {
        Timber.i("setAmStep() step = %s", step);
        checkNotNull(step);

        OutgoingPacket packet = mPacketBuilder.createAmStepSettingNotification(
                step
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRearOutputPreoutOutput(@NonNull RearOutputPreoutOutputSetting setting) {
        Timber.i("setRearOutputPreoutOutput() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createRearOutputPreoutOutputSettingNotification(
                setting
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRearOutput(@NonNull RearOutputSetting setting) {
        Timber.i("setRearOutput() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createRearOutputSettingNotification(
                setting
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMenuDisplayLanguage(@NonNull MenuDisplayLanguageType type) {
        Timber.i("setMenuDisplayLanguage() type = %s", type);
        checkNotNull(type);

        OutgoingPacket packet = mPacketBuilder.createMenuDisplayLanguageSettingNotification(
                type
        );
        mCarDeviceConnection.sendPacket(packet);
    }
}
