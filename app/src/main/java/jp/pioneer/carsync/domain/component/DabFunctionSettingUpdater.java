package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.TASetting;

/**
 * DAB Function設定更新者.
 */
public interface DabFunctionSettingUpdater {

    /**
     * TA設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting} がnull
     */
    void setTa(@NonNull TASetting setting);

    /**
     * SERVICE FOLLOW設定.
     *
     * @param setting 設定内容
     */
    void setServiceFollow(boolean setting);

    /**
     * SOFT LINK設定.
     *
     * @param setting 設定内容
     */
    void setSoftLink(boolean setting);
}
