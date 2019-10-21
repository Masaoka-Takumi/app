package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.UpdateAdasEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionSensitivity;
import jp.pioneer.carsync.domain.model.AdasFunctionType;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.view.AdasSettingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

/**
 * ADAS設定画面のPresenter.
 */
@PresenterLifeCycle
public class AdasSettingPresenter extends Presenter<AdasSettingView>  {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject PreferAdas mPreferCase;
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mStatusHolder;
    /**
     * コンストラクタ
     */
    @Inject
    public AdasSettingPresenter() {
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

    private void updateView() {

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAdasSetting(mPreferCase.getAdasEnabled());
            view.setAdasTrialTermVisible(mPreference.getAdasTrialState()== AdasTrialState.TRIAL_DURING);
            Date endDate = new Date(mPreference.getAdasTrialPeriodEndDate());

            SimpleDateFormat sdf =(SimpleDateFormat) DateFormat.getDateFormat(mContext);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat sidf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            sidf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Timber.d("AdasTrialPeriodEndDate:%s",sidf.format(endDate));
            if(mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING){
                view.setAdasTrialTerm(sdf.format(endDate));
            }
            view.setAdasAlarmSetting(mPreferCase.getAdasAlarmEnabled());
            view.setCameraSetting(mPreferCase.getAdasCameraSetting());
            view.setFcwSetting(mPreferCase.getFunctionSetting(AdasFunctionType.FCW));
            view.setLdwSetting(mPreferCase.getFunctionSetting(AdasFunctionType.LDW));
            view.setLkwSetting(mPreferCase.getFunctionSetting(AdasFunctionType.LKW));
            view.setPcwSetting(mPreferCase.getFunctionSetting(AdasFunctionType.PCW));
        });
    }

    /**
     * ADAS押下
     */
    public void onSelectAdasAction(boolean setting) {
        mPreferCase.setAdasEnabled(setting);
        updateView();
    }

    /**
     * ADAS押下
     */
    public void onSelectAdasAction() {
        mPreferCase.setAdasEnabled(!mPreferCase.getAdasEnabled());
        updateView();
    }

    /**
     * ADAS Alarm押下
     */
    public void onSelectAdasAlarmAction(boolean setting) {
        mPreferCase.setAdasAlarmEnabled(setting);
        updateView();
    }

    /**
     * キャリブレーション設定押下
     */
    public void onSelectCalibrationAction() {
        mEventBus.post(new NavigateEvent(ScreenId.CALIBRATION_SETTING, createSettingsParams(ScreenId.SETTINGS_ADAS, mContext.getString(R.string.set_036))));
    }

    /**
     * カメラ取付位置 高さ設定
     *
     * @param height 高さ[mm]
     */
    public void setCameraHeight(int height){
        AdasCameraSetting setting = mPreferCase.getAdasCameraSetting();
        setting.cameraHeight = height;
        mPreferCase.setAdasCameraSetting(setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * カメラ取付位置 車体先端との距離設定
     *
     * @param distance 距離[mm]
     */
    public void setFrontNoseDistance(int distance){
        AdasCameraSetting setting = mPreferCase.getAdasCameraSetting();
        setting.frontNoseDistance = distance;
        mPreferCase.setAdasCameraSetting(setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * 車幅設定
     *
     * @param width 幅[mm]
     */
    public void setVehicleWidth(int width) {
        AdasCameraSetting setting = mPreferCase.getAdasCameraSetting();
        setting.vehicleWidth = width;
        mPreferCase.setAdasCameraSetting(setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * LDW機能　有効設定
     */
    public void onSelectLdwAction(boolean setting){
        mPreferCase.setFunctionEnabled(AdasFunctionType.LDW, setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * LDW機能　感度設定
     */
    public void onSelectLdwSensitivityAction(){
        AdasFunctionSensitivity sensitivity = mPreferCase.getFunctionSetting(AdasFunctionType.LDW).functionSensitivity;
        mPreferCase.setFunctionSensitivity(AdasFunctionType.LDW, sensitivity.toggle());

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * PCW機能　有効設定
     */
    public void onSelectPcwAction(boolean setting){
        mPreferCase.setFunctionEnabled(AdasFunctionType.PCW, setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * PCW機能　感度設定
     */
    public void onSelectPcwSensitivityAction(){
        AdasFunctionSensitivity sensitivity = mPreferCase.getFunctionSetting(AdasFunctionType.PCW).functionSensitivity;
        mPreferCase.setFunctionSensitivity(AdasFunctionType.PCW, sensitivity.toggle());

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * FCW機能　有効設定
     */
    public void onSelectFcwAction(boolean setting){
        mPreferCase.setFunctionEnabled(AdasFunctionType.FCW, setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * FCW機能　感度設定
     */
    public void onSelectFcwSensitivityAction(){
        AdasFunctionSensitivity sensitivity = mPreferCase.getFunctionSetting(AdasFunctionType.FCW).functionSensitivity;
        mPreferCase.setFunctionSensitivity(AdasFunctionType.FCW, sensitivity.toggle());

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * LKW機能　有効設定
     */
    public void onSelectLkwAction(boolean setting){
        mPreferCase.setFunctionEnabled(AdasFunctionType.LKW, setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * LKW機能　感度設定
     */
    public void onSelectLkwSensitivityAction(){
        AdasFunctionSensitivity sensitivity = mPreferCase.getFunctionSetting(AdasFunctionType.LKW).functionSensitivity;
        mPreferCase.setFunctionSensitivity(AdasFunctionType.LKW, sensitivity.toggle());

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * 使用上の注意＆使い方に遷移
     */
    public void onSelectUsageCautionManualAction(){
        mEventBus.post(new NavigateEvent(ScreenId.ADAS_USAGE_CAUTION, createSettingsParams(ScreenId.SETTINGS_ADAS, mContext.getString(R.string.set_340)+mContext.getString(R.string.set_370))));
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

    public DistanceUnit getDistanceUnit() {
        return mPreference.getDistanceUnit();
    }

    /**
     * ADAS設定更新イベントハンドラ.
     *
     * @param ev ADAS設定更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onUpdateAdasEvent(UpdateAdasEvent ev) {
        updateView();
    }
}
