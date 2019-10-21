package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.DabFunctionSettingUpdater;
import jp.pioneer.carsync.domain.model.DabFunctionType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.TASetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * DABFunctionSettingUpdaterの実装.
 */
public class DabFunctionSettingUpdaterImpl implements DabFunctionSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ
     */
    @Inject
    public DabFunctionSettingUpdaterImpl(){
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTa(@NonNull TASetting setting) {
        Timber.i("setTa() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
            MediaSourceType.DAB,
            DabFunctionType.TA.code,
            setting.code);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServiceFollow(boolean setting) {
        Timber.i("setServiceFollow() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
            MediaSourceType.DAB,
            DabFunctionType.SERVICE_FOLLOW.code,
            setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSoftLink(boolean setting) {
        Timber.i("setSoftLink() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
            MediaSourceType.DAB,
            DabFunctionType.SOFTLINK.code,
            setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }
}
