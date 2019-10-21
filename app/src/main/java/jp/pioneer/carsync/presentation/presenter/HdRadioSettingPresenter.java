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
import jp.pioneer.carsync.domain.event.HdRadioFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.HdRadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferHdRadioFunction;
import jp.pioneer.carsync.domain.model.HdRadioFunctionSetting;
import jp.pioneer.carsync.domain.model.HdRadioFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.HdRadioFunctionSettingStatus;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.HdRadioSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

/**
 * HdRadio設定画面のPresenter.
 */
@PresenterLifeCycle
public class HdRadioSettingPresenter extends Presenter<HdRadioSettingView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject PreferHdRadioFunction mPreferCase;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * コンストラクタ.
     */
    @Inject
    public HdRadioSettingPresenter() {
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
    public void onHdRadioFunctionSettingChangeEvent(HdRadioFunctionSettingChangeEvent event) {
        mHandler.postDelayed(this::updateView, 100);
    }

    /**
     * Radio Function設定状態変更イベントハンドラ.
     *
     * @param event Radio Function設定状態変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHdRadioFunctionSettingStatusChangeEvent(HdRadioFunctionSettingStatusChangeEvent event) {
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
            HdRadioInfo info = holder.getCarDeviceMediaInfoHolder().hdRadioInfo;
            boolean isRadioSettingEnabled = holder.getCarDeviceStatus().hdRadioFunctionSettingEnabled &&
                    holder.getCarDeviceSpec().hdRadioFunctionSettingSupported;
            HdRadioFunctionSettingSpec spec = holder.getCarDeviceSpec().hdRadioFunctionSettingSpec;
            HdRadioFunctionSettingStatus status = holder.getHdRadioFunctionSettingStatus();
            HdRadioFunctionSetting setting = holder.getHdRadioFunctionSetting();
            view.setLocalSetting(
                    spec.localSettingSupported,
                    status.localSettingEnabled && isRadioSettingEnabled && info.getBand() != null,
                    setting.localSetting
            );
            view.setSeekSetting(
                    spec.hdSeekSettingSupported,
                    status.hdSeekSettingEnabled && isRadioSettingEnabled,
                    setting.hdSeekSetting
            );
            view.setBlendingSetting(
                    spec.blendingSettingSupported,
                    status.blendingSettingEnabled && isRadioSettingEnabled,
                    setting.blendingSetting
            );
            view.setActiveRadioSetting(
                    spec.activeRadioSettingSupported,
                    status.activeRadioSettingEnabled && isRadioSettingEnabled,
                    setting.activeRadioSetting
            );
        });
    }

    /**
     * LOCAL設定選択処理.
     */
    public void onSelectLocalSettingAction() {
        mEventBus.post(new NavigateEvent(ScreenId.LOCAL_DIALOG, Bundle.EMPTY));
    }

    /**
     * Seek設定選択処理.
     *
     * @param setting TA設定内容.
     */
    public void onSelectSeekSettingAction(boolean setting) {
        mPreferCase.setSeek(setting);
    }

    /**
     * Blending設定選択処理.
     *
     * @param setting TA設定内容.
     */
    public void onSelectBlendingSettingAction(boolean setting) {
        mPreferCase.setBlending(setting);
    }

    /**
     * ActiveRadio設定選択処理.
     *
     * @param setting TA設定内容.
     */
    public void onSelectActiveRadioSettingAction(boolean setting) {
        mPreferCase.setActiveRadio(setting);
    }
}