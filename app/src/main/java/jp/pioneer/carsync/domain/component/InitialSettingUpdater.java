package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AmStep;
import jp.pioneer.carsync.domain.model.FmStep;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.RearOutputSetting;

/**
 * 初期設定更新.
 */
public interface InitialSettingUpdater {

    /**
     * FM STEP設定.
     *
     * @param step FM STEP
     * @throws NullPointerException {@code step}がnull
     */
    void setFmStep(@NonNull FmStep step);

    /**
     * AM STEP設定.
     *
     * @param step AM STEP
     * @throws NullPointerException {@code step}がnull
     */
    void setAmStep(@NonNull AmStep step);

    /**
     * REAR出力/PREOUT出力設定
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code step}がnull
     */
    void setRearOutputPreoutOutput(@NonNull RearOutputPreoutOutputSetting setting);

    /**
     * REAR出力設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code step}がnull
     */
    void setRearOutput(@NonNull RearOutputSetting setting);

    /**
     * MENU表示言語設定.
     *
     * @param type 設定種別
     * @throws NullPointerException {@code step}がnull
     */
    void setMenuDisplayLanguage(@NonNull MenuDisplayLanguageType type);

    /**
     * DAB ANT PW設定.
     *
     * @param isOn 設定
     */
    void setDabAntennaPower(boolean isOn);
}
