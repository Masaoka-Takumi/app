package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.TASetting;

public interface DabSettingView {
    void setTaSetting(boolean isSupported, boolean isEnabled, TASetting setting);
    void setServiceFollowSetting(boolean isSupported, boolean isEnabled, boolean setting);
    void setSoftLinkSetting(boolean isSupported, boolean isEnabled, boolean setting);
}
