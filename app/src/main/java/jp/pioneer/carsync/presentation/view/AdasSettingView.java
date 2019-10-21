package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionSetting;

/**
 * ADAS設定画面のinterface.
 */
public interface AdasSettingView {

    /**
     * ADAS設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setAdasSetting(boolean setting);

    /**
     * ADAS試用期間表示設定.
     *
     * @param visible 表示/非表示
     */
    void setAdasTrialTermVisible(boolean visible);

    /**
     * ADAS試用期間終了日設定.
     *
     * @param str 試用期間終了日
     */
    void setAdasTrialTerm(String str);
    /**
     * ADAS Alarm設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setAdasAlarmSetting(boolean setting);

    /**
     * カメラ設定.
     *
     * @param setting 設定内容
     */
    void setCameraSetting(AdasCameraSetting setting);

    /**
     * LDW機能設定.
     *
     * @param setting 設定内容
     */
    void setLdwSetting(AdasFunctionSetting setting);

    /**
     * FCW機能設定.
     *
     * @param setting 設定内容
     */
    void setFcwSetting(AdasFunctionSetting setting);

    /**
     * PCW機能設定.
     *
     * @param setting 設定内容
     */
    void setPcwSetting(AdasFunctionSetting setting);

    /**
     * LKW機能設定.
     *
     * @param setting 設定内容
     */
    void setLkwSetting(AdasFunctionSetting setting);
}
