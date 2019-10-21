package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * DAB Function設定ステータス.
 *
 * @see DabFunctionSettingSpec
 */
public class DabFunctionSettingStatus extends SerialVersion {
    /** TA設定有効. */
    public boolean taSettingEnabled;
    /** SERVICE FOLLOW ON/OFF設定有効. */
    public boolean serviceFollowSettingEnabled;
    /** SOFTLINK設定有効. */
    public boolean softlinkSettingEnabled;

    /**
     * コンストラクタ.
     */
    public DabFunctionSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        taSettingEnabled = false;
        serviceFollowSettingEnabled = false;
        softlinkSettingEnabled = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("taSettingEnabled", taSettingEnabled)
                .add("serviceFollowSettingEnabled", serviceFollowSettingEnabled)
                .add("softlinkSettingEnabled", softlinkSettingEnabled)
                .toString();
    }
}
