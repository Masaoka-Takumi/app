package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.AppMusicSourceController;
import jp.pioneer.carsync.domain.component.AudioSettingUpdater;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.SoundFxSettingUpdater;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.AudioSettingEqualizerType;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CustomBandSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundEffectType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/19.
 */
public class PreferSoundFxTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    PreferSoundFx mPreferSoundFx;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock EventBus mEventBus;
    @Mock AppSharedPreference mPreference;
    @Mock SoundFxSettingUpdater mSoundFxUpdater;
    @Mock AudioSettingUpdater mAudioUpdater;

    @Mock CarDevice mCarDevice;
    @Mock AppMusicSourceController mAppMusicSourceController;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    AudioSettingStatus mAudioSettingStatus = new AudioSettingStatus();
    AudioSetting mAudioSetting = new AudioSetting();
    SoundFxSettingStatus mSoundFxSettingStatus = new SoundFxSettingStatus();
    SoundFxSetting mSoundFxSetting = new SoundFxSetting();

    static class Fixture {
        SoundFieldControlSettingType fieldType;
        SoundEffectType effectType;
        SoundEffectSettingType effectSettingType;

        Fixture(SoundFieldControlSettingType fieldType, SoundEffectType effectType, SoundEffectSettingType expected) {
            this.fieldType = fieldType;
            this.effectType = effectType;
            this.effectSettingType = expected;
        }
    }

    @DataPoints
    public static final Fixture[] FIXTURES = {
            new Fixture(SoundFieldControlSettingType.OFF, SoundEffectType.OFF, SoundEffectSettingType.OFF),
            new Fixture(SoundFieldControlSettingType.OFF, SoundEffectType.FEMALE, SoundEffectSettingType.OFF),
            new Fixture(SoundFieldControlSettingType.OFF, SoundEffectType.MALE, SoundEffectSettingType.OFF),
            new Fixture(SoundFieldControlSettingType.LIVE_REC, SoundEffectType.OFF, SoundEffectSettingType.OFF),
            new Fixture(SoundFieldControlSettingType.LIVE_REC, SoundEffectType.FEMALE, SoundEffectSettingType.CLUB_F),
            new Fixture(SoundFieldControlSettingType.LIVE_REC, SoundEffectType.MALE, SoundEffectSettingType.CLUB_M),
            new Fixture(SoundFieldControlSettingType.LIVE, SoundEffectType.OFF, SoundEffectSettingType.OFF),
            new Fixture(SoundFieldControlSettingType.LIVE, SoundEffectType.FEMALE, SoundEffectSettingType.HALL_F),
            new Fixture(SoundFieldControlSettingType.LIVE, SoundEffectType.MALE, SoundEffectSettingType.HALL_M),
            new Fixture(SoundFieldControlSettingType.HALL, SoundEffectType.OFF, SoundEffectSettingType.OFF),
            new Fixture(SoundFieldControlSettingType.HALL, SoundEffectType.FEMALE, SoundEffectSettingType.HALL_F),
            new Fixture(SoundFieldControlSettingType.HALL, SoundEffectType.MALE, SoundEffectSettingType.HALL_M),
            new Fixture(SoundFieldControlSettingType.LIVE_STEMIC, SoundEffectType.OFF, SoundEffectSettingType.OFF),
            new Fixture(SoundFieldControlSettingType.LIVE_STEMIC, SoundEffectType.FEMALE, SoundEffectSettingType.CLUB_F),
            new Fixture(SoundFieldControlSettingType.LIVE_STEMIC, SoundEffectType.MALE, SoundEffectSettingType.CLUB_M),
            new Fixture(SoundFieldControlSettingType.DOME, SoundEffectType.OFF, SoundEffectSettingType.OFF),
            new Fixture(SoundFieldControlSettingType.DOME, SoundEffectType.FEMALE, SoundEffectSettingType.ARENA_F),
            new Fixture(SoundFieldControlSettingType.DOME, SoundEffectType.MALE, SoundEffectSettingType.ARENA_M),
            new Fixture(SoundFieldControlSettingType.STADIUM, SoundEffectType.OFF, SoundEffectSettingType.OFF),
            new Fixture(SoundFieldControlSettingType.STADIUM, SoundEffectType.FEMALE, SoundEffectSettingType.ARENA_F),
            new Fixture(SoundFieldControlSettingType.STADIUM, SoundEffectType.MALE, SoundEffectSettingType.ARENA_M),
    };

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mCarDevice.getSourceController(MediaSourceType.APP_MUSIC)).thenReturn(mAppMusicSourceController);
        mPreferSoundFx = new PreferSoundFx(mCarDevice);
        mPreferSoundFx.mHandler = mHandler;
        mPreferSoundFx.mStatusHolder = mStatusHolder;
        mPreferSoundFx.mEventBus = mEventBus;
        mPreferSoundFx.mPreference = mPreference;
        mPreferSoundFx.mSoundFxSettingUpdater = mSoundFxUpdater;
        mPreferSoundFx.mAudioSettingUpdater = mAudioUpdater;


        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        when(mStatusHolder.getAudioSettingStatus()).thenReturn(mAudioSettingStatus);
        when(mStatusHolder.getAudioSetting()).thenReturn(mAudioSetting);
        when(mStatusHolder.getSoundFxSettingStatus()).thenReturn(mSoundFxSettingStatus);
        when(mStatusHolder.getSoundFxSetting()).thenReturn(mSoundFxSetting);
    }

    @Test
    public void setCustomBand_TypeCustom1() throws Exception {
        // setup
        float[] mleBands = new float[31];
        int[] carDeviceBands = new int[13];
        ArgumentCaptor<CustomBandSetting> captor = ArgumentCaptor.forClass(CustomBandSetting.class);
        mSignal = new CountDownLatch(1);
        mAudioSettingStatus.equalizerSettingEnabled = true;
        when(mAppMusicSourceController.convertBandValue(eq(mleBands))).thenReturn(carDeviceBands);

        // exercise
        mPreferSoundFx.setCustomBand(CustomEqType.CUSTOM1, mleBands);
        mSignal.await();

        // verify
        verify(mAudioUpdater).setCustomBand(CustomEqType.CUSTOM1, carDeviceBands);
        verify(mPreference).setCustomBandSettingA(captor.capture());
        assertThat(captor.getValue().bands, is(mleBands));
    }

    @Test
    public void setCustomBand_TypeCustom2() throws Exception {
        // setup
        float[] mleBands = new float[31];
        int[] carDeviceBands = new int[13];
        ArgumentCaptor<CustomBandSetting> captor = ArgumentCaptor.forClass(CustomBandSetting.class);
        mSignal = new CountDownLatch(1);
        mAudioSettingStatus.equalizerSettingEnabled = true;
        when(mAppMusicSourceController.convertBandValue(eq(mleBands))).thenReturn(carDeviceBands);

        // exercise
        mPreferSoundFx.setCustomBand(CustomEqType.CUSTOM2, mleBands);
        mSignal.await();

        // verify
        verify(mAudioUpdater).setCustomBand(CustomEqType.CUSTOM2, carDeviceBands);
        verify(mPreference).setCustomBandSettingB(captor.capture());
        assertThat(captor.getValue().bands, is(mleBands));
    }

    @Test
    public void setCustomBand_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mAudioSettingStatus.equalizerSettingEnabled = false;

        // exercise
        mPreferSoundFx.setCustomBand(CustomEqType.CUSTOM1, new float[31]);
        mSignal.await();

        // verify
        verify(mAudioUpdater, never()).setCustomBand(any(CustomEqType.class), any(int[].class));
    }

    @Test
    public void setCustomBand_ConvertFailed() throws Exception {
        // setup
        float[] mleBands = new float[31];
        mSignal = new CountDownLatch(1);
        mAudioSettingStatus.equalizerSettingEnabled = true;
        when(mAppMusicSourceController.convertBandValue(eq(mleBands))).thenReturn(null);

        // exercise
        mPreferSoundFx.setCustomBand(CustomEqType.CUSTOM1, mleBands);
        mSignal.await();

        // verify
        verify(mAudioUpdater, never()).setCustomBand(any(CustomEqType.class), any(int[].class));
    }

    @Test(expected = NullPointerException.class)
    public void setCustomBand_ArgTypeNull() throws Exception {
        // exercise
        mPreferSoundFx.setCustomBand(null, new float[31]);
    }

    @Test(expected = NullPointerException.class)
    public void setCustomBand_ArgBandsNull() throws Exception {
        // exercise
        mPreferSoundFx.setCustomBand(CustomEqType.CUSTOM1, null);
    }

    @Test
    public void setEqualizer() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mAudioSettingStatus.equalizerSettingEnabled = true;

        // exercise
        mPreferSoundFx.setEqualizer(SoundFxSettingEqualizerType.ELECTRONICA);
        mSignal.await();

        // verify
        verify(mAudioUpdater).setEqualizer(AudioSettingEqualizerType.ELECTRONICA);
    }

    @Test
    public void setEqualizer_SettingDisabled() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mAudioSettingStatus.equalizerSettingEnabled = false;

        // exercise
        mPreferSoundFx.setEqualizer(SoundFxSettingEqualizerType.ELECTRONICA);
        mSignal.await();

        // verify
        verify(mAudioUpdater, never()).setEqualizer(any(AudioSettingEqualizerType.class));
    }

    @Test(expected = NullPointerException.class)
    public void setEqualizer_ArgNull() throws Exception {
        // exercise
        mPreferSoundFx.setEqualizer(null);
    }

    @Test
    public void setSuperTodoroki() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.superTodorokiSettingEnabled = true;

        // exercise
        mPreferSoundFx.setSuperTodoroki(SuperTodorokiSetting.HIGH);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater).setSuperTodoroki(SuperTodorokiSetting.HIGH);
    }

    @Test
    public void setSuperTodoroki_SettingDisabled() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.superTodorokiSettingEnabled = false;

        // exercise
        mPreferSoundFx.setSuperTodoroki(SuperTodorokiSetting.HIGH);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater, never()).setSuperTodoroki(any(SuperTodorokiSetting.class));
    }

    @Test(expected = NullPointerException.class)
    public void setSuperTodoroki_ArgNull() throws Exception {
        // exercise
        mPreferSoundFx.setSuperTodoroki(null);
    }

    @Test
    public void setSmallCarTa() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.smallCarTaSettingEnabled = true;

        // exercise
        mPreferSoundFx.setSmallCarTa(SmallCarTaSettingType.B, ListeningPosition.LEFT);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater).setSmallCarTa(SmallCarTaSettingType.B, ListeningPosition.LEFT);
    }

    @Test
    public void setSmallCarTa_SettingDisabled() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.smallCarTaSettingEnabled = false;

        // exercise
        mPreferSoundFx.setSmallCarTa(SmallCarTaSettingType.B, ListeningPosition.LEFT);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater, never()).setSmallCarTa(any(SmallCarTaSettingType.class), any(ListeningPosition.class));
    }

    @Test(expected = NullPointerException.class)
    public void setSmallCarTa_ArgTypeNull() throws Exception {
        // exercise
        mPreferSoundFx.setSmallCarTa(null, ListeningPosition.LEFT);
    }

    @Test(expected = NullPointerException.class)
    public void setSmallCarTa_ArgPositionNull() throws Exception {
        // exercise
        mPreferSoundFx.setSmallCarTa(SmallCarTaSettingType.B, null);
    }

    @Theory
    public void setLiveSimulation(Fixture fixture) throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.liveSimulationSettingEnabled = true;

        // exercise
        mPreferSoundFx.setLiveSimulation(fixture.fieldType, fixture.effectType);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater).setLiveSimulation(fixture.fieldType, fixture.effectSettingType);
    }

    @Test
    public void setLiveSimulation_SettingDisabled() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.liveSimulationSettingEnabled = false;

        // exercise
        mPreferSoundFx.setLiveSimulation(SoundFieldControlSettingType.DOME, SoundEffectType.FEMALE);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater, never()).setLiveSimulation(any(SoundFieldControlSettingType.class), any(SoundEffectSettingType.class));
    }

    @Test(expected = NullPointerException.class)
    public void setLiveSimulation_ArgFieldNull() throws Exception {
        // exercise
        mPreferSoundFx.setLiveSimulation(null, SoundEffectType.FEMALE);
    }

    @Test(expected = NullPointerException.class)
    public void setLiveSimulation_ArgEffectNull() throws Exception {
        // exercise
        mPreferSoundFx.setLiveSimulation(SoundFieldControlSettingType.DOME, null);
    }

    @Test
    public void setKaraokeSetting() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.karaokeSettingEnabled = true;

        // exercise
        mPreferSoundFx.setKaraokeSetting(true);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater).setKaraokeSetting(true);
    }

    @Test
    public void setKaraokeSetting_SettingDisabled() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.karaokeSettingEnabled = false;

        // exercise
        mPreferSoundFx.setKaraokeSetting(true);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater, never()).setKaraokeSetting(anyBoolean());
    }

    @Test
    public void setMicVolume() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.karaokeSettingEnabled = true;

        // exercise
        mPreferSoundFx.setMicVolume(5);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater).setMicVolume(5);
    }

    @Test
    public void setMicVolume_SettingDisabled() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.karaokeSettingEnabled = false;

        // exercise
        mPreferSoundFx.setMicVolume(5);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater, never()).setMicVolume(anyInt());
    }

    @Test
    public void setVocalCancel() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.karaokeSettingEnabled = true;

        // exercise
        mPreferSoundFx.setVocalCancel(true);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater).setVocalCancel(true);
    }

    @Test
    public void setVocalCancel_SettingDisabled() throws Exception {
        //setup
        mSignal = new CountDownLatch(1);
        mSoundFxSettingStatus.karaokeSettingEnabled = false;

        // exercise
        mPreferSoundFx.setVocalCancel(true);
        mSignal.await();

        // verify
        verify(mSoundFxUpdater, never()).setVocalCancel(anyBoolean());
    }
}