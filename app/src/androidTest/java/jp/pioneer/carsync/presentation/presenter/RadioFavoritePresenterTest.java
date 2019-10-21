package jp.pioneer.carsync.presentation.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.interactor.SelectRadioFavorite;
import jp.pioneer.carsync.domain.interactor.SelectSiriusXmFavorite;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.RadioFavoriteView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ラジオお気に入りリスト画面presenterのテストコード
 */
public class RadioFavoritePresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks RadioFavoritePresenter mPresenter = new RadioFavoritePresenter();
    @Mock RadioFavoriteView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock QueryTunerItem mTunerCase;
    @Mock SelectRadioFavorite mRadioCase;
    @Mock SelectSiriusXmFavorite mSxmCase;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager mockLoader = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockLoader);

        verify(mockLoader).initLoader(eq(0), any(Bundle.class), any(RadioFavoritePresenter.class));
    }

    @Test
    public void testOnCreateLoaderForRadio() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.sourceType = MediaSourceType.RADIO;
        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);

        mPresenter.initialize();
        mPresenter.onCreateLoader(0, Bundle.EMPTY);

        verify(mTunerCase).getFavoriteList(any(QueryParams.class));
    }

    @Test
    public void testOnCreateLoaderForSxm() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.sourceType = MediaSourceType.SIRIUS_XM;
        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);

        mPresenter.initialize();
        mPresenter.onCreateLoader(0, Bundle.EMPTY);

        verify(mTunerCase).getFavoriteList(any(QueryParams.class));
    }

    @Test
    public void testOnLoadFinished() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setCursor(mockCursor, Bundle.EMPTY);
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);

        mPresenter.onLoaderReset(mockLoader);

        verify(mView).setCursor(null, Bundle.EMPTY);
    }

    @Test
    public void testOnSelectFavoriteActionForRadio() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.sourceType = MediaSourceType.RADIO;
        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);

        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.getColumnIndexOrThrow("tuner_frequency_index")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0);
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(0x01);
        when(mockCursor.getColumnIndexOrThrow("tuner_param1")).thenReturn(2);
        when(mockCursor.getInt(2)).thenReturn(2);

        mPresenter.initialize();
        mPresenter.onSelectFavoriteAction(mockCursor);

        verify(mRadioCase).execute(0, RadioBandType.FM1, 2);
        verify(mEventBus).post(any(GoBackEvent.class));
    }

    @Test
    public void testOnSelectFavoriteActionForSxm() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.sourceType = MediaSourceType.SIRIUS_XM;
        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);

        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.getColumnIndexOrThrow("tuner_param1")).thenReturn(0);
        when(mockCursor.getInt(0)).thenReturn(0);
        when(mockCursor.getColumnIndexOrThrow("tuner_band")).thenReturn(1);
        when(mockCursor.getInt(1)).thenReturn(0x00);
        when(mockCursor.getColumnIndexOrThrow("tuner_channel_key1")).thenReturn(2);
        when(mockCursor.getInt(2)).thenReturn(2);

        mPresenter.initialize();
        mPresenter.onSelectFavoriteAction(mockCursor);

        verify(mSxmCase).execute(0, SxmBandType.SXM1, 2);
        verify(mEventBus).post(any(GoBackEvent.class));
    }
}