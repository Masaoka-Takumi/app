package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AdasCalibrationStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AdasCalibrationSettingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * ADAS Calibration設定画面のPresenter.
 */
@PresenterLifeCycle
public class AdasCalibrationSettingPresenter extends Presenter<AdasCalibrationSettingView> {

    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject GetStatusHolder mGetCase;
    @Inject PreferAdas mPreferAdas;
    private Bundle mArguments;

    /**
     * コンストラクタ.
     */
    @Inject
    public AdasCalibrationSettingPresenter() {
    }

    @Override
    public void initialize() {
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.isAdasCalibrationSetting = true;
        mEventBus.post(new AdasCalibrationStatusChangeEvent());
    }

    @Override
    void onResume() {
        SettingsParams params = SettingsParams.from(mArguments);
        if(params.mScreenId==ScreenId.HOME_CONTAINER) {
            Optional.ofNullable(getView()).ifPresent(view -> view.setBackButtonVisible(false));
        }
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.startCamera();
            view.setSkipButtonVisible(!mPreferAdas.isAdasSettingConfigured());
        });
    }

    @Override
    void onDestroy() {
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.isAdasCalibrationSetting = false;
        mEventBus.post(new AdasCalibrationStatusChangeEvent());
    }

    public AppStatus getAppStatus() {
        return mGetCase.execute().getAppStatus();
    }

    public void setArgument(Bundle args) {
        mArguments = args;
    }

    /**
     * Back押下アクション
     */
    public void onBackAction() {
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.isAdasCalibrationSetting = false;
        SettingsParams params = SettingsParams.from(mArguments);
        if(params.mScreenId==ScreenId.HOME_CONTAINER){
            mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
        }else {
            mEventBus.post(new GoBackEvent());
        }
        mEventBus.post(new AdasCalibrationStatusChangeEvent());
    }

    /**
     * Next押下アクション
     */
    public void onNextAction() {
        SettingsParams params = SettingsParams.from(mArguments);
        mEventBus.post(new NavigateEvent(ScreenId.CALIBRATION_SETTING_FITTING, createSettingsParams(params.mScreenId, "")));
    }

    /**
     * Skip押下アクション
     */
    public void onSkipAction() {
        SettingsParams params = SettingsParams.from(mArguments);
        if (mGetCase.execute().getSessionStatus() == SessionStatus.STARTED) {
            mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
        } else {
            mEventBus.post(new NavigateEvent(ScreenId.CAR_SAFETY_SETTINGS, createSettingsParams(ScreenId.CALIBRATION_SETTING, mContext.getString(R.string.set_038))));
        }
    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
    }

}

