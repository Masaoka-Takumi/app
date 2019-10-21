package jp.pioneer.carsync.presentation.presenter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.AppMusicPlayPositionChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicTrackChangeEvent;
import jp.pioneer.carsync.domain.event.BtAudioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.CdInfoChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.PandoraInfoChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationPostedEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationRemovedEvent;
import jp.pioneer.carsync.domain.event.SpotifyInfoChangeEvent;
import jp.pioneer.carsync.domain.event.SxmInfoChangeEvent;
import jp.pioneer.carsync.domain.event.UsbInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ActionSoftwareShortcutKey;
import jp.pioneer.carsync.domain.interactor.GetReadNotificationList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.BtAudioInfo;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CdInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.Notification;
import jp.pioneer.carsync.domain.model.PandoraMediaInfo;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.SpotifyMediaInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.UsbMediaInfo;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.view.HomeView;
import jp.pioneer.carsync.presentation.view.argument.PermissionParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HOMEのPresenterのテスト
 */
public class HomePresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks HomePresenter mPresenter = new HomePresenter();
    @Mock HomeView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock ActionSoftwareShortcutKey mShortcutCase;
    @Mock GetStatusHolder mGetCase;
    @Mock GetReadNotificationList mNotificationCase;
    @Mock AppSharedPreference mPreference;
    @Mock QueryTunerItem mTunerCase;
    private static final String EMPTY = "";

    private List<Notification> mNotifications;
    private PermissionParams mParams;
    private static final ShortcutKey[] KEY_INDEX = new ShortcutKey[]{
            ShortcutKey.SOURCE,
            ShortcutKey.VOICE,
            ShortcutKey.NAVI,
            ShortcutKey.MESSAGE,
            ShortcutKey.PHONE,
    };
    private static final int[][] KEY_IMAGES = new int[][]{
            {R.drawable.p0161_srcbtn_1nrm, 0},//Source
            {R.drawable.p0162_vrbtn_1nrm, 0},//Voice
            {R.drawable.p0163_navibtn_1nrm, 0},//Navi
            {R.drawable.p0164_messagebtn_1nrm, R.drawable.p0171_notification},//Message
            {R.drawable.p0165_phonebtn_1nrm, 0},//Phone
    };
    private ArrayList<ShortcutKeyItem> mShortCutKeyList = new ArrayList<>();
    private static final int LOADER_ID_PRESET = 1;
    private static final int LOADER_ID_DEVICE = 2;
    private static final String KEY_BAND_TYPE = "band_type";
    private RadioInfo mTestRadio;
    private SxmMediaInfo mTestSxm;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        for (int i = 0; i < KEY_INDEX.length; i++) {
            ShortcutKeyItem key = new ShortcutKeyItem(KEY_INDEX[i], KEY_IMAGES[i][0], KEY_IMAGES[i][1]);
            mShortCutKeyList.add(key);
        }

        mTestRadio = new RadioInfo();
        mTestRadio.band = RadioBandType.FM1;
        mTestRadio.psInfo = "TEST PS";
        mTestRadio.ptyInfo = "TEST PTY";
        mTestRadio.currentFrequency = 99999L;
        mTestRadio.frequencyUnit = TunerFrequencyUnit.MHZ2;
        mTestRadio.songTitle = "TEST SONG";
        mTestRadio.artistName = "TEST ARTIST";
        mTestRadio.antennaLevel = 8;
        mTestRadio.maxAntennaLevel = 10;

        mTestSxm = new SxmMediaInfo();
        mTestSxm.band = SxmBandType.SXM1;
        mTestSxm.currentFrequency = 10;
        mTestSxm.currentChannelNumber = 3;
        mTestSxm.categoryName = "TEST CATEGORY";
        mTestSxm.artistNameOrContentInfo = "TEST ARTIST";
        mTestSxm.channelAndChannelNameOrAdvisoryMessage = "TEST CHANNEL";
        mTestSxm.inReplayMode = false;
        mTestSxm.subscriptionUpdatingShowing = false;
        mTestSxm.totalBufferTime = 100;
        mTestSxm.currentPosition = 50;
        mTestSxm.antennaLevel = 8;
        mTestSxm.maxAntennaLevel = 10;
        mTestSxm.inTuneMix = false;
    }

    /**
     * onTakeViewのテスト mParams.isExecute==trueの場合
     */
    @Test
    public void testOnTakeViewExecuteTrue() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        PermissionParams params = new PermissionParams();
        params.isExecute = true;

        when(mGetCase.execute()).thenReturn(holder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        when(mContext.checkPermission(eq(Manifest.permission.READ_CONTACTS), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_DENIED);

        mPresenter.setArgument(params.toBundle());
        mPresenter.takeView(mView);

        verify(mView).requestPermissions(any(String.class));
    }

    /**
     * onTakeViewのテスト mParams.isExecute==falseの場合
     */
    @Test
    public void testOnTakeViewExecuteFalse() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        PermissionParams params = new PermissionParams();
        params.isExecute = false;

        when(mGetCase.execute()).thenReturn(holder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mPreference.getUiColor()).thenReturn(AQUA);

        mPresenter.setArgument(params.toBundle());
        mPresenter.takeView(mView);

        verify(mView, never()).requestPermissions(any(String[].class));
    }

    /**
     * onResumeのテスト sourceType==APP_MUSICの場合
     */
    @Test
    public void testOnResumeUpdateAndroidMusic() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        status.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "songTitle";
        info.artworkImageLocation = Uri.parse("content://media/external/images/media/1111");
        info.durationInSec = 300;
        info.positionInSec = 20;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);
        verify(mView).setMusicTitle("songTitle");
        verify(mView).setMusicAlbumArt(Uri.parse("content://media/external/images/media/1111"));
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
        verify(mView).setShortcutKeyItems(any(ArrayList.class));
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
     * setArgumentのテスト
     */
    @Test
    public void testSetArgument() throws Exception {
        Bundle args = new Bundle();
        mPresenter.setArgument(args);
    }

    /**
     * onKeyActionのテスト key==Sourceの場合
     */
    @Test
    public void testOnKeyActionSource() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onKeyAction(ShortcutKey.SOURCE);
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.PLAYER_CONTAINER));
    }

    /**
     * onKeyActionのテスト key==Voiceの場合
     */
    @Test
    public void testOnKeyActionVoice_isEnabled() throws Exception {
        when(mPreference.isVoiceRecognitionEnabled()).thenReturn(true);

        mPresenter.onKeyAction(ShortcutKey.VOICE);
        verify(mShortcutCase).execute(ShortcutKey.VOICE);
    }

    @Test
    public void testOnKeyActionVoice_isDisabled() throws Exception {
        when(mPreference.isVoiceRecognitionEnabled()).thenReturn(false);

        mPresenter.onKeyAction(ShortcutKey.VOICE);
        verify(mShortcutCase, never()).execute(any(ShortcutKey.class));
    }

    /**
     * onKeyActionのテスト key==Navigateの場合
     */
    @Test
    public void testOnKeyActionNavigate() throws Exception {
        mPresenter.onKeyAction(ShortcutKey.NAVI);
    }

    /**
     * onKeyActionのテスト key==Messageの場合
     */
    @Test
    public void testOnKeyActionMessage() throws Exception {
        mPresenter.onKeyAction(ShortcutKey.MESSAGE);
        verify(mShortcutCase).execute(ShortcutKey.MESSAGE);
    }

    /**
     * onKeyActionのテスト key==Phoneの場合
     */
    @Test
    public void testOnKeyActionPhone_isAccessible() throws Exception {
        when(mPreference.isPhoneBookAccessible()).thenReturn(true);

        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onKeyAction(ShortcutKey.PHONE);
        verify(mShortcutCase).execute(ShortcutKey.PHONE);
    }

    @Test
    public void testOnKeyActionPhone_isNotAccessible() throws Exception {
        when(mPreference.isPhoneBookAccessible()).thenReturn(false);

        mPresenter.onKeyAction(ShortcutKey.PHONE);
        verify(mShortcutCase,never()).execute(ShortcutKey.PHONE);
    }

    /**
     * onSettingsActionのテスト
     */
    @Test
    public void testOnSettingsAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onSettingsAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.SETTINGS_CONTAINER));
    }

    /**
     * onMediaSourceTypeChangeActionのテスト
     */
    @Test
    public void testOnMediaSourceTypeChangeAction() throws Exception {
        MediaSourceTypeChangeEvent event = mock(MediaSourceTypeChangeEvent.class);
        CdInfo info = mock(CdInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.CD;
        info.playbackMode = PlaybackMode.PLAY;
        info.trackNumber = "3";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.cdInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.onMediaSourceTypeChangeAction(event);

        verify(mView).setMusicTitle("3");
        verify(mView).setCenterImage(R.drawable.p0271_sourceimg);
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
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
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        info.positionInSec = 50;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.initialize();
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
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        status.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "songTitle";
        info.artworkImageLocation = Uri.parse("content://jp.pioneer.carsync.provider/artwork/songs/4");
        info.durationInSec = 300;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.initialize();
        mPresenter.onTrackChangeAction(event);

        verify(mView).setMusicTitle("songTitle");
        verify(mView).setMusicAlbumArt(Uri.parse("content://jp.pioneer.carsync.provider/artwork/songs/4"));
        verify(mView).setMaxProgress(300);
    }

    /**
     * onCdInfoChangeActionのテスト
     */
    @Test
    public void testOnCdInfoChangeAction() throws Exception {
        CdInfoChangeEvent event = mock(CdInfoChangeEvent.class);
        CdInfo info = mock(CdInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.CD;
        info.playbackMode = PlaybackMode.PLAY;
        info.trackNumber = "3";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.cdInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.initialize();
        mPresenter.onCdInfoChangeAction(event);

        verify(mView).setMusicTitle("3");
        verify(mView).setCenterImage(R.drawable.p0271_sourceimg);
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
    }

    /**
     * onUsbMediaInfoChangeActionのテスト
     */
    @Test
    public void testOnUsbMediaInfoChangeAction() throws Exception {
        UsbInfoChangeEvent event = mock(UsbInfoChangeEvent.class);
        UsbMediaInfo info = mock(UsbMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.USB;
        info.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "songTitle";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.usbMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.initialize();
        mPresenter.onUsbMediaInfoChangeAction(event);

        verify(mView).setMusicTitle("songTitle");
        verify(mView).setCenterImage(R.drawable.p0272_sourceimg);
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
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
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.PANDORA;
        info.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "songTitle";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.pandoraMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.initialize();
        mPresenter.onPandoraInfoChangeAction(event);

        verify(mView).setMusicTitle("songTitle");
        verify(mView).setCenterImage(R.drawable.p0274_sourceimg);
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
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
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.SPOTIFY;
        info.playbackMode = PlaybackMode.PLAY;
        info.trackNameOrSpotifyError = "songTitle";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.spotifyMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.initialize();
        mPresenter.onSpotifyInfoChangeAction(event);

        verify(mView).setMusicTitle("songTitle");
        verify(mView).setCenterImage(R.drawable.p0274_sourceimg);
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
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
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.BT_AUDIO;
        info.playbackMode = PlaybackMode.PLAY;
        info.songTitle = "songTitle";
        info.totalSecond = 300;
        info.currentSecond = 20;
        mediaHolder.btAudioInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        LoaderManager mockLoader = mock(LoaderManager.class);
        mPresenter.setLoaderManager(mockLoader);
        mPresenter.initialize();
        mPresenter.onBtAudioInfoChangeAction(event);

        verify(mView).setMusicTitle("songTitle");
        verify(mView).setCenterImage(R.drawable.p0273_sourceimg);
        verify(mView).setMaxProgress(300);
        verify(mView).setCurrentProgress(20);
        verify(mockLoader).restartLoader(eq(LOADER_ID_DEVICE), eq(null), any(HomePresenter.class));

    }

    /**
     * onRadioInfoChangeEventのテスト
     */
    @Test
    public void testOnRadioInfoChangeEvent() throws Exception {
        RadioInfoChangeEvent event = mock(RadioInfoChangeEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.RADIO;
        mediaHolder.radioInfo = mTestRadio;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        LoaderManager mockLoader = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockLoader);
        mPresenter.initialize();
        mPresenter.onRadioInfoChangeEvent(event);

        verify(mView).setMusicTitle("FM1-TEST PS");
        verify(mView).setRadioInfo(deviceStatus,mTestRadio);
        verify(mockLoader).restartLoader(eq(LOADER_ID_PRESET), any(Bundle.class), any(HomePresenter.class));
    }

    /**
     * onRadioInfoChangeEventのテスト(TI)
     */
    @Test
    public void testOnRadioInfoChangeEventTi() throws Exception {
        RadioInfoChangeEvent event = mock(RadioInfoChangeEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.TI;
        mediaHolder.radioInfo = mTestRadio;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.initialize();
        mPresenter.onRadioInfoChangeEvent(event);

        verify(mView).setMusicTitle(mContext.getString(R.string.traffic_information));
        verify(mView).setRadioInfo(deviceStatus, mTestRadio);
        verify(mView).setTiInfo();
    }

    /**
     * onSxmInfoChangeEventのテスト
     */
    @Test
    public void testOnSxmInfoChangeEvent() throws Exception {
        SxmInfoChangeEvent event = mock(SxmInfoChangeEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.SIRIUS_XM;
        mediaHolder.sxmMediaInfo = mTestSxm;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        LoaderManager mockLoader = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockLoader);
        mPresenter.initialize();
        mPresenter.onSxmInfoChangeEvent(event);

        verify(mView).setMusicTitle("SXM1-TEST CHANNEL");
        verify(mView).setSxmInfo(deviceStatus,mTestSxm);
        verify(mockLoader).restartLoader(eq(LOADER_ID_PRESET), any(Bundle.class), any(HomePresenter.class));
    }

    @Test
    public void testOnCreateLoaderPresetRadio() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.RADIO;
        when(mockHolder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(mockHolder);
        Bundle args = new Bundle();
        args.putByte(KEY_BAND_TYPE, (byte) (RadioBandType.FM1.getCode() & 0xFF));
        mPresenter.initialize();
        mPresenter.onCreateLoader(LOADER_ID_PRESET, args);
        verify(mTunerCase).getPresetList(MediaSourceType.RADIO,RadioBandType.FM1);
    }

    @Test
    public void testOnCreateLoaderPresetSxm() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.SIRIUS_XM;
        when(mockHolder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(mockHolder);
        Bundle args = new Bundle();
        args.putByte(KEY_BAND_TYPE, (byte) (SxmBandType.SXM1.getCode() & 0xFF));
        mPresenter.initialize();
        mPresenter.onCreateLoader(LOADER_ID_PRESET, args);
        verify(mTunerCase).getPresetList(MediaSourceType.SIRIUS_XM,SxmBandType.SXM1);
    }

    @Test
    public void testOnLoadFinishedPresetRadio() throws Exception {
        RadioInfoChangeEvent event = mock(RadioInfoChangeEvent.class);
        StatusHolder mockHolder = mock(StatusHolder.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.radioInfo = mTestRadio;
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.RADIO;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(mockHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockLoader.getId()).thenReturn(LOADER_ID_PRESET);
        //BAND
        when(mockCursor.getColumnIndexOrThrow("band_type")).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn("FM1");
        // P.CH
        when(mockCursor.getColumnIndexOrThrow("pch_number")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(0x01);
        // FREQUENCY
        when(mockCursor.getColumnIndexOrThrow("frequency")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(99999L);
        // FREQUENCY UNIT
        when(mockCursor.getColumnIndexOrThrow("frequency_unit")).thenReturn(3);
        when(mockCursor.getString(3)).thenReturn("MHZ2");
        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);

        LoaderManager mockLoaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(mockLoaderManager);
        mPresenter.initialize();
        mPresenter.onRadioInfoChangeEvent(event);
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        verify(mView).setPch(1);
    }

    @Test
    public void testOnLoadFinishedPresetSxm() throws Exception {
        SxmInfoChangeEvent event = mock(SxmInfoChangeEvent.class);
        StatusHolder mockHolder = mock(StatusHolder.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.sxmMediaInfo = mTestSxm;
        CarDeviceStatus deviceStatus = new CarDeviceStatus();
        deviceStatus.sourceType = MediaSourceType.SIRIUS_XM;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(mockHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockLoader.getId()).thenReturn(LOADER_ID_PRESET);
        //BAND
        when(mockCursor.getColumnIndexOrThrow("band_type")).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn("SXM1");
        // P.CH
        when(mockCursor.getColumnIndexOrThrow("pch_number")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(0x02);
        // NAME
        when(mockCursor.getColumnIndexOrThrow("text")).thenReturn(2);
        when(mockCursor.getString(2)).thenReturn("TEST CHANNEL");

        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(false);

        LoaderManager mockLoaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(mockLoaderManager);
        mPresenter.initialize();
        mPresenter.onSxmInfoChangeEvent(event);
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        verify(mView).setPch(2);
    }

    /**
     * onReadNotificationPostedEventNotDisplayedのテスト　通知なしの場合
     */
    @Test
    public void testOnReadNotificationPostedEventNotDisplayed() throws Exception {
        ReadNotificationPostedEvent event = mock(ReadNotificationPostedEvent.class);
        List<Notification> notifications = new ArrayList<>();
        when(mNotificationCase.execute()).thenReturn(notifications);
        ArgumentCaptor<ArrayList<ShortcutKeyItem>> argument = ArgumentCaptor.forClass(ArrayList.class);

        mPresenter.onReadNotificationPostedEvent(event);

        verify(mView).setShortcutKeyItems(argument.capture());
        final ArrayList<ShortcutKeyItem> capturedEvent = argument.getValue();
        assertThat(capturedEvent.get(3).optionImageResource, is(0));

    }

    /**
     * onReadNotificationRemovedEventDisplayedのテスト　通知ありの場合
     */
    @Test
    public void testOnReadNotificationRemovedEventDisplayed() throws Exception {
        ReadNotificationRemovedEvent event = mock(ReadNotificationRemovedEvent.class);
        List<Notification> notifications = new ArrayList<>();
        Notification notification = spy(Notification.class);
        notifications.add(notification);
        when(mNotificationCase.execute()).thenReturn(notifications);
        ArgumentCaptor<ArrayList<ShortcutKeyItem>> argument = ArgumentCaptor.forClass(ArrayList.class);

        mPresenter.onReadNotificationRemovedEvent(event);

        verify(mView).setShortcutKeyItems(argument.capture());
        final ArrayList<ShortcutKeyItem> capturedEvent = argument.getValue();
        assertThat(capturedEvent.get(3).optionImageResource, is(R.drawable.p0171_notification));
    }

}