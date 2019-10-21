package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.greenrobot.eventbus.EventBus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.EnumSet;

import jp.pioneer.carsync.domain.content.SettingListContract;
import jp.pioneer.carsync.domain.interactor.ControlBtSetting;
import jp.pioneer.carsync.domain.interactor.QuerySettingList;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.BtDeviceListView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BtDeviceListのPresenterのTest
 */
public class BtDeviceListPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks BtDeviceListPresenter mPresenter = new BtDeviceListPresenter();
    @Mock BtDeviceListView mView;
    @Mock QuerySettingList mGetSettingList;
    @Mock ControlBtSetting mControlBtSetting;
    @Mock EventBus mEventBus;
    @Mock Context mContext;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
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
        when(mGetSettingList.execute(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())).thenReturn(cursorLoader);

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
    public void onClickSearchButton() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onClickSearchButton();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.BT_DEVICE_SEARCH));
    }

    @Test
    public void onSelectDevice() throws Exception {

    }

    @Test
    public void onDeleteDevice() throws Exception {
        Cursor cursor = mock(Cursor.class);
        mPresenter.onDeleteDevice(cursor);
        verify(mControlBtSetting).deleteDevice(SettingListContract.DeviceList.getBdAddress(cursor));
    }

    @Test
    public void onConnectA2dpDevice() throws Exception {
        Cursor cursor = mock(Cursor.class);
        mPresenter.onConnectA2dpDevice(cursor);
        verify(mControlBtSetting).switchAudioDevice(SettingListContract.DeviceList.getBdAddress(cursor));
    }

    @Test
    public void onConnectHfpDevice() throws Exception {
        Cursor cursor = mock(Cursor.class);
        mPresenter.onConnectHfpDevice(cursor);
        verify(mControlBtSetting).connectPhoneServiceCommand(SettingListContract.DeviceList.getBdAddress(cursor), EnumSet.of(ConnectServiceType.PHONE));
    }

}