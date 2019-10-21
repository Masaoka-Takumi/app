package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.AudioSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAudio;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.CrossoverSetting;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.SpeakerLevelSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.StandardCutoffSetting;
import jp.pioneer.carsync.domain.model.StandardSlopeSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TimeAlignmentSetting;
import jp.pioneer.carsync.presentation.view.AdvancedAudioSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/09/21.
 */
public class AdvancedAudioSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks AdvancedAudioSettingPresenter mPresenter = new AdvancedAudioSettingPresenter();
    @Mock AdvancedAudioSettingView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock PreferAudio mPreferAudio;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock AppSharedPreference mAppSharedPreference;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        mPresenter.onResume();
        verify(mEventBus).register(mPresenter);
    }

    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);

        mPresenter.onResume();
        verify(mEventBus, times(0)).register(mPresenter);
    }

    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnToggleSubWoofer() throws Exception {
        mPresenter.onToggleSubWoofer();
        verify(mPreferAudio).toggleSubWoofer();
    }

    @Test
    public void testOnToggleSubWooferPhase() throws Exception {
        mPresenter.onToggleSubWooferPhase();
        verify(mPreferAudio).toggleSubWooferPhase();
    }

    @Test
    public void testOnToggleCrossoverHpfLpf() throws Exception {
        SpeakerType type = SpeakerType.FRONT;
        mPresenter.onToggleCrossoverHpfLpf(type);
        verify(mPreferAudio).toggleCrossoverHpfLpf(type);
    }

    @Test
    public void testSetCrossoverCutOffInc() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        AudioSetting audioSetting = mock(AudioSetting.class);
        CrossoverSetting setting = new CrossoverSetting();
        setting.front.cutoffSetting = StandardCutoffSetting._50HZ;
        audioSetting.crossoverSetting = setting;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);

        mPresenter.setCrossoverCutOff(SpeakerType.FRONT,true);

        verify(mPreferAudio).setCrossoverCutOff(SpeakerType.FRONT,StandardCutoffSetting._63HZ);
    }

    @Test
    public void testSetCrossoverCutOffDec() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        AudioSetting audioSetting = mock(AudioSetting.class);
        CrossoverSetting setting = new CrossoverSetting();
        setting.front.cutoffSetting = StandardCutoffSetting._50HZ;
        audioSetting.crossoverSetting = setting;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);

        mPresenter.setCrossoverCutOff(SpeakerType.FRONT,false);

        verify(mPreferAudio,times(0)).setCrossoverCutOff(eq(SpeakerType.FRONT),any(StandardCutoffSetting.class));
    }

    @Test
    public void testSetCrossoverSlopeInc() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        AudioSetting audioSetting = mock(AudioSetting.class);
        CrossoverSetting setting = new CrossoverSetting();
        setting.front.slopeSetting = StandardSlopeSetting._6DB;
        audioSetting.crossoverSetting = setting;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);

        mPresenter.setCrossoverSlope(SpeakerType.FRONT,true);

        verify(mPreferAudio).setCrossoverSlope(SpeakerType.FRONT,StandardSlopeSetting._12DB);
    }

    @Test
    public void testSetCrossoverSlopeDec() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        AudioSetting audioSetting = mock(AudioSetting.class);
        CrossoverSetting setting = new CrossoverSetting();
        setting.front.slopeSetting = StandardSlopeSetting._6DB;
        audioSetting.crossoverSetting = setting;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);

        mPresenter.setCrossoverSlope(SpeakerType.FRONT,false);

        verify(mPreferAudio,times(0)).setCrossoverSlope(eq(SpeakerType.FRONT),any(StandardSlopeSetting.class));
    }

    @Test
    public void testSetSpeakerLevelInc() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        AudioSetting audioSetting = mock(AudioSetting.class);
        SpeakerLevelSetting setting = new SpeakerLevelSetting();
        setting.frontLeftHighLeftLevel = 20;
        setting.maximumLevel=0;
        setting.maximumLevel=50;
        audioSetting.speakerLevelSetting = setting;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);

        mPresenter.setSpeakerLevel(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT,true);

        verify(mPreferAudio).setSpeakerLevel(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 21);
    }

    @Test
    public void testSetSpeakerLevelDec() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        AudioSetting audioSetting = mock(AudioSetting.class);
        SpeakerLevelSetting setting = new SpeakerLevelSetting();
        setting.frontLeftHighLeftLevel = 20;
        setting.maximumLevel=0;
        setting.maximumLevel=50;
        audioSetting.speakerLevelSetting = setting;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);

        mPresenter.setSpeakerLevel(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT,false);

        verify(mPreferAudio).setSpeakerLevel(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 19);
    }

    @Test
    public void testSetTimeAlignmentInc() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        AudioSetting audioSetting = mock(AudioSetting.class);
        TimeAlignmentSetting setting = new TimeAlignmentSetting();
        setting.frontLeftHighLeftStep = 20;
        setting.minimumStep=0;
        setting.maximumStep=50;
        audioSetting.timeAlignmentSetting = setting;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);

        mPresenter.setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT,true);

        verify(mPreferAudio).setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 21);
    }

    @Test
    public void testSetTimeAlignmentDec() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        AudioSetting audioSetting = mock(AudioSetting.class);
        TimeAlignmentSetting setting = new TimeAlignmentSetting();
        setting.frontLeftHighLeftStep = 20;
        setting.minimumStep=0;
        setting.maximumStep=50;
        audioSetting.timeAlignmentSetting = setting;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);

        mPresenter.setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT,false);

        verify(mPreferAudio).setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT, 19);
    }

    @Test
    public void testOnToggleTimeAlignmentMode() throws Exception {
        mPresenter.onToggleTimeAlignmentMode();
        verify(mPreferAudio).toggleTimeAlignmentMode();
    }

    @Test
    public void testSetListeningPosition() throws Exception {
        ListeningPositionSetting item = ListeningPositionSetting.FRONT;
        mPresenter.setListeningPosition(item);
        verify(mPreferAudio).setListeningPosition(item);
    }

    @Test
    public void testOnAudioSettingChangeAction() throws Exception {
        AudioSettingChangeEvent event = mock(AudioSettingChangeEvent.class);
        mPresenter.onAudioSettingChangeAction(event);

        verify(mView).applyStatus();
        verify(mView).redrawFilterGraph(true);
    }

}