package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * パーキングセンサー設定スペック.
 */
public class ParkingSensorSettingSpec {
    /** 警告音出力先設定対応. */
    public boolean alarmOutputDestinationSettingSupported;
    /** 警告音量設定対応. */
    public boolean alarmVolumeSettingSupported;
    /** バック信号極性設定対応. */
    public boolean backPolaritySettingSupported;

    /**
     * コンストラクタ.
     */
    public ParkingSensorSettingSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        alarmOutputDestinationSettingSupported = false;
        alarmVolumeSettingSupported = false;
        backPolaritySettingSupported = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("alarmOutputDestinationSettingSupported", alarmOutputDestinationSettingSupported)
                .add("alarmVolumeSettingSupported", alarmVolumeSettingSupported)
                .add("backPolaritySettingSupported", backPolaritySettingSupported)
                .toString();
    }
}
