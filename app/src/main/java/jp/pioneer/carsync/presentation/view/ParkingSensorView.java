package jp.pioneer.carsync.presentation.view;

import android.support.annotation.Nullable;

import jp.pioneer.carsync.domain.model.AlarmOutputDestinationSetting;
import jp.pioneer.carsync.domain.model.BackPolarity;

/**
 * パーキングセンサー設定画面のinterface.
 */
public interface ParkingSensorView {
    /**
     * ParkingSensor設定.
     * <p>
     * 本画面が表示できている時点で設定可能な項目と判断できるため、
     * 必ず表示する。
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setParkingSensorSetting(boolean isSupported,
                                 boolean isEnabled,
                                 boolean setting);

    /**
     * 警告音出力先設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setParkingSensorAlarmOutputDestinationSetting(boolean isSupported,
                                                       boolean isEnabled,
                                                       @Nullable AlarmOutputDestinationSetting setting);

    /**
     * 警告音量設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param min         設定可能最小値
     * @param max         設定可能最大値
     * @param curr        現在値
     */
    void setParkingSensorAlarmVolumeSetting(boolean isSupported,
                                            boolean isEnabled,
                                            int min,
                                            int max,
                                            int curr);

    /**
     * バック信号極性設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setBackPolarity(boolean isSupported,
                         boolean isEnabled,
                         @Nullable BackPolarity setting);
}
