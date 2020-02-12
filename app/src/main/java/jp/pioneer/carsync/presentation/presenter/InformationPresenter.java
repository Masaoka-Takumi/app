package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.InformationView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Information設定画面のPresenter
 */
@PresenterLifeCycle
public class InformationPresenter extends Presenter<InformationView> {
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetCase;
    @Inject Context mContext;
    @Inject
    AppSharedPreference mPreference;
    @Inject
    public InformationPresenter() {
    }
    @Override
    void onInitialize() {
        setViewData();
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        setViewData();
    }

    private void setViewData() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mGetCase.execute();
            String deviceName = holder.getCarDeviceSpec().modelName;
            if(TextUtils.isEmpty(deviceName)){
                deviceName = holder.getBtDeviceName();
            }
            String deviceVersion = holder.getCarDeviceSpec().farmVersion;
            view.setDeviceInformation(TextUtils.isEmpty(deviceName) ? mContext.getString(R.string.set_245) : deviceName);
            view.setDeviceFarmVersion(mPreference.getLastConnectedCarDeviceProtocolVersion().isGreaterThanOrEqual(ProtocolVersion.V4_1),TextUtils.isEmpty(deviceVersion) ? mContext.getString(R.string.set_245) : deviceVersion);
            view.setAppVersion(BuildConfig.VERSION_NAME);
        });
    }

    /**
     * License押下時の処理
     */
    public void onLicenseAction() {
        mEventBus.post(new NavigateEvent(ScreenId.LICENSE, createSettingsParams(mContext.getString(R.string.set_116))));
    }

    /**
     * EULA押下時の処理
     */
    public void onEulaAction() {
        mEventBus.post(new NavigateEvent(ScreenId.EULA, createSettingsParams(mContext.getString(R.string.set_066))));
    }
    /**
     * PrivacyPolicy押下時の処理
     */
    public void onPrivacyPolicyAction() {
        mEventBus.post(new NavigateEvent(ScreenId.PRIVACY_POLICY, createSettingsParams(mContext.getString(R.string.set_165))));
    }

    /**
     * デバッグ設定遷移時の処理
     */
    public void onDebugSettingAction() {
        mEventBus.post(new NavigateEvent(ScreenId.DEBUG_SETTING, createSettingsParams(mContext.getString(R.string.dbg_001))));
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
