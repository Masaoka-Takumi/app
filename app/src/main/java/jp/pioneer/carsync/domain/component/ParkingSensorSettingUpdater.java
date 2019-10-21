package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AlarmOutputDestinationSetting;
import jp.pioneer.carsync.domain.model.BackPolarity;

/**
 * パーキングセンサー設定更新.
 */
public interface ParkingSensorSettingUpdater {

    /**
     * パーキングセンサー設定.
     *
     * @param isEnabled 有効か否か {@code true}:パーキングセンサー設定有効 {@code false}:パーキングセンサー設定無効
     */
    void setParkingSensorSetting(boolean isEnabled);

    /**
     * 警告音出力先設定.
     *
     * @param setting 出力先設定値
     * @throws NullPointerException {@code setting}がnull
     */
    void setAlarmOutputDestination(@NonNull AlarmOutputDestinationSetting setting);

    /**
     * 警告音量設定.
     *
     * @param volume 音量
     */
    void setAlarmVolume(int volume);

    /**
     * バック信号極性設定.
     *
     * @param setting 設定値
     * @throws NullPointerException {@code setting}がnull
     */
    void setBackPolarity(@NonNull BackPolarity setting);
}
