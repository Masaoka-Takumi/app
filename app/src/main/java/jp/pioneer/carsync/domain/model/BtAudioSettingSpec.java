package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * BT Audio設定スペック.
 */
public class BtAudioSettingSpec {
    /** Audio Device Select対応. */
    public boolean audioDeviceSelectSupported;

    /**
     * コンストラクタ.
     */
    public BtAudioSettingSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        audioDeviceSelectSupported = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("audioDeviceSelectSupported", audioDeviceSelectSupported)
                .toString();
    }
}
