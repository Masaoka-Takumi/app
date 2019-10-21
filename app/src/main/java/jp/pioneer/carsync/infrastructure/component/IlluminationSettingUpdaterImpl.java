package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.IlluminationSettingUpdater;
import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.IncomingMessageColorSetting;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * IlluminationSettingUpdaterの実装.
 */
public class IlluminationSettingUpdaterImpl implements IlluminationSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ
     */
    @Inject
    public IlluminationSettingUpdaterImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColor(@NonNull IlluminationTarget target, @NonNull IlluminationColor color) {
        Timber.i("setColor() target = %s, color = %s", target, color);
        checkNotNull(target);
        checkNotNull(color);

        OutgoingPacket packet = mPacketBuilder.createColorSettingNotification(
                target,
                color
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCustomColor(@NonNull IlluminationTarget target, @IntRange(from = 0, to = 60) int red, @IntRange(from = 0, to = 60) int green, @IntRange(from = 0, to = 60) int blue) {
        Timber.i("setCustomColor() target = %s, red = %d, green = %d, blue = %d", target, red, green, blue);
        checkNotNull(target);
        checkArgument(red >= 0 && red <= 60);
        checkArgument(green >= 0 && green <= 60);
        checkArgument(blue >= 0 && blue <= 60);
        checkArgument(Math.max(red, Math.max(green, blue)) >= 10);

        OutgoingPacket packet = mPacketBuilder.createCustomColorSettingNotification(
                target,
                red,
                green,
                blue
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBtPhoneColor(@NonNull BtPhoneColor color) {
        Timber.i("setBtPhoneColor() color = %s", color);
        checkNotNull(color);

        OutgoingPacket packet = mPacketBuilder.createBtPhoneColorSettingNotification(
                color
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDimmer(@NonNull DimmerSetting.Dimmer dimmer) {
        Timber.i("setDimmer() dimmer = %s", dimmer);
        checkNotNull(dimmer);

        OutgoingPacket packet = mPacketBuilder.createDimmerSettingNotification(
                dimmer
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDimmerTime(@NonNull DimmerTimeType type, int hour, int minute) {
        Timber.i("setDimmerTime() type = %s, hour = %d, minute = %d", type, hour, minute);
        checkNotNull(type);

        OutgoingPacket packet = mPacketBuilder.createDimmerTimeSettingNotification(
                type,
                hour,
                minute
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBrightness(@NonNull IlluminationTarget target, int brightness) {
        Timber.i("setBrightness() target = %s, brightness = %d", target, brightness);
        checkNotNull(target);

        OutgoingPacket packet = mPacketBuilder.createKeyDisplayBrightnessSettingNotification(
                target,
                brightness
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCommonBrightness(int brightness) {
        Timber.i("setCommonBrightness() target = %d", brightness);

        OutgoingPacket packet = mPacketBuilder.createBrightnessSettingNotification(
                brightness
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIlluminationEffect(boolean enabled) {
        Timber.i("setIlluminationEffect() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createIlluminationEffectSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAudioLevelMeterLinked(boolean enabled) {
        Timber.i("setAudioLevelMeterLinked() enabled = %s", enabled);

        OutgoingPacket packet = mPacketBuilder.createAudioLevelMeterLinkedSettingNotification(
                enabled
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSphBtPhoneColor(@NonNull SphBtPhoneColorSetting setting) {
        Timber.i("setSphBtPhoneColor() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createSphBtPhoneColorSettingNotification(
                setting
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCommonColor(@NonNull IlluminationColor color) {
        Timber.i("setCommonColor() color = %s", color);
        checkNotNull(color);

        OutgoingPacket packet = mPacketBuilder.createCommonColorSettingNotification(
                color
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCommonCustomColor(@IntRange(from = 0, to = 60) int red, @IntRange(from = 0, to = 60) int green, @IntRange(from = 0, to = 60) int blue) {
        Timber.i("setCommonCustomColor() red = %d, green = %d, blue = %d", red, green, blue);
        checkArgument(red >= 0 && red <= 60);
        checkArgument(green >= 0 && green <= 60);
        checkArgument(blue >= 0 && blue <= 60);
        checkArgument(Math.max(red, Math.max(green, blue)) >= 10);

        OutgoingPacket packet = mPacketBuilder.createCommonCustomColorSettingNotification(
                red,
                green,
                blue
        );
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIncomingMessageColor(@NonNull IncomingMessageColorSetting setting) {
        Timber.i("setIncomingMessageColor() color = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createIncomingMessageColorSettingNotification(
                setting
        );
        mCarDeviceConnection.sendPacket(packet);
    }
}
