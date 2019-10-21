package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Sound FX設定.
 */
public class SoundFxSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** Super"轟"設定. */
    public SuperTodorokiSetting superTodorokiSetting;
    /** Small Car TA設定. */
    public SmallCarTaSetting smallCarTaSetting = new SmallCarTaSetting();
    /** ライブシミュレーション設定. */
    public LiveSimulationSetting liveSimulationSetting = new LiveSimulationSetting();
    /** カラオケ設定ON. */
    public boolean karaokeSetting;
    /** マイク音量設定. */
    public MicVolumeSetting micVolumeSetting = new MicVolumeSetting();
    /** Vocal Cancel設定. */
    public boolean vocalCancelSetting;
    /** イコライザー種別. */
    public SoundFxSettingEqualizerType soundFxSettingEqualizerType;
    /** カスタムバンドA設定. */
    public CustomBandSetting customBandSettingA = new CustomBandSetting(CustomEqType.CUSTOM1);
    /** カスタムバンドB設定. */
    public CustomBandSetting customBandSettingB = new CustomBandSetting(CustomEqType.CUSTOM2);

    /**
     * コンストラクタ.
     */
    public SoundFxSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        superTodorokiSetting = SuperTodorokiSetting.OFF;
        smallCarTaSetting.reset();
        liveSimulationSetting.reset();
        karaokeSetting = false;
        micVolumeSetting.reset();
        vocalCancelSetting = false;
        soundFxSettingEqualizerType = SoundFxSettingEqualizerType.FLAT;
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
                .add("superTodorokiSetting", superTodorokiSetting)
                .add("smallCarTaSetting", smallCarTaSetting)
                .add("liveSimulationSetting", liveSimulationSetting)
                .add("karaokeSetting", karaokeSetting)
                .add("micVolumeSetting", micVolumeSetting)
                .add("vocalCancelSetting", vocalCancelSetting)
                .add("soundFxSettingEqualizerType", soundFxSettingEqualizerType)
                .add("customBandSettingA", customBandSettingA)
                .add("customBandSettingB", customBandSettingB)
                .toString();
    }

    /**
     * 設定されているイコライザー種別の31Band値取得
     */
    @NonNull
    @Size(31)
    public float[] getEqualizerBandArray(@NonNull SoundFxSettingEqualizerType type){
        checkNotNull(type);

        if(type == SoundFxSettingEqualizerType.COMMON_CUSTOM){
            return customBandSettingA.bands;
        } else if(type == SoundFxSettingEqualizerType.COMMON_CUSTOM_2ND){
            return customBandSettingB.bands;
        } else {
            return EqualizerBandMap.getBandValue(type);
        }
    }

    /**
     * 設定されているイコライザー種別の31Band値取得
     */
    @NonNull
    @Size(31)
    public ArrayList<Float> getEqualizerBandList(@NonNull SoundFxSettingEqualizerType type){
        checkNotNull(type);

        float[] bands;
        if(type == SoundFxSettingEqualizerType.COMMON_CUSTOM){
            bands = customBandSettingA.bands;
        } else if(type == SoundFxSettingEqualizerType.COMMON_CUSTOM_2ND){
            bands = customBandSettingB.bands;
        } else {
            bands = EqualizerBandMap.getBandValue(type);
        }

        ArrayList<Float> bandList = new ArrayList<>();
        for (float band : bands) {
            bandList.add(band);
        }

        return bandList;
    }

    /**
     * タグ.
     */
    public static class Tag {
        public static final String SUPER_TODOROKI = "super_todoroki";
        public static final String SMALL_CAR_TA = "small_car_ta";
        public static final String LIVE_SIMULATION = "live_simulation";
        public static final String KARAOKE = "karaoke";
        public static final String MIC_VOLUME = "mic_volume";
        public static final String VOCAL_CANCEL = "vocal_cancel";

        /**
         * 全てのタグを取得.
         *
         * @return 全てのタグ名
         */
        public static List<String> getAllTags(){
            return new ArrayList<String>(){{
                add(SUPER_TODOROKI);
                add(SMALL_CAR_TA);
                add(LIVE_SIMULATION);
                add(KARAOKE);
                add(MIC_VOLUME);
                add(VOCAL_CANCEL);
            }};
        }
    }
}


