package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * パーキングセンサー設定ステータス.
 */
public class ParkingSensorSettingStatus extends SerialVersion {
    /** バック信号極性設定有効. */
    public boolean backPolaritySettingEnabled;
    /** 警告音出力先設定有効. */
    public boolean alarmOutputDestinationSettingEnabled;
    /** 警告音量設定有効. */
    public boolean alarmVolumeSettingEnabled;

    /**
     * コンストラクタ.
     */
    public ParkingSensorSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        backPolaritySettingEnabled = false;
        alarmOutputDestinationSettingEnabled = false;
        alarmVolumeSettingEnabled = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("backPolaritySettingEnabled", backPolaritySettingEnabled)
                .add("alarmOutputDestinationSettingEnabled", alarmOutputDestinationSettingEnabled)
                .add("alarmVolumeSettingEnabled", alarmVolumeSettingEnabled)
                .toString();
    }
}