package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.AppMusicSourceController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.SoundFxSettingUpdater;
import jp.pioneer.carsync.domain.component.AudioSettingUpdater;
import jp.pioneer.carsync.domain.event.EqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.AudioSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundEffectType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Sound FX設定.
 * <p>
 * 競合する設定については本クラスではチェックしないため
 * UI層で確認を実施する
 */
public class PreferSoundFx {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject SoundFxSettingUpdater mSoundFxSettingUpdater;
    @Inject AudioSettingUpdater mAudioSettingUpdater;
    private AppMusicSourceController mAppMusicSourceController;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferSoundFx(CarDevice carDevice) {
        mAppMusicSourceController = (AppMusicSourceController) carDevice.getSourceController(MediaSourceType.APP_MUSIC);
    }

    /**
     * Custom Equalizer設定.
     * <p>
     * Equalizer設定が無効な場合何もしない。
     * 本来はAudio設定に含むべきだが、
     * 設定の項目上Sound FX設定となっているため本クラスに実装。
     * <p>
     * MLE向けから車載機向けのフォーマットに変換(31bandから13bandへ変換)し、
     * 車載機へ通知する。
     * アプリで保持しているCustom Equalizer設定を更新する。
     * <p>
     * MLEへの反映は、車載機の反映が問題ない場合に発行される{@link EqualizerSettingChangeEvent}
     * を受け取った時にMLEへ反映する。
     *
     * @param type CUSTOM種別
     * @param bands 31band値
     * @throws NullPointerException     {@code type}、{@code bands}がnull
     * @throws IllegalArgumentException {@code bands}のサイズが31ではない
     */
    public void setCustomBand(@NonNull CustomEqType type, @NonNull @Size(31) float[] bands) {
        checkNotNull(type);
        checkArgument(checkNotNull(bands).length == 31);

        mHandler.post(() -> {
            AudioSettingStatus status = mStatusHolder.getAudioSettingStatus();

            if(!status.equalizerSettingEnabled){
                Timber.w("setCustomBand() equalizer setting disabled.");
                return;
            }

            int[] carDeviceBands = mAppMusicSourceController.convertBandValue(bands);

            // 変換に使用するプレイヤーが有効ではない
            if(carDeviceBands == null){
                return;
            }

            SoundFxSetting soundFxSetting = mStatusHolder.getSoundFxSetting();
            if (type == CustomEqType.CUSTOM1) {
                soundFxSetting.customBandSettingA.bands = bands;
                mPreference.setCustomBandSettingA(soundFxSetting.customBandSettingA);
            } else {
                soundFxSetting.customBandSettingB.bands = bands;
                mPreference.setCustomBandSettingB(soundFxSetting.customBandSettingB);
            }

            mAudioSettingUpdater.setCustomBand(type, carDeviceBands);
        });
    }

    /**
     * EQ設定.
     * <p>
     * Equalizer設定が無効な場合は何もしない。
     * 本来はAudio設定に含むべきだが、
     * 設定の項目上Sound FX設定となっているため本クラスに実装。
     * <p>
     * 車載機にイコライザのプリセットを通知する。
     * {@code type}がSPECIAL EQ系の場合とPRESET EQ系の場合で通知先が異なる。
     * <p>
     * MLEへの反映は、車載機の反映が問題ない場合に発行される{@link EqualizerSettingChangeEvent}
     * を受け取った時にMLEへ反映する。
     *
     * @param type UI向けイコライザー種別
     * @throws NullPointerException {@code type}がnull
     */
    public void setEqualizer(@NonNull SoundFxSettingEqualizerType type) {
        checkNotNull(type);

        mHandler.post(() -> {
            AudioSettingStatus status = mStatusHolder.getAudioSettingStatus();
            SoundFxSetting setting = mStatusHolder.getSoundFxSetting();

            if(!status.equalizerSettingEnabled){
                Timber.w("setEqualizer() equalizer setting disabled.");
                return;
            }

            if(setting.soundFxSettingEqualizerType == type){
                if(setting.liveSimulationSetting.soundFieldControlSettingType != SoundFieldControlSettingType.OFF){
                    mSoundFxSettingUpdater.setLiveSimulation(SoundFieldControlSettingType.OFF, SoundEffectSettingType.OFF);
                } else if(setting.superTodorokiSetting != SuperTodorokiSetting.OFF){
                    mSoundFxSettingUpdater.setSuperTodoroki(SuperTodorokiSetting.OFF);
                }
            } else {
                if(type.code >= (1 << 8)) {
                    // SPECIAL EQ
                    float[] bands = mStatusHolder.getSoundFxSetting().getEqualizerBandArray(type);
                    int[] carDeviceBands = mAppMusicSourceController.convertBandValue(bands);
                    // 変換に使用するプレイヤーが有効ではない
                    if(carDeviceBands == null){
                        mAudioSettingUpdater.initPresetEq();
                        return;
                    }
                    mAudioSettingUpdater.setSpecialEqualizer(type.code & 0xFF, carDeviceBands);
                } else {
                    // PRESET EQ
                    mAudioSettingUpdater.setEqualizer(AudioSettingEqualizerType.valueOf((byte) type.code));
                }
            }
        });
    }

    /**
     * Super"轟"Sound設定.
     * <p>
     * Super 轟設定が無効な場合は何もしない
     *
     * @param setting 設定
     * @throws NullPointerException {@code setting}がnull
     */
    public void setSuperTodoroki(@NonNull SuperTodorokiSetting setting) {
        checkNotNull(setting);

        mHandler.post(() -> {
            SoundFxSettingStatus status = mStatusHolder.getSoundFxSettingStatus();

            if(!status.superTodorokiSettingEnabled){
                Timber.w("setSuperTodoroki() super todoroki setting disabled.");
                return;
            }

            mSoundFxSettingUpdater.setSuperTodoroki(setting);
        });
    }

    /**
     * Small Car TA設定.
     * <p>
     * Small Car TA設定が無効な場合は何もしない
     *
     * @param type Small Car TA設定
     * @param position Small Car TA設定
     * @throws NullPointerException {@code setting}がnull
     */
    public void setSmallCarTa(@NonNull SmallCarTaSettingType type, @NonNull ListeningPosition position) {
        checkNotNull(type);
        checkNotNull(position);

        mHandler.post(() -> {
            SoundFxSettingStatus status = mStatusHolder.getSoundFxSettingStatus();

            if(!status.smallCarTaSettingEnabled){
                Timber.w("setSmallCarTa() small car ta setting disabled.");
                return;
            }

            mSoundFxSettingUpdater.setSmallCarTa(type, position);
        });
    }

    /**
     * Live Simulation設定.
     * <p>
     * Live Simulation設定が無効な場合は何もしない。
     *
     * @param fieldType 音場設定
     * @param effectType エフェクト設定
     * @throws NullPointerException {@code fieldType}、{@code effectType}がnull
     */
    public void setLiveSimulation(@NonNull SoundFieldControlSettingType fieldType, @NonNull SoundEffectType effectType) {
        checkNotNull(fieldType);
        checkNotNull(effectType);

        mHandler.post(() -> {
            SoundFxSettingStatus status = mStatusHolder.getSoundFxSettingStatus();

            if(!status.liveSimulationSettingEnabled){
                Timber.w("setLiveSimulation() live simulation setting disabled.");
                return;
            }

            SoundEffectType effect = fieldType == SoundFieldControlSettingType.OFF ? SoundEffectType.OFF : effectType;

            mSoundFxSettingUpdater.setLiveSimulation(fieldType, fieldType.getEnableSoundEffectSettingType(effect));
        });
    }

    /**
     * カラオケ設定.
     * <p>
     * カラオケ設定が無効な場合は何もしない
     *
     * @param enabled カラオケ設定がONか否か
     */
    public void setKaraokeSetting(boolean enabled){
        mHandler.post(() -> {
            SoundFxSettingStatus status = mStatusHolder.getSoundFxSettingStatus();

            if(!status.karaokeSettingEnabled){
                Timber.w("setKaraokeSetting() karaoke setting disabled.");
                return;
            }

            mSoundFxSettingUpdater.setKaraokeSetting(enabled);
        });
    }

    /**
     * マイク音量設定.
     * <p>
     * マイク音量設定が無効な場合は何もしない
     *
     * @param volume 音量
     */
    public void setMicVolume(int volume){
        mHandler.post(() -> {
            SoundFxSettingStatus status = mStatusHolder.getSoundFxSettingStatus();

            if(!status.karaokeSettingEnabled){
                Timber.w("setMicVolume() karaoke setting disabled.");
                return;
            }

            mSoundFxSettingUpdater.setMicVolume(volume);
        });
    }

    /**
     * Vocal Cancel設定.
     * <p>
     * Vocal Cancel設定が無効な場合は何もしない
     *
     * @param enabled Vocal Cancel設定がONか否か
     */
    public void setVocalCancel(boolean enabled){
        mHandler.post(() -> {
            SoundFxSettingStatus status = mStatusHolder.getSoundFxSettingStatus();

            if(!status.karaokeSettingEnabled){
                Timber.w("setVocalCancel() karaoke setting disabled.");
                return;
            }

            mSoundFxSettingUpdater.setVocalCancel(enabled);
        });
    }
}
