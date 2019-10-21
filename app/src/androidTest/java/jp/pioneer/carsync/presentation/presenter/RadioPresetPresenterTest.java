package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
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

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferRadioFunction;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.infrastructure.crp.event.CrpListUpdateEvent;
import jp.pioneer.carsync.presentation.model.AbstractPresetItem;
import jp.pioneer.carsync.presentation.view.RadioPresetView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * ラジオプリセットリスト画面presenterのテストコード
 */
public class RadioPresetPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks RadioPresetPresenter mPresenter = new RadioPresetPresenter();
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock RadioPresetView mView;
    @Mock GetStatusHolder mStatusHolder;
    @Mock ControlMediaList mMediaCase;
    @Mock PreferRadioFunction mPreferCase;
    @Mock QueryTunerItem mTunerCase;
    @Mock AppSharedPreference mPreference;
    private static final String KEY_BAND_TYPE = "band_type";
    private MediaSourceType mSourceType;
    private RadioBandType mRadioBand;
    private SxmBandType mSxmBand;
    private LoaderManager mLoaderManager;
    private RadioInfo mTestRadio;
    private SxmMediaInfo mTestSxm;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());


        mTestRadio = new RadioInfo();
        mTestRadio.band = RadioBandType.FM1;
        mTestRadio.psInfo = "TEST PS";
        mTestRadio.ptyInfo = "TEST PTY";
        mTestRadio.currentFrequency = 22222L;
        mTestRadio.frequencyUnit = TunerFrequencyUnit.MHZ2;
        mTestRadio.songTitle = "TEST SONG";
        mTestRadio.artistName = "TEST ARTIST";

        mTestSxm = new SxmMediaInfo();
        mTestSxm.band = SxmBandType.SXM1;
        mTestSxm.categoryName = "TEST CATEGORY";
        mTestSxm.artistNameOrContentInfo = "TEST ARTIST";
        mTestSxm.channelAndChannelNameOrAdvisoryMessage = "TEST CHANNEL";
        mTestSxm.inReplayMode = false;
        mTestSxm.subscriptionUpdatingShowing = false;
    }

    @Test
    public void testSetLoaderManagerForRadio() throws Exception {
        ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        LoaderManager mockLoader = mock(LoaderManager.class);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        mockStatus.sourceType = MediaSourceType.RADIO;
        CarDeviceMediaInfoHolder mockMediaHolder = new CarDeviceMediaInfoHolder();
        mockMediaHolder.radioInfo = mTestRadio;

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        mPresenter.takeView(mView);
        mPresenter.setLoaderManager(mockLoader);

        verify(mockLoader).initLoader(eq(0), argument.capture(), any(RadioPresetPresenter.class));
        Bundle args = argument.getValue();
        assertThat(RadioBandType.valueOf(args.getByte("band_type")), is(RadioBandType.FM1));
    }

    @Test
    public void testSetLoaderManagerForSxm() throws Exception {
        ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        LoaderManager mockLoader = mock(LoaderManager.class);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        mockStatus.sourceType = MediaSourceType.SIRIUS_XM;
        CarDeviceMediaInfoHolder mockMediaHolder = new CarDeviceMediaInfoHolder();
        mockMediaHolder.sxmMediaInfo = mTestSxm;

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        mPresenter.takeView(mView);
        mPresenter.setLoaderManager(mockLoader);

        verify(mockLoader).initLoader(eq(0), argument.capture(), any(RadioPresetPresenter.class));
        Bundle args = argument.getValue();
        assertThat(SxmBandType.valueOf(args.getByte("band_type")), is(SxmBandType.SXM1));
    }

    @Test
    public void testOnCreateLoaderForRadio() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        CarDeviceMediaInfoHolder mockMediaHolder = new CarDeviceMediaInfoHolder();
        mockStatus.sourceType = MediaSourceType.RADIO;
        mockMediaHolder.radioInfo.band = RadioBandType.FM1;
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        Bundle args = new Bundle();
        args.putByte(KEY_BAND_TYPE, (byte) (RadioBandType.FM1.getCode() & 0xFF));
        mPresenter.initialize();
        mPresenter.takeView(mView);
        mPresenter.onCreateLoader(0, args);
        verify(mTunerCase).getPresetList(MediaSourceType.RADIO, null);
    }

    @Test
    public void testOnCreateLoaderForSxm() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        mockStatus.sourceType = MediaSourceType.SIRIUS_XM;

        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);

        mPresenter.initialize();
        mPresenter.onCreateLoader(0, Bundle.EMPTY);
        verify(mTunerCase).getPresetList(MediaSourceType.SIRIUS_XM, null);
    }

    @Test
    public void testOnLoadFinishedForRadio() throws Exception {

        final ArgumentCaptor<ArrayList<AbstractPresetItem>> argument
                = ArgumentCaptor.forClass((Class) ArrayList.class);
        //ArgumentCaptor<ArrayList> argument = ArgumentCaptor.forClass(ArrayList.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        mockStatus.sourceType = MediaSourceType.RADIO;
        CarDeviceMediaInfoHolder mockMediaHolder = new CarDeviceMediaInfoHolder();
        mockMediaHolder.radioInfo = mTestRadio;

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockCursor.moveToFirst()).thenReturn(true);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("band_type")).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn("FM1").thenReturn("FM1").thenReturn("FM1").thenReturn("FM2");
        // P.CH
        when(mockCursor.getColumnIndexOrThrow("pch_number")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(1).thenReturn(2).thenReturn(1);
        // Frequency
        when(mockCursor.getColumnIndexOrThrow("frequency")).thenReturn(2);
        when(mockCursor.getLong(2)).thenReturn(11111L).thenReturn(22222L).thenReturn(99999L);
        when(mockCursor.getColumnIndexOrThrow("frequency_unit")).thenReturn(3);
        when(mockCursor.getString(3)).thenReturn("MHZ2").thenReturn("MHZ2").thenReturn("MHZ2");
        // データ有無
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setPresetList(argument.capture());
        ArrayList<AbstractPresetItem> list = argument.getValue();
        assertThat(list.get(0).frequencyText, is("11.11MHz"));
        assertThat(list.get(1).frequencyText, is("22.22MHz"));
        assertThat(list.get(1).selected, is(true));
    }

    @Test
    public void testOnLoadFinishedForSxm() throws Exception {
        final ArgumentCaptor<ArrayList<AbstractPresetItem>> argument
                = ArgumentCaptor.forClass((Class) ArrayList.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.sourceType = MediaSourceType.SIRIUS_XM;
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.sxmMediaInfo = mTestSxm;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.moveToFirst()).thenReturn(true);
        // BAND
        when(mockCursor.getColumnIndexOrThrow("band_type")).thenReturn(0);
        when(mockCursor.getString(0)).thenReturn("SXM1").thenReturn("SXM1").thenReturn("SXM2");
        // P.CH
        when(mockCursor.getColumnIndexOrThrow("pch_number")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(1).thenReturn(2).thenReturn(1);
        // Text
        when(mockCursor.getColumnIndexOrThrow("text")).thenReturn(2);
        when(mockCursor.getString(2)).thenReturn("TEST1").thenReturn("TEST2").thenReturn("TEST3");
        // データ有無
        when(mockCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setPresetList(argument.capture());
        ArrayList<AbstractPresetItem> list = argument.getValue();
        assertThat(list.get(0).channelName, is("TEST1"));
        assertThat(list.get(1).channelName, is("TEST2"));
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.sourceType = MediaSourceType.SIRIUS_XM;
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.sxmMediaInfo = mTestSxm;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);

        CursorLoader mockLoader = mock(CursorLoader.class);

        mPresenter.takeView(mView);

        verify(mView, times(0)).setPresetList(any(ArrayList.class));
    }

    @Test
    public void test0nSelectPresetNumber() throws Exception {
        mPresenter.onSelectPresetNumber(1);
        verify(mMediaCase).selectListItem(1);
    }

    @Test
    public void testOnCrpListUpdateEvent() throws Exception {
        ArgumentCaptor<Bundle> argument = ArgumentCaptor.forClass(Bundle.class);
        CrpListUpdateEvent event = mock(CrpListUpdateEvent.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        LoaderManager mockLoader = mock(LoaderManager.class);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.sourceType = MediaSourceType.RADIO;
        CarDeviceMediaInfoHolder mockMediaHolder = new CarDeviceMediaInfoHolder();
        mockMediaHolder.radioInfo = mTestRadio;
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        mPresenter.takeView(mView);
        mPresenter.setLoaderManager(mockLoader);
        mPresenter.onCrpListUpdateEvent(event);

        verify(mockLoader).restartLoader(eq(0), argument.capture(), any(RadioPresetPresenter.class));
        Bundle args = argument.getValue();
        assertThat(RadioBandType.valueOf(args.getByte("band_type")), is(RadioBandType.FM1));
    }
}