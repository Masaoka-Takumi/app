package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * オーディオ設定スペック
 * 「サポートオーディオ設定1-4」「オーディオ設定その他1-2」をCarDeviceSpecから分離
 * Created by tsuyosh on 15/11/24.
 */
public class AudioSettingSpec {
    /** サポートオーディオ設定1:Listening position設定の対応有無 */
    public boolean listeningPositionSettingSupported;

    /** サポートオーディオ設定1:Crossover 設定の対応有無 */
    public boolean crossoverSettingSupported;

    /** サポートオーディオ設定1:Speaker level 設定の対応有無 */
    public boolean speakerLevelSettingSupported;

    /** サポートオーディオ設定1:Subwoofer  phase 設定の対応有無 */
    public boolean subwooferPhaseSettingSupported;

    /** サポートオーディオ設定1:Subwoofer on/off 設定の対応有無 */
    public boolean subwooferSettingSupported;

    /** サポートオーディオ設定1:Balance 設定の対応有無 */
    public boolean balanceSettingSupported;

    /** サポートオーディオ設定1:Fader 設定の対応有無 */
    public boolean faderSettingSupported;

    /** サポートオーディオ設定1:EQ 設定の対応有無 */
    public boolean equalizerSettingSupported;


    /** サポートオーディオ設定2:SLA 設定の対応有無 */
    public boolean slaSettingSupported;

    /** サポートオーディオ設定2:ALC 設定の対応有無 */
    public boolean alcSettingSupported;

    /** サポートオーディオ設定2:Loudness 設定の対応有無 */
    public boolean loudnessSettingSupported;

    /** サポートオーディオ設定2:Bass booster 設定の対応有無 */
    public boolean bassBoosterSettingSupported;

    /** サポートオーディオ設定2:Load setting 設定の対応有無 */
    public boolean loadSettingSupported;

    /** サポートオーディオ設定2:Save setting 設定の対応有無 */
    public boolean saveSettingSupported;

    /** サポートオーディオ設定2:AEQ on/off 設定の対応有無 */
    public boolean aeqSettingSupported;

    /** サポートオーディオ設定2:Time alignment 設定の対応有無 */
    public boolean timeAlignmentSettingSupported;


    /** サポートオーディオ設定3:オーディオデータ一括設定の対応有無 */
    public boolean audioDataBulkUpdateSupported;

    /** サポートオーディオ設定3:LEVEL設定の対応有無. */
    public boolean levelSettingSupported;

    /** サポートオーディオ設定3:Beat Blaster設定の対応有無. */
    public boolean beatBlasterSettingSupported;

    /** サポートオーディオ設定3:SOUND RETRIEVER設定の対応有無. */
    public boolean soundRetrieverSettingSupported;


    /** オーディオ設定その他1:Load sound setting対応有無 */
    public boolean loadSoundSettingSupported;

    /** オーディオ設定その他1:Load AEQ setting対応有無 */
    public boolean loadAeqSettingSupported;

    /** オーディオ設定その他1:Time alignmentプリセット ATA対応有無 */
    public boolean timeAlignmentPresetAtaSupported;

    /** オーディオ設定その他1:PRESET EQ 仕向け */
    public PresetEqualizerVariation presetEqualizerVariation;

    /** オーディオ設定その他1:Audio output mode */
    public AudioOutputMode audioOutputMode;

    public void reset() {
        listeningPositionSettingSupported = false;
        crossoverSettingSupported = false;
        speakerLevelSettingSupported = false;
        subwooferPhaseSettingSupported = false;
        subwooferSettingSupported = false;
        balanceSettingSupported = false;
        faderSettingSupported = false;
        equalizerSettingSupported = false;
        slaSettingSupported = false;
        alcSettingSupported = false;
        loudnessSettingSupported = false;
        bassBoosterSettingSupported = false;
        loadSettingSupported = false;
        saveSettingSupported = false;
        aeqSettingSupported = false;
        timeAlignmentSettingSupported = false;
        audioDataBulkUpdateSupported = false;
        loadSoundSettingSupported = false;
        loadAeqSettingSupported = false;
        timeAlignmentPresetAtaSupported = false;
        presetEqualizerVariation = PresetEqualizerVariation.OTHER;
        audioOutputMode = AudioOutputMode.STANDARD;
        levelSettingSupported = false;
        beatBlasterSettingSupported = false;
        soundRetrieverSettingSupported = false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("listeningPositionSettingSupported", listeningPositionSettingSupported)
                .add("crossoverSettingSupported", crossoverSettingSupported)
                .add("speakerLevelSettingSupported", speakerLevelSettingSupported)
                .add("subwooferPhaseSettingSupported", subwooferPhaseSettingSupported)
                .add("subwooferSettingSupported", subwooferSettingSupported)
                .add("balanceSettingSupported", balanceSettingSupported)
                .add("faderSettingSupported", faderSettingSupported)
                .add("equalizerSettingSupported", equalizerSettingSupported)
                .add("slaSettingSupported", slaSettingSupported)
                .add("alcSettingSupported", alcSettingSupported)
                .add("loudnessSettingSupported", loudnessSettingSupported)
                .add("bassBoosterSettingSupported",bassBoosterSettingSupported )
                .add("loadSettingSupported", loadSettingSupported)
                .add("saveSettingSupported", saveSettingSupported)
                .add("aeqSettingSupported", aeqSettingSupported)
                .add("timeAlignmentSettingSupported", timeAlignmentSettingSupported)
                .add("audioDataBulkUpdateSupported", audioDataBulkUpdateSupported)
                .add("loadAeqSettingSupported", loadAeqSettingSupported)
                .add("timeAlignmentPresetAtaSupported", timeAlignmentPresetAtaSupported)
                .add("presetEqualizerVariation", presetEqualizerVariation)
                .add("audioOutputMode", audioOutputMode)
                .add("levelSettingSupported", levelSettingSupported)
                .add("beatBlasterSettingSupported", beatBlasterSettingSupported)
                .add("soundRetrieverSettingSupported", soundRetrieverSettingSupported)
                .toString();
    }
}
