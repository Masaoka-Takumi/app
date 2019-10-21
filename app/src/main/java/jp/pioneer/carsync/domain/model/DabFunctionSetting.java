package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * DAB Function設定.
 */
public class DabFunctionSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** TA設定. */
    public TASetting taSetting;
    /** SERVICE FOLLOW設定ON. */
    public boolean serviceFollowSetting;
    /** SOFTLINK設定ON. */
    public boolean softlinkSetting;

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        taSetting = TASetting.OFF;
        serviceFollowSetting = false;
        softlinkSetting = false;
        clear();
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("requestStatus", requestStatus)
                .add("taSetting", taSetting)
                .add("serviceFollowSetting", serviceFollowSetting)
                .add("softlinkSetting", softlinkSetting)
                .toString();
    }
}
