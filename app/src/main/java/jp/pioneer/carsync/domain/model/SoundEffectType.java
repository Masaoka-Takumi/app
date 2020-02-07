package jp.pioneer.carsync.domain.model;

/**
 * Sound Effect種別.
 * <p>
 * UI層向け.
 * infra層では本値から有効な{@link SoundEffectSettingType}を取得する
 */
public enum SoundEffectType {
    /** OFF. */
    OFF("OFF"),
    /** MALE */
    MALE("1"),
    /** FEMALE. */
    FEMALE("2")
    ;

    /** Analytics用文字列. */
    public final String strValue;

    /**
     * コンストラクタ.
     */
    SoundEffectType(String strValue){
        this.strValue = strValue;
    }

    /**
     * Analytics用文字列取得.
     */
    public String getAnalyticsStr() {
        return strValue;
    }

}
