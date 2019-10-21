package jp.pioneer.carsync.presentation.presenter;

import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.PandoraInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlPandoraSource;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PandoraMediaInfo;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.domain.model.ThumbStatus;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.PandoraView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.model.PlaybackMode.PAUSE;
import static jp.pioneer.carsync.domain.model.PlaybackMode.STOP;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pandora再生のPresenterのテスト
 */
public class PandoraPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    PandoraPresenter mPresenter;
    @Mock PandoraView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock ControlPandoraSource mControlCase;
    @Mock Context mContext;
    @Mock AppSharedPreference mPreference;
    @Mock ControlSource mControlSource;
    @Mock PreferSoundFx mFxCase;
    @Mock GetStatusHolder mStatusHolder;
    @Mock ControlMediaList mMediaCase;
    private ArrayList<SoundFxSettingEqualizerType> mTestEqArray = new ArrayList<>();
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        mTestEqArray.clear();
        mTestEqArray.add(SoundFxSettingEqualizerType.SUPER_BASS);
        mTestEqArray.add(SoundFxSettingEqualizerType.POWERFUL);
        mTestEqArray.add(SoundFxSettingEqualizerType.NATURAL);
        mTestEqArray.add(SoundFxSettingEqualizerType.VOCAL);
        mTestEqArray.add(SoundFxSettingEqualizerType.TODOROKI);
        mTestEqArray.add(SoundFxSettingEqualizerType.POP_ROCK);
        mTestEqArray.add(SoundFxSettingEqualizerType.COMMON_CUSTOM);
        mTestEqArray.add(SoundFxSettingEqualizerType.COMMON_CUSTOM_2ND);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.NOT_LIST;
        carDeviceStatus.sourceType = MediaSourceType.PANDORA;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);
        when(mGetCase.execute()).thenReturn(holder);

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() ->{
            mPresenter = new PandoraPresenter();
            mPresenter.mContext = mContext;
            mPresenter.mPreference = mPreference;
            mPresenter.mEventBus = mEventBus;
            mPresenter.mGetCase = mGetCase;
            mPresenter.mControlCase = mControlCase;
            mPresenter.mFxCase = mFxCase;
            mPresenter.setUp(mStatusHolder,mEventBus,mMediaCase);
        });
    }

    @After
    public void tearDown() throws Exception {
        mPresenter.stopHandler();
    }
    /**
     * onResumeのテスト
     */
    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        PandoraMediaInfo info = mock(PandoraMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PAUSE;
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumTitle";
        info.stationName = "stationName";
        info.totalSecond = 300;
        info.currentSecond = 20;
        info.thumbStatus = ThumbStatus.NONE;
        mediaHolder.pandoraMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);

        AudioSettingStatus audioSettingStatus = mock(AudioSettingStatus.class);
        audioSettingStatus.equalizerSettingEnabled = true;
        when(holder.getAudioSettingStatus()).thenReturn(audioSettingStatus);
        SoundFxSettingStatus settingStatus = new SoundFxSettingStatus();
        settingStatus.liveSimulationSettingEnabled = true;
        settingStatus.superTodorokiSettingEnabled = true;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSoundFxSettingStatus()).thenReturn(settingStatus);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.SUPER_BASS;
        fxSetting.liveSimulationSetting = new LiveSimulationSetting();
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.OFF;
        fxSetting.liveSimulationSetting.soundFieldControlSettingType = SoundFieldControlSettingType.DOME;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);

        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.takeView(mView);
        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);
        verify(mView).setMusicTitle("songTitle");
        verify(mView).setMusicArtist("artistName");
        verify(mView).setMusicAlbum("albumTitle");
        verify(mView).setStationName("stationName");
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
        verify(mView).setThumbStatus(ThumbStatus.NONE);
    }

    /**
     * onResumeのテスト2
     */
    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);

        PandoraMediaInfo info = mock(PandoraMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = STOP;
        info.totalSecond = 300;
        info.currentSecond = 20;
        info.thumbStatus = ThumbStatus.UP;
        mediaHolder.pandoraMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);

        AudioSettingStatus audioSettingStatus = mock(AudioSettingStatus.class);
        audioSettingStatus.equalizerSettingEnabled = true;
        when(holder.getAudioSettingStatus()).thenReturn(audioSettingStatus);
        SoundFxSettingStatus settingStatus = new SoundFxSettingStatus();
        settingStatus.liveSimulationSettingEnabled = true;
        settingStatus.superTodorokiSettingEnabled = true;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSoundFxSettingStatus()).thenReturn(settingStatus);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.SUPER_BASS;
        fxSetting.liveSimulationSetting = new LiveSimulationSetting();
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.OFF;
        fxSetting.liveSimulationSetting.soundFieldControlSettingType = SoundFieldControlSettingType.DOME;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);

        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onResume();

        verify(mEventBus,times(0)).register(mPresenter);
        verify(mView).setThumbStatus(ThumbStatus.UP);
        verify(mView).setMusicTitle("");
        verify(mView).setMusicArtist("");
        verify(mView).setMusicAlbum("");
        verify(mView).setStationName("");
    }

    /**
     * onPauseのテスト
     */
    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    /**
     * onPlayPauseActionのテスト
     */
    @Test
    public void testOnPlayPauseAction() throws Exception {
        mPresenter.onPlayPauseAction();
        verify(mControlCase).togglePlay();
    }

    /**
     * onSkipNextActionのテスト
     */
    @Test
    public void testOnSkipNextAction() throws Exception {
        mPresenter.onSkipNextAction();
        verify(mControlCase).skipNextTrack();
    }

    /**
     * onSettingShowActionのテスト
     */
    @Test
    public void testOnSettingShowAction() throws Exception {
        mPresenter.onSettingShowAction();
        //TODO:onSettingShowActionのテスト
    }

    /**
     * onSelectSourceActionのテスト
     */
    @Test
    public void testOnSelectSourceAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onSelectSourceAction();

        verify(mEventBus).post(any(BackgroundChangeEvent.class));
        verify(mEventBus, atLeast(2)).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getAllValues().get(1);
        assertThat(capturedEvent.screenId, is(ScreenId.SOURCE_SELECT));
    }

    /**
     * onSelectFxActionのテスト
     */
    //@Test
    public void testOnSelectFxAction() throws Exception {
        mPresenter.onSelectFxAction();
        //TODO:onSelectFxActionのテスト
    }

    /**
     * onSelectVisualActionのテスト
     */
    //@Test
    public void testOnSelectVisualAction() throws Exception {
        mPresenter.onSelectVisualAction();
        //TODO:onSelectVisualActionのテスト
    }

    /**
     * onThumbUpActionのテスト
     */
    @Test
    public void testOnThumbUpAction() throws Exception {
        mPresenter.onThumbUpAction();
        verify(mControlCase).setThumbUp();
    }

    /**
     * onThumbDownActionのテスト
     */
    @Test
    public void testOnThumbDownAction() throws Exception {
        mPresenter.onThumbDownAction();
        verify(mControlCase).setThumbDown();
    }

    /**
     * onPandoraInfoChangeActionのテスト
     */
    @Test
    public void testOnPandoraInfoChangeAction() throws Exception {
        PandoraInfoChangeEvent event = mock(PandoraInfoChangeEvent.class);
        PandoraMediaInfo info = mock(PandoraMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PAUSE;
        info.songTitle = "";
        info.artistName = "";
        info.albumName = "";
        info.stationName = "";

        info.totalSecond = 300;
        info.currentSecond = 20;
        info.thumbStatus = ThumbStatus.NONE;
        mediaHolder.pandoraMediaInfo = info;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onPandoraInfoChangeAction(event);
        verify(mView).setMusicTitle("No Title");
        verify(mView).setMusicArtist("No Artist");
        verify(mView).setMusicAlbum("No Album");
        verify(mView).setStationName("No Station");
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
        verify(mView).setThumbStatus(ThumbStatus.NONE);
    }
}