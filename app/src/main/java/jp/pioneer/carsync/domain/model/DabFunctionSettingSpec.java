package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * DAB Function設定スペック.
 */
public class DabFunctionSettingSpec {
    /** TA設定対応. */
    public boolean taSettingSupported;
    /** SERVICE FOLLOW ON/OFF設定対応. */
    public boolean serviceFollowSettingSupported;
    /** SOFTLINK設定対応. */
    public boolean softlinkSettingSupported;

    /**
     * コンストラクタ.
     */
    public DabFunctionSettingSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        taSettingSupported = false;
        serviceFollowSettingSupported = false;
        softlinkSettingSupported = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("taSettingSupported", taSettingSupported)
                .add("serviceFollowSettingSupported", serviceFollowSettingSupported)
                .add("softlinkSettingSupported", softlinkSettingSupported)
                .toString();
    }
}
