package jp.pioneer.carsync.presentation.presenter;

import android.app.Instrumentation;
import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;

import org.greenrobot.eventbus.EventBus;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.AppMusicPlayPositionChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicRepeatModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicShuffleModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicTrackChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ExitMenu;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CustomBandSetting;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.SoundEffectType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AndroidMusicView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.model.CustomEqType.CUSTOM1;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Androidローカルコンテンツ再生のPresenterのテスト
 */
public class AndroidMusicPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    AndroidMusicPresenter mPresenter;
    @Mock AndroidMusicView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock ControlAppMusicSource mControlCase;
    @Mock AppSharedPreference mPreference;
    @Mock PreferSoundFx mFxCase;
    @Mock Context mContext;
    @Mock GetStatusHolder mStatusHolder;
    @Mock ControlMediaList mMediaCase;
    @Mock ExitMenu mExitMenu;
    private ArrayList<SoundFxSettingEqualizerType> mTestEqArray = new ArrayList<>();
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mPreference.getUiColor()).thenReturn(AQUA);
        mTestEqArray.clear();
        mTestEqArray.add(SoundFxSettingEqualizerType.SUPER_BASS);
        mTestEqArray.add(SoundFxSettingEqualizerType.POWERFUL);
        mTestEqArray.add(SoundFxSettingEqualizerType.NATURAL);
        mTestEqArray.add(SoundFxSettingEqualizerType.VOCAL);
        mTestEqArray.add(SoundFxSettingEqualizerType.TODOROKI);
        mTestEqArray.add(SoundFxSettingEqualizerType.POP_ROCK);
        mTestEqArray.add(SoundFxSettingEqualizerType.COMMON_CUSTOM);
        mTestEqArray.add(SoundFxSettingEqualizerType.COMMON_CUSTOM_2ND);

        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.NOT_LIST;
        carDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() ->{
            mPresenter = new AndroidMusicPresenter();
            mPresenter.mContext = mContext;
            mPresenter.mPreference = mPreference;
            mPresenter.mEventBus = mEventBus;
            mPresenter.mGetCase = mGetCase;
            mPresenter.mControlCase = mControlCase;
            mPresenter.mFxCase = mFxCase;
            mPresenter.mExitMenu = mExitMenu;
            mPresenter.setUp(mStatusHolder,mEventBus,mMediaCase);
        });
    }

    @After
    public void tearDown() throws Exception {
        mPresenter.stopHandler();
    }

    /**
     * onResumeのテスト PLAY中の場合
     */
    @Test
    public void testOnResumeOnPlay() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        status.repeatMode = SmartPhoneRepeatMode.OFF;
        status.shuffleMode = ShuffleMode.OFF;
        status.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumTitle = "albumTitle";
        info.genre = "genre";
        info.artworkImageLocation = Uri.parse("content://media/external/images/media/1111");
        info.durationInSec = 300;
        info.positionInSec = 20;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);

        AudioSettingStatus audioSettingStatus = mock(AudioSettingStatus.class);
        audioSettingStatus.equalizerSettingEnabled = true;
        when(holder.getAudioSettingStatus()).thenReturn(audioSettingStatus);
        SoundFxSettingStatus settingStatus = mock(SoundFxSettingStatus.class);
        settingStatus.liveSimulationSettingEnabled = true;
        settingStatus.superTodorokiSettingEnabled = true;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSoundFxSettingStatus()).thenReturn(settingStatus);

        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.SUPER_BASS;
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.OFF;
        fxSetting.liveSimulationSetting = new LiveSimulationSetting();
        fxSetting.liveSimulationSetting.soundFieldControlSettingType = SoundFieldControlSettingType.OFF;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.takeView(mView);
        mPresenter.onResume();

        verify(mEventBus,times(2)).register(mPresenter);
        verify(mView).setRepeatImage(SmartPhoneRepeatMode.OFF);
        verify(mView).setShuffleImage(ShuffleMode.OFF);

        verify(mView).setMusicTitle("songTitle");
        verify(mView).setMusicArtist("artistName");
        verify(mView).setMusicAlbum("albumTitle");
        verify(mView).setMusicGenre("genre");
        verify(mView).setMusicAlbumArt(Uri.parse("content://media/external/images/media/1111"));
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
        verify(mView).setEqFxButtonEnabled(true,true);
        verify(mView).setEqButton(mContext.getString(SoundFxSettingEqualizerType.SUPER_BASS.getLabel()));
        verify(mView).setFxButton(mContext.getString(SoundFieldControlSettingType.OFF.getLabel()));
    }

    /**
     * onResumeのテスト　STOP中の場合
     */
    @Test
    public void testOnResumeOnStop() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        status.repeatMode = SmartPhoneRepeatMode.ONE;
        status.shuffleMode = ShuffleMode.ON;
        status.playbackMode = PlaybackMode.STOP;
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumTitle = "albumTitle";
        info.genre = "genre";
        info.artworkImageLocation = Uri.parse("content://media/external/images/media/1111");
        info.durationInSec = 300;
        info.positionInSec = 20;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);

        AudioSettingStatus audioSettingStatus = mock(AudioSettingStatus.class);
        audioSettingStatus.equalizerSettingEnabled = true;
        when(holder.getAudioSettingStatus()).thenReturn(audioSettingStatus);
        SoundFxSettingStatus settingStatus = mock(SoundFxSettingStatus.class);
        settingStatus.liveSimulationSettingEnabled = true;
        settingStatus.superTodorokiSettingEnabled = false;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSoundFxSettingStatus()).thenReturn(settingStatus);

        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.SUPER_BASS;
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.LOW;
        fxSetting.liveSimulationSetting = new LiveSimulationSetting();
        fxSetting.liveSimulationSetting.soundFieldControlSettingType = SoundFieldControlSettingType.OFF;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onResume();
        verify(mEventBus, times(0)).register(mPresenter);
        verify(mView).setMusicTitle("");
        verify(mView).setMusicArtist("");
        verify(mView).setMusicAlbum("");
        verify(mView).setMusicGenre("");
        verify(mView).setRepeatImage(SmartPhoneRepeatMode.ONE);
        verify(mView).setShuffleImage(ShuffleMode.ON);

        verify(mView).setEqFxButtonEnabled(true,false);
        verify(mView).setEqButton(mContext.getString(SoundFxSettingEqualizerType.SUPER_BASS.getLabel()));
        verify(mView).setFxButton("");
    }

    /**
     * onResumeのテスト 名前が空の場合
     */
    @Test
    public void testOnResumeNameIsEmpty() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        status.repeatMode = SmartPhoneRepeatMode.ALL;
        status.shuffleMode = ShuffleMode.ON;
        status.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "";
        info.artistName = "";
        info.albumTitle = "";
        info.genre = "";
        info.artworkImageLocation = Uri.parse("content://media/external/images/media/1111");
        info.durationInSec = 300;
        info.positionInSec = 20;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);

        AudioSettingStatus audioSettingStatus = mock(AudioSettingStatus.class);
        audioSettingStatus.equalizerSettingEnabled = true;
        when(holder.getAudioSettingStatus()).thenReturn(audioSettingStatus);

        SoundFxSettingStatus settingStatus = mock(SoundFxSettingStatus.class);
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
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        mPresenter.takeView(mView);
        mPresenter.onResume();

        verify(mView).setMusicTitle(mContext.getResources().getString(R.string.no_title));
        verify(mView).setMusicArtist(mContext.getResources().getString(R.string.no_artist));
        verify(mView).setMusicAlbum(mContext.getResources().getString(R.string.no_album));
        verify(mView).setMusicGenre(mContext.getResources().getString(R.string.no_genre));
        verify(mView).setRepeatImage(SmartPhoneRepeatMode.ALL);

        verify(mView).setEqFxButtonEnabled(true,true);
        verify(mView).setEqButton("");
        verify(mView).setFxButton(mContext.getString(SoundFieldControlSettingType.DOME.getLabel()));
    }

    /**
     * onPauseのテスト
     */
    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus,times(2)).unregister(mPresenter);
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
     * onSeekActionのテスト
     */
    @Test
    public void testOnSeekAction() throws Exception {
        //TODO:onSeekActionのテスト
    }

    /**
     * onSkipPreviousActionのテスト
     */
    @Test
    public void testOnSkipPreviousAction() throws Exception {
        mPresenter.onSkipPreviousAction();
        verify(mControlCase).skipPreviousTrack();
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
     * onNowPlayingActionのテスト
     */
    @Test
    public void testOnSettingShowAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onNowPlayingAction();
        verify(mEventBus).post(any(BackgroundChangeEvent.class));
        verify(mEventBus, atLeast(2)).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.NOW_PLAYING_LIST));
    }

    /**
     * onRepeatActionのテスト
     */
    @Test
    public void testOnRepeatAction() throws Exception {
        mPresenter.onRepeatAction();
        verify(mControlCase).toggleRepeatMode();
    }

    /**
     * onShuffleActionのテスト
     */
    @Test
    public void testOnShuffleAction() throws Exception {
        mPresenter.onShuffleAction();
        verify(mControlCase).toggleShuffleMode();
    }

    /**
     * onListShowActionのテスト
     */
    @Test
    public void testOnListShowAction() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);
        mPresenter.onSelectListAction();
        verify(mMediaCase).enterList(ListType.LIST);
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
    @Test
    public void testOnSelectFxAction1() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.SUPER_BASS;
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.OFF;
        fxSetting.liveSimulationSetting = new LiveSimulationSetting();
        fxSetting.liveSimulationSetting.soundFieldControlSettingType = SoundFieldControlSettingType.OFF;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onSelectFxAction();

        verify(mFxCase).setLiveSimulation(SoundFieldControlSettingType.LIVE_REC, SoundEffectType.OFF);
        verify(mFxCase,never()).setSuperTodoroki(SuperTodorokiSetting.OFF);
    }

    /**
     * onSelectFxActionのテスト
     */
    @Test
    public void testOnSelectFxAction2() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.SUPER_BASS;
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.OFF;
        fxSetting.liveSimulationSetting = new LiveSimulationSetting();
        fxSetting.liveSimulationSetting.soundFieldControlSettingType = SoundFieldControlSettingType.STADIUM;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onSelectFxAction();

        verify(mFxCase,never()).setLiveSimulation(SoundFieldControlSettingType.OFF, SoundEffectType.OFF);
        verify(mFxCase).setSuperTodoroki(SuperTodorokiSetting.LAST);
    }

    /**
     * onSelectFxActionのテスト
     */
    @Test
    public void testOnSelectFxAction3() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.SUPER_BASS;
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.SUPER_HIGH;
        fxSetting.liveSimulationSetting = new LiveSimulationSetting();
        fxSetting.liveSimulationSetting.soundFieldControlSettingType = SoundFieldControlSettingType.OFF;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onSelectFxAction();

        verify(mFxCase,never()).setLiveSimulation(SoundFieldControlSettingType.OFF, SoundEffectType.OFF);
        verify(mFxCase).setSuperTodoroki(SuperTodorokiSetting.OFF);
    }

    /**
     * onSelectVisualActionのテスト
     */
    @Test
    public void testOnSelectVisualAction() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.SUPER_BASS;
        fxSetting.customBandSettingA = new CustomBandSetting(CUSTOM1);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onTakeView();
        mPresenter.onSelectVisualAction();

        verify(mFxCase).setEqualizer(SoundFxSettingEqualizerType.POWERFUL);
    }

    /**
     * onPlayPositionChangeActionのテスト
     */
    @Test
    public void testOnPlayPositionChangeAction() throws Exception {
        AppMusicPlayPositionChangeEvent event = mock(AppMusicPlayPositionChangeEvent.class);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.positionInSec = 50;
        mediaHolder.androidMusicMediaInfo = info;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onPlayPositionChangeAction(event);
        verify(mView).setCurrentProgress(50);
    }

    /**
     * onTrackChangeActionのテスト
     */
    @Test
    public void testOnTrackChangeAction() throws Exception {
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        AppMusicTrackChangeEvent event = mock(AppMusicTrackChangeEvent.class);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        status.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumTitle = "albumTitle";
        info.genre = "genre";
        info.artworkImageLocation = Uri.parse("content://jp.pioneer.carsync.provider/artwork/songs/4");
        info.durationInSec = 300;
        mediaHolder.androidMusicMediaInfo = info;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onTrackChangeAction(event);

        verify(mView).setMusicTitle("songTitle");
        verify(mView).setMusicArtist("artistName");
        verify(mView).setMusicAlbum("albumTitle");
        verify(mView).setMusicGenre("genre");
        verify(mView).setMusicAlbumArt(Uri.parse("content://jp.pioneer.carsync.provider/artwork/songs/4"));
        verify(mView).setMaxProgress(300);
    }

    /**
     * onRepeatModeChangeActionのテスト リピートOFF
     */
    @Test
    public void testOnRepeatModeChangeActionOff() throws Exception {
        AppMusicRepeatModeChangeEvent event = mock(AppMusicRepeatModeChangeEvent.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        StatusHolder holder = mock(StatusHolder.class);
        status.repeatMode = SmartPhoneRepeatMode.OFF;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onRepeatModeChangeAction(event);
        verify(mView).setRepeatImage(SmartPhoneRepeatMode.OFF);
    }

    /**
     * onRepeatModeChangeActionのテスト リピートONE
     */
    @Test
    public void testOnRepeatModeChangeActionOne() throws Exception {
        AppMusicRepeatModeChangeEvent event = mock(AppMusicRepeatModeChangeEvent.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        StatusHolder holder = mock(StatusHolder.class);
        status.repeatMode = SmartPhoneRepeatMode.ONE;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onRepeatModeChangeAction(event);
        verify(mView).setRepeatImage(SmartPhoneRepeatMode.ONE);
    }

    /**
     * onRepeatModeChangeActionのテスト リピートALL
     */
    @Test
    public void testOnRepeatModeChangeActionAll() throws Exception {
        AppMusicRepeatModeChangeEvent event = mock(AppMusicRepeatModeChangeEvent.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        StatusHolder holder = mock(StatusHolder.class);
        status.repeatMode = SmartPhoneRepeatMode.ALL;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onRepeatModeChangeAction(event);
        verify(mView).setRepeatImage(SmartPhoneRepeatMode.ALL);
    }

    /**
     * onShuffleModeChangeActionのテスト シャッフルOFF
     */
    @Test
    public void testOnShuffleModeChangeActionOff() throws Exception {
        AppMusicShuffleModeChangeEvent event = mock(AppMusicShuffleModeChangeEvent.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        StatusHolder holder = mock(StatusHolder.class);
        status.shuffleMode = ShuffleMode.OFF;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onShuffleModeChangeAction(event);
        verify(mView).setShuffleImage(ShuffleMode.OFF);
    }

    /**
     * onShuffleModeChangeActionのテスト シャッフルON
     */
    @Test
    public void testOnShuffleModeChangeActionOn() throws Exception {
        AppMusicShuffleModeChangeEvent event = mock(AppMusicShuffleModeChangeEvent.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        StatusHolder holder = mock(StatusHolder.class);
        status.shuffleMode = ShuffleMode.ON;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onShuffleModeChangeAction(event);
        verify(mView).setShuffleImage(ShuffleMode.ON);
    }
}