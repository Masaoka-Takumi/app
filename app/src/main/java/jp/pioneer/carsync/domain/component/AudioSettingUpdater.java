package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import jp.pioneer.carsync.domain.interactor.PreferAudio;
import jp.pioneer.carsync.domain.model.BeatBlasterSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.CutoffSetting;
import jp.pioneer.carsync.domain.model.AudioSettingEqualizerType;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.SlopeSetting;
import jp.pioneer.carsync.domain.model.SoundRetrieverSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.SubwooferPhaseSetting;
import jp.pioneer.carsync.domain.model.SubwooferSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSettingMode;

/**
 * Audio設定更新.
 * <p>
 * 車載機の設定を更新するためのクラス.
 */
public interface AudioSettingUpdater {

    /**
     * BeatBlaster設定.
     *
     * @param setting  設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    void setBeatBlaster(@NonNull BeatBlasterSetting setting);

    /**
     * ラウドネス設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    void setLoudness(@NonNull LoudnessSetting setting);

    /**
     * ソースレベルアジャスター設定.
     *
     * @param step ステップ値
     */
    void setSourceLevelAdjuster(int step);

    /**
     * フェーダーバランス設定.
     *
     * @param fader フェーダー設定値
     * @param balance バランス設定値
     */
    void setFaderBalance(int fader, int balance);

    /**
     * リスニングポジション設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    void setListeningPosition(@NonNull ListeningPositionSetting setting);

    /**
     * タイムアライメントモード設定.
     *
     * @param mode 設定種別
     * @throws NullPointerException {@code mode}がnull
     */
    void setTimeAlignmentMode(@NonNull TimeAlignmentSettingMode mode);

    /**
     * タイムアライメント値設定.
     *
     * @param type スピーカー種別
     * @param step ステップ値
     * @throws NullPointerException {@code type}がnull
     */
    void setTimeAlignment(@NonNull MixedSpeakerType type, int step);

    /**
     * スピーカー出力レベル設定.
     *
     * @param type スピーカー種別
     * @param level レベル値
     * @throws NullPointerException {@code type}がnull
     */
    void setSpeakerLevel(@NonNull MixedSpeakerType type, int level);

    /**
     * ハイパス/ローパスフィルター設定.
     *
     * @param type スピーカー種別
     * @param isOn 有効か否か
     * @throws NullPointerException {@code type}がnull
     */
    void setCrossoverHpfLpf(@NonNull SpeakerType type, boolean isOn);

    /**
     * カットオフ周波数設定.
     *
     * @param type スピーカー種別
     * @param setting 設定内容
     * @throws NullPointerException {@code type}がnull
     * @throws NullPointerException {@code setting}がnull
     */
    void setCrossoverCutOff(@NonNull SpeakerType type, @NonNull CutoffSetting setting);

    /**
     * スロープ設定.
     *
     * @param type スピーカー種別
     * @param setting 設定内容
     * @throws NullPointerException {@code type}がnull
     * @throws NullPointerException {@code setting}がnull
     */
    void setCrossoverSlope(@NonNull SpeakerType type, @NonNull SlopeSetting setting);

    /**
     * サブウーファー設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    void setSubWoofer(@NonNull SubwooferSetting setting);

    /**
     * サブウーファー位相設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    void setSubWooferPhase(@NonNull SubwooferPhaseSetting setting);

    /**
     * Sound Retriever設定.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    void setSoundRetriever(@NonNull SoundRetrieverSetting setting);

    /**
     * Save Setting.
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback}がnull
     */
    void saveAudioSetting(@NonNull PreferAudio.LoadSaveCallback callback);

    /**
     * Load Setting.
     * <p>
     * AEQ設定とSound設定の2つが存在するが、
     * Sound設定をLoadする。
     *
     * @param callback コールバック
     * @throws NullPointerException {@code callback}がnull
     */
    void loadAudioSetting(@NonNull PreferAudio.LoadSaveCallback callback);

    /**
     * カスタムバンド設定.
     *
     * @param type カスタム種別
     * @param bands BAND1からBAND13までの設定値
     * @throws NullPointerException {@code type}、{@code bands}がnull
     * @throws IllegalArgumentException {@code bands}の値が不正
     */
    void setCustomBand(@NonNull CustomEqType type, @NonNull @Size(13) int[] bands);

    /**
     * EQ設定.
     *
     * @param type EQ種別
     * @throws NullPointerException {@code type}がnull
     */
    void setEqualizer(@NonNull AudioSettingEqualizerType type);

    /**
     * SPECIAL EQ設定.
     *
     * @param typeCode SPECIAL EQ種別
     * @param bands BAND1からBAND13までの設定値
     * @throws NullPointerException {@code bands}がnull
     * @throws IllegalArgumentException {@code bands}の値が不正
     */
    void setSpecialEqualizer(int typeCode, @NonNull @Size(13) int[] bands);

    /**
     * プリセットEQ初期化.
     */
    void initPresetEq();
}
