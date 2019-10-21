package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.HdRadioFunctionSettingUpdater;
import jp.pioneer.carsync.domain.model.HdRadioFunctionType;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * HdRadioFunctionSettingUpdaterの実装.
 */
public class HdRadioFunctionSettingUpdaterImpl implements HdRadioFunctionSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ
     */
    @Inject
    public HdRadioFunctionSettingUpdaterImpl(){
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocal(@NonNull LocalSetting setting) {
        Timber.i("setLocal() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
            MediaSourceType.HD_RADIO,
            HdRadioFunctionType.LOCAL.code,
            setting.code);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSeek(boolean setting) {
        Timber.i("setSeek() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
            MediaSourceType.HD_RADIO,
            HdRadioFunctionType.HD_SEEK.code,
            setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlending(boolean setting) {
        Timber.i("setBlending() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
            MediaSourceType.HD_RADIO,
            HdRadioFunctionType.BLENDING.code,
            setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveRadio(boolean setting) {
        Timber.i("setActiveRadio() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
            MediaSourceType.HD_RADIO,
            HdRadioFunctionType.ACTIVE_RADIO.code,
            setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }
}
