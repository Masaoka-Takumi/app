package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;

/**
 * AppMusic設定更新.
 * <p>
 * PMGPlayerの設定を更新するためのクラス
 */
public interface SoundFxSettingUpdater {

    /**
     * Super轟設定.
     *
     * @param setting 設定
     * @throws NullPointerException {@code setting}がnull
     */
    void setSuperTodoroki(@NonNull SuperTodorokiSetting setting);

    /**
     * Small Car TA設定.
     * <p>
     * PMGPlayerのTimeAlignment設定を更新する
     *
     * @param type 音場設定
     * @param position エフェクト設定
     * @throws NullPointerException {@code smallCarTaSettingType}、{@code listeningPosition}がnull
     */
    void setSmallCarTa(@NonNull SmallCarTaSettingType type, @NonNull ListeningPosition position);

    /**
     * LiveSimulation設定.
     * <p>
     * PMGPlayerの音場設定、拍手歓声設定を更新する
     *
     * @param soundFieldControlSettingType 音場設定
     * @param soundEffectSettingType エフェクト設定
     * @throws NullPointerException {@code soundFieldControlSettingType}、{@code soundEffectSettingType}がnull
     */
    void setLiveSimulation(@NonNull SoundFieldControlSettingType soundFieldControlSettingType,
                           @NonNull SoundEffectSettingType soundEffectSettingType);

    /**
     * カラオケ設定.
     *
     * @param isEnabled カラオケ設定がONか否か
     */
    void setKaraokeSetting(boolean isEnabled);

    /**
     * マイク音量設定.
     *
     * @param volume 音量
     */
    void setMicVolume(int volume);

    /**
     * Vocal Cancel設定.
     *
     * @param isEnabled Vocal Cancel設定がONか否か
     */
    void setVocalCancel(boolean isEnabled);
}
