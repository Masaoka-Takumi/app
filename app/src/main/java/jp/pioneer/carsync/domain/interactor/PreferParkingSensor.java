package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.ParkingSensorSettingUpdater;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.ParkingSensorSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

/**
 * パーキングセンサーの設定.
 */
public class PreferParkingSensor {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject ParkingSensorSettingUpdater mUpdater;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferParkingSensor(){
    }

    /**
     * パーキングセンサー設定.
     * <p>
     * パーキングセンサー設定が無効な場合は何もしない
     *
     * @param isEnabled 有効か否か {@code true}:パーキングセンサー設定有効 {@code false}:パーキングセンサー設定無効
     */
    public void setParkingSensorSetting(boolean isEnabled){
        mHandler.post(() -> {
            CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
            if(status.parkingSensorSettingEnabled){
                mUpdater.setParkingSensorSetting(isEnabled);
            } else {
                Timber.w("setParkingSensorSetting() Parking sensor setting disabled.");
                return;
            }
        });
    }

    /**
     * 警告音出力先設定.
     * <p>
     * 警告音出力先設定が無効な場合は何もしない
     */
    public void toggleAlarmOutputDestination(){
        mHandler.post(() -> {
            ParkingSensorSettingStatus status = mStatusHolder.getParkingSensorSettingStatus();
            ParkingSensorSetting setting = mStatusHolder.getParkingSensorSetting();

            if(status.alarmOutputDestinationSettingEnabled){
                mUpdater.setAlarmOutputDestination(setting.alarmOutputDestinationSetting.toggle());
            } else {
                Timber.w("setParkingSensorSetting() Alarm output destination setting disabled.");
                return;
            }
        });
    }

    /**
     * 警告音量設定.
     * <p>
     * 警告音量設定が無効な場合は何もしない
     *
     * @param volume 音量
     */
    public void setAlarmVolume(int volume){
        mHandler.post(() -> {
            ParkingSensorSettingStatus status = mStatusHolder.getParkingSensorSettingStatus();

            if(status.alarmVolumeSettingEnabled){
                mUpdater.setAlarmVolume(volume);
            } else {
                Timber.w("setParkingSensorSetting() Alarm volume setting disabled.");
                return;
            }
        });
    }

    /**
     * バック信号極性設定.
     * <p>
     * バック信号極性設定が無効な場合は何もしない
     */
    public void toggleBackPolarity(){
        mHandler.post(() -> {
            ParkingSensorSettingStatus status = mStatusHolder.getParkingSensorSettingStatus();
            ParkingSensorSetting setting = mStatusHolder.getParkingSensorSetting();

            if(status.backPolaritySettingEnabled){
                mUpdater.setBackPolarity(setting.backPolarity.toggle());
            } else {
                Timber.w("toggleBackPolarity() back polarity setting disabled.");
                return;
            }
        });
    }
}
