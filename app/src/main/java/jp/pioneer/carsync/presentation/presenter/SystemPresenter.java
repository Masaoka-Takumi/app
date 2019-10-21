package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SystemSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SystemSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSystem;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SystemSetting;
import jp.pioneer.carsync.domain.model.SystemSettingSpec;
import jp.pioneer.carsync.domain.model.SystemSettingStatus;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.SystemView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * システム設定画面のpresenter.
 */
@PresenterLifeCycle
public class SystemPresenter extends Presenter<SystemView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject PreferSystem mPreferCase;
    @Inject AppSharedPreference mPreference;
    /**
     * コンストラクタ
     */
    @Inject
    public SystemPresenter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onTakeView() {
        updateView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * システム設定変更イベントハンドラ.
     *
     * @param event システム設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSystemSettingChangeEvent(SystemSettingChangeEvent event) {
        updateView();
    }

    /**
     * システム設定ステータス変更イベントハンドラ
     *
     * @param event システム設定ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSystemSettingStatusChangeEvent(SystemSettingStatusChangeEvent event) {
        updateView();
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        updateView();
    }

    private void updateView() {
        StatusHolder holder = mGetCase.execute();
        boolean systemSettingEnabled = holder.getCarDeviceStatus().systemSettingEnabled &&
                holder.getCarDeviceSpec().systemSettingSupported;
        CarDeviceSpec spec = holder.getCarDeviceSpec();
        CarDeviceStatus status = holder.getCarDeviceStatus();
        SystemSettingSpec systemSpec = spec.systemSettingSpec;
        SystemSetting systemSetting = holder.getSystemSetting();
        SystemSettingStatus systemSettingStatus = holder.getSystemSettingStatus();

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setInitialSettings(
                    spec.initialSettingSupported,
                    systemSettingEnabled && status.initialSettingEnabled
            );
            view.setBeepToneSetting(
                    systemSpec.beepToneSettingSupported,
                    systemSettingEnabled && systemSettingStatus.beepToneSettingEnabled,
                    systemSetting.beepToneSetting
            );
            view.setAutoPiSetting(
                    systemSpec.autoPiSettingSupported,
                    systemSettingEnabled && systemSettingStatus.autoPiSettingEnabled,
                    systemSetting.autoPiSetting)
            ;
            view.setAuxSetting(
                    systemSpec.auxSettingSupported,
                    systemSettingEnabled && systemSettingStatus.auxSettingEnabled,
                    systemSetting.auxSetting
            );
            view.setBtAudioSetting(
                    systemSpec.btAudioSettingSupported,
                    systemSettingEnabled && systemSettingStatus.btAudioSettingEnabled,
                    systemSetting.btAudioSetting
            );
            view.setPandoraSetting(
                    systemSpec.pandoraSettingSupported,
                    systemSettingEnabled && systemSettingStatus.pandoraSettingEnabled,
                    systemSetting.pandoraSetting
            );
            view.setSpotifySetting(
                    systemSpec.spotifySettingSupported,
                    systemSettingEnabled && systemSettingStatus.spotifySettingEnabled,
                    systemSetting.spotifySetting
            );
            view.setPowerSaveModeSetting(
                    systemSpec.powerSaveSettingSupported,
                    systemSettingEnabled && systemSettingStatus.powerSaveSettingEnabled,
                    systemSetting.powerSaveSetting
            );
            view.setAppAutoLaunchSetting(
                    systemSpec.appAutoStartSettingSupported,
                    systemSettingEnabled && systemSettingStatus.appAutoStartSettingEnabled,
                    systemSetting.appAutoStartSetting
            );
            view.setUsbAutoSetting(
                    systemSpec.usbAutoSettingSupported,
                    systemSettingEnabled && systemSettingStatus.usbAutoSettingEnabled,
                    systemSetting.usbAutoSetting
            );
            view.setDisplayOffSetting(
                    systemSpec.displayOffSettingSupported,
                    systemSettingEnabled && systemSettingStatus.displayOffSettingEnabled,
                    systemSetting.displayOffSetting
            );
            view.setAttMuteSetting(
                    systemSpec.attMuteSettingSupported,
                    systemSettingEnabled && systemSettingStatus.attMuteSettingEnabled,
                    systemSetting.attMuteSetting
            );
            boolean isAvailableDevice = systemSpec.distanceUnitSettingSupported&&systemSettingEnabled && systemSettingStatus.distanceUnitSettingEnabled;
            view.setDistanceUnit(
                    true,
                    true,
                    isAvailableDevice?systemSetting.distanceUnit:mPreference.getDistanceUnit()
            );
            view.setTimeFormatSetting(
                    true,
                    true,
                    mPreference.getTimeFormatSetting()
            );
        });
    }

    /**
     * 初期設定
     */
    public void onInitialSettingAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_SYSTEM_INITIAL, createSettingsParams(mContext.getString(R.string.set_104))));
    }

    /**
     * Beep Toneの設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectBeepToneSettingAction(boolean setting) {
        mPreferCase.setBeepTone(setting);
    }

    /**
     * Auto PIの設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectAutoPiSettingAction(boolean setting) {
        mPreferCase.setAutoPi(setting);
    }

    /**
     * AUXの設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectAuxSettingAction(boolean setting) {
        mPreferCase.setAux(setting);
    }

    /**
     * BT Audioの設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectBtAudioSettingAction(boolean setting) {
        mPreferCase.setBtAudio(setting);
    }

    /**
     * Pandoraの設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectPandoraSettingAction(boolean setting) {
        mPreferCase.setPandora(setting);
    }

    /**
     * Spotifyの設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectSpotifySettingAction(boolean setting) {
        mPreferCase.setSpotify(setting);
    }

    /**
     * Power Save Modeの設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectPowerSaveModeSettingAction(boolean setting) {
        mPreferCase.setPowerSave(setting);
    }

    /**
     * 99App自動起動の設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectAppAutoLaunchSettingAction(boolean setting) {
        mPreferCase.setAppAutoStart(setting);
    }

    /**
     * USB AUTOの設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectUsbAutoSettingAction(boolean setting) {
        mPreferCase.setUsbAuto(setting);
    }

    /**
     * Display OFFの設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectDisplayOffSettingAction(boolean setting) {
        mPreferCase.setDisplayOff(setting);
    }

    /**
     * ATT/MUTEの設定.
     */
    public void onSelectAttMuteSettingAction() {
        mPreferCase.toggleAttMute();
    }

    /**
     * 距離単位の設定.
     */
    public void onSelectDistanceUnitSettingAction() {
        StatusHolder holder = mGetCase.execute();
        boolean systemSettingEnabled = holder.getCarDeviceStatus().systemSettingEnabled &&
                holder.getCarDeviceSpec().systemSettingSupported;
        CarDeviceSpec spec = holder.getCarDeviceSpec();
        CarDeviceStatus status = holder.getCarDeviceStatus();
        SystemSettingSpec systemSpec = spec.systemSettingSpec;
        SystemSetting systemSetting = holder.getSystemSetting();
        SystemSettingStatus systemSettingStatus = holder.getSystemSettingStatus();
        boolean isAvailableDevice = systemSpec.distanceUnitSettingSupported&&systemSettingEnabled && systemSettingStatus.distanceUnitSettingEnabled;

        if(isAvailableDevice){
            mPreferCase.toggleDistanceUnit();
        }else {
            mPreference.setDistanceUnit(mPreference.getDistanceUnit().toggle());
        }
        updateView();
    }

    /**
     * 距離単位の設定.
     */
    public void onSelectTimeFormatSettingAction() {
        // 反転させる
        if (mPreference.getTimeFormatSetting() == TimeFormatSetting.TIME_FORMAT_12) {
            mPreference.setTimeFormatSetting(TimeFormatSetting.TIME_FORMAT_24);
        } else {
            mPreference.setTimeFormatSetting(TimeFormatSetting.TIME_FORMAT_12);
        }
        updateView();
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
