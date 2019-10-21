package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.LocalSetting;

/**
 * HD Radio Function設定更新者.
 */
public interface HdRadioFunctionSettingUpdater {

    /**
     * LOCAL設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting} がnull
     */
    void setLocal(@NonNull LocalSetting setting);

    /**
     * SEEK設定.
     *
     * @param setting 設定内容
     */
    void setSeek(boolean setting);

    /**
     * BLENDING設定.
     *
     * @param setting 設定内容
     */
    void setBlending(boolean setting);

    /**
     * ACTIVE RADIO設定.
     *
     * @param setting 設定内容
     */
    void setActiveRadio(boolean setting);
}
