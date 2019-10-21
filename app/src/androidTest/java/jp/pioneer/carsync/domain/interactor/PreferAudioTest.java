package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.AudioSettingUpdater;
import jp.pioneer.carsync.domain.component.SoundFxSettingUpdater;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.BeatBlasterSetting;
import jp.pioneer.carsync.domain.model.BeatBlasterSettingType;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CutoffSetting;
import jp.pioneer.carsync.domain.model.HpfLpfSetting;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.SlopeSetting;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.StandardCutoffSetting;
import jp.pioneer.carsync.domain.model.StandardSlopeSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubwooferPhaseSetting;
import jp.pioneer.carsync.domain.model.SubwooferSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSettingMode;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/28.
 */
//TODO 轟設定無効やSmallCarTa設定OFF等のSoundFxSettingUpdaterに関するテストは仕様分かり次第実装
@RunWith(Theories.class)
public class PreferAudioTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferAudio mPreferAudio;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock AudioSettingUpdater mAudioSettingUpdater;
    @Mock SoundFxSettingUpdater mSoundFxSettingUpdater;

    AudioSettingStatus mAudioSettingStatus;
    CarDeviceStatus mCarDeviceStatus;
    SoundFxSetting mSoundFxSetting;
    AudioSetting mAudioSetting;
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    @DataPoints
    public static final TimeAlignmentSettingMode[] TA_SETTING_MODES = TimeAlignmentSettingMode.values();

    @DataPoints
    public static final HpfLpfSetting[] HPF_LPF_SETTINGS = HpfLpfSetting.values();

    @DataPoints
    public static final SubwooferSetting[] SUBWOOFER_SETTINGS = SubwooferSetting.values();

    @DataPoints
    public static final SubwooferPhaseSetting[] SUBWOOFER_PHASE_SETTINGS = SubwooferPhaseSetting.values();

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        mAudioSettingStatus = new AudioSettingStatus();
        when(mStatusHolder.getAudioSettingStatus()).thenReturn(mAudioSettingStatus);
        mCarDeviceStatus = new CarDeviceStatus();
        mCarDeviceStatus.sourceType = MediaSourceType.RADIO;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        mSoundFxSetting = new SoundFxSetting();
        when(mStatusHolder.getSoundFxSetting()).thenReturn(mSoundFxSetting);
        mAudioSetting = new AudioSetting();
        mAudioSetting.timeAlignmentSetting.mode = TimeAlignmentSettingMode.CUSTOM;
        mAudioSetting.subwooferSetting = SubwooferSetting.ON;
        when(mStatusHolder.getAudioSetting()).thenReturn(mAudioSetting);
    }

    @Test
    public void setBeatBlaster_AudioSettingUpdater() throws Exception {
        // setup
        mAudioSettingStatus.beatBlasterSettingEnabled = true;

        // exercise
        mPreferAudio.setBeatBlaster(BeatBlasterSetting.HIGH);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setBeatBlaster(BeatBlasterSetting.HIGH);
    }

    @Test
    public void setBeatBlaster_AudioSettingUpdater_Disabled() throws Exception {
        // exercise
        mPreferAudio.setBeatBlaster(BeatBlasterSetting.HIGH);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setBeatBlaster(any(BeatBlasterSetting.class));
    }

    @Test
    public void setBeatBlaster_SoundFxSettingUpdater() throws Exception {
        // SoundFXの実装でき次第追加
    }

    @Test(expected = NullPointerException.class)
    public void setBeatBlaster_ArgNull() throws Exception {
        // exercise
        mPreferAudio.setBeatBlaster(null);
    }

    @Test
    public void setLoudness() throws Exception {
        // setup
        mAudioSettingStatus.loudnessSettingEnabled = true;

        // exercise
        mPreferAudio.setLoudness(LoudnessSetting.HIGH);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setLoudness(LoudnessSetting.HIGH);
    }

    @Test
    public void setLoudness_Disabled() throws Exception {
        // exercise
        mPreferAudio.setLoudness(LoudnessSetting.HIGH);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setLoudness(any(LoudnessSetting.class));
    }

    @Test(expected = NullPointerException.class)
    public void setLoudness_ArgNull() throws Exception {
        // exercise
        mPreferAudio.setLoudness(null);
    }

    @Test
    public void setSourceLevelAdjuster() throws Exception {
        // setup
        mAudioSettingStatus.slaSettingEnabled = true;

        // exercise
        mPreferAudio.setSourceLevelAdjuster(1);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setSourceLevelAdjuster(1);
    }

    @Test
    public void setSourceLevelAdjuster_Disabled() throws Exception {
        // exercise
        mPreferAudio.setSourceLevelAdjuster(1);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setSourceLevelAdjuster(anyInt());
    }

    @Test
    public void setFaderBalance() throws Exception {
        // setup
        mAudioSettingStatus.faderSettingEnabled = true;
        mAudioSettingStatus.balanceSettingEnabled = true;

        // exercise
        mPreferAudio.setFaderBalance(1,10);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setFaderBalance(1,10);
    }

    @Test
    public void setFaderBalance_FaderSettingDisabled() throws Exception {
        // setup
        mAudioSettingStatus.balanceSettingEnabled = true;

        // exercise
        mPreferAudio.setFaderBalance(1,10);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setFaderBalance(anyInt(),anyInt());
    }

    @Test
    public void setFaderBalance_BalanceSettingDisabled() throws Exception {
        // setup
        mAudioSettingStatus.faderSettingEnabled = true;

        // exercise
        mPreferAudio.setFaderBalance(1,10);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setFaderBalance(anyInt(),anyInt());
    }

    @Test
    public void setListeningPosition() throws Exception {
        // setup
        mAudioSettingStatus.listeningPositionSettingEnabled = true;

        // exercise
        mPreferAudio.setListeningPosition(ListeningPositionSetting.FRONT);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setListeningPosition(ListeningPositionSetting.FRONT);
    }

    @Test
    public void setListeningPosition_Disabled() throws Exception {
        // exercise
        mPreferAudio.setListeningPosition(ListeningPositionSetting.FRONT);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setListeningPosition(any(ListeningPositionSetting.class));
    }

    @Test(expected = NullPointerException.class)
    public void setListeningPosition_ArgNull() throws Exception {
        // exercise
        mPreferAudio.setListeningPosition(null);
    }

    @Theory
    public void toggleTimeAlignmentMode(TimeAlignmentSettingMode mode) throws Exception {
        // setup
        mAudioSetting.timeAlignmentSetting.mode = mode;
        mAudioSettingStatus.timeAlignmentPresetAtaEnabled = true;

        // exercise
        mPreferAudio.toggleTimeAlignmentMode();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setTimeAlignmentMode(mode.toggle());
    }

    @Theory
    public void toggleTimeAlignmentMode_AtaDisabled(TimeAlignmentSettingMode mode) throws Exception {
        // setup
        mAudioSetting.timeAlignmentSetting.mode = mode;
        mAudioSettingStatus.timeAlignmentPresetAtaEnabled = false;

        // exercise
        mPreferAudio.toggleTimeAlignmentMode();
        mSignal.await();

        // verify
        if(mode.toggle() == TimeAlignmentSettingMode.AUTO_TA){
            verify(mAudioSettingUpdater).setTimeAlignmentMode(TimeAlignmentSettingMode.AUTO_TA.toggle());
        }else {
            verify(mAudioSettingUpdater).setTimeAlignmentMode(mode.toggle());
        }
    }

    @Test
    public void setTimeAlignment() throws Exception {
        // setup
        mAudioSettingStatus.timeAlignmentSettingEnabled = true;

        // exercise
        mPreferAudio.setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 1);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 1);
    }

    @Test
    public void setTimeAlignment_Disabled() throws Exception {
        // exercise
        mPreferAudio.setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 1);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setTimeAlignment(any(MixedSpeakerType.class), anyInt());
    }

    @Test
    public void setTimeAlignment_OtherThanCustom() throws Exception {
        // setup
        mAudioSettingStatus.timeAlignmentSettingEnabled = true;
        mAudioSetting.timeAlignmentSetting.mode = TimeAlignmentSettingMode.INITIAL;

        // exercise
        mPreferAudio.setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 1);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setTimeAlignmentMode(TimeAlignmentSettingMode.CUSTOM);
        verify(mAudioSettingUpdater).setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 1);
    }

    @Test
    public void setTimeAlignment_SubWooferSettingStatusOff() throws Exception {
        // setup
        mAudioSettingStatus.timeAlignmentSettingEnabled = true;
        mAudioSetting.subwooferSetting = SubwooferSetting.OFF;

        // exercise
        mPreferAudio.setTimeAlignment(MixedSpeakerType.SUBWOOFER, 1);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setTimeAlignment(any(MixedSpeakerType.class), anyInt());
    }

    @Test(expected = NullPointerException.class)
    public void setTimeAlignment_ArgNull() throws Exception {
        // exercise
        mPreferAudio.setTimeAlignment(null, 1);
    }

    @Test
    public void setSpeakerLevel() throws Exception {
        // setup
        mAudioSettingStatus.speakerLevelSettingEnabled = true;

        // exercise
        mPreferAudio.setSpeakerLevel(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 1);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setSpeakerLevel(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 1);
    }

    @Test
    public void setSpeakerLevel_Disabled() throws Exception {
        // exercise
        mPreferAudio.setSpeakerLevel(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 1);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setSpeakerLevel(any(MixedSpeakerType.class), anyInt());
    }

    @Test
    public void setSpeakerLevel_SubWooferSettingStatusOff() throws Exception {
        // setup
        mAudioSettingStatus.speakerLevelSettingEnabled = true;
        mAudioSetting.subwooferSetting = SubwooferSetting.OFF;

        // exercise
        mPreferAudio.setSpeakerLevel(MixedSpeakerType.SUBWOOFER, 1);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setSpeakerLevel(any(MixedSpeakerType.class), anyInt());
    }

    @Test(expected = NullPointerException.class)
    public void setSpeakerLevel_ArgNull() throws Exception {
        // exercise
        mPreferAudio.setSpeakerLevel(null, 1);
    }

    @Theory
    public void toggleCrossoverHpfLpf(HpfLpfSetting setting) throws Exception {
        // setup
        mAudioSetting.crossoverSetting.findSpeakerCrossoverSetting(SpeakerType.FRONT).hpfLpfSetting = setting;
        mAudioSettingStatus.crossoverSettingEnabled = true;

        // exercise
        mPreferAudio.toggleCrossoverHpfLpf(SpeakerType.FRONT);
        mSignal.await();

        // verify
        if(setting == HpfLpfSetting.ON){
            verify(mAudioSettingUpdater).setCrossoverHpfLpf(SpeakerType.FRONT, false );
        }else if(setting == HpfLpfSetting.OFF){
            verify(mAudioSettingUpdater).setCrossoverHpfLpf(SpeakerType.FRONT, true );
        }else{
            verify(mAudioSettingUpdater, never()).setCrossoverHpfLpf(any(SpeakerType.class),anyBoolean());
        }
    }

    @Test
    public void toggleCrossoverHpfLpf_Disabled() throws Exception {
        // exercise
        mPreferAudio.toggleCrossoverHpfLpf(SpeakerType.FRONT);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setCrossoverHpfLpf(any(SpeakerType.class), anyBoolean());
    }

    @Test
    public void toggleCrossoverHpfLpf_SubWooferSettingStatusOff() throws Exception {
        // setup
        mAudioSettingStatus.crossoverSettingEnabled = true;
        mAudioSetting.subwooferSetting = SubwooferSetting.OFF;

        // exercise
        mPreferAudio.toggleCrossoverHpfLpf(SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setCrossoverHpfLpf(any(SpeakerType.class), anyBoolean());
    }

    @Test
    public void setCrossoverCutOff() throws Exception {
        // setup
        mAudioSettingStatus.crossoverSettingEnabled = true;

        // exercise
        mPreferAudio.setCrossoverCutOff(SpeakerType.FRONT, StandardCutoffSetting._100HZ);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setCrossoverCutOff(SpeakerType.FRONT, StandardCutoffSetting._100HZ);
    }

    @Test
    public void setCrossoverCutOff_Disabled() throws Exception {
        // exercise
        mPreferAudio.setCrossoverCutOff(SpeakerType.FRONT, StandardCutoffSetting._100HZ);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setCrossoverCutOff(any(SpeakerType.class), any(CutoffSetting.class));
    }

    @Test
    public void setCrossoverCutOff_SubWooferSettingStatusOff() throws Exception {
        // setup
        mAudioSettingStatus.crossoverSettingEnabled = true;
        mAudioSetting.subwooferSetting = SubwooferSetting.OFF;

        // exercise
        mPreferAudio.setCrossoverCutOff(SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE, StandardCutoffSetting._100HZ);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setCrossoverCutOff(any(SpeakerType.class), any(CutoffSetting.class));
    }

    @Test(expected = NullPointerException.class)
    public void setCrossoverCutOff_ArgTypeNull() throws Exception {
        // exercise
        mPreferAudio.setCrossoverCutOff(null, StandardCutoffSetting._100HZ);
    }

    @Test(expected = NullPointerException.class)
    public void setCrossoverCutOff_ArgSettingNull() throws Exception {
        // exercise
        mPreferAudio.setCrossoverCutOff(SpeakerType.FRONT, null);
    }

    @Test
    public void setCrossoverSlope() throws Exception {
        // setup
        mAudioSettingStatus.crossoverSettingEnabled = true;

        // exercise
        mPreferAudio.setCrossoverSlope(SpeakerType.FRONT, StandardSlopeSetting._6DB);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setCrossoverSlope(SpeakerType.FRONT, StandardSlopeSetting._6DB);
    }

    @Test
    public void setCrossoverSlope_Disabled() throws Exception {
        // exercise
        mPreferAudio.setCrossoverSlope(SpeakerType.FRONT, StandardSlopeSetting._6DB);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setCrossoverSlope(any(SpeakerType.class), any(SlopeSetting.class));
    }

    @Test
    public void setCrossoverSlope_SubWooferSettingStatusOff() throws Exception {
        // setup
        mAudioSettingStatus.crossoverSettingEnabled = true;
        mAudioSetting.subwooferSetting = SubwooferSetting.OFF;

        // exercise
        mPreferAudio.setCrossoverSlope(SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE, StandardSlopeSetting._6DB);
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setCrossoverSlope(any(SpeakerType.class), any(SlopeSetting.class));
    }

    @Test(expected = NullPointerException.class)
    public void setCrossoverSlope_ArgTypeNull() throws Exception {
        // exercise
        mPreferAudio.setCrossoverSlope(null, StandardSlopeSetting._6DB);
    }

    @Test(expected = NullPointerException.class)
    public void setCrossoverSlope_ArgSettingNull() throws Exception {
        // exercise
        mPreferAudio.setCrossoverSlope(SpeakerType.FRONT, null);
    }

    @Theory
    public void toggleSubWoofer(SubwooferSetting setting) throws Exception {
        // setup
        mAudioSetting.subwooferSetting = setting;
        mAudioSettingStatus.subwooferSettingEnabled = true;

        // exercise
        mPreferAudio.toggleSubWoofer();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setSubWoofer(setting.toggle());
    }

    @Test
    public void toggleSubWoofer_Disabled() throws Exception {
        // exercise
        mPreferAudio.toggleSubWoofer();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setSubWoofer(any(SubwooferSetting.class));
    }

    @Theory
    public void toggleSubWooferPhase(SubwooferPhaseSetting setting) throws Exception {
        // setup
        mAudioSetting.subwooferPhaseSetting = setting;
        mAudioSettingStatus.subwooferPhaseSettingEnabled = true;

        // exercise
        mPreferAudio.toggleSubWooferPhase();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).setSubWooferPhase(setting.toggle());
    }

    @Test
    public void toggleSubWooferPhase_Disabled() throws Exception {
        // exercise
        mPreferAudio.toggleSubWooferPhase();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setSubWooferPhase(any(SubwooferPhaseSetting.class));
    }

    @Test
    public void toggleSubWooferPhase_SubWooferSettingStatusOff() throws Exception {
        // setup
        mAudioSettingStatus.subwooferPhaseSettingEnabled = true;
        mAudioSetting.subwooferSetting = SubwooferSetting.OFF;

        // exercise
        mPreferAudio.toggleSubWooferPhase();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).setSubWooferPhase(any(SubwooferPhaseSetting.class));
    }

    @Test
    public void saveAudioSetting() throws Exception {
        // setup
        mAudioSettingStatus.saveSettingEnabled = true;

        // exercise
        mPreferAudio.saveAudioSetting();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).saveAudioSetting();
    }

    @Test
    public void saveAudioSetting_SettingDisabled() throws Exception {
        // exercise
        mPreferAudio.saveAudioSetting();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).saveAudioSetting();
    }

    @Test
    public void loadAudioSetting() throws Exception {
        // setup
        mAudioSettingStatus.loadSettingEnabled = true;

        // exercise
        mPreferAudio.loadAudioSetting();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater).loadAudioSetting();
    }

    @Test
    public void loadAudioSetting_SettingDisabled() throws Exception {
        // exercise
        mPreferAudio.loadAudioSetting();
        mSignal.await();

        // verify
        verify(mAudioSettingUpdater, never()).loadAudioSetting();
    }

}