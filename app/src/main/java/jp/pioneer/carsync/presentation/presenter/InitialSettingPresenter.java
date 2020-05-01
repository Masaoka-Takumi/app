package jp.pioneer.carsync.presentation.presenter;

import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.InitialSettingChangeEvent;
import jp.pioneer.carsync.domain.event.InitialSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferInitial;
import jp.pioneer.carsync.domain.model.InitialSetting;
import jp.pioneer.carsync.domain.model.InitialSettingSpec;
import jp.pioneer.carsync.domain.model.InitialSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.InitialSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * 初期設定画面のpresenter
 */
@PresenterLifeCycle
public class InitialSettingPresenter extends Presenter<InitialSettingView> {

    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject PreferInitial mPreferCase;

    /**
     * コンストラクタ
     */
    @Inject
    public InitialSettingPresenter() {
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
        updateView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * 初期設定変更イベントハンドラ.
     *
     * @param event 初期設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInitialSettingChangeEvent(InitialSettingChangeEvent event) {
        updateView();
    }

    /**
     * 初期設定ステータス変更イベントハンドラ
     *
     * @param event 初期設定ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInitialSettingStatusChangeEvent(InitialSettingStatusChangeEvent event) {
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
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mGetCase.execute();
            boolean initialSettingEnabled = holder.getCarDeviceStatus().initialSettingEnabled &&
                    holder.getCarDeviceSpec().initialSettingSupported;
            InitialSettingSpec spec = holder.getCarDeviceSpec().initialSettingSpec;
            InitialSettingStatus status = holder.getInitialSettingStatus();
            InitialSetting setting = holder.getInitialSetting();

            view.setMenuDisplayLanguageSetting(
                    spec.menuDisplayLanguageSettingSupported,
                    initialSettingEnabled && status.menuDisplayLanguageSettingEnabled,
                    setting.menuDisplayLanguageType);
            view.setFmStepSetting(
                    spec.fmStepSettingSupported,
                    initialSettingEnabled && status.fmStepSettingEnabled,
                    setting.fmStep);
            view.setAmStepSetting(
                    spec.amStepSettingSupported,
                    initialSettingEnabled && status.amStepSettingEnabled,
                    setting.amStep);
            view.setRearOutputPreoutOutputSetting(
                    spec.rearOutputPreoutOutputSettingSupported,
                    initialSettingEnabled && status.rearOutputPreoutOutputSettingEnabled,
                    setting.rearOutputPreoutOutputSetting);
            view.setRearOutputSetting(
                    spec.rearOutputSettingSupported,
                    initialSettingEnabled && status.rearOutputSettingEnabled,
                    setting.rearOutputSetting);
            view.setAntennaPowerSetting(
                    spec.dabAntennaPowerSupported,
                    initialSettingEnabled && status.dabAntennaPowerEnabled,
                    setting.dabAntennaPowerSetting);
        });
    }

    /**
     * MENU表示言語の設定.
     */
    public void onSelectMenuDisplayLanguage() {
        mEventBus.post(new NavigateEvent(ScreenId.MENU_DISPLAY_LANGUAGE_DIALOG, Bundle.EMPTY));
    }

    /**
     * FM STEP 50K/100K切換の設定.
     */
    public void onSelectFmStepSetting() {
        mPreferCase.toggleFmStep();
    }

    /**
     * AM STEP 50K/100K切換の設定.
     */
    public void onSelectAmStepSetting() {
        mPreferCase.toggleAmStep();
    }

    /**
     * REAR 出力設定/PREOUT出力の設定.
     */
    public void onSelectRearOutputPreoutOutput() {
        mPreferCase.toggleRearOutputPreoutOutput();
    }

    /**
     * REAR出力の設定.
     */
    public void onSelectRearOutput() {
        mPreferCase.toggleRearOutput();
    }

    /**
     * DAB Antenna Powerの設定.
     */
    public void onAntennaPowerSettingChange(boolean isEnabled) {
        mPreferCase.setDabAntennaPowerEnabled(isEnabled);
    }
}
