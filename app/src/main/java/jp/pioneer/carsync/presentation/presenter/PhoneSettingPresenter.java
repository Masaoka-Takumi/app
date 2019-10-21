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
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.PhoneSettingChangeEvent;
import jp.pioneer.carsync.domain.event.PhoneSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferPhone;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.PhoneSetting;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.PhoneSettingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Phone設定画面のPresenter.
 */
@PresenterLifeCycle
public class PhoneSettingPresenter extends Presenter<PhoneSettingView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject PreferPhone mPreferCase;
    @Inject AppSharedPreference mPreference;
    @Inject Context mContext;

    /**
     * コンストラクタ.
     */
    @Inject
    public PhoneSettingPresenter() {
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
     * Illumination設定変更イベントハンドラ.
     *
     * @param event Illumination設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingChangeEvent(IlluminationSettingChangeEvent event) {
        updateView();
    }

    /**
     * Illumination設定状態変更イベントハンドラ.
     *
     * @param event Illumination設定状態変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingStatusChangeEvent(IlluminationSettingStatusChangeEvent event) {
        updateView();
    }

    /**
     * Phone設定変更イベントハンドラ.
     *
     * @param event Phone設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhoneSettingChangeEvent(PhoneSettingChangeEvent event) {
        updateView();
    }

    /**
     * Phone設定変更イベントハンドラ.
     *
     * @param event Phone設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhoneSettingStatusChangeEvent(PhoneSettingStatusChangeEvent event) {
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
            boolean phoneSettingEnabled = holder.getCarDeviceStatus().phoneSettingEnabled &&
                    holder.getCarDeviceSpec().phoneSettingSupported;
            CarDeviceSpec spec = holder.getCarDeviceSpec();
            PhoneSettingStatus phoneStatus = holder.getPhoneSettingStatus();
            PhoneSetting phoneSetting = holder.getPhoneSetting();
            IlluminationSettingSpec illumiSpec = spec.illuminationSettingSpec;
            IlluminationSetting illumiSetting = holder.getIlluminationSetting();
            IlluminationSettingStatus illumiStatus = holder.getIlluminationSettingStatus();

            view.setDeviceSettings(
                    spec.phoneSettingSupported,
                    phoneSettingEnabled && phoneStatus.deviceListEnabled);
            view.setAutoPairingSetting(
                    spec.phoneSettingSupported,
                    phoneSettingEnabled && phoneStatus.autoPairingSettingEnabled,
                    phoneSetting.autoPairingSetting);
            view.setPhoneBookAccessibleSetting(
                    mPreference.isPhoneBookAccessible());
            view.setIncomingCallPatternSetting(
                    illumiSpec.btPhoneColorSettingSupported,
                    phoneSettingEnabled && illumiStatus.btPhoneColorSettingEnabled,
                    illumiSetting.btPhoneColor
            );
            view.setIncomingCallColorSetting(
                    illumiSpec.sphBtPhoneColorSettingSupported,
                    phoneSettingEnabled && illumiStatus.sphBtPhoneColorSettingEnabled,
                    illumiSetting.sphBtPhoneColorSetting
            );
            view.setAutoAnswerSetting(
                    spec.phoneSettingSupported,
                    phoneSettingEnabled && phoneStatus.autoAnswerSettingEnabled,
                    phoneSetting.autoAnswerSetting
            );
        });
    }

    /**
     * デバイス設定.
     */
    public void onSelectDeviceSettingsAction() {
        mEventBus.post(new NavigateEvent(ScreenId.BT_DEVICE_LIST, createSettingsParams(mContext.getString(R.string.set_034))));
    }

    /**
     * AutoPairing設定.
     *
     * @param isEnabled {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectAutoPairingAction(boolean isEnabled) {
        mPreferCase.setAutoPairing(isEnabled);
    }

    /**
     * DirectCall設定.
     */
    public void onSelectDirectCallAction() {
        mEventBus.post(new NavigateEvent(ScreenId.DIRECT_CALL_SETTING, createSettingsParams(mContext.getString(R.string.set_059))));
    }

    /**
     * 連絡帳アクセス設定.
     *
     * @param isAccessible {@code true}:アクセス有効 {@code false}:アクセス無効
     */
    public void onSelectPhoneBookAccessibleAction(boolean isAccessible) {
        mPreference.setPhoneBookAccessible(isAccessible);
    }

    /**
     * BT着信パターン設定(DEH系向け設定).
     */
    public void onSelectIncomingCallPatternItemAction() {
        mEventBus.post(new NavigateEvent(ScreenId.INCOMING_CALL_PATTERN_SETTING, createSettingsParams(mContext.getString(R.string.set_102))));
    }

    /**
     * BT着信カラー設定(専用機向け設定).
     */
    public void onSelectIncomingCallColorItemAction() {
        mEventBus.post(new NavigateEvent(ScreenId.INCOMING_CALL_COLOR_SETTING, createSettingsParams(mContext.getString(R.string.set_101))));
    }

    /**
     * AutoAnswer設定.
     *
     * @param isEnabled {@code true}:設定有効 {@code false}:設定無効
     */
    public void onSelectIncomingCallAutoAnswer(boolean isEnabled) {
        mPreferCase.setAutoAnswer(isEnabled);
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
