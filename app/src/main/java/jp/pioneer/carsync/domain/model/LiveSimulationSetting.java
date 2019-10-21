package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * ライブシミュレーション設定.
 * <p>
 * 本設定を更新する場合は、{@link SoundFieldControlSettingType}と{@link SoundEffectType}から取得した値を反映し、
 * 更新通知を送る。(設定されているSound Fieldに有効なSound Effectが決まっているため、片方更新して通知するということはしない。)
 */
public class LiveSimulationSetting extends SerialVersion {

    /** Sound Field設定種別. */
    public SoundFieldControlSettingType soundFieldControlSettingType;
    /** Sound Effect設定種別 */
    public SoundEffectSettingType soundEffectSettingType;

    /**
     * コンストラクタ.
     */
    public LiveSimulationSetting(){
        reset();
    }

    /**
     * リセット.
     * <p>
     * ライブシミュレーション設定を初期(OFF設定)状態にする
     */
    public void reset(){
        soundFieldControlSettingType = SoundFieldControlSettingType.OFF;
        soundEffectSettingType = SoundEffectSettingType.OFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("soundFieldControlSettingType", soundFieldControlSettingType)
                .add("soundEffectSettingType", soundEffectSettingType)
                .toString();
    }
}
