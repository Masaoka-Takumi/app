package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * BT Audio Function設定ステータス.
 */
public class BtAudioFunctionSettingStatus extends SerialVersion {
    /** Audio Device Select有効. */
    public boolean audioDeviceSelectEnabled;

    /**
     * コンストラクタ.
     */
    public BtAudioFunctionSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        audioDeviceSelectEnabled = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("audioDeviceSelectEnabled", audioDeviceSelectEnabled)
                .toString();
    }
}
