package jp.pioneer.carsync.domain.model;

/**
 * Sound Effect種別.
 * <p>
 * UI層向け.
 * infra層では本値から有効な{@link SoundEffectSettingType}を取得する
 */
public enum SoundEffectType {
    /** OFF. */
    OFF,
    /** MALE */
    MALE,
    /** FEMALE. */
    FEMALE
}
