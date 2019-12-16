package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * 初期設定ステータス.
 */
public class InitialSettingStatus extends SerialVersion {
    /** MENU表示言語設定有効.*/
    public boolean menuDisplayLanguageSettingEnabled;
    /** REAR出力設定有効.*/
    public boolean rearOutputSettingEnabled;
    /** REAR出力設定/PREOUT出力設定有効.*/
    public boolean rearOutputPreoutOutputSettingEnabled;
    /** AM STEP設定有効.*/
    public boolean amStepSettingEnabled;
    /** FM STEP設定有効.*/
    public boolean fmStepSettingEnabled;
    /** DAB ANT PW設定.*/
    public boolean dabAntennaPowerEnabled;
    /**
     * コンストラクタ.
     */
    public InitialSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        menuDisplayLanguageSettingEnabled = false;
        rearOutputSettingEnabled = false;
        rearOutputPreoutOutputSettingEnabled = false;
        amStepSettingEnabled = false;
        fmStepSettingEnabled = false;
        dabAntennaPowerEnabled = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("menuDisplayLanguageSettingEnabled", menuDisplayLanguageSettingEnabled)
                .add("rearOutputSettingEnabled", rearOutputSettingEnabled)
                .add("rearOutputPreoutOutputSettingEnabled", rearOutputPreoutOutputSettingEnabled)
                .add("amStepSettingEnabled", amStepSettingEnabled)
                .add("fmStepSettingEnabled", fmStepSettingEnabled)
                .add("dabAntennaPowerEnabled", dabAntennaPowerEnabled)
                .toString();
    }
}
