package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * オーディオ設定ステータス.
 */
public class AudioSettingStatus extends SerialVersion {
    /** Listen position 設定有効. */
    public boolean listeningPositionSettingEnabled;
    /** Crossover 設定有効. */
    public boolean crossoverSettingEnabled;
    /** Speaker level 設定有効. */
    public boolean speakerLevelSettingEnabled;
    /** Subwoofer  phase 設定有効. */
    public boolean subwooferPhaseSettingEnabled;
    /** Subwoofer on/off 設定有効. */
    public boolean subwooferSettingEnabled;
    /** Balance 設定有効. */
    public boolean balanceSettingEnabled;
    /** Fader 設定有効. */
    public boolean faderSettingEnabled;
    /** EQ 設定有効. */
    public boolean equalizerSettingEnabled;
    /** SLA 設定有効. */
    public boolean slaSettingEnabled;
    /** ALC 設定有効. */
    public boolean alcSettingEnabled;
    /** Loudness 設定有効. */
    public boolean loudnessSettingEnabled;
    /** Bass booster 設定有効. */
    public boolean bassBoosterSettingEnabled;
    /** Load setting 設定有効. */
    public boolean loadSettingEnabled;
    /** Save setting 設定有効. */
    public boolean saveSettingEnabled;
    /** AEQ on/off 設定有効. */
    public boolean aeqSettingEnabled;
    /** Time alignment 設定有効. */
    public boolean timeAlignmentSettingEnabled;
    /**	SOUND RETRIEVER設定有効. */
    public boolean soundRetrieverSettingEnabled;
    /**	LEVEL設定有効. */
    public boolean levelSettingEnabled;
    /** Beat Blaster 設定有効. */
    public boolean beatBlasterSettingEnabled;
    /** オーディオデータ一括設定有効. */
    public boolean audioDataBulkSettingEnabled;
    /** Rear Speaker選択可能. */
    public boolean rearSpeakerEnabled;
    /** Subwoofer speaker選択可能. */
    public boolean subwooferSpeakerEnabled;
    /** Time Alignment プリセットATA選択可能. */
    public boolean timeAlignmentPresetAtaEnabled;
    /** Save setting有効. */
    public boolean saveSettingEnabled2;
    /** Load AEQ setting有効. */
    public boolean loadAeqSettingEnabled;
    /** Load sound setting有効. */
    public boolean loadSoundSettingEnabled;

    /**
     * コンストラクタ.
     */
    public AudioSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        listeningPositionSettingEnabled = false;
        crossoverSettingEnabled = false;
        speakerLevelSettingEnabled = false;
        subwooferPhaseSettingEnabled = false;
        subwooferSettingEnabled = false;
        balanceSettingEnabled = false;
        faderSettingEnabled = false;
        equalizerSettingEnabled = false;
        slaSettingEnabled = false;
        alcSettingEnabled = false;
        loudnessSettingEnabled = false;
        bassBoosterSettingEnabled = false;
        loadSettingEnabled = false;
        saveSettingEnabled = false;
        aeqSettingEnabled = false;
        timeAlignmentSettingEnabled = false;
        soundRetrieverSettingEnabled = false;
        levelSettingEnabled = false;
        beatBlasterSettingEnabled = false;
        audioDataBulkSettingEnabled = false;
        rearSpeakerEnabled = false;
        subwooferSpeakerEnabled = false;
        timeAlignmentPresetAtaEnabled = false;
        saveSettingEnabled2 = false;
        loadAeqSettingEnabled = false;
        loadSoundSettingEnabled = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("listeningPositionSettingEnabled", listeningPositionSettingEnabled)
                .add("crossoverSettingEnabled", crossoverSettingEnabled)
                .add("speakerLevelSettingEnabled", speakerLevelSettingEnabled)
                .add("subwooferPhaseSettingEnabled", subwooferPhaseSettingEnabled)
                .add("subwooferSettingEnabled", subwooferSettingEnabled)
                .add("balanceSettingEnabled", balanceSettingEnabled)
                .add("faderSettingEnabled", faderSettingEnabled)
                .add("equalizerSettingEnabled", equalizerSettingEnabled)
                .add("slaSettingEnabled", slaSettingEnabled)
                .add("alcSettingEnabled", alcSettingEnabled)
                .add("loudnessSettingEnabled", loudnessSettingEnabled)
                .add("bassBoosterSettingEnabled", bassBoosterSettingEnabled)
                .add("loadSettingEnabled", loadSettingEnabled)
                .add("saveSettingEnabled", saveSettingEnabled)
                .add("aeqSettingEnabled", aeqSettingEnabled)
                .add("timeAlignmentSettingEnabled", timeAlignmentSettingEnabled)
                .add("soundRetrieverSettingEnabled", soundRetrieverSettingEnabled)
                .add("levelSettingEnabled", levelSettingEnabled)
                .add("beatBlasterSettingEnabled", beatBlasterSettingEnabled)
                .add("audioDataBulkSettingEnabled", audioDataBulkSettingEnabled)
                .add("rearSpeakerEnabled", rearSpeakerEnabled)
                .add("subwooferSpeakerEnabled", subwooferSpeakerEnabled)
                .add("timeAlignmentPresetAtaEnabled", timeAlignmentPresetAtaEnabled)
                .add("saveSettingEnabled2", saveSettingEnabled2)
                .add("loadAeqSettingEnabled", loadAeqSettingEnabled)
                .add("loadSoundSettingEnabled", loadSoundSettingEnabled)
                .toString();
    }
}
