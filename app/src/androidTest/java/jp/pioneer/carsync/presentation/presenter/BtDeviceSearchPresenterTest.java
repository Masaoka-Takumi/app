package jp.pioneer.carsync.presentation.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.EnumSet;

import jp.pioneer.carsync.domain.content.SettingListContract;
import jp.pioneer.carsync.domain.event.SettingListCommandStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlBtSetting;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QuerySettingList;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.DeviceSearchStatus;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.SettingListInfoMap;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.BtDeviceSearchView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2018/01/19.
 */
public class BtDeviceSearchPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks BtDeviceSearchPresenter mPresenter = new BtDeviceSearchPresenter();
    @Mock BtDeviceSearchView mView;
    @Mock QuerySettingList mGetSettingList;
    @Mock ControlBtSetting mControlBtSetting;
    @Mock GetStatusHolder mGetCase;
    @Mock EventBus mEventBus;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

    }

    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec mockSpec = mock(CarDeviceSpec.class);
        mockSpec.phoneSettingSupported = true;
        PhoneSettingStatus phoneStatus = new PhoneSettingStatus();
        SettingListInfoMap infoMap = mock(SettingListInfoMap.class);
        infoMap.deviceSearchStatus = DeviceSearchStatus.SEARCHING;
        when(holder.getSettingListInfoMap()).thenReturn(infoMap);
        when(holder.getCarDeviceSpec()).thenReturn(mockSpec);
        when(holder.getPhoneSettingStatus()).thenReturn(phoneStatus);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);
        verify(mControlBtSetting).startPhoneSearch();
        verify(mView).setSearchStatus(DeviceSearchStatus.SEARCHING.searching);
    }

    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
        verify(mControlBtSetting).stopPhoneSearchAndReconnectA2dp();
    }

    /**
     * setLoaderManagerのテスト
     */
    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);
        mPresenter.setLoaderManager(loaderManager);
        verify(loaderManager).initLoader(0,  Bundle.EMPTY, mPresenter);
    }

    /**
     * onCreateLoaderのテスト
     */
    @Test
    public void testOnCreateLoader() throws Exception {
        Bundle args = new Bundle();
        CursorLoader cursorLoader = mock(CursorLoader.class);
        when(mGetSettingList.execute(SettingListContract.QuerySettingListParamsBuilder.createSearchList())).thenReturn(cursorLoader);

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
        mPresenter.onLoadFinished(loader, cursor);

        verify(mView).setDeviceCursor(cursor, Bundle.EMPTY);
    }

    /**
     * onLoaderResetのテスト
     */
    @Test
    public void testOnLoaderResetSetAudioDeviceName() throws Exception {
        CursorLoader loader = mock(CursorLoader.class);
        mPresenter.onLoaderReset(loader);
    }

    @Test
    public void testOnSelectDevice() throws Exception {
        Cursor cursor = mock(Cursor.class);
        mPresenter.onSelectDevice(cursor);

        verify(mControlBtSetting).pairingDevice(SettingListContract.SearchList.getBdAddress(cursor), EnumSet.of(ConnectServiceType.PHONE, ConnectServiceType.AUDIO));
    }

    @Test
    public void testOnStartSearch() throws Exception {
        mPresenter.onStartSearch();
        verify(mControlBtSetting).startPhoneSearch();
    }

    @Test
    public void testOnStopSearch() throws Exception {
        mPresenter.onStopSearch();
        verify(mControlBtSetting).stopPhoneSearch();
    }

    @Test
    public void testOnSettingListCommandStatusChangeEvent() throws Exception {

        StatusHolder holder = mock(StatusHolder.class);
        SettingListInfoMap infoMap = mock(SettingListInfoMap.class);
        infoMap.deviceSearchStatus = DeviceSearchStatus.COMPLETED;
        when(holder.getSettingListInfoMap()).thenReturn(infoMap);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onSettingListCommandStatusChangeEvent(new SettingListCommandStatusChangeEvent(SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE));

        verify(mView).setSearchStatus(DeviceSearchStatus.COMPLETED.searching);
    }

}