package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.ParkingSensorSettingChangeEvent;
import jp.pioneer.carsync.domain.event.ParkingSensorSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferParkingSensor;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.ParkingSensorSettingSpec;
import jp.pioneer.carsync.domain.model.ParkingSensorSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.application.util.AppUtil;
import jp.pioneer.carsync.presentation.view.ParkingSensorView;

/**
 * パーキングセンサー設定画面のPresenter.
 */
public class ParkingSensorPresenter extends Presenter<ParkingSensorView> {
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject PreferParkingSensor mPreferCase;

    /**
     * コンストラクタ
     */
    @Inject
    public ParkingSensorPresenter() {
    }

    @Override
    public void onInitialize() {
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

    /**
     * パーキングセンサー設定変更イベント.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onParkingSensorSettingChangeEvent(ParkingSensorSettingChangeEvent event) {
        updateView();
    }

    /**
     * パーキングセンサー設定ステータス変更イベント.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onParkingSensorSettingStatusChangeEvent(ParkingSensorSettingStatusChangeEvent event) {
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
            if(AppUtil.isScreenOn(mContext)) {
                StatusHolder holder = mGetStatusHolder.execute();
                boolean isParkingSensorSettingEnabled = holder.getCarDeviceStatus().parkingSensorSettingEnabled &&
                        holder.getCarDeviceSpec().parkingSensorSettingSupported;
                CarDeviceSpec spec = holder.getCarDeviceSpec();
                CarDeviceStatus status = holder.getCarDeviceStatus();
                ParkingSensorSettingSpec settingSpec = spec.parkingSensorSettingSpec;
                ParkingSensorSettingStatus settingStatus = holder.getParkingSensorSettingStatus();
                ParkingSensorSetting setting = holder.getParkingSensorSetting();

                view.setParkingSensorSetting(
                        spec.parkingSensorSettingSupported,
                        isParkingSensorSettingEnabled,
                        setting.parkingSensorSetting
                );
                view.setParkingSensorAlarmOutputDestinationSetting(
                        settingSpec.alarmOutputDestinationSettingSupported,
                        isParkingSensorSettingEnabled && settingStatus.alarmOutputDestinationSettingEnabled,
                        setting.alarmOutputDestinationSetting
                );
                view.setParkingSensorAlarmVolumeSetting(
                        settingSpec.alarmVolumeSettingSupported,
                        isParkingSensorSettingEnabled && settingStatus.alarmVolumeSettingEnabled,
                        setting.alarmVolumeSetting.min,
                        setting.alarmVolumeSetting.max,
                        setting.alarmVolumeSetting.current
                );
                view.setBackPolarity(
                        settingSpec.backPolaritySettingSupported,
                        isParkingSensorSettingEnabled && settingStatus.backPolaritySettingEnabled,
                        setting.backPolarity
                );
            }
        });
    }

    /**
     * ParkingSensor設定スイッチ押下時の処理
     *
     * @param newValue スイッチの状態
     */
    public void onParkingSensorChange(boolean newValue) {
        mPreferCase.setParkingSensorSetting(newValue);
    }

    /**
     * 警告音出力先切換処理
     */
    public void onParkingSensorAlarmOutputChange() {
        mPreferCase.toggleAlarmOutputDestination();
    }

    /**
     * 警告音量変更処理
     */
    public void onParkingSensorAlarmVolumeChange(int volume){
        mPreferCase.setAlarmVolume(volume);
    }

    /**
     * バック信号極性切り替え処理
     */
    public void onBackPolarityChange() {
        mPreferCase.toggleBackPolarity();
    }
}
