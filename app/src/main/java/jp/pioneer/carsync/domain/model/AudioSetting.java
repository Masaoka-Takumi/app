package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * オーディオ設定.
 */
public class AudioSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** イコライザー設定. */
    public EqualizerSetting equalizerSetting = new EqualizerSetting();
    /** Fader/Balance設定. */
    public FaderBalanceSetting faderBalanceSetting = new FaderBalanceSetting();
    /** Subwoofer設定値. */
    public SubwooferSetting subwooferSetting;
    /** Subwoofer位相設定値. */
    public SubwooferPhaseSetting subwooferPhaseSetting;
    /** Speaker Level設定. */
    public SpeakerLevelSetting speakerLevelSetting = new SpeakerLevelSetting();
    /** Crossover設定. */
    public CrossoverSetting crossoverSetting = new CrossoverSetting();
    /** Listening Position設定. */
    public ListeningPositionSetting listeningPositionSetting;
    /** Time Alignment設定. */
    public TimeAlignmentSetting timeAlignmentSetting = new TimeAlignmentSetting();
    /** Auto EQ補正設定. */
    public AutoEqCorrectionSetting autoEqCorrectionSetting;
    /** BASS BOOSTERレベル設定. */
    public BassBoosterSetting bassBoosterSetting = new BassBoosterSetting();
    /** LOUDNESS設定情報. */
    public LoudnessSetting loudnessSetting;
    /** ALC設定. */
    public AlcSetting alcSetting;
    /** SLA設定. */
    public SlaSetting slaSetting = new SlaSetting();
    /** Beat Blaster設定. */
    public BeatBlasterSetting beatBlasterSetting;
    /** カスタムイコライザー設定. */
    public CustomEqualizerSetting customEqualizerSetting = new CustomEqualizerSetting();
    /** SOUND RETRIEVER設定. */
    public SoundRetrieverSetting soundRetrieverSetting;

    /**
     * コンストラクタ.
     */
    public AudioSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        equalizerSetting.reset();
        faderBalanceSetting.reset();
        subwooferSetting = SubwooferSetting.OFF;
        subwooferPhaseSetting = SubwooferPhaseSetting.NORMAL;
        speakerLevelSetting.reset();
        crossoverSetting.reset();
        listeningPositionSetting = ListeningPositionSetting.OFF;
        timeAlignmentSetting.reset();
        autoEqCorrectionSetting = AutoEqCorrectionSetting.OFF;
        bassBoosterSetting.reset();
        loudnessSetting = LoudnessSetting.OFF;
        alcSetting = AlcSetting.OFF;
        slaSetting.reset();
        beatBlasterSetting = BeatBlasterSetting.OFF;
        customEqualizerSetting.reset();
        soundRetrieverSetting = SoundRetrieverSetting.OFF;
        clear();
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("requestStatus", requestStatus)
                .add("equalizerSetting", equalizerSetting)
                .add("faderBalanceSetting", faderBalanceSetting)
                .add("subwooferSetting", subwooferSetting)
                .add("subwooferPhaseSetting", subwooferPhaseSetting)
                .add("speakerLevelSetting", speakerLevelSetting)
                .add("crossoverSetting", crossoverSetting)
                .add("listeningPositionSetting", listeningPositionSetting)
                .add("timeAlignmentSetting", timeAlignmentSetting)
                .add("autoEqCorrectionSetting", autoEqCorrectionSetting)
                .add("bassBoosterSetting", bassBoosterSetting)
                .add("loudnessSetting", loudnessSetting)
                .add("alcSetting", alcSetting)
                .add("slaSetting", slaSetting)
                .add("beatBlasterSetting", beatBlasterSetting)
                .add("customEqualizerSetting", customEqualizerSetting)
                .add("soundRetrieverSetting", soundRetrieverSetting)
                .toString();
    }

    /**
     * タグ.
     */
    public static class Tag {
        public static final String EQUALIZER = "equalizer";
        public static final String FADER_BALANCE = "fader_balance";
        public static final String SUBWOOFER = "subwoofer";
        public static final String SUBWOOFER_PHASE = "subwoofer_phase";
        public static final String SPEAKER_LEVEL = "speaker_level";
        public static final String CROSSOVER = "crossover";
        public static final String LISTENING_POSITION = "listening_position";
        public static final String TIME_ALIGNMENT = "time_alignment";
        public static final String AUTO_EQ_CORRECTION = "auto_eq_correction";
        public static final String BASS_BOOSTER = "bass_booster";
        public static final String LOUDNESS = "loudness";
        public static final String ALC = "alc";
        public static final String SLA = "sla";
        public static final String BEAT_BLASTER = "beat_blaster";
        public static final String LEVEL = "level";
        public static final String SOUND_RETRIEVER = "sound_retriever";

        /**
         * 全てのタグを取得.
         *
         * @return 全てのタグ名
         */
        public static List<String> getAllTags(){
            return new ArrayList<String>(){{
                add(EQUALIZER);
                add(FADER_BALANCE);
                add(SUBWOOFER);
                add(SUBWOOFER_PHASE);
                add(SPEAKER_LEVEL);
                add(CROSSOVER);
                add(LISTENING_POSITION);
                add(TIME_ALIGNMENT);
                add(AUTO_EQ_CORRECTION);
                add(BASS_BOOSTER);
                add(LOUDNESS);
                add(ALC);
                add(SLA);
                add(BEAT_BLASTER);
                add(LEVEL);
                add(SOUND_RETRIEVER);
            }};
        }
    }
}
