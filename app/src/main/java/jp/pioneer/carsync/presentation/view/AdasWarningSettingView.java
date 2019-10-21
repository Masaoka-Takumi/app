package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.AdasFunctionSetting;

/**
 * Created by NSW00_007906 on 2018/07/04.
 */

public interface AdasWarningSettingView {
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

}
