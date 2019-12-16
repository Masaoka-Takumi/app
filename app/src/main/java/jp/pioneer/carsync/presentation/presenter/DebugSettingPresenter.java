package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import java.util.EnumSet;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.ControlSmartPhoneInterruption;
import jp.pioneer.carsync.domain.interactor.GetRunningStatus;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.interactor.UpdateWarningEvents;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionSensitivity;
import jp.pioneer.carsync.domain.model.AdasFunctionType;
import jp.pioneer.carsync.domain.model.AdasWarningEvent;
import jp.pioneer.carsync.domain.model.AdasWarningStatus;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.SmartPhoneInterruption;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.TipsContentsEndpoint;
import jp.pioneer.carsync.presentation.view.DebugSettingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;

/**
 * デバッグ設定のPresenter
 */
@PresenterLifeCycle
public class DebugSettingPresenter extends Presenter<DebugSettingView> {

    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject UpdateWarningEvents mUpdateWarningEvents;
    @Inject ControlSmartPhoneInterruption mInterruption;
    @Inject PreferAdas mPreferAdas;
    @Inject GetStatusHolder mStatusCase;
    @Inject GetRunningStatus mGetRunningStatusCase;
    /**
     * コンストラクタ
     */
    @Inject
    public DebugSettingPresenter() {
    }

    @Override
    void onTakeView() {
        updateView();
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            AppStatus appStatus = mStatusCase.execute().getAppStatus();
            view.setLogEnabled(mPreference.isLogEnabled());
            view.setImpactDetectionDebugModeEnabled(mPreference.isImpactDetectionDebugModeEnabled());
            view.setSpecialEqDebugModeEnabled(mPreference.isDebugSpecialEqEnabled());
            view.setTipsServer(mPreference.getTipsListEndpoint());
            view.setVersion1_1FunctionEnabled(mPreference.isVersion_1_1_FunctionEnabled());
            view.setAdasSimJudgement(appStatus.adasSimJudgement);
            view.setAdasPurchased(appStatus.adasPurchased);
            view.setAdasConfigured(mPreferAdas.isAdasSettingConfigured());
            view.setAdasPseudoCooperation(mPreference.isAdasPseudoCooperation());
            view.setAdasCarSpeed(appStatus.adasCarSpeed);
            view.setVRDelayTime(appStatus.speechRecognizerDelayTime);
            view.setDebugRunningStatus(appStatus.getRunningStatus);
            view.setHomeCenterView(appStatus.homeCenterViewAdas);
            view.setAdasLdwMinSpeed(appStatus.adasLdwMinSpeed);
            view.setAdasLdwMaxSpeed(appStatus.adasLdwMaxSpeed);
            view.setAdasPcwMinSpeed(appStatus.adasPcwMinSpeed);
            view.setAdasPcwMaxSpeed(appStatus.adasPcwMaxSpeed);
            view.setAdasFcwMinSpeed(appStatus.adasFcwMinSpeed);
            view.setAdasFcwMaxSpeed(appStatus.adasFcwMaxSpeed);
            view.setAdasAccelerateY(appStatus.adasAccelerateYRange);
            view.setAdasAccelerateZMin(appStatus.adasAccelerateZRangeMin);
            view.setAdasAccelerateZMax(appStatus.adasAccelerateZRangeMax);
            view.setAdasFps(appStatus.adasFps);
            view.setAdasCameraPreview(appStatus.adasCameraView);
            view.setAlexaSimJudgement(mPreference.isAlexaRequiredSimCheck());
        });
    }

    public void onLogEnabledAction(boolean newValue) {
        mPreference.setLogEnabled(newValue);
        Optional.ofNullable(getView()).ifPresent(view -> view.setLogEnabled(newValue));
    }

    public void onClassicBTLinkKeyAction() {
        mEventBus.post(new NavigateEvent(ScreenId.PAIRING_DEVICE_LIST, createSettingsParams(mContext.getString(R.string.dbg_003))));
    }

    public void onBLELinkKeyAction() {
        mEventBus.post(new NavigateEvent(ScreenId.PAIRING_DEVICE_LIST, createSettingsParams(mContext.getString(R.string.dbg_004))));
    }

    public void onImpactDetectionDebugModeEnabledAction(boolean newValue) {
        mPreference.setImpactDetectionDebugModeEnabled(newValue);
        Optional.ofNullable(getView()).ifPresent(view -> view.setImpactDetectionDebugModeEnabled(newValue));
    }

    public void onAdasWarningAction(AdasWarningStatus status) {
        Set<AdasWarningEvent> warningEvents = EnumSet.noneOf(AdasWarningEvent.class);

        switch (status) {
            case SINGLE:
                warningEvents.add(AdasWarningEvent.PEDESTRIAN_WARNING_EVENT);
                break;
            case CONTINUOUS:
                warningEvents.add(AdasWarningEvent.OFF_ROAD_LEFT_SOLID_EVENT);
                warningEvents.add(AdasWarningEvent.OFF_ROAD_RIGHT_SOLID_EVENT);
                break;
            default:
                break;
        }

        mUpdateWarningEvents.execute(warningEvents);
    }

    public void onSmartPhoneInterruptAction(SmartPhoneInterruption interruption, boolean isRelease) {
        if (interruption == null || isRelease) {
            mInterruption.releaseInterruption();
        } else {
            mInterruption.addInterruption(interruption);
        }
    }

    public void onGoHomeAction() {
        mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
    }

    public void onDebugSpecialEqAction(boolean newValue) {
        mPreference.setDebugSpecialEqEnabled(newValue);
        Optional.ofNullable(getView()).ifPresent(view -> view.setSpecialEqDebugModeEnabled(newValue));
    }

    public void onTipsServerAction() {
        TipsContentsEndpoint endpoint = mPreference.getTipsListEndpoint().toggle();
        mPreference.setTipsListEndpoint(endpoint);
        Optional.ofNullable(getView()).ifPresent(view -> view.setTipsServer(endpoint));
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }

    public void onVersion1_1Action(boolean newValue) {
        mPreference.setVersion_1_1_FunctionEnabled(newValue);
        if(!newValue){
            mPreferAdas.setAdasEnabled(false);
            mPreference.setImpactDetectionEnabled(false);
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setVersion1_1FunctionEnabled(newValue));
    }

    public void onAdasSimJudgement(boolean newValue) {
        mStatusCase.execute().getAppStatus().adasSimJudgement = newValue;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasSimJudgement(newValue));
        if(!newValue){
            mStatusCase.execute().getAppStatus().isAdasAvailableCountry = true;
        }
    }

    public void onAdasTrialReset() {
        mPreference.setAdasTrialState(AdasTrialState.TRIAL_BEFORE);
        Bundle bundle = new Bundle();
        bundle.putString(StatusPopupDialogFragment.TAG, "adas_trial_reset");
        bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.setting_debug_adas_trial_reset_dialog_text));
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
    }

    public void onAdasPurchasedAction(boolean newValue) {
        mStatusCase.execute().getAppStatus().adasPurchased = newValue;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasPurchased(newValue));
    }

    public void onAdasConfiguredAction(boolean newValue) {
        mPreferAdas.setAdasSettingConfigured(newValue);
        //初期設定済フラグをOFFにしたらADAS設定初期化
        if(!newValue){
            mPreferAdas.setAdasEnabled(false);
            mPreferAdas.setAdasAlarmEnabled(true);
            mPreferAdas.setAdasCameraSetting(new AdasCameraSetting());
            mPreferAdas.setFunctionEnabled(AdasFunctionType.LDW,true);
            mPreferAdas.setFunctionSensitivity(AdasFunctionType.LDW, AdasFunctionSensitivity.MIDDLE);
            mPreferAdas.setFunctionEnabled(AdasFunctionType.PCW,true);
            mPreferAdas.setFunctionSensitivity(AdasFunctionType.PCW, AdasFunctionSensitivity.MIDDLE);
            mPreferAdas.setFunctionEnabled(AdasFunctionType.FCW,true);
            mPreferAdas.setFunctionSensitivity(AdasFunctionType.FCW, AdasFunctionSensitivity.MIDDLE);
            mPreferAdas.setFunctionEnabled(AdasFunctionType.LKW,false);
            mPreferAdas.setFunctionSensitivity(AdasFunctionType.LKW, AdasFunctionSensitivity.MIDDLE);
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasConfigured(newValue));
    }

    public void onAdasPseudoCooperation(boolean newValue) {
        mPreference.setAdasPseudoCooperation(newValue);
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasPseudoCooperation(newValue));
    }

    public void onVRDelayTime(int value) {
        mStatusCase.execute().getAppStatus().speechRecognizerDelayTime = value;
        Optional.ofNullable(getView()).ifPresent(view -> view.setVRDelayTime(value));
    }

    public void onDebugRunningStatus(boolean newValue) {
        mStatusCase.execute().getAppStatus().getRunningStatus = newValue;
        Optional.ofNullable(getView()).ifPresent(view -> view.setDebugRunningStatus(newValue));
        if(newValue){
            mGetRunningStatusCase.start();
        }else{
            mGetRunningStatusCase.stop();
        }
    }

    public void onHomeCenterAction(){
        boolean isAdas = mStatusCase.execute().getAppStatus().homeCenterViewAdas;
        mStatusCase.execute().getAppStatus().homeCenterViewAdas = !isAdas;
        Optional.ofNullable(getView()).ifPresent(view -> view.setHomeCenterView(!isAdas));
    }

    public void onAdasCarSpeedAction(int value){
        mStatusCase.execute().getAppStatus().adasCarSpeed = value;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasCarSpeed(value));
    }

    public void onAdasLdwMinSpeed(int value){
        mStatusCase.execute().getAppStatus().adasLdwMinSpeed = value;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasLdwMinSpeed(value));
    }

    public void onAdasLdwMaxSpeed(int value){
        mStatusCase.execute().getAppStatus().adasLdwMaxSpeed = value;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasLdwMaxSpeed(value));
    }

    public void onAdasPcwMinSpeed(int value){
        mStatusCase.execute().getAppStatus().adasPcwMinSpeed = value;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasPcwMinSpeed(value));
    }

    public void onAdasPcwMaxSpeed(int value){
        mStatusCase.execute().getAppStatus().adasPcwMaxSpeed = value;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasPcwMaxSpeed(value));
    }

    public void onAdasFcwMinSpeed(int value){
        mStatusCase.execute().getAppStatus().adasFcwMinSpeed = value;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasFcwMinSpeed(value));
    }

    public void onAdasFcwMaxSpeed(int value){
        mStatusCase.execute().getAppStatus().adasFcwMaxSpeed = value;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasFcwMaxSpeed(value));
    }

    public void onAdasAccelerateY(int value){
        float fValue = value/1000f;
        mStatusCase.execute().getAppStatus().adasAccelerateYRange = fValue;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasAccelerateY(fValue));
    }

    public void onAdasAccelerateZMin(int value){
        float fValue = value/1000f;
        mStatusCase.execute().getAppStatus().adasAccelerateZRangeMin = fValue;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasAccelerateZMin(fValue));
    }

    public void onAdasAccelerateZMax(int value){
        float fValue = value/1000f;
        mStatusCase.execute().getAppStatus().adasAccelerateZRangeMax = fValue;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasAccelerateZMax(fValue));
    }

    public void onAdasFps(int value){
        mStatusCase.execute().getAppStatus().adasFps = value;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasFps(value));
    }

    public void onAdasCameraPreview(boolean newValue){
        mStatusCase.execute().getAppStatus().adasCameraView = newValue;
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdasCameraPreview(newValue));
    }

    public void onAlexaSimJudgement(boolean newValue){
        mPreference.setIsAlexaRequiredSimCheck(newValue);
        {
            // TODO #5244 デバッグ用
            mPreference.setIsAlexaAvailableConfirmShowed(false);
        }
        Optional.ofNullable(getView()).ifPresent(view ->{
            view.setAlexaSimJudgement(newValue);
            if(newValue){
                view.recheckSim();
            }
        });
        if(!newValue) {
            mStatusCase.execute().getAppStatus().isAlexaAvailableCountry = true;
        }
    }
}
