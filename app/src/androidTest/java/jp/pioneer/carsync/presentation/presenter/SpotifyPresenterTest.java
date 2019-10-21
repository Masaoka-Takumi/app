package jp.pioneer.carsync.presentation.presenter;

import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.SpotifyInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlSpotifySource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceRepeatMode;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.SpotifyMediaInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.domain.model.ThumbStatus;
import jp.pioneer.carsync.presentation.view.SpotifyView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Spotify再生のPresenterのテスト
 */
public class SpotifyPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    SpotifyPresenter mPresenter;
    @Mock SpotifyView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock ControlSpotifySource mControlCase;
    @Mock Context mContext;
    @Mock AppSharedPreference mPreference;
    @Mock PreferSoundFx mFxCase;
    @Mock GetStatusHolder mStatusHolder;
    @Mock ControlMediaList mMediaCase;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.NOT_LIST;
        carDeviceStatus.sourceType = MediaSourceType.PANDORA;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);
        when(mGetCase.execute()).thenReturn(holder);

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() ->{
            mPresenter = new SpotifyPresenter();
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
     * onResumeのテスト PLAY中の場合
     */
    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        SpotifyMediaInfo info = mock(SpotifyMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PlaybackMode.PLAY;
        info.radioPlaying = true;
        info.trackNameOrSpotifyError = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumTitle";
        info.playingTrackSource = "playList";
        info.totalSecond = 300;
        info.currentSecond = 20;
        info.thumbStatus = ThumbStatus.NONE;
        info.repeatMode = CarDeviceRepeatMode.OFF;
        info.shuffleMode = ShuffleMode.OFF;
        mediaHolder.spotifyMediaInfo = info;
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
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);
        verify(mView).setModeView(true);
        verify(mView).setMusicTitle("songTitle");
        verify(mView).setMusicArtist("artistName");
        verify(mView).setMusicAlbum("albumTitle");
        verify(mView).setPlayingTrackSource("playList");
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
        verify(mView).setThumbStatus(ThumbStatus.NONE);
        verify(mView).setRepeatImage(CarDeviceRepeatMode.OFF);
        verify(mView).setShuffleImage(ShuffleMode.OFF);
    }

    /**
     * onResumeのテスト　STOP中の場合
     */
    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);

        SpotifyMediaInfo info = mock(SpotifyMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PlaybackMode.STOP;
        info.radioPlaying = false;
        info.trackNameOrSpotifyError = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumTitle";
        info.playingTrackSource = "playList";
        info.totalSecond = 300;
        info.currentSecond = 20;
        info.thumbStatus = ThumbStatus.UP;
        info.repeatMode = CarDeviceRepeatMode.ONE;
        info.shuffleMode = ShuffleMode.ON;
        mediaHolder.spotifyMediaInfo = info;
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
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onResume();

        verify(mEventBus,times(0)).register(mPresenter);
        verify(mView).setModeView(false);
        verify(mView).setMusicTitle("");
        verify(mView).setMusicArtist("");
        verify(mView).setMusicAlbum("");
        verify(mView).setPlayingTrackSource("");
        verify(mView).setThumbStatus(ThumbStatus.UP);
        verify(mView).setRepeatImage(CarDeviceRepeatMode.ONE);
        verify(mView).setShuffleImage(ShuffleMode.ON);
    }

    /**
     * onResumeのテスト 名前が空の場合
     */
    @Test
    public void testOnResumeNameIsEmpty() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);

        SpotifyMediaInfo info = mock(SpotifyMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PlaybackMode.PLAY;
        info.radioPlaying = true;
        info.trackNameOrSpotifyError = "";
        info.artistName = "";
        info.albumName = "";
        info.playingTrackSource = "";
        info.totalSecond = 300;
        info.currentSecond = 20;
        info.thumbStatus = ThumbStatus.UP;
        info.repeatMode = CarDeviceRepeatMode.ALL;
        info.shuffleMode = ShuffleMode.OFF;
        mediaHolder.spotifyMediaInfo = info;
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
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onResume();

        verify(mEventBus,times(0)).register(mPresenter);
        verify(mView).setMusicTitle(mContext.getResources().getString(R.string.no_title));
        verify(mView).setMusicArtist(mContext.getResources().getString(R.string.no_artist));
        verify(mView).setMusicAlbum(mContext.getResources().getString(R.string.no_album));
        verify(mView).setPlayingTrackSource(mContext.getResources().getString(R.string.no_source));
        verify(mView).setThumbStatus(ThumbStatus.UP);
        verify(mView).setRepeatImage(CarDeviceRepeatMode.ALL);
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
        mPresenter.onSelectSourceAction();
        //TODO:onSelectSourceActionのテスト
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
        SpotifyMediaInfo info = mock(SpotifyMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.radioPlaying = true;
        mediaHolder.spotifyMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onThumbUpAction();
        verify(mControlCase).setThumbUp();
    }

    /**
     * onThumbUpActionのテスト RadioModeでない場合
     */
    @Test
    public void testOnThumbUpActionFalse() throws Exception {
        SpotifyMediaInfo info = mock(SpotifyMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.radioPlaying = false;
        mediaHolder.spotifyMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onThumbUpAction();
        verify(mControlCase, times(0)).setThumbUp();
    }

    /**
     * onThumbDownActionのテスト
     */
    @Test
    public void testOnThumbDownAction() throws Exception {
        SpotifyMediaInfo info = mock(SpotifyMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.radioPlaying = true;
        mediaHolder.spotifyMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onThumbDownAction();
        verify(mControlCase).setThumbDown();
    }

    /**
     * onThumbDownActionのテスト RadioModeでない場合
     */
    @Test
    public void testOnThumbDownActionFalse() throws Exception {
        SpotifyMediaInfo info = mock(SpotifyMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.radioPlaying = false;
        mediaHolder.spotifyMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onThumbDownAction();
        verify(mControlCase, times(0)).setThumbDown();
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
     * onSpotifyInfoChangeActionのテスト
     */
    @Test
    public void testOnSpotifyInfoChangeAction() throws Exception {
        SpotifyInfoChangeEvent event = mock(SpotifyInfoChangeEvent.class);
        SpotifyMediaInfo info = mock(SpotifyMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PlaybackMode.PLAY;
        info.radioPlaying = true;
        info.trackNameOrSpotifyError = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumTitle";
        info.playingTrackSource = "playList";
        info.totalSecond = 300;
        info.currentSecond = 20;
        info.thumbStatus = ThumbStatus.NONE;
        info.repeatMode = CarDeviceRepeatMode.FOLDER;
        info.shuffleMode = ShuffleMode.OFF;
        mediaHolder.spotifyMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.takeView(mView);
        mPresenter.onSpotifyInfoChangeAction(event);

        verify(mView).setMusicTitle("songTitle");
        verify(mView).setMusicArtist("artistName");
        verify(mView).setMusicAlbum("albumTitle");
        verify(mView).setPlayingTrackSource("playList");
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
        verify(mView).setThumbStatus(ThumbStatus.NONE);
        // TODO repeatMode.FOLDER

    }

}