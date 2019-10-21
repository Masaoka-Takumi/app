package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.AudioSettingUpdater;
import jp.pioneer.carsync.domain.component.SoundFxSettingUpdater;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.BeatBlasterSetting;
import jp.pioneer.carsync.domain.model.CutoffSetting;
import jp.pioneer.carsync.domain.model.HpfLpfSetting;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.SlopeSetting;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundRetrieverSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubwooferPhaseSetting;
import jp.pioneer.carsync.domain.model.SubwooferSetting;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSettingMode;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Audio設定.
 */
public class PreferAudio {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject AudioSettingUpdater mAudioSettingUpdater;
    @Inject SoundFxSettingUpdater mSoundFxSettingUpdater;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferAudio() {

    }

    /**
     * BeatBlaster設定.
     * <p>
     * 車載機に対してBeatBlaster設定を反映する時に、
     * BeatBlaster設定が無効な場合は何もしない。
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    public void setBeatBlaster(@NonNull BeatBlasterSetting setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().beatBlasterSettingEnabled) {
                Timber.w("setBeatBlaster() Beat blaster setting disabled.");
                return;
            }

            AudioSetting audioSetting = mStatusHolder.getAudioSetting();
            SoundFxSetting soundFxSetting = mStatusHolder.getSoundFxSetting();

            if(audioSetting.beatBlasterSetting == setting){
                if(soundFxSetting.liveSimulationSetting.soundFieldControlSettingType != SoundFieldControlSettingType.OFF){
                    mSoundFxSettingUpdater.setLiveSimulation(SoundFieldControlSettingType.OFF, SoundEffectSettingType.OFF);
                } else if(soundFxSetting.superTodorokiSetting != SuperTodorokiSetting.OFF){
                    mSoundFxSettingUpdater.setSuperTodoroki(SuperTodorokiSetting.OFF);
                }
            } else {
                mAudioSettingUpdater.setBeatBlaster(setting);
            }
        });
    }

    /**
     * ラウドネス設定.
     * <p>
     * ラウドネス設定が無効な場合は何もしない。
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    public void setLoudness(@NonNull LoudnessSetting setting) {
        checkNotNull(setting);

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().loudnessSettingEnabled) {
                Timber.w("setLoudness() Loudness setting disabled.");
                return;
            }

            mAudioSettingUpdater.setLoudness(setting);
        });
    }

    /**
     * ソースレベルアジャスター設定.
     * <p>
     * ソースレベルアジャスター設定が無効な場合は何もしない。
     *
     * @param step ステップ値
     */
    public void setSourceLevelAdjuster(int step) {

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().slaSettingEnabled) {
                Timber.w("setSourceLevelAdjuster() Source level adjuster setting disabled.");
                return;
            }

            mAudioSettingUpdater.setSourceLevelAdjuster(step);
        });
    }

    /**
     * フェーダーバランス設定.
     * <p>
     * フェーダー設定、又はバランス設定が無効の場合は何もしない。
     *
     * @param fader   フェーダー設定値
     * @param balance バランス設定値
     */
    public void setFaderBalance(int fader, int balance) {

        mHandler.post(() -> {
            AudioSettingStatus status = mStatusHolder.getAudioSettingStatus();
            if (!status.balanceSettingEnabled) {
                Timber.w("setFaderBalance() Balance setting disabled.");
                return;
            }

            mAudioSettingUpdater.setFaderBalance(status.faderSettingEnabled ? fader : 0, balance);
        });
    }

    /**
     * リスニングポジション設定.
     * <p>
     * リスニングポジション設定が無効な場合は何もしない。
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    public void setListeningPosition(@NonNull ListeningPositionSetting setting) {
        checkNotNull(setting);

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().listeningPositionSettingEnabled) {
                Timber.w("setListeningPosition() Listening position setting disabled.");
                return;
            }

            mAudioSettingUpdater.setListeningPosition(setting);
        });
    }

    /**
     * タイムアライメントモード設定トグル処理.
     * <p>
     * タイムアライメントプリセット設定に使用する。
     * トグル処理後のタイムアライメントモードがAutoTaでAutoTa設定が無効な場合はスキップする。
     * タイムアライメントモード(INITIAL,AUTO_TA,CUSTOM,OFF)を切り替える
     * SmallCarTAの設定をOFFにする。
     *
     * @throws NullPointerException {@code setting}がnull
     */
    public void toggleTimeAlignmentMode() {

        mHandler.post(() -> {

            TimeAlignmentSettingMode mode = mStatusHolder.getAudioSetting().timeAlignmentSetting.mode.toggle();
            // 変更後の設定モードがAUTO_TAで、AUTO_TA設定が無効の場合はスキップする
            if(mode == TimeAlignmentSettingMode.AUTO_TA && !mStatusHolder.getAudioSettingStatus().timeAlignmentPresetAtaEnabled){
                mode = mode.toggle();
            }

            mAudioSettingUpdater.setTimeAlignmentMode(mode);
        });
    }

    /**
     * タイムアライメント値設定.
     * <p>
     * タイムアライメント値設定が無効な場合は何もしない。
     * 設定対象がサブウーファーでサブウーファー設定がOFFの場合は何もしない。
     * タイムアライメントモードがカスタム以外の場合はカスタムに設定する。
     * SmallCarTAの設定をOFFにする。
     *
     * @param type スピーカー位置
     * @param step ステップ値
     * @throws NullPointerException {@code type}がnull
     */
    public void setTimeAlignment(@NonNull MixedSpeakerType type, int step) {
        checkNotNull(type);

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().timeAlignmentSettingEnabled) {
                Timber.w("setTimeAlignment() Time alignment setting disabled.");
                return;
            }

            if (type == MixedSpeakerType.SUBWOOFER &&
                    mStatusHolder.getAudioSetting().subwooferSetting != SubwooferSetting.ON) {
                Timber.w("setTimeAlignment() Subwoofer setting unchangeable.");
                return;
            }

            TimeAlignmentSetting currentSetting = mStatusHolder.getAudioSetting().timeAlignmentSetting;
            if (currentSetting.mode != TimeAlignmentSettingMode.CUSTOM) {
                // TA設定がカスタム以外の場合はカスタムへ変更
                mAudioSettingUpdater.setTimeAlignmentMode(TimeAlignmentSettingMode.CUSTOM);
            }

            mAudioSettingUpdater.setTimeAlignment(type, step);
        });
    }

    /**
     * スピーカー出力レベル設定.
     * <p>
     * スピーカー出力レベル設定が無効の場合は何もしない。
     * 設定対象がサブウーファーでサブウーファー設定がOFFの場合は何もしない。
     *
     * @param type  スピーカー位置
     * @param level レベル値
     * @throws NullPointerException {@code type}がnull
     */
    public void setSpeakerLevel(@NonNull MixedSpeakerType type, int level) {
        checkNotNull(type);

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().speakerLevelSettingEnabled) {
                Timber.w("setSpeakerLevel() Speaker level setting disabled.");
                return;
            }

            if (type == MixedSpeakerType.SUBWOOFER &&
                    mStatusHolder.getAudioSetting().subwooferSetting != SubwooferSetting.ON) {
                Timber.w("setSpeakerLevel() Subwoofer setting unchangeable.");
                return;
            }

            mAudioSettingUpdater.setSpeakerLevel(type, level);
        });
    }

    /**
     * ハイパス/ローパスフィルター設定トグル処理.
     * <p>
     * クロスオーバー設定が無効の場合は何もしない。
     * 設定対象がサブウーファーでサブウーファー設定がOFFの場合は何もしない。
     * ハイパス/ローパスフィルター設定(ON,OFF)を切り替える
     *
     * @param type スピーカー種別
     * @throws NullPointerException {@code type}がnull
     */
    public void toggleCrossoverHpfLpf(@NonNull SpeakerType type) {
        checkNotNull(type);

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().crossoverSettingEnabled) {
                Timber.w("toggleCrossoverHpfLpf() Crossover setting disabled.");
                return;
            }

            if ((type == SpeakerType.SUBWOOFER_STANDARD_MODE ||
                    type == SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE) &&
                    mStatusHolder.getAudioSetting().subwooferSetting != SubwooferSetting.ON) {
                Timber.w("toggleCrossoverHpfLpf() Subwoofer setting unchangeable.");
                return;
            }

            boolean isOn;
            HpfLpfSetting setting = mStatusHolder.getAudioSetting().crossoverSetting.findSpeakerCrossoverSetting(type).hpfLpfSetting;
            switch(setting.toggle()){
                case OFF:
                    isOn = false;
                    break;
                case ON:
                    isOn = true;
                    break;
                case OFF_FIXED:
                case ON_FIXED:
                default:
                    Timber.w("toggleCrossoverHpfLpf() can't happen.");
                    return;
            }
            mAudioSettingUpdater.setCrossoverHpfLpf(type, isOn);
        });
    }

    /**
     * カットオフ周波数設定.
     * <p>
     * クロスオーバー設定が無効の場合は何もしない。
     * 設定対象がサブウーファーでサブウーファー設定がOFFの場合は何もしない。
     *
     * @param type    スピーカー種別
     * @param setting 設定内容
     * @throws NullPointerException {@code type}がnull
     * @throws NullPointerException {@code setting}がnull
     */
    public void setCrossoverCutOff(@NonNull SpeakerType type, @NonNull CutoffSetting setting) {
        checkNotNull(type);
        checkNotNull(setting);

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().crossoverSettingEnabled) {
                Timber.w("setCrossoverCutOff() Crossover setting disabled.");
                return;
            }

            if ((type == SpeakerType.SUBWOOFER_STANDARD_MODE ||
                    type == SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE) &&
                    mStatusHolder.getAudioSetting().subwooferSetting != SubwooferSetting.ON) {
                Timber.w("setCrossoverCutOff() Subwoofer setting unchangeable.");
                return;
            }

            mAudioSettingUpdater.setCrossoverCutOff(type, setting);
        });
    }

    /**
     * スロープ設定.
     * <p>
     * クロスオーバー設定が無効の場合は何もしない。
     * 設定対象がサブウーファーでサブウーファー設定がOFFの場合は何もしない。
     *
     * @param type    スピーカー種別
     * @param setting 設定内容
     * @throws NullPointerException {@code type}がnull
     * @throws NullPointerException {@code setting}がnull
     */
    public void setCrossoverSlope(@NonNull SpeakerType type, @NonNull SlopeSetting setting) {
        checkNotNull(type);
        checkNotNull(setting);

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().crossoverSettingEnabled) {
                Timber.w("setCrossoverSlope() Crossover setting disabled.");
                return;
            }

            if ((type == SpeakerType.SUBWOOFER_STANDARD_MODE ||
                    type == SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE) &&
                    mStatusHolder.getAudioSetting().subwooferSetting != SubwooferSetting.ON) {
                Timber.w("setCrossoverSlope() Subwoofer setting unchangeable.");
                return;
            }

            mAudioSettingUpdater.setCrossoverSlope(type, setting);
        });
    }

    /**
     * サブウーファー設定トグル処理.
     * <p>
     * サブウーファー設定が無効の場合は何もしない。
     * サブウーファー設定(ON,OFF)を切り替える。
     */
    public void toggleSubWoofer() {

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().subwooferSettingEnabled) {
                Timber.w("toggleSubWoofer() Subwoofer setting disabled.");
                return;
            }

            SubwooferSetting setting = mStatusHolder.getAudioSetting().subwooferSetting;
            mAudioSettingUpdater.setSubWoofer(setting.toggle());
        });
    }

    /**
     * サブウーファー位相設定トグル処理.
     * <p>
     * サブウーファー位相設定が無効の場合は何もしない。
     * 位相設定(ノーマル,リバース)を切り替える
     */
    public void toggleSubWooferPhase() {

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().subwooferPhaseSettingEnabled) {
                Timber.w("toggleSubWooferPhase() Subwoofer phase setting disabled.");
                return;
            }

            if(mStatusHolder.getAudioSetting().subwooferSetting != SubwooferSetting.ON){
                Timber.w("toggleSubWooferPhase() Subwoofer setting unchangeable.");
                return;
            }

            SubwooferPhaseSetting setting = mStatusHolder.getAudioSetting().subwooferPhaseSetting;
            mAudioSettingUpdater.setSubWooferPhase(setting.toggle());
        });
    }

    /**
     * Sound Retriever設定トグル処理.
     * <p>
     * Sound Retriever設定が無効の場合は何もしない。
     * Sound Retriever設定を切り替える
     */
    public void toggleSoundRetriever() {

        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().soundRetrieverSettingEnabled) {
                Timber.w("toggleSoundRetriever() Sound retriever setting disabled.");
                return;
            }

            SoundRetrieverSetting setting = mStatusHolder.getAudioSetting().soundRetrieverSetting;
            mAudioSettingUpdater.setSoundRetriever(setting.toggle());
        });
    }

    /**
     * Save Setting.
     * <p>
     * save settingが無効な場合は何もしない
     */
    public void saveAudioSetting(@NonNull LoadSaveCallback callback){
        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().saveSettingEnabled) {
                Timber.w("saveAudioSetting() save setting disabled.");
                return;
            }

            mAudioSettingUpdater.saveAudioSetting(callback);
        });
    }

    /**
     * Load Setting.
     * <p>
     * load settingが無効な場合は何もしない
     */
    public void loadAudioSetting(@NonNull LoadSaveCallback callback){
        mHandler.post(() -> {
            if (!mStatusHolder.getAudioSettingStatus().loadSettingEnabled) {
                Timber.w("loadAudioSetting() load setting disabled.");
                return;
            }

            mAudioSettingUpdater.loadAudioSetting(callback);
        });
    }

    /**
     * Audio設定 Load/Save Callback.
     */
    public interface LoadSaveCallback{
        /** Load or Save 成功. */
        @UiThread
        void onSuccess();

        /** Load or Save 失敗. */
        @UiThread
        void onError();
    }
}
