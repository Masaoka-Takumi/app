package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * 車種専用セッティング.
 */
public class CarModelSpecializedSetting {
    /** イルミカラー設定対応. */
    public boolean illumiColorSettingSupported;
    /** オーディオ設定対応. */
    public boolean audioSettingSupported;

    /**
     * コンストラクタ.
     */
    public CarModelSpecializedSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        illumiColorSettingSupported = false;
        audioSettingSupported = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("illumiColorSettingSupported", illumiColorSettingSupported)
                .add("audioSettingSupported", audioSettingSupported)
                .toString();
    }
}
