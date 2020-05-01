package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.EnumSet;
import java.util.Set;

/**
 * 初期設定スペック.
 */
public class InitialSettingSpec {
    /** MENU表示言語設定対応.*/
    public boolean menuDisplayLanguageSettingSupported;
    /** REAR出力設定対応.*/
    public boolean rearOutputSettingSupported;
    /** REAR出力設定/PREOUT出力設定対応.*/
    public boolean rearOutputPreoutOutputSettingSupported;
    /** AM STEP設定対応.*/
    public boolean amStepSettingSupported;
    /** FM STEP設定対応.*/
    public boolean fmStepSettingSupported;
    /** サポートMENU表示言語群. */
    public Set<MenuDisplayLanguageType> supportedMenuDisplayLanguages;
    /** DAB ANT PW設定.*/
    public boolean dabAntennaPowerSupported;

    /**
     * コンストラクタ.
     */
    public InitialSettingSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        menuDisplayLanguageSettingSupported = false;
        rearOutputSettingSupported = false;
        rearOutputPreoutOutputSettingSupported = false;
        amStepSettingSupported = false;
        fmStepSettingSupported = false;
        supportedMenuDisplayLanguages = EnumSet.noneOf(MenuDisplayLanguageType.class);
        dabAntennaPowerSupported = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("menuDisplayLanguageSettingSupported", menuDisplayLanguageSettingSupported)
                .add("rearOutputSettingSupported", rearOutputSettingSupported)
                .add("rearOutputPreoutOutputSettingSupported", rearOutputPreoutOutputSettingSupported)
                .add("amStepSettingSupported", amStepSettingSupported)
                .add("fmStepSettingSupported", fmStepSettingSupported)
                .add("supportedMenuDisplayLanguages", supportedMenuDisplayLanguages)
                .add("dabAntennaPowerSupported", dabAntennaPowerSupported)
                .toString();
    }
}
