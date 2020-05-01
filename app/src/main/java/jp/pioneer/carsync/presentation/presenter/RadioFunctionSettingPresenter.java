package jp.pioneer.carsync.presentation.presenter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferDabFunction;
import jp.pioneer.carsync.domain.interactor.PreferRadioFunction;
import jp.pioneer.carsync.domain.model.DabFunctionSetting;
import jp.pioneer.carsync.domain.model.DabFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.DabFunctionSettingStatus;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TASetting;
import jp.pioneer.carsync.domain.model.TunerFunctionSetting;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingStatus;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.RadioFunctionSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * RadioFunction設定画面のPresenter.
 */
@PresenterLifeCycle
public class RadioFunctionSettingPresenter extends Presenter<RadioFunctionSettingView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject PreferRadioFunction mPreferCase;
    @Inject
    PreferDabFunction mPreferCaseDab;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * コンストラクタ.
     */
    @Inject
    public RadioFunctionSettingPresenter() {
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
     * Radio Function設定変更イベントハンドラ.
     *
     * @param event Radio Function設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioFunctionSettingChangeEvent(RadioFunctionSettingChangeEvent event) {
        mHandler.postDelayed(this::updateView, 100);
    }

    /**
     * Radio Function設定状態変更イベントハンドラ.
     *
     * @param event Radio Function設定状態変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioFunctionSettingStatusChangeEvent(RadioFunctionSettingStatusChangeEvent event) {
        mHandler.postDelayed(this::updateView, 100);
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        mHandler.postDelayed(this::updateView, 100);
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {

            StatusHolder holder = mGetCase.execute();
            RadioInfo info = holder.getCarDeviceMediaInfoHolder().radioInfo;
            boolean isRadioSettingEnabled = holder.getCarDeviceStatus().tunerFunctionSettingEnabled &&
                    holder.getCarDeviceSpec().tunerFunctionSettingSupported;
            TunerFunctionSettingSpec spec = holder.getCarDeviceSpec().tunerFunctionSettingSpec;
            TunerFunctionSettingStatus status = holder.getTunerFunctionSettingStatus();
            TunerFunctionSetting setting = holder.getTunerFunctionSetting();

            view.setFmTunerSetting(
                    spec.fmSettingSupported,
                    status.fmSettingEnabled && isRadioSettingEnabled,
                    setting.fmTunerSetting
            );
            view.setRegionSetting(
                    spec.regSettingSupported,
                    status.regSettingEnabled && isRadioSettingEnabled,
                    setting.regSetting
            );
            view.setLocalSetting(
                    spec.localSettingSupported,
                    status.localSettingEnabled && isRadioSettingEnabled && info.getBand() != null,
                    setting.localSetting
            );
            boolean isDabSettingEnabled = holder.getCarDeviceStatus().dabFunctionSettingEnabled &&
                    holder.getCarDeviceSpec().dabFunctionSettingSupported;
            DabFunctionSettingSpec dabSpec = holder.getCarDeviceSpec().dabFunctionSettingSpec;
            DabFunctionSettingStatus dabStatus = holder.getDabFunctionSettingStatus();
            DabFunctionSetting dabSetting = holder.getDabFunctionSetting();
            view.setTaSetting(
                    spec.taSettingSupported&&!dabSpec.taSettingSupported,
                    status.taSettingEnabled && isRadioSettingEnabled,
                    setting.taSetting
            );
            view.setTaDabSetting(
                    dabSpec.taSettingSupported,
                    status.taSettingEnabled && isRadioSettingEnabled,
                    setting.taDabSetting
            );
            view.setAfSetting(
                    spec.afSettingSupported,
                    status.afSettingEnabled && isRadioSettingEnabled,
                    setting.afSetting
            );
            view.setNewsSetting(
                    spec.newsSettingSupported,
                    status.newsSettingEnabled && isRadioSettingEnabled,
                    setting.newsSetting
            );
            view.setAlarmSetting(
                    spec.alarmSettingSupported,
                    status.alarmSettingEnabled && isRadioSettingEnabled,
                    setting.alarmSetting
            );
            view.setPchManual(
                    spec.pchManualSupported,
                    status.pchManualEnabled && isRadioSettingEnabled && !holder.getProtocolSpec().isSphCarDevice(),
                    setting.pchManualSetting
            );


        });
    }

    /**
     * FM Tuner設定処理.
     */
    public void onSelectFmTunerSettingAction() {
        mPreferCase.toggleFmTuner();
    }

    /**
     * REG広域設定選択処理.
     *
     * @param isEnabled {@code true}:REG広域設定有効 {@code false}:REG広域設定無効
     */
    public void onSelectRegionSettingAction(boolean isEnabled) {
        mPreferCase.setReg(isEnabled);
    }

    /**
     * LOCAL設定選択処理.
     */
    public void onSelectLocalSettingAction() {
        mEventBus.post(new NavigateEvent(ScreenId.LOCAL_DIALOG, Bundle.EMPTY));
    }

    /**
     * TA設定選択処理.
     *
     * @param setting TA設定内容.
     */
    public void onSelectTaSettingAction(boolean setting) {
        mPreferCase.setTa(setting);
    }

    /**
     * TA設定選択処理.
     */
    public void onSelectTaSettingAction() {
        StatusHolder holder = mGetCase.execute();
        TunerFunctionSetting setting = holder.getTunerFunctionSetting();
        TASetting taSetting = setting.taDabSetting;
        mPreferCase.setTa(taSetting.toggle());
    }

    /**
     * AF設定選択処理.
     *
     * @param isEnabled {@code true}:AF設定有効 {@code false}:AF設定無効
     */
    public void onSelectAfSettingAction(boolean isEnabled) {
        mPreferCase.setAf(isEnabled);
    }

    /**
     * NEWS設定選択処理.
     *
     * @param isEnabled {@code true}:NEWS設定有効 {@code false}:NEWS設定無効
     */
    public void onSelectNewsSettingAction(boolean isEnabled) {
        mPreferCase.setNews(isEnabled);
    }

    /**
     * ALARM設定選択処理.
     *
     * @param isEnabled {@code true}:ALARM設定有効 {@code false}:ALARM設定無効
     */
    public void onSelectAlarmSettingAction(boolean isEnabled) {
        mPreferCase.setAlarm(isEnabled);
    }

    /**
     * P.CH / Manual設定選択処理.
     */
    public void onSelectPchManualSettingAction() {
        mPreferCase.togglePchManual();
    }

}
