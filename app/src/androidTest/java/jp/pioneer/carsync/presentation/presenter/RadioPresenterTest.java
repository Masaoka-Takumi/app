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
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlRadioSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PtySearchSetting;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingStatus;
import jp.pioneer.carsync.domain.model.TunerSeekStep;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.util.FrequencyUtil;
import jp.pioneer.carsync.presentation.view.RadioView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ラジオ再生画面presenterのテストコード
 */
public class RadioPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    RadioPresenter mPresenter;
    @Mock RadioView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mStatusHolder;
    @Mock ControlRadioSource mControlCase;
    @Mock QueryTunerItem mTunerCase;
    @Mock ControlMediaList mMediaCase;
    @Mock AppSharedPreference mPreference;
    @Mock PreferSoundFx mFxCase;
    private RadioInfo mTestRadio;
    private static final int LOADER_ID_FAVORITE = 0;
    private static final int LOADER_ID_PRESET = 1;
    private static final String KEY_BAND_TYPE = "band_type";
    private RadioBandType mRadioBand;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
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
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.NOT_LIST;
        carDeviceStatus.sourceType = MediaSourceType.RADIO;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() ->{
            mPresenter = new RadioPresenter();
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
        StatusHolder mockHolder = mock(StatusHolder.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.radioInfo = mTestRadio;

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        TunerFunctionSettingStatus tunerFunctionSettingStatus = new TunerFunctionSettingStatus();
        tunerFunctionSettingStatus.ptySearchSettingEnabled = true;
        when(mockHolder.getTunerFunctionSettingStatus()).thenReturn(tunerFunctionSettingStatus);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        LoaderManager mockLoaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(mockLoaderManager);
        mPresenter.takeView(mView);
        mPresenter.initialize();

        verify(mView).setBand(mTestRadio.band);
        verify(mView).setFrequency(FrequencyUtil.toString(getTargetContext(), mTestRadio.currentFrequency, mTestRadio.frequencyUnit));
        verify(mView).setPsInformation(mTestRadio.psInfo);
        verify(mView).setPtyName(mTestRadio.songTitle);
        //verify(mView).setTitle(mTestRadio.songTitle);
        verify(mView).setArtist(mTestRadio.artistName);
        verify(mView).setAntennaLevel((float)mTestRadio.antennaLevel/mTestRadio.maxAntennaLevel);
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager mockLoader = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockLoader);

        verify(mockLoader).initLoader(eq(0), any(Bundle.class), any(RadioPresenter.class));
    }

    @Test
    public void testOnCreateLoaderFavorite() throws Exception {
        mPresenter.onCreateLoader(LOADER_ID_FAVORITE, Bundle.EMPTY);
        verify(mTunerCase).getFavoriteList(any(QueryParams.class));
    }

    @Test
    public void testOnCreateLoaderPreset() throws Exception {
        Bundle args = new Bundle();
        args.putByte(KEY_BAND_TYPE, (byte) (RadioBandType.FM1.getCode() & 0xFF));
        mPresenter.onCreateLoader(LOADER_ID_PRESET, args);
        verify(mTunerCase).getPresetList(MediaSourceType.RADIO,RadioBandType.FM1);
    }

    @Test
    public void testOnLoadFinishedWithTrue() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.radioInfo = mTestRadio;

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        TunerFunctionSettingStatus tunerFunctionSettingStatus = new TunerFunctionSettingStatus();
        tunerFunctionSettingStatus.ptySearchSettingEnabled = true;
        when(mockHolder.getTunerFunctionSettingStatus()).thenReturn(tunerFunctionSettingStatus);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0x01);
        // FREQUENCY
        when(mockCursor.getColumnIndexOrThrow("tuner_channel_key1")).thenReturn(1);
        when(mockCursor.getLong(1)).thenReturn(88888L).thenReturn(99999L);
        // ID
        when(mockCursor.getColumnIndexOrThrow("_id")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(1L).thenReturn(2L);
        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        LoaderManager mockLoaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(mockLoaderManager);
        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setFavorite(true);
    }

    @Test
    public void testOnLoadFinishedWithFalse() throws Exception {
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        StatusHolder mockHolder = mock(StatusHolder.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.radioInfo = mTestRadio;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        TunerFunctionSettingStatus tunerFunctionSettingStatus = new TunerFunctionSettingStatus();
        tunerFunctionSettingStatus.ptySearchSettingEnabled = true;
        when(mockHolder.getTunerFunctionSettingStatus()).thenReturn(tunerFunctionSettingStatus);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockLoader.getId()).thenReturn(LOADER_ID_FAVORITE);

        // BAND
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0x01).thenReturn(0x02);;
        // FREQUENCY
        when(mockCursor.getColumnIndexOrThrow("tuner_channel_key1")).thenReturn(1);
        when(mockCursor.getLong(1)).thenReturn(11111L).thenReturn(22222L);
        // ID
        when(mockCursor.getColumnIndexOrThrow("_id")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(1L).thenReturn(2L);
        // データ有無
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        LoaderManager mockLoaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(mockLoaderManager);
        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setFavorite(false);
    }

    @Test
    public void testOnLoadFinishedPreset() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.radioInfo = mTestRadio;

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);
        when(mStatusHolder.execute()).thenReturn(mockHolder);

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
        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        verify(mView).setPch(1);
    }

    @Test
    public void testPresetUp() throws Exception {
        mPresenter.onNextPresetAction();
        verify(mControlCase).channelUp();
    }

    @Test
    public void testPresetDown() throws Exception {
        mPresenter.onPreviousPresetAction();
        verify(mControlCase).channelDown();
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
        mockMediaHolder.radioInfo = mTestRadio;
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.seekStep = TunerSeekStep._9KHZ;

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        TunerFunctionSettingStatus tunerFunctionSettingStatus = new TunerFunctionSettingStatus();
        tunerFunctionSettingStatus.ptySearchSettingEnabled = true;
        when(mockHolder.getTunerFunctionSettingStatus()).thenReturn(tunerFunctionSettingStatus);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        Resources mockResources = mock(Resources.class);
        when(mContext.getResources()).thenReturn(mockResources);
        when(mockResources.getString(TunerFrequencyUnit.MHZ2.code)).thenReturn("MHZ2");

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0x01).thenReturn(0x02);
        // FREQUENCY
        when(mockCursor.getColumnIndexOrThrow("tuner_channel_key1")).thenReturn(1);
        when(mockCursor.getLong(1)).thenReturn(11111L).thenReturn(22222L);
        // ID
        when(mockCursor.getColumnIndexOrThrow("_id")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(1L).thenReturn(2L);
        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        LoaderManager mockLoaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(mockLoaderManager);
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
        mockMediaHolder.radioInfo = mTestRadio;

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        TunerFunctionSettingStatus tunerFunctionSettingStatus = new TunerFunctionSettingStatus();
        tunerFunctionSettingStatus.ptySearchSettingEnabled = true;
        when(mockHolder.getTunerFunctionSettingStatus()).thenReturn(tunerFunctionSettingStatus);

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0x01);
        // FREQUENCY
        when(mockCursor.getColumnIndexOrThrow("tuner_channel_key1")).thenReturn(1);
        when(mockCursor.getLong(1)).thenReturn(88888L).thenReturn(99999L);
        // ID
        when(mockCursor.getColumnIndexOrThrow("_id")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(1L).thenReturn(2L);
        // データ有無
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        LoaderManager mockLoaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(mockLoaderManager);
        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        mPresenter.onFavoriteAction();

        verify(mTunerCase).unregisterFavorite(any(DeleteParams.class));
    }

    @Test
    public void testOnListShowAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onSelectListAction();

        verify(mMediaCase).enterList(ListType.PCH_LIST);
    }

    @Test
    public void testOnPtySearchAction() throws Exception {
        mPresenter.onPtySearchAction(0);
        verify(mControlCase).startPtySearch(PtySearchSetting.valueOf((byte)0));
    }
}