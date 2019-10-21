package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.RadioFunctionSettingUpdater;
import jp.pioneer.carsync.domain.model.FMTunerSetting;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.TunerFunctionType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * RadioFunctionSettingUpdaterの実装.
 */
public class RadioFunctionSettingUpdaterImpl implements RadioFunctionSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;

    /**
     * コンストラクタ
     */
    @Inject
    public RadioFunctionSettingUpdaterImpl(){
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocal(@NonNull LocalSetting setting) {
        Timber.i("setLocal() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
                MediaSourceType.RADIO,
                TunerFunctionType.LOCAL.code,
                setting.code);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFmTuner(@NonNull FMTunerSetting setting) {
        Timber.i("setFmTuner() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
                MediaSourceType.RADIO,
                TunerFunctionType.FM_TUNER_SETTING.code,
                setting.code);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReg(boolean setting) {
        Timber.i("setReg() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
                MediaSourceType.RADIO,
                TunerFunctionType.REG.code,
                setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTa(boolean setting) {
        Timber.i("setTa() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
                MediaSourceType.RADIO,
                TunerFunctionType.TA.code,
                setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAf(boolean setting) {
        Timber.i("setAf() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
                MediaSourceType.RADIO,
                TunerFunctionType.AF.code,
                setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNews(boolean setting) {
        Timber.i("setNews() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
                MediaSourceType.RADIO,
                TunerFunctionType.NEWS.code,
                setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlarm(boolean setting) {
        Timber.i("setAlarm() setting = %s", setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
                MediaSourceType.RADIO,
                TunerFunctionType.ALARM.code,
                setting ? 0x01 : 0x00);
        mCarDeviceConnection.sendPacket(packet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPchManual(@NonNull PCHManualSetting setting) {
        Timber.i("setPchManual() setting = %s", setting);
        checkNotNull(setting);

        OutgoingPacket packet = mPacketBuilder.createFunctionSettingNotification(
                MediaSourceType.RADIO,
                TunerFunctionType.PCH_MANUAL.code,
                setting.code);
        mCarDeviceConnection.sendPacket(packet);
    }

}
