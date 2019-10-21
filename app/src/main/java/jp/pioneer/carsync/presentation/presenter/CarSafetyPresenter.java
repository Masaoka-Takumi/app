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
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceDestinationInfo;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.view.CarSafetyView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.CarSafetyFragment;

/**
 * CarSafety設定のPresenter
 */
@PresenterLifeCycle
public class CarSafetyPresenter extends Presenter<CarSafetyView>{
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject PreferAdas mPreferAdas;
    /**
     * コンストラクタ.
     */
    @Inject
    public CarSafetyPresenter() {
    }

    @Override
    void onTakeView() {
        updateView();
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent ev){
        updateView();
    }

    /**
     * ImpactDetection設定.
     */
    public void onSelectImpactDetectionAction() {
        mEventBus.post(new NavigateEvent(ScreenId.IMPACT_DETECTION_SETTINGS, createSettingsParams(mContext.getString(R.string.set_099))));
    }

    /**
     * パーキングセンサー設定.
     */
    public void onSelectParkingSensorAction() {
        mEventBus.post(new NavigateEvent(ScreenId.PARKING_SENSOR_SETTING, createSettingsParams(mContext.getString(R.string.set_150))));
    }

    /**
     * ADAS設定.
     */
    public void onSelectAdasAction() {
        boolean isAdasPurchased = mGetStatusHolder.execute().getAppStatus().adasPurchased;
        boolean adasTrial = mPreference.getAdasTrialState()==AdasTrialState.TRIAL_DURING;
        if(isAdasPurchased||adasTrial) {
            if (mPreferAdas.isAdasSettingConfigured()) {
                mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_ADAS, createSettingsParams(mContext.getString(R.string.set_003))));
            } else {
                mEventBus.post(new NavigateEvent(ScreenId.ADAS_MANUAL, createSettingsParams(ScreenId.CAR_SAFETY_SETTINGS, mContext.getString(R.string.set_341))));
            }
        }else{
            Bundle bundle = new Bundle();
            bundle.putString(StatusPopupDialogFragment.TAG, CarSafetyFragment.TAG_DIALOG_ADAS_BILLING);
            bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.set_282));
            bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
            mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
        }
    }

    /**
     * ADAS設定.
     */
    public void onAdasBillingAction() {
        if(mGetStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED){
            //
        }else{
            mEventBus.post(new NavigateEvent(ScreenId.ADAS_TUTORIAL, createSettingsParams("")));
        }
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mGetStatusHolder.execute();
            CarDeviceStatus status = holder.getCarDeviceStatus();
            CarDeviceSpec spec = holder.getCarDeviceSpec();
            CarDeviceDestinationInfo destinationInfo = CarDeviceDestinationInfo.valueOf((byte)mPreference.getLastConnectedCarDeviceDestination());
            AppStatus appStatus = holder.getAppStatus();
            boolean adasAvailable = mPreference.getLastConnectedCarDeviceAdasAvailable();
            if(!mGetStatusHolder.execute().getAppStatus().adasPurchased&&
                    !appStatus.isAdasAvailableCountry&&
                    mPreference.getAdasTrialState()!= AdasTrialState.TRIAL_DURING){
                adasAvailable = false;
            }
            view.setParkingSensorSetting(
                    spec.parkingSensorSettingSupported,
                    status.parkingSensorSettingEnabled
            );
            view.setImpactDetectionSetting(
                    mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN
            );
            view.setAdasSetting(
                    adasAvailable || mPreference.isAdasPseudoCooperation()
            );
            view.setPurchaseIcon(mGetStatusHolder.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING);
        });
    }

}
