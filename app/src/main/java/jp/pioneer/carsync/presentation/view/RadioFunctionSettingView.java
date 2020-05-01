package jp.pioneer.carsync.presentation.view;

import android.support.annotation.Nullable;

import jp.pioneer.carsync.domain.model.FMTunerSetting;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.TASetting;

/**
 * RadioFunction設定画面の抽象クラス.
 */
public interface RadioFunctionSettingView {

    /**
     * FM Tuner設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setFmTunerSetting(boolean isSupported,
                           boolean isEnabled,
                           @Nullable FMTunerSetting setting);

    /**
     * Region広域設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setRegionSetting(boolean isSupported,
                          boolean isEnabled,
                          boolean setting);

    /**
     * Local設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setLocalSetting(boolean isSupported,
                         boolean isEnabled,
                         @Nullable LocalSetting setting);

    /**
     * TA設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setTaSetting(boolean isSupported,
                      boolean isEnabled,
                      boolean setting);

    void setTaDabSetting(boolean isSupported, boolean isEnabled, TASetting setting);

    /**
     * AF設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setAfSetting(boolean isSupported,
                      boolean isEnabled,
                      boolean setting);

    /**
     * NEWS設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setNewsSetting(boolean isSupported,
                        boolean isEnabled,
                        boolean setting);

    /**
     * Alarm設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setAlarmSetting(boolean isSupported,
                         boolean isEnabled,
                         boolean setting);

    /**
     * PCH./Manual(Seek)設定
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setPchManual(boolean isSupported,
                      boolean isEnabled,
                      @Nullable PCHManualSetting setting);
}
