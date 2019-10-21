package jp.pioneer.carsync.infrastructure.component;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.PhoneSettingUpdater;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

/**
 * PhoneSettingUpdaterの実装.
 */
public class PhoneSettingUpdaterImpl implements PhoneSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ
     */
    @Inject
    public PhoneSettingUpdaterImpl(){

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoAnswer(boolean enabled) {
        Timber.i("setAutoAnswer() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createAutoAnswerSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoPairing(boolean enabled) {
        Timber.i("setAutoPairing() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createAutoPairingSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }
}
