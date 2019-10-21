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
import jp.pioneer.carsync.domain.event.UsbInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlUsbSource;
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
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.domain.model.UsbMediaInfo;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.UsbView;
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
 * Created by NSW00_906320 on 2017/05/30.
 */
public class UsbPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    UsbPresenter mPresenter;
    @Mock UsbView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock ControlUsbSource mControlCase;
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
        carDeviceStatus.sourceType = MediaSourceType.USB;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);
        when(mGetCase.execute()).thenReturn(holder);

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() ->{
            mPresenter = new UsbPresenter();
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

        UsbMediaInfo info = mock(UsbMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.repeatMode = CarDeviceRepeatMode.OFF;
        info.shuffleMode = ShuffleMode.OFF;

        info.playbackMode = STOP;

        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.usbMediaInfo = info;
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
        verify(mEventBus,times(2)).register(mPresenter);
        verify(mView).setRepeatImage(CarDeviceRepeatMode.OFF);
        verify(mView).setShuffleImage(ShuffleMode.OFF);

        verify(mView).setMusicTitle("");
        verify(mView).setMusicArtist("");
        verify(mView).setMusicAlbum("");
        verify(mView).setMusicGenre("");
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
    }

    /**
     * onResumeのテスト2
     */
    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        UsbMediaInfo info = mock(UsbMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.repeatMode = CarDeviceRepeatMode.ONE;
        info.shuffleMode = ShuffleMode.ON;
        info.playbackMode = PAUSE;
        info.songTitle = "";
        info.artistName = "";
        info.albumName = "";
        info.genre = "";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.usbMediaInfo = info;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
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

        verify(mEventBus, times(0)).register(mPresenter);
        verify(mView).setRepeatImage(CarDeviceRepeatMode.ONE);
        verify(mView).setShuffleImage(ShuffleMode.ON);
        verify(mView).setMusicTitle("No Title");
        verify(mView).setMusicArtist("No Artist");
        verify(mView).setMusicAlbum("No Album");
        verify(mView).setMusicGenre("No Genre");
    }

    /**
     * onResumeのテスト3
     */
    @Test
    public void testOnResume3() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        UsbMediaInfo info = mock(UsbMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.repeatMode = CarDeviceRepeatMode.ALL;
        info.shuffleMode = ShuffleMode.ON;
        info.playbackMode = PAUSE;
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumName";
        info.genre = "genre";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.usbMediaInfo = info;
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

        verify(mView).setRepeatImage(CarDeviceRepeatMode.ALL);
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
        mPresenter.onSeekAction();
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
     * onSettingShowActionのテスト
     */
    @Test
    public void testOnSettingShowAction() throws Exception {
        mPresenter.onSettingShowAction();
        //TODO:onSettingShowActionのテスト
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
     * onUsbMediaInfoChangeActionのテスト1
     */
    @Test
    public void testOnUsbStatusChangeAction1() throws Exception {
        UsbInfoChangeEvent event = mock(UsbInfoChangeEvent.class);
        UsbMediaInfo info = mock(UsbMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumName";
        info.genre = "genre";
        info.playbackMode = PAUSE;
        info.currentSecond = 50;
        info.totalSecond = 300;
        mediaHolder.usbMediaInfo = info;
        info.repeatMode = CarDeviceRepeatMode.OFF;
        info.shuffleMode = ShuffleMode.OFF;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onUsbMediaInfoChangeAction(event);
        verify(mView).setCurrentProgress(50);
    }

    /**
     * onUsbMediaInfoChangeActionのテスト2
     */
    @Test
    public void testOnUsbStatusChangeAction2() throws Exception {
        UsbInfoChangeEvent event = mock(UsbInfoChangeEvent.class);
        UsbMediaInfo info = mock(UsbMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumName";
        info.genre = "genre";
        info.playbackMode = PAUSE;
        info.currentSecond = 50;
        info.totalSecond = 300;
        mediaHolder.usbMediaInfo = info;
        info.repeatMode = CarDeviceRepeatMode.ONE;
        info.shuffleMode = ShuffleMode.ON;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onUsbMediaInfoChangeAction(event);
        verify(mView).setCurrentProgress(50);
    }

    /**
     * onUsbMediaInfoChangeActionのテスト3
     */
    @Test
    public void testOnUsbStatusChangeAction3() throws Exception {
        UsbInfoChangeEvent event = mock(UsbInfoChangeEvent.class);
        UsbMediaInfo info = mock(UsbMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        info.songTitle = "songTitle";
        info.artistName = "artistName";
        info.albumName = "albumName";
        info.genre = "genre";
        info.playbackMode = PAUSE;
        info.currentSecond = 50;
        info.totalSecond = 300;
        mediaHolder.usbMediaInfo = info;
        info.repeatMode = CarDeviceRepeatMode.ALL;
        info.shuffleMode = ShuffleMode.OFF;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.takeView(mView);
        mPresenter.onUsbMediaInfoChangeAction(event);
        verify(mView).setCurrentProgress(50);
    }
}