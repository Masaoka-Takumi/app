package jp.pioneer.carsync.presentation.presenter;

import android.app.Instrumentation;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

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

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.content.SettingListContract;
import jp.pioneer.carsync.domain.event.BtAudioInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlBtAudioSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ExitMenu;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.interactor.QuerySettingList;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.BtAudioInfo;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CustomBandSetting;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
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
import jp.pioneer.carsync.presentation.view.BtAudioView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.content.SettingListContract.SettingListBaseColumn.DEVICE_NAME;
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
 * BtAudio再生のPresenterのテスト
 */
public class BtAudioPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    BtAudioPresenter mPresenter;
    @Mock BtAudioView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock ControlBtAudioSource mControlCase;
    @Mock Context mContext;
    @Mock QuerySettingList mGetSettingList;
    @Mock AppSharedPreference mPreference;
    @Mock PreferSoundFx mFxCase;
    @Mock GetStatusHolder mStatusHolder;
    @Mock ControlMediaList mMediaCase;
    @Mock ExitMenu mExitMenu;
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
        carDeviceStatus.sourceType = MediaSourceType.BT_AUDIO;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);
        when(mGetCase.execute()).thenReturn(holder);

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() ->{
            mPresenter = new BtAudioPresenter();
            mPresenter.mContext = mContext;
            mPresenter.mPreference = mPreference;
            mPresenter.mEventBus = mEventBus;
            mPresenter.mGetCase = mGetCase;
            mPresenter.mControlCase = mControlCase;
            mPresenter.mGetSettingList = mGetSettingList;
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
     * setLoaderManagerのテスト
     */
    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(loaderManager);
        verify(loaderManager).initLoader(0, null, mPresenter);
    }

    /**
     * onCreateLoaderのテスト
     */
    @Test
    public void testOnCreateLoader() throws Exception {
        Bundle args = new Bundle();
        CursorLoader cursorLoader = mock(CursorLoader.class);
        when(mGetSettingList.execute(SettingListContract.QuerySettingListParamsBuilder.createAudioConnectedDevice())).thenReturn(cursorLoader);

        Loader<Cursor> loader = mPresenter.onCreateLoader(0, args);

        assertThat(loader, is(cursorLoader));
    }

    /**
     * onLoadFinishedのテスト
     */
    @Test
    public void testOnLoadFinishedSetAudioDeviceName() throws Exception {
        CursorLoader loader = mock(CursorLoader.class);
        Cursor cursor = mock(Cursor.class);
        when(loader.getId()).thenReturn(0);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getString(cursor.getColumnIndexOrThrow(DEVICE_NAME))).thenReturn("myPhone");
        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(loader, cursor);

        verify(mView).setAudioDeviceName("myPhone");
    }

    /**
     * onLoaderResetのテスト
     */
    @Test
    public void testOnLoaderResetSetAudioDeviceName() throws Exception {
        CursorLoader loader = mock(CursorLoader.class);
        Cursor data = mock(Cursor.class);
        when(loader.getId()).thenReturn(0);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        mPresenter.takeView(mView);
        mPresenter.onLoaderReset(loader);

        verify(mView).setAudioDeviceName(mContext.getResources().getString(R.string.no_name));
    }

    /**
     * onResumeのテスト PLAY中の場合
     */
    @Test
    public void testOnResumeOnPlay() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        BtAudioInfo info = mock(BtAudioInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);

        info.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumTitle";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.btAudioInfo = info;

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

        verify(mEventBus,times(2)).register(mPresenter);
        verify(mView).setMusicTitle("songTitle");
        verify(mView).setMusicArtist("artistName");
        verify(mView).setMusicAlbum("albumTitle");
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);

    }

    /**
     * onResumeのテスト　STOP中の場合
     */
    @Test
    public void testOnResumeOnStop() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);

        BtAudioInfo info = mock(BtAudioInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PlaybackMode.STOP;
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumTitle";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.btAudioInfo = info;
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
        verify(mView).setMusicTitle("");
        verify(mView).setMusicArtist("");
        verify(mView).setMusicAlbum("");
    }

    /**
     * onResumeのテスト 情報が空の場合
     */
    @Test
    public void testOnResumeNameIsEmpty() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        BtAudioInfo info = mock(BtAudioInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);

        info.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "";
        info.artistName = "";
        info.albumName = "";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.btAudioInfo = info;
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
    }

    /**
     * onResumeのテスト デバイスA2DP接続中の場合
     */
    @Test
    public void testOnResumeIsConnecting() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        BtAudioInfo info = mock(BtAudioInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "Connecting";
        info.artistName = "";
        info.albumName = "";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.btAudioInfo = info;
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

        verify(mView).setMusicTitle("Connecting");
    }

    /**
     * onResumeのテスト デバイスA2DP接続失敗の場合
     */
    @Test
    public void testOnResumesIsNoService() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        BtAudioInfo info = mock(BtAudioInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "No Service";
        info.artistName = "";
        info.albumName = "";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.btAudioInfo = info;
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

        verify(mView).setMusicTitle("No Service");
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
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
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

        verify(mFxCase,never()).setLiveSimulation(SoundFieldControlSettingType.OFF, SoundEffectType.OFF);
        verify(mFxCase).setSuperTodoroki(SuperTodorokiSetting.LAST);
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
        verify(mFxCase).setSuperTodoroki(SuperTodorokiSetting.OFF);
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
     * onBtAudioInfoChangeActionのテスト
     */
    @Test
    public void testOnBtAudioInfoChangeAction() throws Exception {
        BtAudioInfoChangeEvent event = mock(BtAudioInfoChangeEvent.class);
        BtAudioInfo info = mock(BtAudioInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumTitle";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.btAudioInfo = info;

        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestEqArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetCase.execute()).thenReturn(holder);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        mPresenter.takeView(mView);
        mPresenter.onBtAudioInfoChangeAction(event);

        verify(mView).setMusicTitle("songTitle");
        verify(mView).setMusicArtist("artistName");
        verify(mView).setMusicAlbum("albumTitle");
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
    }

}