package jp.pioneer.carsync.presentation.presenter;

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;

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
import jp.pioneer.carsync.domain.content.DeleteParams;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.content.UpdateParams;
import jp.pioneer.carsync.domain.event.SxmInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlSiriusXmSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.SxmView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008316 on 2017/06/09.
 */
public class SxmPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    SxmPresenter mPresenter;
    @Mock SxmView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mStatusHolder;
    @Mock ControlSiriusXmSource mControlCase;
    @Mock QueryTunerItem mTunerCase;
    @Mock ControlMediaList mMediaCase;
    @Mock AppSharedPreference mPreference;
    @Mock PreferSoundFx mFxCase;
    private SxmMediaInfo mTestSxm;
    private static final int LOADER_ID_FAVORITE = 0;
    private static final int LOADER_ID_PRESET = 1;
    private static final String KEY_BAND_TYPE = "band_type";
    private SxmBandType mSxmBand;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
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

        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.NOT_LIST;
        carDeviceStatus.sourceType = MediaSourceType.SIRIUS_XM;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() ->{
            mPresenter = new SxmPresenter();
            mPresenter.mContext = mContext;
            mPresenter.mPreference = mPreference;
            mPresenter.mEventBus = mEventBus;
            mPresenter.mStatusHolder = mStatusHolder;
            mPresenter.mControlCase = mControlCase;
            mPresenter.mTunerCase = mTunerCase;
            mPresenter.mFxCase = mFxCase;
            mPresenter.setUp(mStatusHolder,mEventBus,mMediaCase);
        });
    }
    @After
    public void tearDown() throws Exception {
        mPresenter.stopHandler();
    }
    @Test
    public void testOnInitialize() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.sxmMediaInfo = mTestSxm;

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        mPresenter.takeView(mView);
        mPresenter.initialize();

        verify(mView).setBand("SXM1");
        verify(mView).setChannelNumber("CH 003");
        verify(mView).setChannelName(mTestSxm.channelAndChannelNameOrAdvisoryMessage);
        verify(mView).setTitle(mTestSxm.songTitle);
        verify(mView).setArtist(mTestSxm.artistNameOrContentInfo);
        verify(mView).setCategory(mTestSxm.categoryName);
        verify(mView).setReplayMode(mTestSxm.inReplayMode);
        verify(mView).setMaxProgress(mTestSxm.totalBufferTime);
        verify(mView).setCurrentProgress(mTestSxm.currentPosition);
        verify(mView).setAntennaLevel((float)mTestSxm.antennaLevel/mTestSxm.maxAntennaLevel);
        verify(mView).setTuneMix(mTestSxm.inTuneMix);
    }

    @Test
    public void testOnRadioInfoChangeEvent() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mTestSxm.subscriptionUpdatingShowing = true;
        mockMediaHolder.sxmMediaInfo = mTestSxm;

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        mPresenter.takeView(mView);
        mPresenter.onSxmInfoChangeEvent(new SxmInfoChangeEvent());

        verify(mView).setBand("SXM1");
        verify(mView).setChannelNumber("CH 003");
        verify(mView).setChannelName(mTestSxm.channelAndChannelNameOrAdvisoryMessage);
        verify(mView).setTitle(mTestSxm.songTitle);
        verify(mView).setArtist(mTestSxm.artistNameOrContentInfo);
        verify(mView).setCategory(mTestSxm.categoryName);
        verify(mView).setReplayMode(mTestSxm.inReplayMode);
        verify(mView).setMaxProgress(mTestSxm.totalBufferTime);
        verify(mView).setCurrentProgress(mTestSxm.currentPosition);
        verify(mView).setAntennaLevel((float)mTestSxm.antennaLevel/mTestSxm.maxAntennaLevel);
        verify(mView).setTuneMix(mTestSxm.inTuneMix);
        verify(mView).showSubscription();
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager mockLoader = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockLoader);

        verify(mockLoader).initLoader(eq(0), any(Bundle.class), any(SxmPresenter.class));
    }

    @Test
    public void testOnCreateLoader() throws Exception {
        mPresenter.onCreateLoader(LOADER_ID_FAVORITE, Bundle.EMPTY);
        verify(mTunerCase).getFavoriteList(any(QueryParams.class));
    }
    @Test
    public void testOnCreateLoaderPreset() throws Exception {
        Bundle args = new Bundle();
        args.putByte(KEY_BAND_TYPE, (byte) (SxmBandType.SXM1.getCode() & 0xFF));
        mPresenter.onCreateLoader(LOADER_ID_PRESET, args);
        verify(mTunerCase).getPresetList(MediaSourceType.SIRIUS_XM,SxmBandType.SXM1);
    }
    @Test
    public void testOnLoadFinishedWithTrue() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.sxmMediaInfo = mTestSxm;

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockLoader.getId()).thenReturn(LOADER_ID_FAVORITE);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0x00);
        // Channel Number
        when(mockCursor.getColumnIndexOrThrow("tuner_param1")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(10).thenReturn(11);
        // ID
        when(mockCursor.getColumnIndexOrThrow("_id")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(1L).thenReturn(2L);
        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setFavorite(true);
    }

    @Test
    public void testOnLoadFinishedWithFalse() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.sxmMediaInfo = mTestSxm;

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockLoader.getId()).thenReturn(LOADER_ID_FAVORITE);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0x00).thenReturn(0x01);
        // Channel Number
        when(mockCursor.getColumnIndexOrThrow("tuner_param1")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(9).thenReturn(10);
        // ID
        when(mockCursor.getColumnIndexOrThrow("_id")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(1L).thenReturn(2L);
        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setFavorite(false);
    }
    @Test
    public void testOnLoadFinishedPreset() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.sxmMediaInfo = mTestSxm;

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockLoader.getId()).thenReturn(LOADER_ID_PRESET);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("band_type")).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn("SXM1").thenReturn("SXM1");
        // P.CH
        when(mockCursor.getColumnIndexOrThrow("pch_number")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0x01).thenReturn(0x02);
        // CHANNEL
        when(mockCursor.getColumnIndexOrThrow("text")).thenReturn(1);
        when(mockCursor.getString(1)).thenReturn("CHANNEL").thenReturn("TEST CHANNEL");

        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setPch(anyInt());
    }
    @Test
    public void testPresetUp() throws Exception {
        mPresenter.onNextPresetAction();
        verify(mControlCase).presetUp();
    }

    @Test
    public void testPresetDown() throws Exception {
        mPresenter.onPreviousPresetAction();
        verify(mControlCase).presetDown();
    }

    @Test
    public void testOnToggleBandAction() throws Exception {
        mPresenter.onToggleBandAction();
        verify(mControlCase).toggleBand();
    }

    @Test
    public void testOnFavoriteActionOn() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.sxmMediaInfo = mTestSxm;
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        Resources mockResources = mock(Resources.class);
        when(mContext.getResources()).thenReturn(mockResources);
        when(mockResources.getString(mTestSxm.band.getLabel())).thenReturn("SXM1");

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0x00).thenReturn(0x01);
        // Channel Number
        when(mockCursor.getColumnIndexOrThrow("tuner_param1")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(9).thenReturn(10);
        // ID
        when(mockCursor.getColumnIndexOrThrow("_id")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(1L).thenReturn(2L);
        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        mPresenter.onFavoriteAction();

        verify(mTunerCase).registerFavorite(any(UpdateParams.class));
    }

    @Test
    public void testOnFavoriteActionOff() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.sxmMediaInfo = mTestSxm;
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        Resources mockResources = mock(Resources.class);
        when(mContext.getResources()).thenReturn(mockResources);
        when(mockResources.getString(mTestSxm.band.getLabel())).thenReturn("SXM1");

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0x00).thenReturn(0x01);
        // Channel Number
        when(mockCursor.getColumnIndexOrThrow("tuner_param1")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(10).thenReturn(11);
        // ID
        when(mockCursor.getColumnIndexOrThrow("_id")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(1L).thenReturn(2L);
        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        mPresenter.onFavoriteAction();

        verify(mTunerCase).unregisterFavorite(any(DeleteParams.class));
    }

    @Test
    public void testOnReplayAction() throws Exception {
        mPresenter.onReplayAction();
        verify(mControlCase).toggleReplayMode();
    }

    @Test
    public void testOnTuneMixAction() throws Exception {
        mPresenter.onTuneMixAction();
        verify(mControlCase).toggleTuneMix();
    }

    @Test
    public void testOnLiveAction() throws Exception {
        mPresenter.onLiveAction();
        verify(mControlCase).toggleLiveMode();
    }

    @Test
    public void testOnChannelAction() throws Exception {
        mPresenter.onChannelAction();
        verify(mControlCase).toggleChannelMode();
    }

/*    //@Test
    public void testOnPresetShowAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onPresetShowAction();

        verify(mEventBus).post(any(BackgroundChangeEvent.class));
        verify(mEventBus, atLeast(2)).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getAllValues().get(1);
        assertThat(capturedEvent.screenId, is(ScreenId.RADIO_PRESET));
    }*/

    @Test
    public void testOnListShowAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onSelectListAction();

        verify(mMediaCase).enterList(ListType.PCH_LIST);

    }

    @Test
    public void testOnReleaseSubscription() throws Exception {
        mPresenter.onReleaseSubscription();
        verify(mControlCase).releaseSubscriptionUpdating();
    }
}