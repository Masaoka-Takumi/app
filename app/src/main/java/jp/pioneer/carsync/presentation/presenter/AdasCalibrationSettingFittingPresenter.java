package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.event.AdasCalibrationStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AdasCalibrationSettingFittingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by NSW00_007906 on 2018/07/17.
 */

public class AdasCalibrationSettingFittingPresenter extends Presenter<AdasCalibrationSettingFittingView> {

    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject GetStatusHolder mGetCase;
    @Inject PreferAdas mPreferAdas;
    private Bundle mArguments;
    private int mOrientation;

    /**
     * コンストラクタ.
     */
    @Inject
    public AdasCalibrationSettingFittingPresenter() {
    }

    @Override
    void onInitialize() {
        SettingsParams params = SettingsParams.from(mArguments);
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.isAdasCalibrationSetting = true;
        mEventBus.post(new AdasCalibrationStatusChangeEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onResume() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.startCamera();
            mOrientation = view.getOrientation();
        });
        updateView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onPause() {
    }

    @Override
    void onDestroy() {
        //mEventBus.post(new AdasCalibrationStatusChangeEvent());
    }

    @Override
    void onDropView() {
        mEventBus.post(new AdasCalibrationStatusChangeEvent());
    }

    public void setArgument(Bundle args) {
        mArguments = args;
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mPreferAdas.isAdasSettingConfigured()) {
                view.setCalibrationHeight(mPreferAdas.getAdasCalibrationSetting());
            }
        });
    }

    public boolean isAdasSettingConfigured() {
        return mPreferAdas.isAdasSettingConfigured();
    }

    public void setCalibrationHeight(int calibrationHeight) {
        mPreferAdas.setAdasCalibrationSetting(calibrationHeight);
    }

    /**
     * Back押下アクション
     */
    public void onBackAction() {
        mEventBus.post(new GoBackEvent());
    }

    /**
     * Next押下アクション
     */
    public void onNextAction() {
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.isAdasCalibrationSetting = false;
        SettingsParams params = SettingsParams.from(mArguments);
        if (params.mScreenId == ScreenId.HOME_CONTAINER) {
            mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
        } else if (params.mScreenId == ScreenId.SETTINGS_ADAS||params.mScreenId == ScreenId.CAR_SAFETY_SETTINGS||params.mScreenId == ScreenId.ADAS_BILLING) {
            if (mPreferAdas.isAdasSettingConfigured()) {
                mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_ADAS, createSettingsParams(ScreenId.CALIBRATION_SETTING_FITTING, mContext.getString(R.string.set_003))));
            } else {
                mEventBus.post(new NavigateEvent(ScreenId.ADAS_CAMERA_SETTING, createSettingsParams(ScreenId.CALIBRATION_SETTING_FITTING, mContext.getString(R.string.set_297))));
            }
        }
        //mEventBus.post(new AdasCalibrationStatusChangeEvent());
    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
    }

}
