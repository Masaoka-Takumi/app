package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.FMTunerSetting;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.TASetting;

/**
 * Radio Function設定更新者.
 */
public interface RadioFunctionSettingUpdater {

    /**
     * LOCAL設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting} がnull
     */
    void setLocal(@NonNull LocalSetting setting);

    /**
     * FM TUNER設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting} がnull
     */
    void setFmTuner(@NonNull FMTunerSetting setting);

    /**
     * REG広域設定.
     *
     * @param setting 設定内容
     */
    void setReg(boolean setting);

    /**
     * TA設定.
     *
     * @param setting 設定内容
     */
    void setTa(boolean setting);

    /**
     * TA設定(DAB対応モデル).
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting} がnull
     */
    void setTa(@NonNull TASetting setting);

    /**
     * AF設定.
     *
     * @param setting 設定内容
     */
    void setAf(boolean setting);

    /**
     * NEWS設定.
     *
     * @param setting 設定内容
     */
    void setNews(boolean setting);

    /**
     * ALARM設定.
     *
     * @param setting 設定内容
     */
    void setAlarm(boolean setting);

    /**
     * P.CH/MANUAL設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting} がnull
     */
    void setPchManual(@NonNull PCHManualSetting setting);

}
