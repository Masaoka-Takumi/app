package jp.pioneer.carsync.presentation.view;

import android.support.annotation.Nullable;

import jp.pioneer.carsync.domain.model.AmStep;
import jp.pioneer.carsync.domain.model.FmStep;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.RearOutputSetting;

/**
 * 初期設定画面のinterface.
 */
public interface InitialSettingView {

    /**
     * MENU表示言語設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setMenuDisplayLanguageSetting(boolean isSupported,
                                       boolean isEnabled,
                                       @Nullable MenuDisplayLanguageType setting);

    /**
     * FM STEP 50K/100K切換設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setFmStepSetting(boolean isSupported,
                          boolean isEnabled,
                          @Nullable FmStep setting);

    /**
     * AM STEP 50K/100K切換設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setAmStepSetting(boolean isSupported,
                          boolean isEnabled,
                          @Nullable AmStep setting);

    /**
     * REAR 出力設定/PREOUT出力設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setRearOutputPreoutOutputSetting(boolean isSupported,
                                          boolean isEnabled,
                                          @Nullable RearOutputPreoutOutputSetting setting);

    /**
     * REAR出力設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setRearOutputSetting(boolean isSupported,
                              boolean isEnabled,
                              @Nullable RearOutputSetting setting);
}
